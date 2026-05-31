import { Button, Card, Descriptions, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  RadarChartOutlined, ReloadOutlined, PlusOutlined,
  BarChartOutlined, FileTextOutlined
} from "@ant-design/icons";
import { EmptyState, ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";
import type { ResearchTask } from "../../services/types";

const { Text } = Typography;

function formatDateTime(value?: string) {
  if (!value) return "--";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString("zh-CN", { hour12: false });
}

function getStatusColor(status?: string): string {
  const normalized = (status ?? "").toUpperCase();
  switch (normalized) {
    case "COMPLETED":
    case "DONE":
      return "green";
    case "RUNNING":
    case "IN_PROGRESS":
      return "blue";
    case "PENDING":
      return "orange";
    case "FAILED":
      return "red";
    default:
      return "gray";
  }
}

export default function ResearchCenterPage() {
  const [tasks, setTasks] = useState<ResearchTask[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadTasks = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const page = await api.research.list({ pageNum: 1, pageSize: 20 });
      setTasks(page.data ?? []);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "加载调研任务失败");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadTasks();
  }, [loadTasks]);

  const metrics = useMemo(() => {
    const total = tasks.length;
    const completed = tasks.filter((t) => ["COMPLETED", "DONE"].includes((t.status ?? "").toUpperCase())).length;
    const running = tasks.filter((t) => ["RUNNING", "IN_PROGRESS"].includes((t.status ?? "").toUpperCase())).length;
    const pending = tasks.filter((t) => (t.status ?? "").toUpperCase() === "PENDING").length;
    return { total, completed, running, pending };
  }, [tasks]);

  const columns: ColumnsType<ResearchTask> = [
    { title: "ID", dataIndex: "id", width: 70 },
    { title: "任务名称", dataIndex: "taskName", ellipsis: true },
    { title: "类目", dataIndex: "category", width: 120 },
    { title: "负责人", dataIndex: "owner", width: 100 },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      render: (status: string) => (
        <Tag color={getStatusColor(status)}>{status || "PENDING"}</Tag>
      )
    },
    { title: "创建时间", dataIndex: "createTime", width: 160, render: formatDateTime },
    {
      title: "操作",
      key: "action",
      width: 100,
      render: (_, record) => (
        <Link to={`/research/tasks/${record.id}`}>
          <Button type="link" size="small">详情</Button>
        </Link>
      )
    }
  ];

  if (error) {
    return <ErrorState title="加载失败" description={error} onRetry={loadTasks} />;
  }

  return (
    <div>
      {/* 页面标题 */}
      <div className="df-page-header">
        <h1 className="df-page-title">市场调研中心</h1>
        <p className="df-page-desc">管理市场调研任务，分析竞品和用户需求</p>
      </div>

      {/* 指标卡片 */}
      <div className="df-grid-4" style={{ marginBottom: "var(--df-space-6)" }}>
        <div className="df-metric-card">
          <div className="df-metric-icon blue"><RadarChartOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">总任务数</div>
            <div className="df-metric-value">{metrics.total}</div>
          </div>
        </div>
        <div className="df-metric-card">
          <div className="df-metric-icon green"><FileTextOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">已完成</div>
            <div className="df-metric-value">{metrics.completed}</div>
          </div>
        </div>
        <div className="df-metric-card">
          <div className="df-metric-icon blue"><BarChartOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">进行中</div>
            <div className="df-metric-value">{metrics.running}</div>
          </div>
        </div>
        <div className="df-metric-card">
          <div className="df-metric-icon orange"><RadarChartOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">待处理</div>
            <div className="df-metric-value">{metrics.pending}</div>
          </div>
        </div>
      </div>

      {/* 任务列表 */}
      <Card>
        <div className="df-section-header">
          <span className="df-section-title">调研任务列表</span>
          <Space>
            <Link to="/research/new">
              <Button type="primary" icon={<PlusOutlined />}>
                新建调研任务
              </Button>
            </Link>
            <Button icon={<ReloadOutlined />} onClick={loadTasks} loading={loading}>
              刷新
            </Button>
          </Space>
        </div>
        <Table
          dataSource={tasks}
          columns={columns}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: <EmptyState title="暂无调研任务" description="创建第一个市场调研任务" compact /> }}
        />
      </Card>
    </div>
  );
}