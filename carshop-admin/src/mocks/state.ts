// MSW 后端的内存状态机 · 从 artifacts/fixtures/ 的真实响应初始化,
// CRUD 全部在内存里做,保证 UI 操作有真实反馈(不是死 fixture)。
//
// 字段名 / 类型 / 嵌套结构 100% 跟 fixtures 对齐——这是"金标准"。

import getCategoriesJson from './fixtures/catalog/get-categories.json';
import getAdminProductsJson from './fixtures/catalog/get-admin-products.json';
import getAdminBannersJson from './fixtures/banner/get-admin-banners.json';
import adminGetOrdersJson from './fixtures/order/admin-get-orders-list.json';

import type { Category } from '../api/category';
import type { Product } from '../api/product';
import type { Banner } from '../api/banner';
import type { Order } from '../api/order';

export interface MockState {
  loggedIn: boolean;
  categories: Category[];
  products: Product[];
  banners: Banner[];
  orders: Order[];
  nextCategoryId: number;
  nextProductId: number;
  nextBannerId: number;
}

function deepClone<T>(x: T): T {
  return JSON.parse(JSON.stringify(x));
}

function freshState(): MockState {
  const categories = deepClone((getCategoriesJson as any).data) as Category[];
  const products = deepClone((getAdminProductsJson as any).data.list) as Product[];
  const banners = deepClone((getAdminBannersJson as any).data) as Banner[];
  const orders = deepClone((adminGetOrdersJson as any).data.list) as Order[];
  return {
    loggedIn: false,
    categories,
    products,
    banners,
    orders,
    nextCategoryId: Math.max(0, ...categories.map((c) => c.id)) + 1,
    nextProductId: Math.max(0, ...products.map((p) => p.id)) + 1,
    nextBannerId: Math.max(0, ...banners.map((b) => b.id)) + 1,
  };
}

const LS_KEY = 'carshop-msw-state';

function loadFromStorage(): MockState {
  if (typeof localStorage === 'undefined') return freshState();
  try {
    const raw = localStorage.getItem(LS_KEY);
    if (!raw) return freshState();
    const parsed = JSON.parse(raw);
    return { ...freshState(), ...parsed };
  } catch {
    return freshState();
  }
}

export const state: MockState = loadFromStorage();

export function persist() {
  if (typeof localStorage === 'undefined') return;
  try {
    localStorage.setItem(LS_KEY, JSON.stringify(state));
  } catch {
    /* ignore quota */
  }
}

export function resetState() {
  Object.assign(state, freshState());
  persist();
}

export function nowIso(): string {
  return new Date().toISOString().replace('Z', '+00:00');
}
