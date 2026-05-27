import { apiClient, unwrap } from './client';

export interface Category {
  id: number;
  name: string;
  icon_url: string;
  sort: number;
  created_at: string;
}

export interface CategoryInput {
  name: string;
  icon_url: string;
  sort: number;
}

export const categoryApi = {
  listPublic: () => unwrap<Category[]>(apiClient.get('/categories')),
  listAdmin: () => unwrap<Category[]>(apiClient.get('/admin/categories')),
  create: (body: CategoryInput) =>
    unwrap<Category>(apiClient.post('/admin/categories', body)),
  update: (id: number, body: Partial<CategoryInput>) =>
    unwrap<Category>(apiClient.put(`/admin/categories/${id}`, body)),
  remove: (id: number) =>
    unwrap<{ id: number; deleted: boolean }>(apiClient.delete(`/admin/categories/${id}`)),
};
