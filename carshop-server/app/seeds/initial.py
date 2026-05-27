"""初始 seed:5 分类 + 1 admin + 3 banner + 12 商品(覆盖 physical / service_voucher 两种)"""
from passlib.context import CryptContext
from sqlalchemy.orm import Session

from app.models import Admin, Banner, Category, Product, ProductType
from app.settings import ADMIN_DEFAULT_PASSWORD, ADMIN_DEFAULT_USERNAME

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


# SPEC §5.0:车机商城分类
CATEGORIES = [
    {"name": "汽车用品", "icon_slug": "car-parts", "sort": 1},
    {"name": "加油充电", "icon_slug": "fuel-charge", "sort": 2},
    {"name": "洗车保养", "icon_slug": "wash-maintain", "sort": 3},
    {"name": "周边餐饮", "icon_slug": "food-nearby", "sort": 4},
    {"name": "旅行服务", "icon_slug": "travel-service", "sort": 5},
]

# 12 个商品(覆盖 physical + service_voucher 两种 product_type)
# 价格全部「分」· 主图用 picsum 占位(seed 确定性)
PRODUCTS = [
    # 1. 汽车用品(physical)
    {"cat": "汽车用品", "title": "车载磁吸手机支架",        "type": "physical",        "price":  8900, "original":  12900, "spec": "通用款 · 银色", "img": "carmount"},
    {"cat": "汽车用品", "title": "便携式车载吸尘器 120W",   "type": "physical",        "price": 19900, "original":  29900, "spec": "120W · 含三种吸头", "img": "carvac"},
    {"cat": "汽车用品", "title": "车窗遮阳挡(前挡)",      "type": "physical",        "price":  3900, "original":   None, "spec": "通用尺寸 145×80 cm", "img": "sunshade"},
    # 2. 加油充电(service_voucher)
    {"cat": "加油充电", "title": "中石化加油卡 100 元",      "type": "service_voucher", "price":  9500, "original":  10000, "spec": "100 元面值",        "img": "fuel100"},
    {"cat": "加油充电", "title": "中石化加油卡 500 元",      "type": "service_voucher", "price": 47500, "original":  50000, "spec": "500 元面值",        "img": "fuel500"},
    {"cat": "加油充电", "title": "特来电充电券 30 度",       "type": "service_voucher", "price":  3600, "original":   None, "spec": "30 kWh · 全国通用",  "img": "charge30"},
    # 3. 洗车保养(service_voucher)
    {"cat": "洗车保养", "title": "标准洗车单次券",            "type": "service_voucher", "price":  2900, "original":   3900, "spec": "市区门店通用",        "img": "wash1"},
    {"cat": "洗车保养", "title": "小保养套餐(机油+机滤)",  "type": "service_voucher", "price": 19900, "original":  29900, "spec": "适用 1.5T 及以下",   "img": "maint"},
    # 4. 周边餐饮(service_voucher)
    {"cat": "周边餐饮", "title": "星巴克中杯券",              "type": "service_voucher", "price":  2800, "original":   3500, "spec": "全国门店通用",        "img": "starbucks"},
    {"cat": "周边餐饮", "title": "肯德基早餐套餐券",          "type": "service_voucher", "price":  1800, "original":   2200, "spec": "早 6:30-10:30 使用",  "img": "kfc"},
    # 5. 旅行服务(physical + service_voucher)
    {"cat": "旅行服务", "title": "车载应急工具包",            "type": "physical",        "price": 12900, "original":  15900, "spec": "含搭电线/拖车绳/三角警示牌", "img": "emergency"},
    {"cat": "旅行服务", "title": "高速公路通行券 50 元",      "type": "service_voucher", "price":  4800, "original":   5000, "spec": "ETC 通用",            "img": "etc50"},
]

# 3 个 banner 占位(SPEC §5)
BANNERS = [
    {"img": "banner-sale",     "sort": 1, "link_type": "none", "link_target": None},
    {"img": "banner-charge",   "sort": 2, "link_type": "none", "link_target": None},
    {"img": "banner-newuser",  "sort": 3, "link_type": "none", "link_target": None},
]


def _picsum(slug: str, w: int = 600, h: int = 400) -> str:
    """picsum 占位图,seed 确定性"""
    return f"https://picsum.photos/seed/{slug}/{w}/{h}"


def _icon_url(slug: str) -> str:
    """分类图标占位 URL · 用相对路径,absolutize_url 在响应时拼当前 BASE_URL。
    07 修:从 hardcode http://localhost:8000/... 改为 /static/icons/...,这样部署到公网时跟着切。
    真实 icon 文件由 00-design-system 提供(当前缺,见 TD-014)"""
    return f"/static/icons/{slug}.svg"


def seed_all(db: Session) -> dict:
    """跑全套 seed,返回每种数据的计数"""
    counts = {"categories": 0, "admins": 0, "banners": 0, "products": 0}

    # ---- 分类 ----
    name_to_cat: dict[str, Category] = {}
    for c in CATEGORIES:
        existing = db.query(Category).filter_by(name=c["name"]).one_or_none()
        if existing:
            name_to_cat[c["name"]] = existing
            continue
        cat = Category(name=c["name"], icon_url=_icon_url(c["icon_slug"]), sort=c["sort"])
        db.add(cat)
        db.flush()
        name_to_cat[c["name"]] = cat
        counts["categories"] += 1

    # ---- admin ----
    if not db.query(Admin).filter_by(username=ADMIN_DEFAULT_USERNAME).one_or_none():
        admin = Admin(
            username=ADMIN_DEFAULT_USERNAME,
            password_hash=pwd_context.hash(ADMIN_DEFAULT_PASSWORD),
        )
        db.add(admin)
        counts["admins"] += 1

    # ---- banner ----
    if db.query(Banner).count() == 0:
        for b in BANNERS:
            db.add(Banner(
                image_url=_picsum(b["img"], 1680, 480),
                link_type=b["link_type"],
                link_target=b["link_target"],
                sort=b["sort"],
                on_show=True,
            ))
            counts["banners"] += 1

    # ---- products ----
    if db.query(Product).count() == 0:
        for p in PRODUCTS:
            cat = name_to_cat[p["cat"]]
            # 校验 product_type 合法
            assert p["type"] in (ProductType.PHYSICAL.value, ProductType.SERVICE_VOUCHER.value), \
                f"bad product_type: {p['type']}"
            db.add(Product(
                category_id=cat.id,
                title=p["title"],
                product_type=p["type"],
                price=p["price"],
                original_price=p["original"],
                spec=p["spec"],
                main_image_url=_picsum(p["img"]),
                description=f"{p['title']} · 演示商品,Demo 阶段使用占位描述。",
                on_sale=True,
            ))
            counts["products"] += 1

    db.commit()
    return counts
