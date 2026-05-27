"""初始化数据库:创建所有表 + 跑 seed。重复跑安全(seed 自带去重)。"""
import sys
from pathlib import Path

# 把项目根目录加进 sys.path
ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT))

from app.db import Base, SessionLocal, engine
from app.models import Admin, AdminSession, Banner, Category, Order, OrderItem, Product  # noqa: F401 触发表注册
from app.seeds.initial import seed_all


def main() -> None:
    print(f"[init_db] creating tables in {engine.url}")
    Base.metadata.create_all(bind=engine)

    print("[init_db] running seeds")
    db = SessionLocal()
    try:
        counts = seed_all(db)
    finally:
        db.close()

    print(f"[init_db] seed inserted: {counts}")
    print("[init_db] done.")


if __name__ == "__main__":
    main()
