"""datetime 工具 · SPEC §13:API 返回时间一律 ISO 8601 字符串(含 tz)

SQLite 不保留 timezone 信息,SQLAlchemy 把 `DateTime(timezone=True)` 列读回来是
naive datetime。seed/写入时统一用 UTC,因此读回来的 naive datetime 视作 UTC 即可。
"""
from datetime import datetime, timezone


def to_iso(dt: datetime | None) -> str | None:
    """把 datetime 序列化成带 tz 的 ISO 8601 字符串。naive 当 UTC 处理。"""
    if dt is None:
        return None
    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)
    return dt.isoformat()
