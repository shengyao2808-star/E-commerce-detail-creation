import { useEffect, useState } from "react";
import { Button, Card, Select, Space, Table, Tag, Typography, message } from "antd";
import { ReloadOutlined } from "@ant-design/icons";
import type { OperationAuditLog } from "../../services/types";

const { Title } = Typography;

export default function AuditLogPage() {
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
    } catch { msg.error("Failed to load audit logs"); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchLogs(); }, [pageNum, actionFilter, targetFilter]);

  const columns = [
    { title: "ID", dataIndex: "id", width: 60 },
    { title: "Operator", dataIndex: "operatorName", width: 120 },
    { title: "Action", dataIndex: "action", width: 120, render: (s: string) => <Tag>{s}</Tag> },
    { title: "Target Type", dataIndex: "targetType", width: 130 },
    { title: "Target ID", dataIndex: "targetId", width: 100 },
    { title: "Detail", dataIndex: "detailJson", ellipsis: true },
    { title: "Time", dataIndex: "createTime", width: 170 }
  ];

  return (
    <div style={{ padding: 24 }}>
      {contextHolder}
      <Title level={4}>Operation Audit Log</Title>
      <Card size="small" style={{ marginBottom: 16 }}>
        <Space wrap>
          <Select allowClear placeholder="Action" style={{ width: 150 }} value={actionFilter} onChange={setActionFilter}
            options={[{ value: "CREATE" }, { value: "UPDATE" }, { value: "DELETE" }, { value: "EXPORT" }, { value: "AUDIT" }, { value: "LOGIN" }]} />
          <Select allowClear placeholder="Target Type" style={{ width: 150 }} value={targetFilter} onChange={setTargetFilter}
            options={[{ value: "product_detail" }, { value: "product_material" }, { value: "audit_record" }, { value: "export_record" }, { value: "image_job" }]} />
          <Button icon={<ReloadOutlined />} onClick={fetchLogs}>Refresh</Button>
        </Space>
      </Card>
      <Table rowKey="id" columns={columns} dataSource={logs} loading={loading}
        pagination={{ current: pageNum, pageSize: 20, total, onChange: setPageNum }}
        size="small" />
    </div>
  );
}