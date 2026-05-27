"""统一响应包装 {code, data, message} · 对照 SPEC.md §12"""
from typing import Any

from app.errors import ErrorCode


def ok(data: Any = None, message: str = "ok") -> dict:
    return {"code": ErrorCode.OK.value, "data": data, "message": message}


def err(code: ErrorCode, message: str = "") -> dict:
    return {
        "code": code.value,
        "data": None,
        "message": message or code.name.lower(),
    }
