import { useEffect, useState } from "react";
import { Card, Col, Row, Statistic, Table, Tag, Typography, message } from "antd";
import { api } from "../../services/api";
import { useLang } from "../../i18n";
import type { CostConfig, CostStats, TaskCostRecord } from "../../services/types";

const { Title } = Typography;

export default function CostManagementPage() {
  const { t } = useLang();
  const [stats, setStats] = useState<CostStats | null>(null);
  const [configs, setConfigs] = useState<CostConfig[]>([]);
  const [records, setRecords] = useState<TaskCostRecord[]>([]);
  const [recordsTotal, setRecordsTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [msg, contextHolder] = message.useMessage();

  const fetchAll = async () => {
    setLoading(true);
    try {
      const [s, c, r] = await Promise.all([
        api.costStats.overall(),
        api.costConfigs.list({ pageNum: 1, pageSize: 100 }),
        api.costStats.records({ pageNum, pageSize: 20 })
      ]);
      setStats(s);
      const cData = c as unknown as { data?: CostConfig[] };
      setConfigs(cData.data ?? []);
      const rData = r as unknown as { data?: TaskCostRecord[]; total?: number };
      setRecords(rData.data ?? []);
      setRecordsTotal(rData.total ?? 0);
    } catch {
      msg.error(t("cost.loadFailed"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAll(); }, [pageNum]);

  const configColumns = [
    { title: "ID", dataIndex: "id", key: "id", width: 60 },
    { title: t("cost.service"), dataIndex: "providerType", key: "providerType" },
    { title: t("cost.service") + " Code", dataIndex: "providerCode", key: "providerCode" },
    { title: t("cost.price"), dataIndex: "unitPrice", key: "unitPrice", render: (v: number) => v != null ? `${v}` : "-" },
    { title: t("cost.unit"), dataIndex: "unitType", key: "unitType" },
    { title: "货币", dataIndex: "currency", key: "currency", width: 80 },
    { title: "描述", dataIndex: "description", key: "description", ellipsis: true }
  ];

  const recordColumns = [
    { title: "ID", dataIndex: "id", key: "id", width: 60 },
    { title: "任务类型", dataIndex: "taskType", key: "taskType" },
    { title: "任务 ID", dataIndex: "taskId", key: "taskId", width: 80 },
    { title: t("cost.service"), dataIndex: "toolCode", key: "toolCode" },
    { title: t("cost.model"), dataIndex: "modelCode", key: "modelCode" },
    { title: t("cost.amount"), dataIndex: "costAmount", key: "costAmount", render: (v: number) => v != null ? `${v}` : "-" },
    { title: "货币", dataIndex: "costCurrency", key: "costCurrency", width: 80 },
    { title: t("common.status"), dataIndex: "status", key: "status", width: 100, render: (s: string) => <Tag>{s}</Tag> },
    { title: t("cost.records.time"), dataIndex: "createTime", key: "createTime", width: 170 }
  ];

  return (
    <div style={{ padding: 24 }}>
      {contextHolder}
      <Title level={4}>{t("cost.title")}</Title>
      {stats && (
        <Row gutter={16} style={{ marginBottom: 24 }}>
          <Col span={4}><Card><Statistic title="总任务数" value={stats.totalJobs ?? 0} /></Card></Col>
          <Col span={4}><Card><Statistic title="成功" value={stats.succeededJobs ?? 0} valueStyle={{ color: "#3f8600" }} /></Card></Col>
          <Col span={4}><Card><Statistic title="失败" value={stats.failedJobs ?? 0} valueStyle={{ color: "#cf1322" }} /></Card></Col>
          <Col span={4}><Card><Statistic title="已取消" value={stats.canceledJobs ?? 0} /></Card></Col>
          <Col span={4}><Card><Statistic title={t("cost.totalCost")} value={stats.totalCost ?? 0} precision={2} suffix={stats.costCurrency} /></Card></Col>
          <Col span={4}><Card><Statistic title="平均成本/任务" value={stats.avgCostPerJob ?? 0} precision={4} suffix={stats.costCurrency} /></Card></Col>
        </Row>
      )}
      <Title level={5}>{t("cost.config")}</Title>
      <Table rowKey="id" columns={configColumns} dataSource={configs} loading={loading} pagination={false} size="small" style={{ marginBottom: 24 }} />
      <Title level={5}>{t("cost.records")}</Title>
      <Table rowKey="id" columns={recordColumns} dataSource={records} loading={loading}
        pagination={{ current: pageNum, pageSize: 20, total: recordsTotal, onChange: setPageNum }}
        size="small" />
    </div>
  );
}