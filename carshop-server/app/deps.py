"""FastAPI 依赖项"""
from typing import Generator, Optional

from fastapi import Cookie, Depends, Header
from sqlalchemy.orm import Session

from app.db import SessionLocal
from app.errors import CarshopException, ErrorCode
from app.models.admin import Admin
from app.services import auth_service
from app.settings import SESSION_COOKIE_NAME


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def get_device_id(x_device_id: Optional[str] = Header(None, alias="X-Device-Id")) -> Optional[str]:
    """从 X-Device-Id header 提取车机设备 ID(SPEC §11 共享约定 5)"""
    return x_device_id


def require_admin(
    session: Optional[str] = Cookie(default=None, alias=SESSION_COOKIE_NAME),
    db: Session = Depends(get_db),
) -> Admin:
    """后台接口登录校验(05 session 落地)

    从 cookie 取 session_id → 查 admin_sessions 表 → 找到对应 Admin 返回。
    cookie 缺失 / session 不存在 / session 过期 → 抛 CarshopException(UNAUTHORIZED, code=2000)。
    """
    admin = auth_service.get_admin_by_session(db, session)
    if admin is None:
        raise CarshopException(ErrorCode.UNAUTHORIZED, "未登录或登录已过期")
    return admin
