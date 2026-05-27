import { useEffect, useState } from 'react';
import { Card, Table, Button, Space, Switch, Popconfirm, Select, Tag, App, Image } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { categoryApi, type Category } from '../api/category';
import { productApi, type Product } from '../api/product';
import { PriceDisplay } from '../components/PriceDisplay';
import { ApiError } from '../api/client';
import { colors } from '../theme/tokens';

export default function ProductList() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const [list, setList] = useState<Product[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [categoryId, setCategoryId] = useState<number | undefined>();
  const [onSale, setOnSale] = useState<boolean | undefined>();
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const res = await productApi.list({
        category_id: categoryId,
        on_sale: onSale,
        page,
        page_size: pageSize,
      });
      setList(res.list);
      setTotal(res.total);
    } catch (e) {
      message.error((e as Error).message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void categoryApi.listAdmin().then(setCategories).catch(() => undefined);
  }, []);

  useEffect(() => {
    void load();
  }, [page, pageSize, categoryId, onSale]);

  const onToggleSale = async (p: Product, checked: boolean) => {
    try {
      await productApi.setOnSale(p.id, checked);
      message.success(checked ? '已上架' : '已下架');
      await load();
    } catch (e) {
      if (e instanceof ApiError) message.error(e.message);
    }
  };

  const onDelete = async (id: number) => {
    try {
      await productApi.remove(id);
      message.success('已删除');
      await load();
    } catch (e) {
      if (e instanceof ApiError) message.error(e.message);
    }
  };

  return (
    <Card
      title="商品管理"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/products/new')}>
          新建商品
        </Button>
      }
    >
      <Space style={{ marginBottom: 16 }} wrap>
        <span>分类:</span>
        <Select
          allowClear
          placeholder="全部分类"
          style={{ width: 180 }}
          value={categoryId}
          onChange={(v) => {
            setCategoryId(v);
            setPage(1);
          }}
          options={categories.map((c) => ({ label: c.name, value: c.id }))}
        />
        <span style={{ marginLeft: 16 }}>上架状态:</span>
        <Select
          allowClear
          placeholder="全部"
          style={{ width: 140 }}
          value={onSale}
          onChange={(v) => {
            setOnSale(v);
            setPage(1);
          }}
          options={[
            { label: '在售', value: true },
            { label: '已下架', value: false },
          ]}
        />
      </Space>
      <Table<Product>
        rowKey="id"
        dataSource={list}
        loading={loading}
        pagination={{
          current: page,
          pageSize,
          total,
          showSizeChanger: true,
          onChange: (p, ps) => {
            setPage(p);
            setPageSize(ps);
          },
        }}
        columns={[
          { title: 'ID', dataIndex: 'id', width: 60 },
          {
            title: '主图',
            dataIndex: 'main_image_url',
            width: 80,
            render: (url: string) => (
              <Image src={url} width={48} height={48} style={{ objectFit: 'cover', borderRadius: 4 }} />
            ),
          },
          { title: '标题', dataIndex: 'title' },
          { title: '分类', dataIndex: 'category_name', width: 120 },
          {
            title: '类型',
            dataIndex: 'product_type',
            width: 100,
            render: (t: string) =>
              t === 'physical' ? <Tag color={colors.error}>实物</Tag> : <Tag color={colors.tertiary}>服务券</Tag>,
          },
          {
            title: '现价',
            dataIndex: 'price',
            width: 100,
            render: (v: number, p: Product) => (
              <PriceDisplay
                cents={v}
                style={{
                  color: p.product_type === 'physical' ? colors.textPrice : colors.textPriceEnergy,
                  fontWeight: 600,
                }}
              />
            ),
          },
          {
            title: '原价',
            dataIndex: 'original_price',
            width: 100,
            render: (v: number | null) => <PriceDisplay cents={v} strike style={{ color: colors.textPriceStrike }} />,
          },
          {
            title: '上架',
            dataIndex: 'on_sale',
            width: 80,
            render: (v: boolean, p: Product) => <Switch checked={v} onChange={(c) => onToggleSale(p, c)} />,
          },
          {
            title: '操作',
            width: 180,
            render: (_: unknown, p: Product) => (
              <Space>
                <Button icon={<EditOutlined />} onClick={() => navigate(`/products/${p.id}/edit`)}>
                  编辑
                </Button>
                <Popconfirm title="确认删除该商品?" onConfirm={() => onDelete(p.id)}>
                  <Button danger icon={<DeleteOutlined />}>
                    删除
                  </Button>
                </Popconfirm>
              </Space>
            ),
          },
        ]}
      />
    </Card>
  );
}
