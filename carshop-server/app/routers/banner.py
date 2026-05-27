"""Banner 路由 · SPEC §6.1 公开 + §6.2 后台"""
from typing import List

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.deps import get_db, require_admin
from app.errors import CarshopException, ErrorCode
from app.models import Banner, Category, Product
from app.response import ok
from app.schemas.banner import BannerCreate, BannerOut, BannerUpdate
from app.settings import BASE_URL

router = APIRouter()


# ---------- helpers ----------

def _absolutize_image_url(url: str) -> str:
    """`/static/...` 开头的相对路径拼 BASE_URL;已是完整 URL 原样返回。
    兼容两种来源:01 seed 用 picsum 完整 URL,05 上传后存 /static/uploads/xxx 相对路径。
    SPEC §13:API 返回的 image_url 必须是完整 URL。
    """
    if url.startswith("/"):
        return f"{BASE_URL}{url}"
    return url


def _banner_to_dict(b: Banner) -> dict:
    return {
        "id": b.id,
        "image_url": _absolutize_image_url(b.image_url),
        "link_type": b.link_type,
        "link_target": b.link_target,
        "sort": b.sort,
        "on_show": b.on_show,
    }


def _validate_link_target_exists(db: Session, link_type: str, link_target: int | None) -> None:
    """跨表存在性校验(形状校验在 Pydantic schema 里做)"""
    if link_type == "product":
        if not db.query(Product.id).filter(Product.id == link_target).first():
            raise CarshopException(
                ErrorCode.PARAM_INVALID,
                f"link_target 指向的商品不存在(product_id={link_target})",
            )
    elif link_type == "category":
        if not db.query(Category.id).filter(Category.id == link_target).first():
            raise CarshopException(
                ErrorCode.PARAM_INVALID,
                f"link_target 指向的分类不存在(category_id={link_target})",
            )


# ---------- 公开接口 ----------

@router.get("/banners", tags=["banners"])
def list_banners_public(db: Session = Depends(get_db)) -> dict:
    """首页 banner · 仅 on_show=true · 按 sort 升序"""
    rows: List[Banner] = (
        db.query(Banner).filter(Banner.on_show == True).order_by(Banner.sort.asc(), Banner.id.asc()).all()  # noqa: E712
    )
    return ok([_banner_to_dict(b) for b in rows])


# ---------- 后台接口 ----------

@router.get("/admin/banners", tags=["admin-banners"], dependencies=[Depends(require_admin)])
def list_banners_admin(db: Session = Depends(get_db)) -> dict:
    """后台 banner 列表 · 全部(含 on_show=false)· 按 sort 升序"""
    rows: List[Banner] = db.query(Banner).order_by(Banner.sort.asc(), Banner.id.asc()).all()
    return ok([_banner_to_dict(b) for b in rows])


@router.post("/admin/banners", tags=["admin-banners"], dependencies=[Depends(require_admin)])
def create_banner(payload: BannerCreate, db: Session = Depends(get_db)) -> dict:
    _validate_link_target_exists(db, payload.link_type, payload.link_target)
    banner = Banner(
        image_url=payload.image_url,
        link_type=payload.link_type,
        link_target=payload.link_target,
        sort=payload.sort,
        on_show=payload.on_show,
    )
    db.add(banner)
    db.commit()
    db.refresh(banner)
    return ok(_banner_to_dict(banner))


@router.put("/admin/banners/{banner_id}", tags=["admin-banners"], dependencies=[Depends(require_admin)])
def update_banner(banner_id: int, payload: BannerUpdate, db: Session = Depends(get_db)) -> dict:
    banner = db.query(Banner).filter(Banner.id == banner_id).one_or_none()
    if not banner:
        raise CarshopException(ErrorCode.NOT_FOUND, f"banner 不存在(id={banner_id})")

    data = payload.model_dump(exclude_unset=True)

    # 算出 update 后的 link_type / link_target,做联动校验(包含未传字段的当前值)
    new_link_type = data.get("link_type", banner.link_type)
    new_link_target = data.get("link_target", banner.link_target)

    if new_link_type == "none" and new_link_target is not None:
        raise CarshopException(
            ErrorCode.PARAM_INVALID,
            "link_type=none 时 link_target 必须为 null",
        )
    if new_link_type in ("product", "category") and new_link_target is None:
        raise CarshopException(
            ErrorCode.PARAM_INVALID,
            f"link_type={new_link_type} 时 link_target 不能为空",
        )
    _validate_link_target_exists(db, new_link_type, new_link_target)

    for field, value in data.items():
        setattr(banner, field, value)
    db.commit()
    db.refresh(banner)
    return ok(_banner_to_dict(banner))


@router.delete("/admin/banners/{banner_id}", tags=["admin-banners"], dependencies=[Depends(require_admin)])
def delete_banner(banner_id: int, db: Session = Depends(get_db)) -> dict:
    """直接删 · banner 是独立资源,无依赖校验(session spec §8.3)"""
    banner = db.query(Banner).filter(Banner.id == banner_id).one_or_none()
    if not banner:
        raise CarshopException(ErrorCode.NOT_FOUND, f"banner 不存在(id={banner_id})")
    db.delete(banner)
    db.commit()
    return ok({"id": banner_id, "deleted": True})
