import { Button, Card, Empty, Space, Table, Tag, Typography } from "antd";
import { useQuery } from "@tanstack/react-query";
import { useMemo } from "react";
import { Link } from "react-router-dom";
import {
  FileImageOutlined, CheckCircleOutlined, ClockCircleOutlined,
  EyeOutlined, DownloadOutlined
} from "@ant-design/icons";
import { ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";

const { Text } = Typography;

interface GenerationResult {
  id: number;
  taskName: string;
  status: string;
  resultUrl?: string;
  thumbnailUrl?: string;
  createTime: string;
}

function normalizeStatus(status?: string) {
  return (status ?? "").trim().toUpperCase();
}

function getStatusMeta(status?: string): { label: string; color: string } {
  const normalized = normalizeStatus(status);
  switch (normalized) {
    case "SUCCEEDED":
    case "SUCCESS":
      return { label: "已完成", color: "green" };
    case "PENDING":
    case "QUEUED":
      return { label: "等待中", color: "orange" };
    case "RUNNING":
    case "PROCESSING":
      return { label: "生成中", color: "blue" };
    case "FAILED":
    case "ERROR":
      return { label: "失败", color: "red" };
    default:
      return { label: normalized || "未知", color: "gray" };
  }
}

function formatDateTime(value?: string) {
  if (!value) return "--";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString("zh-CN", { hour12: false });
}

export default function ResultsPreviewPage() {
  const resultsQuery = useQuery({
    queryKey: ["generation-results"],
    queryFn: () => api.generationResults.list({ pageNum: 1, pageSize: 50 })
  });

  const results = useMemo(() => {
    const data = resultsQuery.data as { data?: GenerationResult[] } | undefined;
    return data?.data ?? [];
  }, [resultsQuery.data]);

  const metrics = useMemo(() => {
    const total = results.length;
    const succeeded = results.filter((r) => normalizeStatus(r.status) === "SUCCEEDED").length;
    const pending = results.filter((r) => ["PENDING", "QUEUED", "RUNNING"].includes(normalizeStatus(r.status))).length;
    return { total, succeeded, pending };
  }, [results]);

  const columns = [
    { title: "ID", dataIndex: "id", width: 70 },
    { title: "任务名称", dataIndex: "taskName", ellipsis: true },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      render: (status: string) => {
        const meta = getStatusMeta(status);
        return <Tag color={meta.color}>{meta.label}</Tag>;
      }
    },
    {
      title: "预览",
      key: "preview",
      width: 80,
      render: (_: unknown, record: GenerationResult) =>
        record.thumbnailUrl ? (
          <img
            src={record.thumbnailUrl}
            alt={record.taskName}
            style={{ width: 48, height: 48, objectFit: "cover", borderRadius: 6 }}
          />
        ) : (
          <div style={{
            width: 48, height: 48, background: "var(--df-bg)",
            borderRadius: 6, display: "flex", alignItems: "center", justifyContent: "center"
          }}>
            <FileImageOutlined style={{ color: "var(--df-text-muted)" }} />
          </div>
        )
    },
    { title: "创建时间", dataIndex: "createTime", width: 160, render: formatDateTime },
    {
      title: "操作",
      key: "action",
      width: 120,
      render: (_: unknown, record: GenerationResult) => (
        <Space>
          {record.resultUrl && (
            <Button type="link" size="small" icon={<EyeOutlined />} href={record.resultUrl} target="_blank">
              查看
            </Button>
          )}
          {record.resultUrl && (
            <Button type="link" size="small" icon={<DownloadOutlined />} href={record.resultUrl} download>
              下载
            </Button>
          )}
        </Space>
      )
    }
  ];

  if (resultsQuery.isError) {
    return <ErrorState title="结果加载失败" description={resultsQuery.error?.message} onRetry={() => void resultsQuery.refetch()} />;
  }

  return (
    <div>
      {/* 页面标题 */}
      <div className="df-page-header">
        <h1 className="df-page-title">生成结果</h1>
        <p className="df-page-desc">查看 AI 生成的图片和内容结果</p>
      </div>

      {/* 指标卡片 */}
      <div className="df-grid-3" style={{ marginBottom: "var(--df-space-6)" }}>
        <div className="df-metric-card">
          <div className="df-metric-icon blue"><FileImageOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">总结果数</div>
            <div className="df-metric-value">{metrics.total}</div>
          </div>
        </div>
        <div className="df-metric-card">
          <div className="df-metric-icon green"><CheckCircleOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">已完成</div>
            <div className="df-metric-value">{metrics.succeeded}</div>
          </div>
        </div>
        <div className="df-metric-card">
          <div className="df-metric-icon orange"><ClockCircleOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">处理中</div>
            <div className="df-metric-value">{metrics.pending}</div>
          </div>
        </div>
      </div>

      {/* 结果列表 */}
      <Card>
        <div className="df-section-header">
          <span className="df-section-title">结果列表</span>
          <Button onClick={() => void resultsQuery.refetch()} loading={resultsQuery.isFetching}>
            刷新
          </Button>
        </div>
        <Table
          dataSource={results}
          columns={columns}
          rowKey="id"
          loading={resultsQuery.isPending}
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: <Empty description="暂无生成结果" /> }}
        />
      </Card>
    </div>
  );
}