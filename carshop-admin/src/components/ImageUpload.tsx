import { useState } from 'react';
import { Upload, message, Image, Button, Space } from 'antd';
import { UploadOutlined, DeleteOutlined } from '@ant-design/icons';
import type { UploadFile } from 'antd/es/upload/interface';
import { uploadApi } from '../api/upload';
import { ApiError } from '../api/client';

export interface ImageUploadProps {
  value?: string;
  onChange?: (url: string) => void;
  disabled?: boolean;
}

const ACCEPT = '.jpg,.jpeg,.png,.webp';
const MAX_BYTES = 5 * 1024 * 1024;

// AntD Upload + customRequest 显式调 /api/v1/admin/upload。
// 拿到完整 URL 后塞进 form 字段(SPEC 共享约定 2:图片走 URL)。
export function ImageUpload({ value, onChange, disabled }: ImageUploadProps) {
  const [loading, setLoading] = useState(false);

  const beforeUpload = (file: File) => {
    const ext = (file.name.split('.').pop() ?? '').toLowerCase();
    if (!['jpg', 'jpeg', 'png', 'webp'].includes(ext)) {
      message.error('文件格式不支持(仅 jpg/png/webp)');
      return Upload.LIST_IGNORE;
    }
    if (file.size > MAX_BYTES) {
      message.error('文件不能超过 5MB');
      return Upload.LIST_IGNORE;
    }
    return true;
  };

  const customRequest = async ({ file, onSuccess, onError }: any) => {
    setLoading(true);
    try {
      const { url } = await uploadApi.uploadImage(file as File);
      onChange?.(url);
      onSuccess?.({ url });
      message.success('上传成功');
    } catch (e) {
      const err = e instanceof ApiError ? e : new Error('上传失败');
      message.error(err.message);
      onError?.(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Space direction="vertical" align="start">
      {value && (
        <div style={{ position: 'relative', display: 'inline-block' }}>
          <Image src={value} alt="main" width={160} height={120} style={{ objectFit: 'cover', borderRadius: 8 }} />
        </div>
      )}
      <Space>
        <Upload
          accept={ACCEPT}
          showUploadList={false}
          beforeUpload={beforeUpload}
          customRequest={customRequest}
          disabled={disabled || loading}
        >
          <Button icon={<UploadOutlined />} loading={loading} disabled={disabled}>
            {value ? '替换图片' : '上传图片'}
          </Button>
        </Upload>
        {value && (
          <Button icon={<DeleteOutlined />} danger onClick={() => onChange?.('')} disabled={disabled}>
            清空
          </Button>
        )}
      </Space>
    </Space>
  );
}
