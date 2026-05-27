"""FastAPI 入口 · 挂路由、CORS、全局异常、静态文件"""
import logging

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles

from app.errors import CarshopException, ErrorCode, http_status_for
from app.response import err
from app.routers import admin_auth, admin_order, banner, category, health, order, product, upload
from app.settings import CORS_ALLOW_ORIGINS, DOWNLOAD_DIR, STATIC_DIR, UPLOAD_DIR, ICON_DIR

logger = logging.getLogger("carshop")

app = FastAPI(title="carshop-server", version="0.1.0")

# 07 早集成:allow_credentials=True 时浏览器拒绝 allow_origins="*",必须显式列。
# 默认放行 localhost:5173(dev),生产用 CARSHOP_CORS_ALLOW_ORIGINS env 加上 https://carshop.hearagain.space 等。
app.add_middleware(
    CORSMiddleware,
    allow_origins=CORS_ALLOW_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.exception_handler(CarshopException)
async def carshop_exception_handler(_: Request, exc: CarshopException) -> JSONResponse:
    return JSONResponse(
        status_code=http_status_for(exc.code),
        content=err(exc.code, exc.message),
    )


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(_: Request, exc: RequestValidationError) -> JSONResponse:
    return JSONResponse(
        status_code=400,
        content=err(ErrorCode.PARAM_INVALID, str(exc.errors()[0]["msg"]) if exc.errors() else "参数错误"),
    )


@app.exception_handler(Exception)
async def unhandled_exception_handler(_: Request, exc: Exception) -> JSONResponse:
    logger.exception("unhandled: %s", exc)
    return JSONResponse(
        status_code=500,
        content=err(ErrorCode.SERVER_ERROR, "服务端内部错误"),
    )


# 确保静态目录存在(首次启动时 uploads/icons 子目录可能尚未由 init_db 建好)
STATIC_DIR.mkdir(parents=True, exist_ok=True)
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
ICON_DIR.mkdir(parents=True, exist_ok=True)
DOWNLOAD_DIR.mkdir(parents=True, exist_ok=True)

# SPEC §6.3 / §13:/static/uploads/* 和 /static/icons/*
app.mount("/static", StaticFiles(directory=str(STATIC_DIR), html=False), name="static")
# APK 下载目录(11 session):https://carshop.hearagain.space/download/
app.mount("/download", StaticFiles(directory=str(DOWNLOAD_DIR), html=True), name="download")

# SPEC §13:所有 API 前缀 /api/v1
app.include_router(health.router, prefix="/api/v1", tags=["health"])
app.include_router(banner.router, prefix="/api/v1")
# session 02 · 分类
app.include_router(category.public_router, prefix="/api/v1", tags=["categories"])
app.include_router(category.admin_router, prefix="/api/v1/admin", tags=["admin-categories"])
# session 02 · 商品
app.include_router(product.public_router, prefix="/api/v1", tags=["products"])
app.include_router(product.admin_router, prefix="/api/v1/admin", tags=["admin-products"])
# session 04 · 订单(公开 + 后台,都走 /api/v1 前缀,路径里自带 /admin)
app.include_router(order.router, prefix="/api/v1")
app.include_router(admin_order.router, prefix="/api/v1")
# session 05 · 后台认证 + 图片上传
app.include_router(admin_auth.router, prefix="/api/v1")
app.include_router(upload.router, prefix="/api/v1")
