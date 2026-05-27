// MSW handlers · 完整覆盖 02/03/04/05 的所有后台接口,行为对齐 fixtures 和 SPEC §6/§12/§13。
// 内存状态机见 ./state.ts,所有 CRUD 真改 state,刷新页面会重置。

import { http, HttpResponse } from 'msw';
import { state, nowIso, persist } from './state';
import type { Banner, BannerLinkType } from '../api/banner';
import type { Product, ProductType } from '../api/product';

import postUploadSuccess from './fixtures/auth/post-upload-success.json';

// ===== 通用响应壳 =====
function ok<T>(data: T) {
  persist(); // 写后 always persist;读不写也无副作用
  return HttpResponse.json({ code: 0, data, message: 'ok' });
}
function err(code: number, message: string, status = 400) {
  return HttpResponse.json({ code, data: null, message }, { status });
}
function requireAdmin() {
  if (!state.loggedIn) return err(2000, '未登录或登录已过期', 401);
  return null;
}

// ===== 鉴权 =====
const authHandlers = [
  http.post('/api/v1/admin/login', async ({ request }) => {
    const body = (await request.json()) as { username?: string; password?: string };
    console.log('[MSW] login attempt', JSON.stringify(body));
    if (body?.username === 'admin' && body?.password === 'admin123') {
      state.loggedIn = true;
      persist();
      return HttpResponse.json(
        { code: 0, data: { id: 1, username: 'admin' }, message: 'ok' },
        { headers: { 'Set-Cookie': 'session=mock-session; Path=/; HttpOnly; SameSite=Lax; Max-Age=86400' } },
      );
    }
    return err(2001, '用户名或密码错误', 401);
  }),
  http.post('/api/v1/admin/logout', () => {
    state.loggedIn = false;
    return ok(null);
  }),
  http.get('/api/v1/admin/me', () => {
    if (!state.loggedIn) return err(2000, '未登录或登录已过期', 401);
    return ok({ id: 1, username: 'admin' });
  }),
];

// ===== 上传 =====
const uploadHandlers = [
  http.post('/api/v1/admin/upload', async ({ request }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const fd = await request.formData();
    const file = fd.get('file');
    if (!(file instanceof File)) return err(1000, '未上传文件');
    if (file.size > 5 * 1024 * 1024) return err(1000, '文件超过 5MB');
    const ext = (file.name.split('.').pop() ?? '').toLowerCase();
    const allowed = ['jpg', 'jpeg', 'png', 'webp'];
    if (!allowed.includes(ext)) return err(1000, '仅支持 jpg/png/webp');
    // 用 fixture 里的成功 URL(picsum 占位图,前端能加载到)
    const seed = `mock-${Date.now()}`;
    return ok({ url: `https://picsum.photos/seed/${seed}/600/400` });
  }),
];

// ===== 分类 =====
const categoryHandlers = [
  http.get('/api/v1/admin/categories', () => {
    const guard = requireAdmin();
    if (guard) return guard;
    return ok([...state.categories].sort((a, b) => a.sort - b.sort));
  }),
  http.post('/api/v1/admin/categories', async ({ request }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const body = (await request.json()) as any;
    if (!body?.name) return err(1000, 'name 必填');
    const cat = {
      id: state.nextCategoryId++,
      name: String(body.name),
      icon_url: String(body.icon_url ?? ''),
      sort: Number(body.sort ?? 99),
      created_at: nowIso(),
    };
    state.categories.push(cat);
    return ok(cat);
  }),
  http.put('/api/v1/admin/categories/:id', async ({ request, params }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const id = Number(params.id);
    const cat = state.categories.find((c) => c.id === id);
    if (!cat) return err(1001, '分类不存在', 404);
    const body = (await request.json()) as any;
    if (body.name !== undefined) cat.name = body.name;
    if (body.icon_url !== undefined) cat.icon_url = body.icon_url;
    if (body.sort !== undefined) cat.sort = body.sort;
    return ok(cat);
  }),
  http.delete('/api/v1/admin/categories/:id', ({ params }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const id = Number(params.id);
    const cat = state.categories.find((c) => c.id === id);
    if (!cat) return err(1001, '分类不存在', 404);
    const productCount = state.products.filter((p) => p.category_id === id).length;
    if (productCount > 0) {
      return err(1002, `该分类下还有 ${productCount} 个商品,请先移除或删除商品`, 409);
    }
    state.categories = state.categories.filter((c) => c.id !== id);
    return ok({ id, deleted: true });
  }),
];

