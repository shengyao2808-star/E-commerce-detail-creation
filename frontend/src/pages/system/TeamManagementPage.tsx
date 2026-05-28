import { useEffect, useState } from "react";
import { Button, Card, Form, Input, Modal, Row, Space, Table, Tag, Typography, message } from "antd";
import { PlusOutlined, ReloadOutlined } from "@ant-design/icons";
import type { TeamUser, TeamRole } from "../../services/types";

const { Title } = Typography;

type TabKey = "users" | "roles";

export default function TeamManagementPage() {
  const [tab, setTab] = useState<TabKey>("users");
  const [users, setUsers] = useState<TeamUser[]>([]);
  const [roles, setRoles] = useState<TeamRole[]>([]);
  const [loading, setLoading] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();
  const [msg, contextHolder] = message.useMessage();

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await fetch("/api/v1/team/users/list?pageNum=1&pageSize=100");
      const json = await res.json();
      setUsers(json.data ?? []);
    } catch { msg.error("Failed to load users"); }
    finally { setLoading(false); }
  };

  const fetchRoles = async () => {
    setLoading(true);
    try {
      const res = await fetch("/api/v1/team/roles/all");
      const json = await res.json();
      setRoles(json.data ?? []);
    } catch { msg.error("Failed to load roles"); }
    finally { setLoading(false); }
  };

  useEffect(() => { tab === "users" ? fetchUsers() : fetchRoles(); }, [tab]);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      const url = tab === "users" ? "/api/v1/team/users" : "/api/v1/team/roles";
      await fetch(url, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(values) });
      msg.success("Created");
      setCreateOpen(false);
      form.resetFields();
      tab === "users" ? fetchUsers() : fetchRoles();
    } catch { msg.error("Create failed"); }
  };

  const userCols = [
    { title: "ID", dataIndex: "id", width: 60 },
    { title: "Username", dataIndex: "username" },
    { title: "Display Name", dataIndex: "displayName" },
    { title: "Email", dataIndex: "email" },
    { title: "Status", dataIndex: "status", render: (s: string) => <Tag color={s === "ACTIVE" ? "green" : "default"}>{s}</Tag> },
    { title: "Created", dataIndex: "createTime", width: 170 }
  ];

  const roleCols = [
    { title: "ID", dataIndex: "id", width: 60 },
    { title: "Code", dataIndex: "roleCode" },
    { title: "Name", dataIndex: "roleName" },
    { title: "Description", dataIndex: "description", ellipsis: true },
    { title: "Created", dataIndex: "createTime", width: 170 }
  ];

  return (
    <div style={{ padding: 24 }}>
      {contextHolder}
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>Team Management</Title>
        <Space>
          <Button.Group>
            <Button type={tab === "users" ? "primary" : "default"} onClick={() => setTab("users")}>Users</Button>
            <Button type={tab === "roles" ? "primary" : "default"} onClick={() => setTab("roles")}>Roles</Button>
          </Button.Group>
          <Button icon={<ReloadOutlined />} onClick={() => tab === "users" ? fetchUsers() : fetchRoles()}>Refresh</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>New {tab === "users" ? "User" : "Role"}</Button>
        </Space>
      </Row>
      {tab === "users" ? (
        <Table rowKey="id" columns={userCols} dataSource={users} loading={loading} pagination={{ pageSize: 20 }} size="small" />
      ) : (
        <Table rowKey="id" columns={roleCols} dataSource={roles} loading={loading} pagination={{ pageSize: 20 }} size="small" />
      )}
      <Modal title={`Create ${tab === "users" ? "User" : "Role"}`} open={createOpen} onCancel={() => setCreateOpen(false)} onOk={handleCreate} destroyOnClose>
        <Form form={form} layout="vertical">
          {tab === "users" ? (
            <>
              <Form.Item name="username" label="Username" rules={[{ required: true }]}><Input /></Form.Item>
              <Form.Item name="displayName" label="Display Name" rules={[{ required: true }]}><Input /></Form.Item>
              <Form.Item name="email" label="Email"><Input /></Form.Item>
            </>
          ) : (
            <>
              <Form.Item name="roleCode" label="Role Code" rules={[{ required: true }]}><Input /></Form.Item>
              <Form.Item name="roleName" label="Role Name" rules={[{ required: true }]}><Input /></Form.Item>
              <Form.Item name="description" label="Description"><Input.TextArea rows={3} /></Form.Item>
            </>
          )}
        </Form>
      </Modal>
    </div>
  );
}