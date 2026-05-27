import { useEffect, useState } from 'react';
import { Card, Table, Button, Modal, Form, InputNumber, Select, Switch, Space, Popconfirm, App, Image, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { bannerApi, type Banner, type BannerInput, type BannerLinkType } from '../api/banner';
import { categoryApi, type Category } from '../api/category';
import { productApi, type Product } from '../api/product';
import { ImageUpload } from '../components/ImageUpload';
import { ApiError } from '../api/client';

const LINK_TYPE_OPTIONS = [
  { label: '无跳转', value: 'none' as const },
  { label: '跳商品', value: 'product' as const },
  { label: '跳分类', value: 'category' as const },
];

export default function BannerList() {
  const { message } = App.useApp();
  const [list, setList] = useState<Banner[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState<{ open: boolean; editing?: Banner }>({ open: false });
  const [form] = Form.useForm<BannerInput>();
  const linkType = Form.useWatch('link_type', form);

  const load = async () => {
    setLoading(true);
    try {
      setList(await bannerApi.list());
    } catch (e) {
      message.error((e as Error).message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
    void productApi.list({ page: 1, page_size: 100 }).then((r) => setProducts(r.list));
    void categoryApi.listAdmin().then(setCategories);
  }, []);

  const openCreate = () => {
    form.resetFields();
    form.setFieldsValue({
      link_type: 'none' as BannerLinkType,
      link_target: null,
      sort: 99,
      on_show: true,
    });
    setModal({ open: true });
  };
  const openEdit = (b: Banner) => {
    form.resetFields();
    form.setFieldsValue({
      image_url: b.image_url,
      link_type: b.link_type,
      link_target: b.link_target,
      sort: b.sort,
      on_show: b.on_show,
    });
    setModal({ open: true, editing: b });
  };

  const onSubmit = async () => {
    try {
      const values = await form.validateFields();
      // link_type=none 时强制 target=null
      if (values.link_type === 'none') values.link_target = null;
      if (modal.editing) {
        await bannerApi.update(modal.editing.id, values);
        message.success('已更新');
      } else {
        await bannerApi.create(values);
        message.success('已新建');
      }
      setModal({ open: false });
      await load();
    } catch (e) {
      if (e instanceof ApiError) message.error(e.message);
    }
  };

  const onDelete = async (id: number) => {
    try {
      await bannerApi.remove(id);
      message.success('已删除');
      await load();
    } catch (e) {
      if (e instanceof ApiError) message.error(e.message);
    }
  };

  const onToggleShow = async (b: Banner, checked: boolean) => {
    try {
      await bannerApi.update(b.id, { on_show: checked });
      message.success(checked ? '已展示' : '已隐藏');
      await load();
    } catch (e) {
      if (e instanceof ApiError) message.error(e.message);
    }
  };

  return (
    <Card
      title="Banner 管理"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          新建 Banner
        </Button>
      }
    >
      <Table<Banner>
        rowKey="id"
        dataSource={list}
        loading={loading}
        pagination={false}
        columns={[
          { title: 'ID', dataIndex: 'id', width: 60 },
          {
            title: '图片',
            dataIndex: 'image_url',
            width: 240,
            render: (url: string) => (
              <Image src={url} width={200} height={56} style={{ objectFit: 'cover', borderRadius: 4 }} />
            ),
          },
          {
            title: '跳转',
            render: (_: unknown, b: Banner) => {
              if (b.link_type === 'none') return <Tag>无跳转</Tag>;
              if (b.link_type === 'product') {
                const p = products.find((x) => x.id === b.link_target);
                return <Tag color="blue">商品: {p?.title ?? `#${b.link_target}`}</Tag>;
              }
              const c = categories.find((x) => x.id === b.link_target);
              return <Tag color="green">分类: {c?.name ?? `#${b.link_target}`}</Tag>;
            },
          },
          { title: '排序', dataIndex: 'sort', width: 80 },
          {
            title: '展示',
            dataIndex: 'on_show',
            width: 80,
            render: (v: boolean, b: Banner) => <Switch checked={v} onChange={(c) => onToggleShow(b, c)} />,
          },
          {
            title: '操作',
            width: 180,
            render: (_: unknown, b: Banner) => (
              <Space>
                <Button icon={<EditOutlined />} onClick={() => openEdit(b)}>
                  编辑
                </Button>
                <Popconfirm title="确认删除该 Banner?" onConfirm={() => onDelete(b.id)}>
                  <Button danger icon={<DeleteOutlined />}>
                    删除
                  </Button>
                </Popconfirm>
              </Space>
            ),
          },
        ]}
      />
      <Modal
        open={modal.open}
        title={modal.editing ? '编辑 Banner' : '新建 Banner'}
        onOk={onSubmit}
        onCancel={() => setModal({ open: false })}
        destroyOnClose
        width={600}
      >
        <Form form={form} layout="vertical" autoComplete="off">
          <Form.Item label="图片" name="image_url" rules={[{ required: true, message: '请上传图片' }]}>
            <ImageUpload />
          </Form.Item>
          <Form.Item label="跳转类型" name="link_type" rules={[{ required: true }]}>
            <Select options={LINK_TYPE_OPTIONS} />
          </Form.Item>
          {linkType === 'product' && (
            <Form.Item label="跳转商品" name="link_target" rules={[{ required: true, message: '请选择商品' }]}>
              <Select
                showSearch
                placeholder="选择商品"
                optionFilterProp="label"
                options={products.map((p) => ({ label: `#${p.id} ${p.title}`, value: p.id }))}
              />
            </Form.Item>
          )}
          {linkType === 'category' && (
            <Form.Item label="跳转分类" name="link_target" rules={[{ required: true, message: '请选择分类' }]}>
              <Select
                placeholder="选择分类"
                options={categories.map((c) => ({ label: c.name, value: c.id }))}
              />
            </Form.Item>
          )}
          <Form.Item label="排序" name="sort" rules={[{ required: true }]}>
            <InputNumber min={0} max={9999} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="是否展示" name="on_show" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
