# carshop-server

车机商店服务端 · FastAPI + SQLAlchemy + SQLite。

Session 01 产物:项目骨架 + 数据模型 + seed,**未实现业务接口**。

## 启动

```bash
# 1. 建 venv + 装依赖
python3.11 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt

# 2. 初始化数据库 + seed
python scripts/init_db.py

# 3. 跑开发服务器
bash scripts/dev.sh
# 或: uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## 验证

```bash
curl http://localhost:8000/api/v1/health
# {"code":0,"data":{"status":"ok"},"message":"ok"}

sqlite3 carshop.db "SELECT name FROM categories ORDER BY sort"
sqlite3 carshop.db "SELECT title, product_type, price FROM products LIMIT 5"
```

## 默认账号

- 用户名:`admin`
- 密码:`admin123`

## 目录结构

```
app/
├── main.py          FastAPI app 入口
├── settings.py      配置(BASE_URL / DB_PATH / Admin 默认账号)
├── db.py            SQLAlchemy engine / Session
├── deps.py          依赖项(get_db / get_device_id)
├── response.py      统一响应 {code, data, message}
├── errors.py        错误码 + CarshopException
├── models/          ORM(Category / Product / Banner / Order / Admin)
├── schemas/         Pydantic(02~05 session 填)
├── routers/         路由(目前只有 health)
└── seeds/           初始化数据
scripts/
├── init_db.py       建表 + seed
└── dev.sh           启动开发服务器
static/
├── uploads/         上传图(.gitignore 内容)
└── icons/           分类图标
```

## SPEC.md 是真理之源

任何接口契约/数据模型变更先改 SPEC.md。
