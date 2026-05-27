"""Product 的请求 / 响应模型 · SPEC §5 / §6"""
from datetime import datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, field_serializer

from app.utils.datetime import to_iso
from app.utils.url import absolutize_url

ProductTypeLiteral = Literal["physical", "service_voucher"]


class ProductOut(BaseModel):
    """GET /products/:id 和列表里单条的响应"""
    model_config = ConfigDict(from_attributes=True)

    id: int
    category_id: int
    category_name: str = ""  # 由路由层 join 后填充
    title: str
    product_type: ProductTypeLiteral
    price: int  # 分
    original_price: int | None
    spec: str | None
    main_image_url: str
    description: str
    on_sale: bool
    created_at: datetime
    updated_at: datetime

    @field_serializer("main_image_url")
    def _abs_image(self, v: str) -> str:
        return absolutize_url(v) or ""

    @field_serializer("created_at", "updated_at")
    def _iso_ts(self, v: datetime) -> str:
        return to_iso(v) or ""


class ProductCreate(BaseModel):
    """POST /admin/products 入参"""
    category_id: int
    title: str = Field(..., min_length=1, max_length=128)
    product_type: ProductTypeLiteral
    price: int = Field(..., ge=0)  # 分
    original_price: int | None = Field(default=None, ge=0)
    spec: str | None = Field(default=None, max_length=128)
    main_image_url: str = Field(default="", max_length=512)
    description: str = ""
    on_sale: bool = True


class ProductUpdate(BaseModel):
    """PUT /admin/products/:id 入参 · 全部可选(on_sale 走专门的 PATCH 接口,不在这)"""
    category_id: int | None = None
    title: str | None = Field(default=None, min_length=1, max_length=128)
    product_type: ProductTypeLiteral | None = None
    price: int | None = Field(default=None, ge=0)
    original_price: int | None = Field(default=None, ge=0)
    spec: str | None = Field(default=None, max_length=128)
    main_image_url: str | None = Field(default=None, max_length=512)
    description: str | None = None


class ProductOnSalePatch(BaseModel):
    """PATCH /admin/products/:id/on_sale 入参"""
    on_sale: bool