// ===== 商品 =====
function enrichProduct(p: Product): Product {
  const cat = state.categories.find((c) => c.id === p.category_id);
  return { ...p, category_name: cat?.name ?? '' };
}

const productHandlers = [
  http.get('/api/v1/admin/products', ({ request }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const url = new URL(request.url);
    const categoryId = url.searchParams.get('category_id');
    const onSale = url.searchParams.get('on_sale');
    const page = Number(url.searchParams.get('page') ?? 1);
    const pageSize = Number(url.searchParams.get('page_size') ?? 20);
    let list = state.products.map(enrichProduct);
    if (categoryId !== null) list = list.filter((p) => p.category_id === Number(categoryId));
    if (onSale !== null) list = list.filter((p) => p.on_sale === (onSale === 'true'));
    const total = list.length;
    list = list.slice((page - 1) * pageSize, page * pageSize);
    return ok({ list, total, page, page_size: pageSize });
  }),
  http.get('/api/v1/admin/products/:id', ({ params }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const id = Number(params.id);
    const p = state.products.find((x) => x.id === id);
    if (!p) return err(1001, '商品不存在', 404);
    return ok(enrichProduct(p));
  }),
  http.post('/api/v1/admin/products', async ({ request }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const body = (await request.json()) as any;
    const required = ['category_id', 'title', 'product_type', 'price', 'main_image_url'];
    for (const k of required) {
      if (body[k] === undefined || body[k] === null || body[k] === '') {
        return err(1000, `Field required: ${k}`);
      }
    }
    if (!state.categories.find((c) => c.id === Number(body.category_id))) {
      return err(1001, '分类不存在', 404);
    }
    const now = nowIso();
    const p: Product = {
      id: state.nextProductId++,
      category_id: Number(body.category_id),
      category_name: '',
      title: String(body.title),
      product_type: body.product_type as ProductType,
      price: Number(body.price),
      original_price: body.original_price ?? null,
      spec: body.spec ?? null,
      main_image_url: String(body.main_image_url),
      description: body.description ?? '',
      on_sale: body.on_sale ?? true,
      created_at: now,
      updated_at: now,
    };
    state.products.push(p);
    return ok(enrichProduct(p));
  }),
  http.put('/api/v1/admin/products/:id', async ({ request, params }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const id = Number(params.id);
    const p = state.products.find((x) => x.id === id);
    if (!p) return err(1001, '商品不存在', 404);
    const body = (await request.json()) as any;
    const editable = [
      'category_id',
      'title',
      'product_type',
      'price',
      'original_price',
      'spec',
      'main_image_url',
      'description',
    ];
    for (const k of editable) {
      if (body[k] !== undefined) (p as any)[k] = body[k];
    }
    if (body.category_id !== undefined && !state.categories.find((c) => c.id === Number(body.category_id))) {
      return err(1001, '分类不存在', 404);
    }
    p.updated_at = nowIso();
    return ok(enrichProduct(p));
  }),
  http.patch('/api/v1/admin/products/:id/on_sale', async ({ request, params }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const id = Number(params.id);
    const p = state.products.find((x) => x.id === id);
    if (!p) return err(1001, '商品不存在', 404);
    const body = (await request.json()) as any;
    if (typeof body?.on_sale !== 'boolean') return err(1000, 'on_sale 必须为 boolean');
    p.on_sale = body.on_sale;
    p.updated_at = nowIso();
    return ok(enrichProduct(p));
  }),
  http.delete('/api/v1/admin/products/:id', ({ params }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const id = Number(params.id);
    if (!state.products.find((x) => x.id === id)) return err(1001, '商品不存在', 404);
    state.products = state.products.filter((p) => p.id !== id);
    return ok({ id, deleted: true });
  }),
];

