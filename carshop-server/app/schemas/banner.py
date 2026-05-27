"""Banner Pydantic schemas · SPEC §5 · §6.1 / §6.2"""
from typing import Literal, Optional

from pydantic import BaseModel, ConfigDict, Field, model_validator

LinkType = Literal["none", "product", "category"]


class BannerOut(BaseModel):
    """API 响应模型 · SPEC §6.1"""
    model_config = ConfigDict(from_attributes=True)

    id: int
    image_url: str
    link_type: LinkType
    link_target: Optional[int]
    sort: int
    on_show: bool


class BannerCreate(BaseModel):
    """新建 banner · POST /api/v1/admin/banners"""
    image_url: str = Field(min_length=1, max_length=512)
    link_type: LinkType = "none"
    link_target: Optional[int] = None
    sort: int = 0
    on_show: bool = True

    @model_validator(mode="after")
    def _check_link_target_shape(self) -> "BannerCreate":
        # 形状层面:link_type=none 必须 link_target=null;其它必须非空
        # 跨表存在性校验放在 router(需要 DB)
        if self.link_type == "none" and self.link_target is not None:
            raise ValueError("link_type=none 时 link_target 必须为 null")
        if self.link_type in ("product", "category") and self.link_target is None:
            raise ValueError(f"link_type={self.link_type} 时 link_target 不能为空")
        return self


class BannerUpdate(BaseModel):
    """编辑 banner · PUT /api/v1/admin/banners/:id · 字段全部可选(partial update)"""
    image_url: Optional[str] = Field(default=None, min_length=1, max_length=512)
    link_type: Optional[LinkType] = None
    link_target: Optional[int] = None
    sort: Optional[int] = None
    on_show: Optional[bool] = None
