import { apiClient, unwrap, type Page } from './client';

export type OrderStatus = 'pending' | 'paid';

export interface OrderItem {
  id: number;
  product_id: number | null;
  product_snapshot: {
    id: number;
    title: string;
    product_type: 'physical' | 'service_voucher';
    price: number;
    spec: string | null;
    main_image_url: string;
  };
  quantity: number;
  price: number;
}

export interface ShippingInfo {
  name: string;
  phone: string;
  address: string;
}

export interface Order {
  id: string;
  device_id: string;
  device_id_short: string; // 后台特有,后 6 位
  status: OrderStatus;
  total_amount: number; // 分
  shipping_info: ShippingInfo;
  items: OrderItem[];
  created_at: string;
  paid_at: string | null;
}

export interface OrderListParams {
  status?: OrderStatus;
  page?: number;
  page_size?: number;
}

export const orderApi = {
  list: (params: OrderListParams = {}) =>
    unwrap<Page<Order>>(apiClient.get('/admin/orders', { params })),
  detail: (id: string) => unwrap<Order>(apiClient.get(`/admin/orders/${id}`)),
};