// ===== Banner =====
function validateLink(linkType: BannerLinkType, linkTarget: number | null): string | null {
  if (linkType === 'none') {
    if (linkTarget !== null && linkTarget !== undefined)
      return 'link_type=none 时 link_target 必须为 null';
    return null;
  }
  if (linkTarget === null || linkTarget === undefined)
    return `link_type=${linkType} 时 link_target 必填`;
  if (linkType === 'product') {
    if (!state.products.find((p) => p.id === linkTarget))
      return `link_target 指向的商品不存在(product_id=${linkTarget})`;
  } else if (linkType === 'category') {
    if (!state.categories.find((c) => c.id === linkTarget))
      return `link_target 指向的分类不存在(category_id=${linkTarget})`;
  }
  return null;
}

const bannerHandlers = [
  http.get('/api/v1/admin/banners', () => {
    const guard = requireAdmin();
    if (guard) return guard;
    return ok([...state.banners].sort((a, b) => a.sort - b.sort));
  }),
  http.post('/api/v1/admin/banners', async ({ request }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const body = (await request.json()) as any;
    if (!body?.image_url) return err(1000, 'image_url 必填');
    const linkType = (body.link_type ?? 'none') as BannerLinkType;
    const linkTarget = body.link_target ?? null;
    const msg = validateLink(linkType, linkTarget);
    if (msg) return err(1000, msg);
    const b: Banner = {
      id: state.nextBannerId++,
      image_url: body.image_url,
      link_type: linkType,
      link_target: linkTarget,
      sort: body.sort ?? 0,
      on_show: body.on_show ?? true,
    };
    state.banners.push(b);
    return ok(b);
  }),
  http.put('/api/v1/admin/banners/:id', async ({ request, params }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const id = Number(params.id);
    const b = state.banners.find((x) => x.id === id);
    if (!b) return err(1001, `banner 不存在(id=${id})`, 404);
    const body = (await request.json()) as any;
    const merged = {
      image_url: body.image_url ?? b.image_url,
      link_type: (body.link_type ?? b.link_type) as BannerLinkType,
      link_target: body.link_target !== undefined ? body.link_target : b.link_target,
      sort: body.sort ?? b.sort,
      on_show: body.on_show !== undefined ? body.on_show : b.on_show,
    };
    const msg = validateLink(merged.link_type, merged.link_target);
    if (msg) return err(1000, msg);
    Object.assign(b, merged);
    return ok(b);
  }),
  http.delete('/api/v1/admin/banners/:id', ({ params }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const id = Number(params.id);
    if (!state.banners.find((b) => b.id === id))
      return err(1001, `banner 不存在(id=${id})`, 404);
    state.banners = state.banners.filter((b) => b.id !== id);
    return ok({ id, deleted: true });
  }),
];

// ===== 订单(只读) =====
const orderHandlers = [
  http.get('/api/v1/admin/orders', ({ request }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const url = new URL(request.url);
    const status = url.searchParams.get('status');
    const page = Number(url.searchParams.get('page') ?? 1);
    const pageSize = Number(url.searchParams.get('page_size') ?? 20);
    let list = [...state.orders];
    if (status) list = list.filter((o) => o.status === status);
    list.sort((a, b) => b.created_at.localeCompare(a.created_at));
    const total = list.length;
    list = list.slice((page - 1) * pageSize, page * pageSize);
    return ok({ list, total, page, page_size: pageSize });
  }),
  http.get('/api/v1/admin/orders/:id', ({ params }) => {
    const guard = requireAdmin();
    if (guard) return guard;
    const o = state.orders.find((x) => x.id === params.id);
    if (!o) return err(1001, '订单不存在', 404);
    return ok(o);
  }),
];

export const handlers = [
  ...authHandlers,
  ...uploadHandlers,
  ...categoryHandlers,
  ...productHandlers,
  ...bannerHandlers,
  ...orderHandlers,
];

// avoid unused import warning when production-tree-shaken
void postUploadSuccess;
