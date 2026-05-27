"""统一错误码 · 对照 SPEC.md §12"""
from enum import IntEnum


class ErrorCode(IntEnum):
    OK = 0
    PARAM_INVALID = 1000
    NOT_FOUND = 1001
    CONFLICT = 1002
    UNAUTHORIZED = 2000
    LOGIN_FAILED = 2001
    BUSINESS_RULE = 3000
    SERVER_ERROR = 9000


_HTTP_STATUS = {
    ErrorCode.OK: 200,
    ErrorCode.PARAM_INVALID: 400,
    ErrorCode.NOT_FOUND: 404,
    ErrorCode.CONFLICT: 409,
    ErrorCode.UNAUTHORIZED: 401,
    ErrorCode.LOGIN_FAILED: 401,
    ErrorCode.BUSINESS_RULE: 422,
    ErrorCode.SERVER_ERROR: 500,
}


def http_status_for(code: ErrorCode) -> int:
    return _HTTP_STATUS.get(code, 500)


class CarshopException(Exception):
    def __init__(self, code: ErrorCode, message: str = ""):
        self.code = code
        self.message = message or code.name.lower()
        super().__init__(self.message)
