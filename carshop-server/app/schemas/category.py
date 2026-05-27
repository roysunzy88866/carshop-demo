"""Category 的请求 / 响应模型 · SPEC §5 / §6"""
from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field, field_serializer

from app.utils.datetime import to_iso
from app.utils.url import absolutize_url


class CategoryOut(BaseModel):
    """GET /categories 单条返回"""
    model_config = ConfigDict(from_attributes=True)

    id: int
    name: str
    icon_url: str
    sort: int
    created_at: datetime

    @field_serializer("icon_url")
    def _abs_icon(self, v: str) -> str:
        return absolutize_url(v) or ""

    @field_serializer("created_at")
    def _iso_created(self, v: datetime) -> str:
        return to_iso(v) or ""


class CategoryCreate(BaseModel):
    """POST /admin/categories 入参"""
    name: str = Field(..., min_length=1, max_length=64)
    icon_url: str = Field(default="", max_length=512)
    sort: int = 0


class CategoryUpdate(BaseModel):
    """PUT /admin/categories/:id 入参 · 全部可选"""
    name: str | None = Field(default=None, min_length=1, max_length=64)
    icon_url: str | None = Field(default=None, max_length=512)
    sort: int | None = None
