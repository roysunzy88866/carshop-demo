"""后台认证服务 · 05 session 引入

- 密码校验:passlib + bcrypt(seed 时已 hash)
- session 管理:存数据库 admin_sessions 表,session_id = uuid4().hex
"""
import logging
from datetime import datetime, timedelta
from typing import Optional
from uuid import uuid4

from passlib.context import CryptContext
from sqlalchemy.orm import Session

from app.models.admin import Admin
from app.models.admin_session import AdminSession
from app.settings import SESSION_MAX_AGE_SECONDS

# 屏蔽 passlib 1.7.4 + bcrypt 4.x 的无害 trapped warning(见 01 artifact §6 坑 1)
logging.getLogger("passlib").setLevel(logging.ERROR)

_pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def verify_password(plain: str, hashed: str) -> bool:
    return _pwd_context.verify(plain, hashed)


def authenticate(db: Session, username: str, password: str) -> Optional[Admin]:
    admin = db.query(Admin).filter(Admin.username == username).one_or_none()
    if admin is None:
        return None
    if not verify_password(password, admin.password_hash):
        return None
    return admin


def create_session(db: Session, admin: Admin) -> str:
    """创建 session,顺手清理本人过期 session(见 spec §1.5 + §8.6)"""
    now = datetime.utcnow()
    db.query(AdminSession).filter(
        AdminSession.admin_id == admin.id,
        AdminSession.expires_at < now,
    ).delete(synchronize_session=False)

    session_id = uuid4().hex
    db.add(
        AdminSession(
            session_id=session_id,
            admin_id=admin.id,
            expires_at=now + timedelta(seconds=SESSION_MAX_AGE_SECONDS),
            created_at=now,
        )
    )
    db.commit()
    return session_id


def get_admin_by_session(db: Session, session_id: Optional[str]) -> Optional[Admin]:
    if not session_id:
        return None
    row = db.query(AdminSession).filter(AdminSession.session_id == session_id).one_or_none()
    if row is None:
        return None
    if row.expires_at < datetime.utcnow():
        db.delete(row)
        db.commit()
        return None
    return db.query(Admin).filter(Admin.id == row.admin_id).one_or_none()


def delete_session(db: Session, session_id: Optional[str]) -> None:
    if not session_id:
        return
    db.query(AdminSession).filter(AdminSession.session_id == session_id).delete(synchronize_session=False)
    db.commit()
