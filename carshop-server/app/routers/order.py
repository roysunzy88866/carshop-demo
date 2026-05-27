"""订单公开路由 · SPEC §6.1

车机端用,所有接口必须带 `X-Device-Id` header。
跨设备查不到 → code=1001(不泄露存在,session spec §8.3)
"""
from typing import Optional

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.deps import get_db, get_device_id
from app.errors import CarshopException, ErrorCode
from app.models import Order
from app.response import ok
from app.schemas.order import OrderCreateIn, OrderStatusLiteral
from app.services import order_service
from app.settings import PAGE_SIZE_DEFAULT, PAGE_SIZE_MAX

router = APIRouter()


def _require_device_id(device_id: Optional[str]) -> str:
    """没传 X-Device-Id → code=1000(session spec §1.3 创建订单 step 2)"""
    if not device_id:
        raise CarshopException(ErrorCode.PARAM_INVALID, "缺少 X-Device-Id header")
    return device_id


def _get_order_for_device(db: Session, order_id: str, device_id: str) -> Order:
    """取当前设备的订单 · 跨设备查不到统一 1001(不区分"不存在"/"不属于你")"""
    order = (
        db.query(Order)
        .filter(Order.id == order_id, Order.device_id == device_id)
        .one_or_none()
    )
    if not order:
        raise CarshopException(ErrorCode.NOT_FOUND, "订单不存在")
    return order


@router.post("/orders", tags=["orders"])
def create_order(
    payload: OrderCreateIn,
    db: Session = Depends(get_db),
    device_id: Optional[str] = Depends(get_device_id),
) -> dict:
    did = _require_device_id(device_id)
    order = order_service.create_order(
        db,
        device_id=did,
        product_id=payload.product_id,
        quantity=payload.quantity,
    )
    return ok(order_service.order_to_dict(order))


@router.get("/orders", tags=["orders"])
def list_orders(
    db: Session = Depends(get_db),
    device_id: Optional[str] = Depends(get_device_id),
    status: Optional[OrderStatusLiteral] = Query(default=None),
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=PAGE_SIZE_DEFAULT, ge=1, le=PAGE_SIZE_MAX),
) -> dict:
    did = _require_device_id(device_id)
    q = db.query(Order).filter(Order.device_id == did)
    if status is not None:
        q = q.filter(Order.status == status)
    total = q.count()
    rows = (
        q.order_by(Order.created_at.desc(), Order.id.desc())
        .offset((page - 1) * page_size)
        .limit(page_size)
        .all()
    )
    return ok({
        "list": [order_service.order_to_dict(o) for o in rows],
        "total": total,
        "page": page,
        "page_size": page_size,
    })


@router.get("/orders/{order_id}", tags=["orders"])
def get_order(
    order_id: str,
    db: Session = Depends(get_db),
    device_id: Optional[str] = Depends(get_device_id),
) -> dict:
    did = _require_device_id(device_id)
    order = _get_order_for_device(db, order_id, did)
    return ok(order_service.order_to_dict(order))


@router.post("/orders/{order_id}/mock_pay", tags=["orders"])
def mock_pay_order(
    order_id: str,
    db: Session = Depends(get_db),
    device_id: Optional[str] = Depends(get_device_id),
) -> dict:
    did = _require_device_id(device_id)
    order = _get_order_for_device(db, order_id, did)
    order = order_service.mock_pay(db, order)
    return ok(order_service.order_to_dict(order))
