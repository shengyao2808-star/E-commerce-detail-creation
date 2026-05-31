import { useEffect, useState } from "react";
import { Button, Card, Form, Input, Modal, Row, Space, Table, Tag, Typography, message } from "antd";
import { PlusOutlined, ReloadOutlined } from "@ant-design/icons";
import { useLang } from "../../i18n";
import type { TeamUser, TeamRole } from "../../services/types";

const { Title } = Typography;

type TabKey = "users" | "roles";

export default function TeamManagementPage() {
  const { t } = useLang();
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
    } catch { msg.error(t("team.loadFailed")); }
    finally { setLoading(false); }
  };

  const fetchRoles = async () => {
    setLoading(true);
    try {
      const res = await fetch("/api/v1/team/roles/all");
      const json = await res.json();
      setRoles(json.data ?? []);
    } catch { msg.error(t("team.loadFailed")); }
    finally { setLoading(false); }
  };

  useEffect(() => { tab === "users" ? fetchUsers() : fetchRoles(); }, [tab]);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      const url = tab === "users" ? "/api/v1/team/users" : "/api/v1/team/roles";
      await fetch(url, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(values) });
      msg.success(t("common.success"));
      setCreateOpen(false);
      form.resetFields();
      tab === "users" ? fetchUsers() : fetchRoles();
    } catch { msg.error(t("common.error")); }
  };

  const userCols = [
    { title: "ID", dataIndex: "id", width: 60 },
    { title: t("team.username"), dataIndex: "username" },
    { title: "显示名称", dataIndex: "displayName" },
    { title: t("team.email"), dataIndex: "email" },
    { title: t("team.status"), dataIndex: "status", render: (s: string) => <Tag color={s === "ACTIVE" ? "green" : "default"}>{s}</Tag> },
    { title: "创建时间", dataIndex: "createTime", width: 170 }
  ];

  const roleCols = [
    { title: "ID", dataIndex: "id", width: 60 },
    { title: "Code", dataIndex: "roleCode" },
    { title: t("team.roleName"), dataIndex: "roleName" },
    { title: "描述", dataIndex: "description", ellipsis: true },
    { title: "创建时间", dataIndex: "createTime", width: 170 }
  ];

  return (
    <div style={{ padding: 24 }}>
      {contextHolder}
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>{t("team.title")}</Title>
        <Space>
          <Button.Group>
            <Button type={tab === "users" ? "primary" : "default"} onClick={() => setTab("users")}>{t("team.users")}</Button>
            <Button type={tab === "roles" ? "primary" : "default"} onClick={() => setTab("roles")}>{t("team.roles")}</Button>
          </Button.Group>
          <Button icon={<ReloadOutlined />} onClick={() => tab === "users" ? fetchUsers() : fetchRoles()}>{t("common.refresh")}</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>{tab === "users" ? t("team.createUser") : t("team.createRole")}</Button>
        </Space>
      </Row>
      {tab === "users" ? (
        <Table rowKey="id" columns={userCols} dataSource={users} loading={loading} pagination={{ pageSize: 20 }} size="small" />
      ) : (
        <Table rowKey="id" columns={roleCols} dataSource={roles} loading={loading} pagination={{ pageSize: 20 }} size="small" />
      )}
      <Modal title={tab === "users" ? t("team.createUser") : t("team.createRole")} open={createOpen} onCancel={() => setCreateOpen(false)} onOk={handleCreate} destroyOnClose>
        <Form form={form} layout="vertical">
          {tab === "users" ? (
            <>
              <Form.Item name="username" label={t("team.username")} rules={[{ required: true }]}><Input /></Form.Item>
              <Form.Item name="displayName" label="显示名称" rules={[{ required: true }]}><Input /></Form.Item>
              <Form.Item name="email" label={t("team.email")}><Input /></Form.Item>
            </>
          ) : (
            <>
              <Form.Item name="roleCode" label="角色编码" rules={[{ required: true }]}><Input /></Form.Item>
              <Form.Item name="roleName" label={t("team.roleName")} rules={[{ required: true }]}><Input /></Form.Item>
              <Form.Item name="description" label="描述"><Input.TextArea rows={3} /></Form.Item>
            </>
          )}
        </Form>
      </Modal>
    </div>
  );
}