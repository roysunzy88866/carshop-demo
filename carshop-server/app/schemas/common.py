"""通用 schema · 分页 envelope 等"""
from typing import Generic, TypeVar

from pydantic import BaseModel

T = TypeVar("T")


class Page(BaseModel, Generic[T]):
    """SPEC §13:分页统一返回 {list, total, page, page_size}"""
    list: list[T]
    total: int
    page: int
    page_size: int
