"""订单业务逻辑 · SPEC §5 / §6 / §13

集中放:
- 订单号生成(O + YYYYMMDDHHMMSS + 4 位随机,retry 3 次防并发碰撞)
- 默认收货信息(SPEC §5.1)
- 下单时商品快照构造
- Order ORM → response dict 的序列化(公开 / 后台两种)
"""
import random
from datetime import datetime, timezone, timedelta
from typing import Any

from sqlalchemy.orm import Session

from app.errors import CarshopException, ErrorCode
from app.models import Order, OrderItem, Product
from app.utils.url import absolutize_url

# 中国标准时区(SPEC §13:ISO 8601 字符串,带 +08:00 偏移)
_CST = timezone(timedelta(hours=8))

# SPEC §5.1:订单 shipping_info 默认值,Demo 阶段写死
DEFAULT_SHIPPING_INFO: dict[str, str] = {
    "name": "车主",
    "phone": "138****0000",
    "address": "上海市浦东新区世纪大道 100 号",
}


# ---------- 订单号 ----------

def _generate_order_id(now: datetime | None = None) -> str:
    """O{YYYYMMDDHHMMSS}{4位随机} · SPEC §13 + 04 session spec §1.3"""
    ts = (now or datetime.now(_CST)).strftime("%Y%m%d%H%M%S")
    return f"O{ts}{random.randint(1000, 9999)}"


def allocate_order_id(db: Session, max_retry: int = 3) -> str:
    """生成不冲突的订单号 · 已知坑 1:并发碰撞 retry 3 次"""
    for _ in range(max_retry):
        candidate = _generate_order_id()
        if not db.query(Order.id).filter(Order.id == candidate).first():
            return candidate
    # 极端情况兜底:用更长的随机数再试一次,失败就抛
    candidate = f"{_generate_order_id()}{random.randint(0, 9999):04d}"
    if not db.query(Order.id).filter(Order.id == candidate).first():
        return candidate
    raise CarshopException(ErrorCode.SERVER_ERROR, "订单号生成冲突,请重试")


# ---------- 快照 ----------

def build_product_snapshot(product: Product) -> dict[str, Any]:
    """下单时刻的商品快照 · 后续即使商品改名 / 改价 / 删除,订单展示仍是当时状态"""
    return {
        "id": product.id,
        "title": product.title,
        "product_type": product.product_type,
        "price": product.price,
        "spec": product.spec,
        "main_image_url": absolutize_url(product.main_image_url) or "",
    }


# ---------- 序列化 ----------

def _iso(dt: datetime | None) -> str | None:
    if dt is None:
        return None
    # 兼容存进 DB 时是 UTC、读出来 tzinfo 可能丢的情况:统一按 +08:00 输出
    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)
    return dt.astimezone(_CST).isoformat()


def _item_to_dict(item: OrderItem) -> dict[str, Any]:
    snap = item.product_snapshot or {}
    # 兜底:历史快照里 main_image_url 万一是相对路径,序列化时统一拼完整 URL
    if "main_image_url" in snap and snap["main_image_url"]:
        snap = {**snap, "main_image_url": absolutize_url(snap["main_image_url"]) or ""}
    return {
        "id": item.id,
        "product_id": item.product_id,
        "product_snapshot": snap,
        "quantity": item.quantity,
        "price": item.price,
    }


def order_to_dict(order: Order, *, admin: bool = False) -> dict[str, Any]:
    """Order ORM → API 响应 dict

    admin=True 时多一个 `device_id_short`(后 6 位,SPEC §6.2 / session spec §1.4)。
    """
    data: dict[str, Any] = {
        "id": order.id,
        "device_id": order.device_id,
        "status": order.status,
        "total_amount": order.total_amount,
        "shipping_info": order.shipping_info_dict,
        "items": [_item_to_dict(i) for i in order.items],
        "created_at": _iso(order.created_at),
        "paid_at": _iso(order.paid_at),
    }
    if admin:
        data["device_id_short"] = (order.device_id or "")[-6:]
    return data


# ---------- 创建订单 ----------

def create_order(
    db: Session,
    *,
    device_id: str,
    product_id: int,
    quantity: int,
) -> Order:
    """创建订单 · 业务规则见 session spec §1.3「创建订单」

    - 商品不存在 → NOT_FOUND
    - 商品已下架 → BUSINESS_RULE
    - 生成订单号 + 写 Order + 写 OrderItem(含快照)
    """
    product = db.query(Product).filter(Product.id == product_id).one_or_none()
    if not product:
        raise CarshopException(ErrorCode.NOT_FOUND, f"商品不存在(id={product_id})")
    if not product.on_sale:
        raise CarshopException(ErrorCode.BUSINESS_RULE, "商品已下架")

    order_id = allocate_order_id(db)
    order = Order(
        id=order_id,
        device_id=device_id,
        status="pending",
        total_amount=product.price * quantity,
    )
    order.shipping_info_dict = DEFAULT_SHIPPING_INFO

    item = OrderItem(
        order_id=order_id,
        product_id=product.id,
        quantity=quantity,
        price=product.price,
    )
    item.product_snapshot = build_product_snapshot(product)
    order.items.append(item)

    db.add(order)
    db.commit()
    db.refresh(order)
    return order


# ---------- 模拟支付 ----------

def mock_pay(db: Session, order: Order) -> Order:
    """模拟支付 · 业务规则见 session spec §1.3「模拟支付」

    - 已 paid → 幂等返回(不报错,不改 paid_at)
    - 其它非 pending 状态 → BUSINESS_RULE
    - pending → 推到 paid,写 paid_at
    """
    if order.status == "paid":
        return order
    if order.status != "pending":
        raise CarshopException(ErrorCode.BUSINESS_RULE, "订单状态不支持支付")
    order.status = "paid"
    order.paid_at = datetime.now(timezone.utc)
    db.commit()
    db.refresh(order)
    return order
