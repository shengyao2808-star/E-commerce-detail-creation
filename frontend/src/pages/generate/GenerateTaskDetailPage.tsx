import { Alert, Button, Card, Descriptions, Empty, Progress, Space, Tag, Typography } from "antd";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { EmptyState, ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";
import type { ImageJob } from "../../services/types";
import { P0Scaffold } from "../p0/P0Scaffold";

const { Text, Paragraph } = Typography;
const JOB_REFRESH_INTERVAL_MS = 30_000;

type NoticeTone = "info" | "success" | "warning" | "error";

type Notice = {
  tone: NoticeTone;
  title: string;
  message: string;
};

const terminalStatuses = new Set(["SUCCEEDED", "FAILED", "CANCELED", "CANCELLED"]);

function normalizeStatus(status?: string) {
  return (status ?? "").trim().toUpperCase();
}

function isTerminalStatus(status?: string) {
  return terminalStatuses.has(normalizeStatus(status));
}

function getStatusMeta(status?: string): { label: string; color: string } {
  const normalized = normalizeStatus(status);

  switch (normalized) {
    case "PENDING":
    case "QUEUED":
      return { label: "等待中", color: "warning" };
    case "RUNNING":
    case "PROCESSING":
      return { label: "执行中", color: "processing" };
    case "SUCCEEDED":
    case "SUCCESS":
      return { label: "已完成", color: "success" };
    case "FAILED":
    case "ERROR":
      return { label: "失败", color: "error" };
    case "CANCELED":
    case "CANCELLED":
      return { label: "已取消", color: "default" };
    default:
      return { label: normalized || "未知", color: "default" };
  }
}

function formatDateTime(value?: string) {
  if (!value) {
    return "--";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("zh-CN", { hour12: false });
}

function formatProgress(value?: number) {
  if (value === undefined || value === null || Number.isNaN(value)) {
    return "--";
  }

  return `${value}%`;
}

function NoticeBanner({ notice }: { notice: Notice }) {
  return (
    <Alert
      showIcon
      type={notice.tone}
      message={notice.title}
      description={notice.message}
      style={{ marginBottom: 16 }}
    />
  );
}

export default function GenerateTaskDetailPage() {
  const { taskId = "" } = useParams<{ taskId: string }>();
  const queryClient = useQueryClient();
  const [busyAction, setBusyAction] = useState<"retry" | "cancel" | null>(null);
  const [notice, setNotice] = useState<Notice>({
    tone: "info",
    title: "任务详情轮询",
    message: "只读取 /api/v1/image-jobs/{id} 返回的真实状态、进度、外部任务 ID 和错误信息。"
  });

  const jobQuery = useQuery({
    queryKey: ["image-jobs", taskId],
    queryFn: async () => api.imageJobs.get(taskId),
    enabled: Boolean(taskId),
    refetchInterval: (query) => {
      const job = query.state.data as ImageJob | undefined;
      return job && !isTerminalStatus(job.status) ? JOB_REFRESH_INTERVAL_MS : false;
    }
  });

  const retryMutation = useMutation({
    mutationFn: async () => api.imageJobs.retry(taskId),
    onSuccess: async () => {
      setNotice({
        tone: "success",
        title: "任务已重试",
        message: `#${taskId} 已提交重试请求`
      });
      await queryClient.invalidateQueries({ queryKey: ["image-jobs"] });
      await jobQuery.refetch();
    },
    onError: (error) => {
      setNotice({
        tone: "error",
        title: "重试失败",
        message: error instanceof Error ? error.message : "重试请求失败"
      });
    }
  });

  const cancelMutation = useMutation({
    mutationFn: async () => api.imageJobs.cancel(taskId),
    onSuccess: async () => {
      setNotice({
        tone: "success",
        title: "任务已取消",
        message: `#${taskId} 已提交取消请求`
      });
      await queryClient.invalidateQueries({ queryKey: ["image-jobs"] });
      await jobQuery.refetch();
    },
    onError: (error) => {
      setNotice({
        tone: "error",
        title: "取消失败",
        message: error instanceof Error ? error.message : "取消请求失败"
      });
    }
  });

  const job = jobQuery.data;
  const statusMeta = useMemo(() => getStatusMeta(job?.status), [job?.status]);
  const progressValue = typeof job?.progress === "number" ? Math.max(0, Math.min(100, job.progress)) : undefined;
  const isBusy = busyAction !== null || retryMutation.isPending || cancelMutation.isPending;

  const handleRetry = async () => {
    if (!taskId) {
      return;
    }

    setBusyAction("retry");
    try {
      await retryMutation.mutateAsync();
    } finally {
      setBusyAction(null);
    }
  };

  const handleCancel = async () => {
    if (!taskId) {
      return;
    }

    setBusyAction("cancel");
    try {
      await cancelMutation.mutateAsync();
    } finally {
      setBusyAction(null);
    }
  };

  return (
    <P0Scaffold
      eyebrow="生成"
      title={`生图任务详情${taskId ? ` #${taskId}` : ""}`}
      description="这里只展示 image-job 的持久化详情；轮询只看后端真实状态，不补充虚构进度、结果图或合规结论。"
      actions={[
        { label: "返回工作台", to: "/generate" },
        { label: "结果预览", to: "/results" }
      ]}
      flow={["任务记录", "状态轮询", "结果预览"]}
      apiNotice={false}
      toolNotice={false}
      capabilities={[
        { title: "真实轮询", description: "useQuery 的 refetchInterval 只对未终态任务生效。", status: "available" },
        { title: "真实重试", description: "retry 请求直接回传后端消息。", status: "available" },
        { title: "真实取消", description: "cancel 请求直接回传后端消息。", status: "available" }
      ]}
    >
      <NoticeBanner notice={notice} />

      {!taskId ? (
        <EmptyState title="缺少任务 ID" description="路由参数为空，无法调用 GET /api/v1/image-jobs/{id}。" />
      ) : jobQuery.isError ? (
        <ErrorState
          title="任务详情加载失败"
          description={jobQuery.error instanceof Error ? jobQuery.error.message : "无法读取 image-job 详情"}
          onRetry={() => void jobQuery.refetch()}
        />
      ) : jobQuery.isPending ? (
        <LoadingState title="正在加载任务详情" description={`GET /api/v1/image-jobs/${taskId}`} />
      ) : job ? (
        <Space direction="vertical" size={16} style={{ width: "100%" }}>
          <Card
            className="p0-card"
            title="任务信息"
            extra={
              <Space wrap>
                <Button onClick={() => void jobQuery.refetch()} loading={jobQuery.isFetching}>
                  刷新
                </Button>
                <Button type="primary" disabled={isBusy || !taskId} onClick={() => void handleRetry()}>
                  重试
                </Button>
                <Button danger disabled={isBusy || !taskId} onClick={() => void handleCancel()}>
                  取消
                </Button>
              </Space>
            }
          >
            <Descriptions bordered column={2}>
              <Descriptions.Item label="任务 ID">{job.id ? `#${job.id}` : "--"}</Descriptions.Item>
              <Descriptions.Item label="任务名称">{job.taskName || "--"}</Descriptions.Item>
              <Descriptions.Item label="工具编码">{job.toolCode || "--"}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusMeta.color}>{statusMeta.label}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="进度">{formatProgress(job.progress)}</Descriptions.Item>
              <Descriptions.Item label="外部任务 ID">{job.externalJobId || "--"}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{formatDateTime(job.createTime)}</Descriptions.Item>
              <Descriptions.Item label="更新时间">{formatDateTime(job.updateTime)}</Descriptions.Item>
            </Descriptions>
          </Card>

          <Card className="p0-card" title="进度与错误">
            <Space direction="vertical" size={12} style={{ width: "100%" }}>
              <Progress percent={progressValue} status={normalizeStatus(job.status) === "FAILED" ? "exception" : undefined} />
              {job.errorMessage ? (
                <Alert showIcon type="error" message="后端错误信息" description={job.errorMessage} />
              ) : (
                <Text type="secondary">后端未返回错误信息。</Text>
              )}
            </Space>
          </Card>

          <Card className="p0-card" title="原始字段">
            <Descriptions bordered column={1}>
              <Descriptions.Item label="任务状态原文">
                <Paragraph style={{ marginBottom: 0 }}>{job.status || "--"}</Paragraph>
              </Descriptions.Item>
            </Descriptions>
          </Card>
        </Space>
      ) : (
        <EmptyState title="暂无任务详情" description="后端没有返回该任务记录，列表也不会伪造任何内容。" />
      )}
    </P0Scaffold>
  );
}
