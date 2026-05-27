"""商品路由 · SPEC §6.1 / §6.2"""
from fastapi import APIRouter, Depends, Query
from sqlalchemy import update
from sqlalchemy.orm import Session

from app.deps import get_db, require_admin
from app.errors import CarshopException, ErrorCode
from app.models import Category, OrderItem, Product
from app.response import ok
from app.schemas.product import (
    ProductCreate,
    ProductOnSalePatch,
    ProductOut,
    ProductUpdate,
)
from app.settings import PAGE_SIZE_DEFAULT, PAGE_SIZE_MAX

public_router = APIRouter()
admin_router = APIRouter(dependencies=[Depends(require_admin)])


def _product_to_out(p: Product, category_name: str | None) -> dict:
    out = ProductOut.model_validate(p)
    # category_name 在 model_validate 时拿不到关系字段,这里补上
    out_dict = out.model_dump(mode="json")
    out_dict["category_name"] = category_name or ""
    return out_dict


def _serialize_with_categories(rows: list[Product], db: Session) -> list[dict]:
    if not rows:
        return []
    cat_ids = {r.category_id for r in rows}
    name_map = {
        c.id: c.name
        for c in db.query(Category).filter(Category.id.in_(cat_ids)).all()
    }
    return [_product_to_out(r, name_map.get(r.category_id)) for r in rows]


# ---------- 公开接口 ----------

@public_router.get("/products")
def list_products(
    category_id: int | None = Query(default=None),
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=PAGE_SIZE_DEFAULT, ge=1, le=PAGE_SIZE_MAX),
    db: Session = Depends(get_db),
) -> dict:
    q = db.query(Product).filter(Product.on_sale.is_(True))
    if category_id is not None:
        q = q.filter(Product.category_id == category_id)
    total = q.count()
    rows = (
        q.order_by(Product.id.asc())
        .offset((page - 1) * page_size)
        .limit(page_size)
        .all()
    )
    return ok({
        "list": _serialize_with_categories(rows, db),
        "total": total,
        "page": page,
        "page_size": page_size,
    })


@public_router.get("/products/{pid}")
def get_product(pid: int, db: Session = Depends(get_db)) -> dict:
    p = db.get(Product, pid)
    # session spec §1.1:下架商品也能返回(前端按 on_sale 字段决定怎么显示)
    if not p:
        raise CarshopException(ErrorCode.NOT_FOUND, "商品不存在")
    cat = db.get(Category, p.category_id)
    return ok(_product_to_out(p, cat.name if cat else None))


# ---------- 后台接口 ----------

@admin_router.get("/products")
def admin_list_products(
    category_id: int | None = Query(default=None),
    on_sale: bool | None = Query(default=None),
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=PAGE_SIZE_DEFAULT, ge=1, le=PAGE_SIZE_MAX),
    db: Session = Depends(get_db),
) -> dict:
    q = db.query(Product)
    if category_id is not None:
        q = q.filter(Product.category_id == category_id)
    if on_sale is not None:
        q = q.filter(Product.on_sale.is_(on_sale))
    total = q.count()
    rows = (
        q.order_by(Product.id.asc())
        .offset((page - 1) * page_size)
        .limit(page_size)
        .all()
    )
    return ok({
        "list": _serialize_with_categories(rows, db),
        "total": total,
        "page": page,
        "page_size": page_size,
    })


@admin_router.get("/products/{pid}")
def admin_get_product(pid: int, db: Session = Depends(get_db)) -> dict:
    p = db.get(Product, pid)
    if not p:
        raise CarshopException(ErrorCode.NOT_FOUND, "商品不存在")
    cat = db.get(Category, p.category_id)
    return ok(_product_to_out(p, cat.name if cat else None))


@admin_router.post("/products")
def admin_create_product(payload: ProductCreate, db: Session = Depends(get_db)) -> dict:
    cat = db.get(Category, payload.category_id)
    if not cat:
        raise CarshopException(ErrorCode.NOT_FOUND, "所属分类不存在")
    p = Product(
        category_id=payload.category_id,
        title=payload.title,
        product_type=payload.product_type,
        price=payload.price,
        original_price=payload.original_price,
        spec=payload.spec,
        main_image_url=payload.main_image_url,
        description=payload.description,
        on_sale=payload.on_sale,
    )
    db.add(p)
    db.commit()
    db.refresh(p)
    return ok(_product_to_out(p, cat.name))


@admin_router.put("/products/{pid}")
def admin_update_product(pid: int, payload: ProductUpdate, db: Session = Depends(get_db)) -> dict:
    p = db.get(Product, pid)
    if not p:
        raise CarshopException(ErrorCode.NOT_FOUND, "商品不存在")
    if payload.category_id is not None:
        if not db.get(Category, payload.category_id):
            raise CarshopException(ErrorCode.NOT_FOUND, "所属分类不存在")
        p.category_id = payload.category_id
    for field in ("title", "product_type", "price", "original_price",
                  "spec", "main_image_url", "description"):
        v = getattr(payload, field)
        if v is not None:
            setattr(p, field, v)
    db.commit()
    db.refresh(p)
    cat = db.get(Category, p.category_id)
    return ok(_product_to_out(p, cat.name if cat else None))


@admin_router.patch("/products/{pid}/on_sale")
def admin_patch_on_sale(pid: int, payload: ProductOnSalePatch, db: Session = Depends(get_db)) -> dict:
    p = db.get(Product, pid)
    if not p:
        raise CarshopException(ErrorCode.NOT_FOUND, "商品不存在")
    p.on_sale = payload.on_sale
    db.commit()
    db.refresh(p)
    cat = db.get(Category, p.category_id)
    return ok(_product_to_out(p, cat.name if cat else None))


@admin_router.delete("/products/{pid}")
def admin_delete_product(pid: int, db: Session = Depends(get_db)) -> dict:
    p = db.get(Product, pid)
    if not p:
        raise CarshopException(ErrorCode.NOT_FOUND, "商品不存在")
    # OrderItem.product_id 可空,保留快照 → 删商品前把外键置空
    db.execute(update(OrderItem).where(OrderItem.product_id == pid).values(product_id=None))
    db.delete(p)
    db.commit()
    return ok({"id": pid})
