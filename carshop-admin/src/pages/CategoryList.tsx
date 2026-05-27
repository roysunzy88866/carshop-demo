import { useEffect, useState } from 'react';
import { Card, Table, Button, Modal, Form, Input, InputNumber, Space, Popconfirm, App } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { categoryApi, type Category, type CategoryInput } from '../api/category';
import { ApiError } from '../api/client';

export default function CategoryList() {
  const { message } = App.useApp();
  const [list, setList] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState<{ open: boolean; editing?: Category }>({ open: false });
  const [form] = Form.useForm<CategoryInput>();

  const load = async () => {
    setLoading(true);
    try {
      setList(await categoryApi.listAdmin());
    } catch (e) {
      message.error((e as Error).message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const openCreate = () => {
    form.resetFields();
    form.setFieldsValue({ sort: 99 });
    setModal({ open: true });
  };
  const openEdit = (c: Category) => {
    form.resetFields();
    form.setFieldsValue({ name: c.name, icon_url: c.icon_url, sort: c.sort });
    setModal({ open: true, editing: c });
  };

  const onSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (modal.editing) {
        await categoryApi.update(modal.editing.id, values);
        message.success('已更新');
      } else {
        await categoryApi.create(values);
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
      await categoryApi.remove(id);
      message.success('已删除');
      await load();
    } catch (e) {
      if (e instanceof ApiError) message.error(e.message);
    }
  };

  return (
    <Card
      title="分类管理"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          新建分类
        </Button>
      }
    >
      <Table<Category>
        rowKey="id"
        dataSource={list}
        loading={loading}
        pagination={false}
        columns={[
          { title: 'ID', dataIndex: 'id', width: 80 },
          { title: '名称', dataIndex: 'name' },
          {
            title: '图标',
            dataIndex: 'icon_url',
            render: (url: string) =>
              url ? <img src={url} alt="" width={32} height={32} style={{ objectFit: 'contain' }} /> : '—',
          },
          { title: '排序', dataIndex: 'sort', width: 100 },
          {
            title: '操作',
            width: 200,
            render: (_: unknown, c: Category) => (
              <Space>
                <Button icon={<EditOutlined />} onClick={() => openEdit(c)}>
                  编辑
                </Button>
                <Popconfirm title="确认删除该分类?" onConfirm={() => onDelete(c.id)}>
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
        title={modal.editing ? '编辑分类' : '新建分类'}
        onOk={onSubmit}
        onCancel={() => setModal({ open: false })}
        destroyOnClose
      >
        <Form form={form} layout="vertical" autoComplete="off">
          <Form.Item label="名称" name="name" rules={[{ required: true, message: '请输入分类名' }]}>
            <Input maxLength={32} />
          </Form.Item>
          <Form.Item label="图标 URL" name="icon_url" rules={[{ required: true, message: '请输入图标 URL' }]}>
            <Input placeholder="/static/icons/xxx.svg 或 完整 URL" />
          </Form.Item>
          <Form.Item label="排序(小的在前)" name="sort" rules={[{ required: true }]}>
            <InputNumber min={0} max={9999} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
