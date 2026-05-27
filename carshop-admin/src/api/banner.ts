import { apiClient, unwrap } from './client';

export type BannerLinkType = 'none' | 'product' | 'category';

export interface Banner {
  id: number;
  image_url: string;
  link_type: BannerLinkType;
  link_target: number | null;
  sort: number;
  on_show: boolean;
}

export interface BannerInput {
  image_url: string;
  link_type: BannerLinkType;
  link_target: number | null;
  sort: number;
  on_show: boolean;
}

export const bannerApi = {
  list: () => unwrap<Banner[]>(apiClient.get('/admin/banners')),
  create: (body: BannerInput) => unwrap<Banner>(apiClient.post('/admin/banners', body)),
  update: (id: number, body: Partial<BannerInput>) =>
    unwrap<Banner>(apiClient.put(`/admin/banners/${id}`, body)),
  remove: (id: number) =>
    unwrap<{ id: number; deleted: boolean }>(apiClient.delete(`/admin/banners/${id}`)),
};
