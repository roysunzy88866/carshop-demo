"""URL 工具 · 把 DB 里可能是相对路径的图片地址拼成完整 URL

SPEC §13:API 返回的图片 URL 必须是完整 URL(scheme + host)。
- 01 session 的 seed 已经存了完整 URL(http://localhost / https://picsum.photos)
- 05 session 上传图片时可能存相对路径(`/static/uploads/xxx.png`)
本函数兼容两种,在 schema 序列化阶段统一抹平。
"""
from app.settings import BASE_URL


def absolutize_url(value: str | None) -> str | None:
    """
    - None / 空串 → 原样返回
    - 已经是完整 URL(含 "://" 或 schemeless "//xxx") → 原样返回
    - 以 "/" 开头的相对路径 → 拼 BASE_URL
    - 其它(裸文件名等)→ 当作 /static/uploads/ 下的文件拼一下,保险起见
    """
    if not value:
        return value
    if "://" in value or value.startswith("//"):
        return value
    if value.startswith("/"):
        return f"{BASE_URL}{value}"
    return f"{BASE_URL}/static/uploads/{value}"
