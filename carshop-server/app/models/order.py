"""Order / OrderItem · SPEC §5"""
import json
from datetime import datetime, timezone
from typing import Any

from sqlalchemy import DateTime, ForeignKey, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db import Base


class Order(Base):
    __tablename__ = "orders"

    # SPEC §13: 订单号 O20260525120030001 字符串,不暴露自增 ID
    id: Mapped[str] = mapped_column(String(32), primary_key=True)
    device_id: Mapped[str] = mapped_column(String(128), nullable=False, index=True)
    # "pending" | "paid"
    status: Mapped[str] = mapped_column(String(16), nullable=False, default="pending")
    total_amount: Mapped[int] = mapped_column(Integer, nullable=False)
    # 收货信息 JSON 字符串(SPEC §5.1 默认值由服务端写)
    shipping_info: Mapped[str] = mapped_column(Text, nullable=False, default="{}")
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False,
        default=lambda: datetime.now(timezone.utc),
    )
    paid_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), nullable=True,
    )

    items: Mapped[list["OrderItem"]] = relationship(
        back_populates="order", cascade="all, delete-orphan",
    )

    @property
    def shipping_info_dict(self) -> dict[str, Any]:
        try:
            return json.loads(self.shipping_info or "{}")
        except json.JSONDecodeError:
            return {}

    @shipping_info_dict.setter
    def shipping_info_dict(self, value: dict[str, Any]) -> None:
        self.shipping_info = json.dumps(value, ensure_ascii=False)


class OrderItem(Base):
    __tablename__ = "order_items"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    order_id: Mapped[str] = mapped_column(
        String(32), ForeignKey("orders.id", ondelete="CASCADE"),
        nullable=False, index=True,
    )
    # 商品被删后保留快照,所以可空 + 不强外键约束
    product_id: Mapped[int | None] = mapped_column(Integer, nullable=True)
    # 下单时商品快照 JSON(标题/价格/规格/图)
    product_snapshot_json: Mapped[str] = mapped_column(Text, nullable=False, default="{}")
    # SPEC §5: MVP 阶段固定为 1
    quantity: Mapped[int] = mapped_column(Integer, nullable=False, default=1)
    # 下单时单价(分)
    price: Mapped[int] = mapped_column(Integer, nullable=False)

    order: Mapped["Order"] = relationship(back_populates="items")

    @property
    def product_snapshot(self) -> dict[str, Any]:
        try:
            return json.loads(self.product_snapshot_json or "{}")
        except json.JSONDecodeError:
            return {}

    @product_snapshot.setter
    def product_snapshot(self, value: dict[str, Any]) -> None:
        self.product_snapshot_json = json.dumps(value, ensure_ascii=False)
