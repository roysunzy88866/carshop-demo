import { apiClient, unwrap, type Page } from './client';

export type ProductType = 'physical' | 'service_voucher';

export interface Product {
  id: number;
  category_id: number;
  category_name: string;
  title: string;
  product_type: ProductType;
  price: number; // 分
  original_price: number | null;
  spec: string | null;
  main_image_url: string;
  description: string;
  on_sale: boolean;
  created_at: string;
  updated_at: string;
}

export interface ProductInput {
  category_id: number;
  title: string;
  product_type: ProductType;
  price: number;
  original_price?: number | null;
  spec?: string | null;
  main_image_url: string;
  description?: string;
  on_sale: boolean;
}

export interface ProductListParams {
  category_id?: number;
  on_sale?: boolean;
  page?: number;
  page_size?: number;
}

export const productApi = {
  list: (params: ProductListParams = {}) =>
    unwrap<Page<Product>>(apiClient.get('/admin/products', { params })),
  detail: (id: number) => unwrap<Product>(apiClient.get(`/admin/products/${id}`)),
  create: (body: ProductInput) =>
    unwrap<Product>(apiClient.post('/admin/products', body)),
  update: (id: number, body: Partial<Omit<ProductInput, 'on_sale'>>) =>
    unwrap<Product>(apiClient.put(`/admin/products/${id}`, body)),
  setOnSale: (id: number, on_sale: boolean) =>
    unwrap<Product>(apiClient.patch(`/admin/products/${id}/on_sale`, { on_sale })),
  remove: (id: number) =>
    unwrap<{ id: number; deleted: boolean }>(apiClient.delete(`/admin/products/${id}`)),
};
