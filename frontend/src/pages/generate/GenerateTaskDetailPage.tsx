import { Button, Card, Descriptions, Space, Tag, Typography } from "antd";
import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { ArrowLeftOutlined } from "@ant-design/icons";
import { ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";
import type { ImageJob } from "../../services/types";

const { Text } = Typography;

function normalizeStatus(status?: string) {
  return (status ?? "").trim().toUpperCase();
}

function getStatusMeta(status?: string): { label: string; color: string } {
  const normalized = normalizeStatus(status);
  switch (normalized) {
    case "PENDING":
    case "QUEUED":
      return { label: "等待中", color: "orange" };
    case "RUNNING":
    case "PROCESSING":
      return { label: "执行中", color: "blue" };
    case "SUCCEEDED":
    case "SUCCESS":
      return { label: "已完成", color: "green" };
    case "FAILED":
    case "ERROR":
      return { label: "失败", color: "red" };
    case "CANCELED":
    case "CANCELLED":
      return { label: "已取消", color: "gray" };
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

export default function GenerateTaskDetailPage() {
  const { taskId } = useParams<{ taskId: string }>();

  const jobQuery = useQuery({
    queryKey: ["image-job", taskId],
    queryFn: () => api.imageJobs.get(taskId ?? ""),
    enabled: Boolean(taskId)
  });

  if (jobQuery.isPending) {
    return <LoadingState title="加载中" description="正在加载任务详情" />;
  }

  if (jobQuery.isError) {
    return <ErrorState title="加载失败" description={jobQuery.error?.message} onRetry={() => void jobQuery.refetch()} />;
  }

  const job = jobQuery.data as ImageJob | undefined;
  if (!job) {
    return <ErrorState title="任务不存在" description="未找到该任务" />;
  }

  const statusMeta = getStatusMeta(job.status);

  return (
    <div>
      {/* 页面标题 */}
      <div className="df-page-header">
        <Space>
          <Link to="/generate">
            <Button type="text" icon={<ArrowLeftOutlined />}>返回</Button>
          </Link>
          <div>
            <h1 className="df-page-title">任务详情</h1>
            <p className="df-page-desc">任务 ID: {String(job.id ?? "")}</p>
          </div>
        </Space>
      </div>

      {/* 基本信息 */}
      <Card style={{ marginBottom: "var(--df-space-6)" }}>
        <div className="df-section-header">
          <span className="df-section-title">基本信息</span>
          <Tag color={statusMeta.color}>{statusMeta.label}</Tag>
        </div>
        <Descriptions column={2}>
          <Descriptions.Item label="任务名称">{job.taskName || "--"}</Descriptions.Item>
          <Descriptions.Item label="工具编码">{job.toolCode || "--"}</Descriptions.Item>
          <Descriptions.Item label="进度">{job.progress != null ? `${job.progress}%` : "--"}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{formatDateTime(job.createTime)}</Descriptions.Item>
          <Descriptions.Item label="更新时间">{formatDateTime(job.updateTime)}</Descriptions.Item>
          <Descriptions.Item label="完成时间">{formatDateTime(job.updateTime)}</Descriptions.Item>
        </Descriptions>
      </Card>

      {/* 输入数据 */}
      {job.inputData != null && (
        <Card style={{ marginBottom: "var(--df-space-6)" }}>
          <div className="df-section-header">
            <span className="df-section-title">输入数据</span>
          </div>
          <pre style={{
            background: "var(--df-bg)",
            padding: "var(--df-space-4)",
            borderRadius: "var(--df-radius-md)",
            overflow: "auto",
            fontSize: "var(--df-text-sm)",
            maxHeight: 300
          }}>
            {String(typeof job.inputData === "string" ? job.inputData : JSON.stringify(job.inputData, null, 2))}
          </pre>
        </Card>
      )}



      {/* 错误信息 */}
      {job.errorMessage && (
        <Card>
          <div className="df-section-header">
            <span className="df-section-title" style={{ color: "var(--df-danger)" }}>错误信息</span>
          </div>
          <Text type="danger">{job.errorMessage}</Text>
        </Card>
      )}
    </div>
  );
}