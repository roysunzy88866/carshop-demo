import { apiClient, unwrap } from './client';

export const uploadApi = {
  uploadImage: (file: File) => {
    const fd = new FormData();
    fd.append('file', file);
    return unwrap<{ url: string }>(
      apiClient.post('/admin/upload', fd, {
        headers: { 'Content-Type': 'multipart/form-data' },
      }),
    );
  },
};
