"""Order 的请求 / 响应模型 · SPEC §5 / §6 / §11

- 入参用 pydantic 校验(product_id 必填、quantity ≥ 1)
- 响应不强制走 pydantic;routers 用 service 里的 `order_to_dict()` 手动构建
  (跟 banner.py 风格一致,因为 items[].product_snapshot 是动态 JSON,
  pydantic 表达不如手写直接)
"""
from typing import Literal

from pydantic import BaseModel, Field

OrderStatusLiteral = Literal["pending", "paid"]


class OrderCreateIn(BaseModel):
    """POST /api/v1/orders 入参"""
    product_id: int = Field(..., ge=1)
    # MVP 阶段固定为 1,但参数依然接收(SPEC §5 / session spec §1.3)
    quantity: int = Field(default=1, ge=1)
