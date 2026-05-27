"""订单后台路由 · SPEC §6.2(只读)

后台运营查看订单,不按设备过滤。每条多一个 `device_id_short`(后 6 位)。
鉴权走 02 阶段桩 `require_admin`(05 接管后变真鉴权,本路由不需要改)。
"""
from typing import Optional

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.deps import get_db, require_admin
from app.errors import CarshopException, ErrorCode
from app.models import Order
from app.response import ok
from app.schemas.order import OrderStatusLiteral
from app.services import order_service
from app.settings import PAGE_SIZE_DEFAULT, PAGE_SIZE_MAX

router = APIRouter()


@router.get("/admin/orders", tags=["admin-orders"], dependencies=[Depends(require_admin)])
def list_orders_admin(
    db: Session = Depends(get_db),
    status: Optional[OrderStatusLiteral] = Query(default=None),
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=PAGE_SIZE_DEFAULT, ge=1, le=PAGE_SIZE_MAX),
) -> dict:
    q = db.query(Order)
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
        "list": [order_service.order_to_dict(o, admin=True) for o in rows],
        "total": total,
        "page": page,
        "page_size": page_size,
    })


@router.get(
    "/admin/orders/{order_id}",
    tags=["admin-orders"],
    dependencies=[Depends(require_admin)],
)
def get_order_admin(order_id: str, db: Session = Depends(get_db)) -> dict:
    order = db.query(Order).filter(Order.id == order_id).one_or_none()
    if not order:
        raise CarshopException(ErrorCode.NOT_FOUND, f"订单不存在(id={order_id})")
    return ok(order_service.order_to_dict(order, admin=True))
