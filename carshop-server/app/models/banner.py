"""Banner · SPEC §5"""
from sqlalchemy import Boolean, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from app.db import Base


class Banner(Base):
    __tablename__ = "banners"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    image_url: Mapped[str] = mapped_column(String(512), nullable=False)
    # "none" | "product" | "category"
    link_type: Mapped[str] = mapped_column(String(32), nullable=False, default="none")
    link_target: Mapped[int | None] = mapped_column(Integer, nullable=True)
    sort: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    on_show: Mapped[bool] = mapped_column(Boolean, nullable=False, default=True)
