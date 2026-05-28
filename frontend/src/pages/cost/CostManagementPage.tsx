import { useEffect, useState } from "react";
import { Card, Col, Row, Statistic, Table, Tag, Typography, message } from "antd";
import { api } from "../../services/api";
import type { CostConfig, CostStats, TaskCostRecord } from "../../services/types";

const { Title } = Typography;

export default function CostManagementPage() {
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
      msg.error("Failed to load cost data");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAll(); }, [pageNum]);

  const configColumns = [
    { title: "ID", dataIndex: "id", key: "id", width: 60 },
    { title: "Provider Type", dataIndex: "providerType", key: "providerType" },
    { title: "Provider Code", dataIndex: "providerCode", key: "providerCode" },
    { title: "Unit Price", dataIndex: "unitPrice", key: "unitPrice", render: (v: number) => v != null ? `${v}` : "-" },
    { title: "Unit Type", dataIndex: "unitType", key: "unitType" },
    { title: "Currency", dataIndex: "currency", key: "currency", width: 80 },
    { title: "Description", dataIndex: "description", key: "description", ellipsis: true }
  ];

  const recordColumns = [
    { title: "ID", dataIndex: "id", key: "id", width: 60 },
    { title: "Task Type", dataIndex: "taskType", key: "taskType" },
    { title: "Task ID", dataIndex: "taskId", key: "taskId", width: 80 },
    { title: "Tool", dataIndex: "toolCode", key: "toolCode" },
    { title: "Model", dataIndex: "modelCode", key: "modelCode" },
    { title: "Cost", dataIndex: "costAmount", key: "costAmount", render: (v: number) => v != null ? `${v}` : "-" },
    { title: "Currency", dataIndex: "costCurrency", key: "costCurrency", width: 80 },
    { title: "Status", dataIndex: "status", key: "status", width: 100, render: (s: string) => <Tag>{s}</Tag> },
    { title: "Created", dataIndex: "createTime", key: "createTime", width: 170 }
  ];

  return (
    <div style={{ padding: 24 }}>
      {contextHolder}
      <Title level={4}>Cost Management</Title>
      {stats && (
        <Row gutter={16} style={{ marginBottom: 24 }}>
          <Col span={4}><Card><Statistic title="Total Jobs" value={stats.totalJobs ?? 0} /></Card></Col>
          <Col span={4}><Card><Statistic title="Succeeded" value={stats.succeededJobs ?? 0} valueStyle={{ color: "#3f8600" }} /></Card></Col>
          <Col span={4}><Card><Statistic title="Failed" value={stats.failedJobs ?? 0} valueStyle={{ color: "#cf1322" }} /></Card></Col>
          <Col span={4}><Card><Statistic title="Canceled" value={stats.canceledJobs ?? 0} /></Card></Col>
          <Col span={4}><Card><Statistic title="Total Cost" value={stats.totalCost ?? 0} precision={2} suffix={stats.costCurrency} /></Card></Col>
          <Col span={4}><Card><Statistic title="Avg Cost/Job" value={stats.avgCostPerJob ?? 0} precision={4} suffix={stats.costCurrency} /></Card></Col>
        </Row>
      )}
      <Title level={5}>Cost Configs</Title>
      <Table rowKey="id" columns={configColumns} dataSource={configs} loading={loading} pagination={false} size="small" style={{ marginBottom: 24 }} />
      <Title level={5}>Cost Records</Title>
      <Table rowKey="id" columns={recordColumns} dataSource={records} loading={loading}
        pagination={{ current: pageNum, pageSize: 20, total: recordsTotal, onChange: setPageNum }}
        size="small" />
    </div>
  );
}