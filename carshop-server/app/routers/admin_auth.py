"""后台认证路由 · 05 session 引入

SPEC §6.2:
- POST /api/v1/admin/login
- POST /api/v1/admin/logout
- GET  /api/v1/admin/me
"""
from typing import Optional

from fastapi import APIRouter, Cookie, Depends, Response
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.deps import get_db, require_admin
from app.errors import CarshopException, ErrorCode
from app.models.admin import Admin
from app.response import ok
from app.services import auth_service
from app.settings import (
    SESSION_COOKIE_NAME,
    SESSION_COOKIE_SAMESITE,
    SESSION_COOKIE_SECURE,
    SESSION_MAX_AGE_SECONDS,
)

router = APIRouter(prefix="/admin", tags=["admin-auth"])


class LoginBody(BaseModel):
    username: str
    password: str


def _admin_payload(admin: Admin) -> dict:
    return {"id": admin.id, "username": admin.username}


@router.post("/login")
def login(body: LoginBody, response: Response, db: Session = Depends(get_db)):
    admin = auth_service.authenticate(db, body.username, body.password)
    if admin is None:
        raise CarshopException(ErrorCode.LOGIN_FAILED, "用户名或密码错误")
    session_id = auth_service.create_session(db, admin)
    response.set_cookie(
        key=SESSION_COOKIE_NAME,
        value=session_id,
        max_age=SESSION_MAX_AGE_SECONDS,
        httponly=True,
        samesite=SESSION_COOKIE_SAMESITE,
        secure=SESSION_COOKIE_SECURE,
    )
    return ok(_admin_payload(admin))


@router.post("/logout")
def logout(
    response: Response,
    session: Optional[str] = Cookie(default=None, alias=SESSION_COOKIE_NAME),
    db: Session = Depends(get_db),
):
    auth_service.delete_session(db, session)
    response.delete_cookie(key=SESSION_COOKIE_NAME, samesite=SESSION_COOKIE_SAMESITE)
    return ok(None)


@router.get("/me")
def me(admin: Admin = Depends(require_admin)):
    return ok(_admin_payload(admin))
