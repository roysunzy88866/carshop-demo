import { useEffect, useState } from 'react';
import { Card, Table, Tabs, Drawer, Descriptions, Tag, Space, Typography, App, Image } from 'antd';
import dayjs from 'dayjs';
import { orderApi, type Order, type OrderStatus } from '../api/order';
import { PriceDisplay } from '../components/PriceDisplay';
import { colors } from '../theme/tokens';

const { Text } = Typography;

const STATUS_TABS: { key: 'all' | OrderStatus; label: string }[] = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '待支付' },
  { key: 'paid', label: '已支付' },
];

function StatusTag({ status }: { status: OrderStatus }) {
  if (status === 'paid') return <Tag color={colors.tertiary}>已支付</Tag>;
  return <Tag color={colors.error}>待支付</Tag>;
}

export default function OrderList() {
  const { message } = App.useApp();
  const [tab, setTab] = useState<'all' | OrderStatus>('all');
  const [list, setList] = useState<Order[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<Order | null>(null);

  const load = async () => {
    setLoading(true);
    try {
      const res = await orderApi.list({
        status: tab === 'all' ? undefined : tab,
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
    void load();
  }, [tab, page, pageSize]);

  return (
    <Card title="订单查看">
      <Tabs
        activeKey={tab}
        onChange={(k) => {
          setTab(k as any);
          setPage(1);
        }}
        items={STATUS_TABS.map((t) => ({ key: t.key, label: t.label }))}
      />
      <Table<Order>
        rowKey="id"
        dataSource={list}
        loading={loading}
        pagination={{
          current: page,
          pageSize,
          total,
          onChange: (p, ps) => {
            setPage(p);
            setPageSize(ps);
          },
        }}
        onRow={(o) => ({
          onClick: () => setDetail(o),
          style: { cursor: 'pointer' },
        })}
        columns={[
          { title: '订单号', dataIndex: 'id', width: 220, render: (v: string) => <Text code>{v}</Text> },
          {
            title: '设备',
            dataIndex: 'device_id_short',
            width: 100,
            render: (v: string) => <Tag>…{v}</Tag>,
          },
          {
            title: '商品',
            render: (_: unknown, o: Order) =>
              o.items[0]?.product_snapshot.title + (o.items.length > 1 ? ` 等 ${o.items.length} 件` : ''),
          },
          {
            title: '总金额',
            dataIndex: 'total_amount',
            width: 120,
            render: (v: number) => <PriceDisplay cents={v} style={{ fontWeight: 600 }} />,
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 100,
            render: (v: OrderStatus) => <StatusTag status={v} />,
          },
          {
            title: '下单时间',
            dataIndex: 'created_at',
            width: 180,
            render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm:ss'),
          },
        ]}
      />
      <Drawer
        title={detail ? `订单详情 · ${detail.id}` : '订单详情'}
        open={!!detail}
        onClose={() => setDetail(null)}
        width={560}
      >
        {detail && (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Descriptions column={1} bordered size="small">
              <Descriptions.Item label="订单号">{detail.id}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <StatusTag status={detail.status} />
              </Descriptions.Item>
              <Descriptions.Item label="设备 ID(完整)">{detail.device_id}</Descriptions.Item>
              <Descriptions.Item label="下单时间">
                {dayjs(detail.created_at).format('YYYY-MM-DD HH:mm:ss')}
              </Descriptions.Item>
              <Descriptions.Item label="支付时间">
                {detail.paid_at ? dayjs(detail.paid_at).format('YYYY-MM-DD HH:mm:ss') : '—'}
              </Descriptions.Item>
              <Descriptions.Item label="总金额">
                <PriceDisplay cents={detail.total_amount} style={{ fontWeight: 600 }} />
              </Descriptions.Item>
              <Descriptions.Item label="收货信息">
                {detail.shipping_info.name} · {detail.shipping_info.phone}
                <br />
                {detail.shipping_info.address}
              </Descriptions.Item>
            </Descriptions>
            <Card size="small" title="商品明细">
              {detail.items.map((it) => (
                <Space key={it.id} align="start" style={{ width: '100%', marginBottom: 12 }}>
                  <Image
                    src={it.product_snapshot.main_image_url}
                    width={72}
                    height={72}
                    style={{ objectFit: 'cover', borderRadius: 4 }}
                  />
                  <div>
                    <div style={{ fontWeight: 600 }}>{it.product_snapshot.title}</div>
                    {it.product_snapshot.spec && (
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        {it.product_snapshot.spec}
                      </Text>
                    )}
                    <div>
                      <PriceDisplay cents={it.price} /> × {it.quantity}
                    </div>
                  </div>
                </Space>
              ))}
            </Card>
          </Space>
        )}
      </Drawer>
    </Card>
  );
}
