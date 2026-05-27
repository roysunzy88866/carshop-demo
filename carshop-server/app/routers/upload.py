"""图片上传路由 · 05 session 引入

SPEC §6.2 / §14:
- POST /api/v1/admin/upload,字段 file,multipart/form-data
- 单文件 5 MB
- 格式 jpg / png / webp(扩展名 + MIME 双重校验)
- 返回 {url: <完整 URL>}(SPEC §13:含 scheme + host)
"""
from pathlib import Path
from uuid import uuid4

from fastapi import APIRouter, Depends, File, UploadFile

from app.deps import require_admin
from app.errors import CarshopException, ErrorCode
from app.response import ok
from app.settings import (
    BASE_URL,
    UPLOAD_ALLOWED_EXT,
    UPLOAD_ALLOWED_MIME,
    UPLOAD_DIR,
    UPLOAD_MAX_BYTES,
)

router = APIRouter(prefix="/admin", tags=["admin-upload"], dependencies=[Depends(require_admin)])


def _extract_ext(filename: str) -> str:
    """从原文件名取后缀(不含点),全小写,jpeg→jpg"""
    suffix = Path(filename or "").suffix.lower().lstrip(".")
    if suffix == "jpeg":
        return "jpg"
    return suffix


@router.post("/upload")
async def upload(file: UploadFile = File(...)):
    ext = _extract_ext(file.filename or "")
    if ext not in UPLOAD_ALLOWED_EXT:
        raise CarshopException(ErrorCode.PARAM_INVALID, "仅支持 jpg/png/webp")
    if (file.content_type or "").lower() not in UPLOAD_ALLOWED_MIME:
        raise CarshopException(ErrorCode.PARAM_INVALID, "仅支持 jpg/png/webp")

    # 流式读取 + 累计大小,超过 5MB 立刻拒(不落地)
    contents = bytearray()
    while True:
        chunk = await file.read(64 * 1024)
        if not chunk:
            break
        contents.extend(chunk)
        if len(contents) > UPLOAD_MAX_BYTES:
            raise CarshopException(ErrorCode.PARAM_INVALID, "文件超过 5MB")

    filename = f"{uuid4().hex}.{ext}"
    target = UPLOAD_DIR / filename
    target.write_bytes(bytes(contents))

    return ok({"url": f"{BASE_URL}/static/uploads/{filename}"})
