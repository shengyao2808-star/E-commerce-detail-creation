import { useEffect, useState } from "react";
import { Card, Col, Descriptions, Row, Spin, Table, Tag, Typography, message } from "antd";
import { api } from "../../services/api";
import type { EnvironmentDiagnostic } from "../../services/types";

const { Title } = Typography;

const STATUS_COLOR: Record<string, string> = {
  READY: "success", AVAILABLE: "success", OK: "success",
  DEGRADED: "warning", WARNING: "warning", NOT_CONFIGURED: "default",
  NOT_READY: "error", CONFIG_ERROR: "error", ERROR: "error", MISSING: "error", NOT_WRITABLE: "error"
};

export default function DiagnosticsPage() {
  const [diag, setDiag] = useState<EnvironmentDiagnostic | null>(null);
  const [loading, setLoading] = useState(true);
  const [msg, contextHolder] = message.useMessage();

  useEffect(() => {
    api.system.diagnostics()
      .then(setDiag)
      .catch(() => msg.error("Failed to load diagnostics"))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spin style={{ padding: 48 }} />;
  if (!diag) return <div style={{ padding: 24 }}>No diagnostic data</div>;

  return (
    <div style={{ padding: 24 }}>
      {contextHolder}
      <Title level={4}>Environment Diagnostics</Title>
      <Card style={{ marginBottom: 16 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="Overall Status">
            <Tag color={STATUS_COLOR[diag.overallStatus ?? ""] ?? "default"}>{diag.overallStatus}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Message">{diag.message ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="Generated At">
            {diag.generatedAt ? new Date(diag.generatedAt).toLocaleString() : "-"}
          </Descriptions.Item>
        </Descriptions>
      </Card>
      {diag.aiRelay && (
        <Card title="AI Relay" style={{ marginBottom: 16 }}>
          <Descriptions column={2} size="small">
            <Descriptions.Item label="Status"><Tag color={STATUS_COLOR[diag.aiRelay.status ?? ""]}>{diag.aiRelay.status}</Tag></Descriptions.Item>
            <Descriptions.Item label="Enabled">{diag.aiRelay.enabled ? "Yes" : "No"}</Descriptions.Item>
            <Descriptions.Item label="Base URL">{diag.aiRelay.baseUrl ?? "-"}</Descriptions.Item>
            <Descriptions.Item label="Model">{diag.aiRelay.model ?? "-"}</Descriptions.Item>
            <Descriptions.Item label="Has API Key">{diag.aiRelay.hasApiKey ? "Yes" : "No"}</Descriptions.Item>
            <Descriptions.Item label="Message">{diag.aiRelay.message ?? "-"}</Descriptions.Item>
          </Descriptions>
        </Card>
      )}
      {diag.tools && diag.tools.length > 0 && (
        <Card title="Tool Adapters" style={{ marginBottom: 16 }}>
          <Table
            rowKey="code"
            dataSource={diag.tools}
            pagination={false}
            size="small"
            columns={[
              { title: "Code", dataIndex: "code", width: 140 },
              { title: "Name", dataIndex: "name", width: 160 },
              { title: "Category", dataIndex: "category", width: 180 },
              { title: "Status", dataIndex: "status", width: 130, render: (s: string) => <Tag color={STATUS_COLOR[s]}>{s}</Tag> },
              { title: "Enabled", dataIndex: "enabled", width: 80, render: (v: boolean) => v ? "Yes" : "No" },
              { title: "Message", dataIndex: "message", ellipsis: true }
            ]}
          />
        </Card>
      )}
      {diag.export && (
        <Card title="Export Formats" style={{ marginBottom: 16 }}>
          <Descriptions column={1} size="small">
            <Descriptions.Item label="Supported">{diag.export.supportedFormats?.join(", ") ?? "-"}</Descriptions.Item>
            <Descriptions.Item label="Unimplemented">{diag.export.unimplementedFormats?.join(", ") ?? "-"}</Descriptions.Item>
            <Descriptions.Item label="Status"><Tag color={STATUS_COLOR[diag.export.status ?? ""]}>{diag.export.status}</Tag></Descriptions.Item>
          </Descriptions>
        </Card>
      )}
    </div>
  );
}