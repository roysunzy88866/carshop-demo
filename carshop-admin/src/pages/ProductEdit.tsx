import { useEffect, useState } from 'react';
import { Card, Form, Input, Select, Radio, Switch, Button, Space, App } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { categoryApi, type Category } from '../api/category';
import { productApi, type ProductInput } from '../api/product';
import { PriceInput } from '../components/PriceInput';
import { ImageUpload } from '../components/ImageUpload';
import { ApiError } from '../api/client';

export default function ProductEdit() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const editing = !!id;
  const { message } = App.useApp();
  const [form] = Form.useForm<ProductInput>();
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    void categoryApi.listAdmin().then(setCategories);
  }, []);

  useEffect(() => {
    if (!editing) {
      form.setFieldsValue({
        product_type: 'physical',
        on_sale: true,
      } as any);
      return;
    }
    setLoading(true);
    productApi
      .detail(Number(id))
      .then((p) => {
        form.setFieldsValue({
          category_id: p.category_id,
          title: p.title,
          product_type: p.product_type,
          price: p.price,
          original_price: p.original_price,
          spec: p.spec,
          main_image_url: p.main_image_url,
          description: p.description,
          on_sale: p.on_sale,
        });
      })
      .catch((e) => message.error((e as Error).message))
      .finally(() => setLoading(false));
  }, [id]);

  const onFinish = async (values: ProductInput) => {
    setSaving(true);
    try {
      if (editing) {
        // PUT 不含 on_sale,需要单独 PATCH
        const { on_sale, ...rest } = values;
        await productApi.update(Number(id), rest);
        // 编辑时如果改了 on_sale 也同步一下
        await productApi.setOnSale(Number(id), on_sale);
        message.success('已保存');
      } else {
        await productApi.create(values);
        message.success('已新建');
      }
      navigate('/products');
    } catch (e) {
      if (e instanceof ApiError) message.error(e.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <Card title={editing ? '编辑商品' : '新建商品'} loading={loading}>
      <Form<ProductInput>
        form={form}
        layout="vertical"
        style={{ maxWidth: 720 }}
        onFinish={onFinish}
        autoComplete="off"
      >
        <Form.Item label="标题" name="title" rules={[{ required: true, min: 1, max: 100 }]}>
          <Input placeholder="商品标题" />
        </Form.Item>
        <Form.Item label="所属分类" name="category_id" rules={[{ required: true, message: '请选择分类' }]}>
          <Select
            placeholder="选择分类"
            options={categories.map((c) => ({ label: c.name, value: c.id }))}
          />
        </Form.Item>
        <Form.Item label="商品类型" name="product_type" rules={[{ required: true }]}>
          <Radio.Group>
            <Radio value="physical">实物</Radio>
            <Radio value="service_voucher">服务券</Radio>
          </Radio.Group>
        </Form.Item>
        <Form.Item label="现价" name="price" rules={[{ required: true, message: '请输入现价' }]}>
          <PriceInput placeholder="单位:元" />
        </Form.Item>
        <Form.Item label="原价(可选)" name="original_price">
          <PriceInput placeholder="可空" />
        </Form.Item>
        <Form.Item label="规格(可选)" name="spec">
          <Input maxLength={50} placeholder="例如:100 元面值" />
        </Form.Item>
        <Form.Item label="主图" name="main_image_url" rules={[{ required: true, message: '请上传主图' }]}>
          <ImageUpload />
        </Form.Item>
        <Form.Item label="描述(可选)" name="description">
          <Input.TextArea rows={4} maxLength={500} placeholder="支持换行" />
        </Form.Item>
        <Form.Item label="是否上架" name="on_sale" valuePropName="checked">
          <Switch />
        </Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={saving}>
            保存
          </Button>
          <Button onClick={() => navigate('/products')}>取消</Button>
        </Space>
      </Form>
    </Card>
  );
}
