"""服务端配置 · 对照 SPEC.md §14"""
import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

BASE_URL = os.getenv("CARSHOP_BASE_URL", "http://localhost:8000")

DB_PATH = BASE_DIR / "carshop.db"
DB_URL = f"sqlite:///{DB_PATH}"

STATIC_DIR = BASE_DIR / "static"
UPLOAD_DIR = STATIC_DIR / "uploads"
ICON_DIR = STATIC_DIR / "icons"
DOWNLOAD_DIR = Path(os.getenv("CARSHOP_DOWNLOAD_DIR", str(BASE_DIR / "downloads")))

ADMIN_DEFAULT_USERNAME = "admin"
ADMIN_DEFAULT_PASSWORD = "admin123"

UPLOAD_MAX_BYTES = 5 * 1024 * 1024
UPLOAD_ALLOWED_EXT = {"jpg", "jpeg", "png", "webp"}
UPLOAD_ALLOWED_MIME = {"image/jpeg", "image/png", "image/webp"}

PAGE_SIZE_DEFAULT = 20
PAGE_SIZE_MAX = 100

# 后台 session cookie · 05 session 引入
# Dev 默认 SameSite=Lax + 不开 Secure(http://localhost 跑得通)
# 07 早集成上线时改 env:CARSHOP_COOKIE_SAMESITE=none + CARSHOP_COOKIE_SECURE=1
SESSION_COOKIE_NAME = "session"
SESSION_MAX_AGE_SECONDS = 86400
SESSION_COOKIE_SAMESITE = os.getenv("CARSHOP_COOKIE_SAMESITE", "lax").lower()
SESSION_COOKIE_SECURE = os.getenv("CARSHOP_COOKIE_SECURE", "0") == "1"

# CORS · 07 加 env 切换。Dev 默认放行 localhost:5173;生产用 env 显式声明
# 注意:allow_credentials=True + allow_origins=["*"] 会被浏览器拒,必须显式列
_default_cors = "http://localhost:5173,http://127.0.0.1:5173"
CORS_ALLOW_ORIGINS = [
    o.strip() for o in os.getenv("CARSHOP_CORS_ALLOW_ORIGINS", _default_cors).split(",") if o.strip()
]
