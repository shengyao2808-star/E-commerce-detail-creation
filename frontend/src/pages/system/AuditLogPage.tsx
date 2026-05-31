import { useEffect, useState } from "react";
import { Button, Card, Select, Space, Table, Tag, Typography, message } from "antd";
import { ReloadOutlined } from "@ant-design/icons";
import { useLang } from "../../i18n";
import type { OperationAuditLog } from "../../services/types";

const { Title } = Typography;

export default function AuditLogPage() {
  const { t } = useLang();
  const [logs, setLogs] = useState<OperationAuditLog[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [actionFilter, setActionFilter] = useState<string | undefined>();
  const [targetFilter, setTargetFilter] = useState<string | undefined>();
  const [msg, contextHolder] = message.useMessage();

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ pageNum: String(pageNum), pageSize: "20" });
      if (actionFilter) params.set("action", actionFilter);
      if (targetFilter) params.set("targetType", targetFilter);
      const res = await fetch(`/api/v1/audit-logs/list?${params}`);
      const json = await res.json();
      setLogs(json.data ?? []);
      setTotal(json.total ?? 0);
    } catch { msg.error(t("auditlog.loadFailed")); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchLogs(); }, [pageNum, actionFilter, targetFilter]);

  const columns = [
    { title: "ID", dataIndex: "id", width: 60 },
    { title: t("auditlog.user"), dataIndex: "operatorName", width: 120 },
    { title: t("auditlog.action"), dataIndex: "action", width: 120, render: (s: string) => <Tag>{s}</Tag> },
    { title: t("auditlog.target"), dataIndex: "targetType", width: 130 },
    { title: "目标 ID", dataIndex: "targetId", width: 100 },
    { title: t("auditlog.detail"), dataIndex: "detailJson", ellipsis: true },
    { title: t("auditlog.time"), dataIndex: "createTime", width: 170 }
  ];

  return (
    <div style={{ padding: 24 }}>
      {contextHolder}
      <Title level={4}>{t("auditlog.title")}</Title>
      <Card size="small" style={{ marginBottom: 16 }}>
        <Space wrap>
          <Select allowClear placeholder={t("auditlog.filter.action")} style={{ width: 150 }} value={actionFilter} onChange={setActionFilter}
            options={[{ value: "CREATE", label: "创建" }, { value: "UPDATE", label: "更新" }, { value: "DELETE", label: "删除" }, { value: "EXPORT", label: "导出" }, { value: "AUDIT", label: "审核" }, { value: "LOGIN", label: "登录" }]} />
          <Select allowClear placeholder={t("auditlog.filter.target")} style={{ width: 150 }} value={targetFilter} onChange={setTargetFilter}
            options={[{ value: "product_detail", label: "商品详情" }, { value: "product_material", label: "商品素材" }, { value: "audit_record", label: "审核记录" }, { value: "export_record", label: "导出记录" }, { value: "image_job", label: "图片任务" }]} />
          <Button icon={<ReloadOutlined />} onClick={fetchLogs}>{t("common.refresh")}</Button>
        </Space>
      </Card>
      <Table rowKey="id" columns={columns} dataSource={logs} loading={loading}
        pagination={{ current: pageNum, pageSize: 20, total, onChange: setPageNum }}
        size="small" />
    </div>
  );
}