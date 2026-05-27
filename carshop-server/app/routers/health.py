"""健康检查 · GET /api/v1/health"""
from fastapi import APIRouter

from app.response import ok

router = APIRouter()


@router.get("/health")
def health() -> dict:
    return ok({"status": "ok"})
