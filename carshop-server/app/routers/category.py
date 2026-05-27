"""分类路由 · SPEC §6.1 / §6.2"""
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.deps import get_db, require_admin
from app.errors import CarshopException, ErrorCode
from app.models import Category, Product
from app.response import ok
from app.schemas.category import CategoryCreate, CategoryOut, CategoryUpdate

public_router = APIRouter()
admin_router = APIRouter(dependencies=[Depends(require_admin)])


# ---------- 公开接口 ----------

@public_router.get("/categories")
def list_categories(db: Session = Depends(get_db)) -> dict:
    rows = db.query(Category).order_by(Category.sort.asc(), Category.id.asc()).all()
    return ok([CategoryOut.model_validate(r).model_dump(mode="json") for r in rows])


# ---------- 后台接口 ----------

@admin_router.get("/categories")
def admin_list_categories(db: Session = Depends(get_db)) -> dict:
    rows = db.query(Category).order_by(Category.sort.asc(), Category.id.asc()).all()
    return ok([CategoryOut.model_validate(r).model_dump(mode="json") for r in rows])


@admin_router.post("/categories")
def admin_create_category(payload: CategoryCreate, db: Session = Depends(get_db)) -> dict:
    row = Category(name=payload.name, icon_url=payload.icon_url, sort=payload.sort)
    db.add(row)
    db.commit()
    db.refresh(row)
    return ok(CategoryOut.model_validate(row).model_dump(mode="json"))


@admin_router.put("/categories/{cid}")
def admin_update_category(cid: int, payload: CategoryUpdate, db: Session = Depends(get_db)) -> dict:
    row = db.get(Category, cid)
    if not row:
        raise CarshopException(ErrorCode.NOT_FOUND, "分类不存在")
    if payload.name is not None:
        row.name = payload.name
    if payload.icon_url is not None:
        row.icon_url = payload.icon_url
    if payload.sort is not None:
        row.sort = payload.sort
    db.commit()
    db.refresh(row)
    return ok(CategoryOut.model_validate(row).model_dump(mode="json"))


@admin_router.delete("/categories/{cid}")
def admin_delete_category(cid: int, db: Session = Depends(get_db)) -> dict:
    row = db.get(Category, cid)
    if not row:
        raise CarshopException(ErrorCode.NOT_FOUND, "分类不存在")
    # SPEC §6.2 / session spec §1.4:分类下还有商品时不能删
    n = db.query(Product).filter(Product.category_id == cid).count()
    if n > 0:
        raise CarshopException(
            ErrorCode.CONFLICT,
            f"该分类下还有 {n} 个商品,请先移除或删除商品",
        )
    db.delete(row)
    db.commit()
    return ok({"id": cid})
