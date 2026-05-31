import { useEffect, useState } from "react";
import { Card, Col, Descriptions, Row, Spin, Table, Tag, Typography, message } from "antd";
import { api } from "../../services/api";
import { useLang } from "../../i18n";
import type { EnvironmentDiagnostic } from "../../services/types";

const { Title } = Typography;

const STATUS_COLOR: Record<string, string> = {
  READY: "success", AVAILABLE: "success", OK: "success",
  DEGRADED: "warning", WARNING: "warning", NOT_CONFIGURED: "default",
  NOT_READY: "error", CONFIG_ERROR: "error", ERROR: "error", MISSING: "error", NOT_WRITABLE: "error"
};

export default function DiagnosticsPage() {
  const { t } = useLang();
  const [diag, setDiag] = useState<EnvironmentDiagnostic | null>(null);
  const [loading, setLoading] = useState(true);
  const [msg, contextHolder] = message.useMessage();

  useEffect(() => {
    api.system.diagnostics()
      .then(setDiag)
      .catch(() => msg.error(t("diag.loadFailed")))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spin style={{ padding: 48 }} />;
  if (!diag) return <div style={{ padding: 24 }}>{t("common.noData")}</div>;

  return (
    <div style={{ padding: 24 }}>
      {contextHolder}
      <Title level={4}>{t("diag.title")}</Title>
      <Card style={{ marginBottom: 16 }}>
        <Descriptions column={2}>
          <Descriptions.Item label={t("common.status")}>
            <Tag color={STATUS_COLOR[diag.overallStatus ?? ""] ?? "default"}>{diag.overallStatus}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label={t("common.info")}>{diag.message ?? "-"}</Descriptions.Item>
          <Descriptions.Item label={t("common.time")}>
            {diag.generatedAt ? new Date(diag.generatedAt).toLocaleString() : "-"}
          </Descriptions.Item>
        </Descriptions>
      </Card>
      {diag.aiRelay && (
        <Card title={t("diag.aiRelay")} style={{ marginBottom: 16 }}>
          <Descriptions column={2} size="small">
            <Descriptions.Item label={t("common.status")}><Tag color={STATUS_COLOR[diag.aiRelay.status ?? ""]}>{diag.aiRelay.status}</Tag></Descriptions.Item>
            <Descriptions.Item label={t("common.enabled")}>{diag.aiRelay.enabled ? t("common.yes") : t("common.no")}</Descriptions.Item>
            <Descriptions.Item label="Base URL">{diag.aiRelay.baseUrl ?? "-"}</Descriptions.Item>
            <Descriptions.Item label={t("cost.model")}>{diag.aiRelay.model ?? "-"}</Descriptions.Item>
            <Descriptions.Item label="API Key">{diag.aiRelay.hasApiKey ? t("common.yes") : t("common.no")}</Descriptions.Item>
            <Descriptions.Item label={t("common.info")}>{diag.aiRelay.message ?? "-"}</Descriptions.Item>
          </Descriptions>
        </Card>
      )}
      {diag.tools && diag.tools.length > 0 && (
        <Card title={t("diag.tools")} style={{ marginBottom: 16 }}>
          <Table
            rowKey="code"
            dataSource={diag.tools}
            pagination={false}
            size="small"
            columns={[
              { title: "Code", dataIndex: "code", width: 140 },
              { title: t("common.name"), dataIndex: "name", width: 160 },
              { title: t("template.category"), dataIndex: "category", width: 180 },
              { title: t("common.status"), dataIndex: "status", width: 130, render: (s: string) => <Tag color={STATUS_COLOR[s]}>{s}</Tag> },
              { title: t("common.enabled"), dataIndex: "enabled", width: 80, render: (v: boolean) => v ? t("common.yes") : t("common.no") },
              { title: t("common.info"), dataIndex: "message", ellipsis: true }
            ]}
          />
        </Card>
      )}
      {diag.export && (
        <Card title={t("diag.export")} style={{ marginBottom: 16 }}>
          <Descriptions column={1} size="small">
            <Descriptions.Item label="支持格式">{diag.export.supportedFormats?.join(", ") ?? "-"}</Descriptions.Item>
            <Descriptions.Item label="未实现">{diag.export.unimplementedFormats?.join(", ") ?? "-"}</Descriptions.Item>
            <Descriptions.Item label={t("common.status")}><Tag color={STATUS_COLOR[diag.export.status ?? ""]}>{diag.export.status}</Tag></Descriptions.Item>
          </Descriptions>
        </Card>
      )}
    </div>
  );
}