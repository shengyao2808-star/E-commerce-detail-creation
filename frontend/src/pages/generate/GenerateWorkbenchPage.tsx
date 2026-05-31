import { Button, Card, Form, Input, Space, Table, Tag, Typography, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  FileImageOutlined, ReloadOutlined, PlusOutlined,
  ClockCircleOutlined, CheckCircleOutlined, CloseCircleOutlined
} from "@ant-design/icons";
import { EmptyState, ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";
import type { ImageJob, ImageJobCreateRequest } from "../../services/types";

const { TextArea } = Input;
const { Text, Paragraph } = Typography;
const JOB_REFRESH_INTERVAL_MS = 30_000;

function normalizeStatus(status?: string) {
  return (status ?? "").trim().toUpperCase();
}

function isTerminalStatus(status?: string) {
  const terminal = new Set(["SUCCEEDED", "FAILED", "CANCELED", "CANCELLED"]);
  return terminal.has(normalizeStatus(status));
}

function getStatusMeta(status?: string): { label: string; color: string; icon: React.ReactNode } {
  const normalized = normalizeStatus(status);
  switch (normalized) {
    case "PENDING":
    case "QUEUED":
      return { label: "等待中", color: "orange", icon: <ClockCircleOutlined /> };
    case "RUNNING":
    case "PROCESSING":
      return { label: "执行中", color: "blue", icon: <ReloadOutlined spin /> };
    case "SUCCEEDED":
    case "SUCCESS":
      return { label: "已完成", color: "green", icon: <CheckCircleOutlined /> };
    case "FAILED":
    case "ERROR":
      return { label: "失败", color: "red", icon: <CloseCircleOutlined /> };
    case "CANCELED":
    case "CANCELLED":
      return { label: "已取消", color: "gray", icon: <CloseCircleOutlined /> };
    default:
      return { label: normalized || "未知", color: "gray", icon: null };
  }
}

function formatDateTime(value?: string) {
  if (!value) return "--";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString("zh-CN", { hour12: false });
}

export default function GenerateWorkbenchPage() {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [creating, setCreating] = useState(false);

  const jobsQuery = useQuery({
    queryKey: ["image-jobs"],
    queryFn: () => api.imageJobs.list({ pageNum: 1, pageSize: 20 }),
    refetchInterval: (query) => {
      const data = query.state.data as { data?: ImageJob[] } | undefined;
      const hasRunning = data?.data?.some((j) => !isTerminalStatus(j.status));
      return hasRunning ? JOB_REFRESH_INTERVAL_MS : false;
    }
  });

  const createMutation = useMutation({
    mutationFn: async (values: { taskName: string; toolCode: string; inputData?: string }) => {
      let parsedInput: Record<string, unknown> = {};
      if (values.inputData?.trim()) {
        try {
          parsedInput = JSON.parse(values.inputData);
        } catch {
          throw new Error("输入数据 JSON 格式错误");
        }
      }
      const payload: ImageJobCreateRequest = {
        taskName: values.taskName.trim(),
        toolCode: values.toolCode.trim(),
        inputData: JSON.stringify(parsedInput)
      };
      return api.imageJobs.create(payload);
    },
    onSuccess: async () => {
      message.success("任务已创建");
      form.resetFields();
      setCreating(false);
      await queryClient.invalidateQueries({ queryKey: ["image-jobs"] });
    },
    onError: (err) => {
      message.error(err instanceof Error ? err.message : "创建失败");
      setCreating(false);
    }
  });

  const jobs = useMemo(() => jobsQuery.data?.data ?? [], [jobsQuery.data]);

  const metrics = useMemo(() => {
    const running = jobs.filter((j) => !isTerminalStatus(j.status)).length;
    const today = jobs.filter((j) => {
      if (!j.createTime) return false;
      const d = new Date(j.createTime);
      const now = new Date();
      return d.toDateString() === now.toDateString();
    }).length;
    const succeeded = jobs.filter((j) => normalizeStatus(j.status) === "SUCCEEDED").length;
    return { running, today, succeeded, total: jobs.length };
  }, [jobs]);

  const columns: ColumnsType<ImageJob> = [
    { title: "ID", dataIndex: "id", width: 70 },
    { title: "任务名称", dataIndex: "taskName", ellipsis: true },
    { title: "工具", dataIndex: "toolCode", width: 120 },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      render: (status: string) => {
        const meta = getStatusMeta(status);
        return <Tag color={meta.color} icon={meta.icon}>{meta.label}</Tag>;
      }
    },
    { title: "进度", dataIndex: "progress", width: 80, render: (v: number) => v != null ? `${v}%` : "--" },
    { title: "创建时间", dataIndex: "createTime", width: 160, render: formatDateTime },
    {
      title: "操作",
      key: "action",
      width: 100,
      render: (_, record) => (
        <Link to={`/generate/${record.id}`}>
          <Button type="link" size="small">详情</Button>
        </Link>
      )
    }
  ];

  if (jobsQuery.isError) {
    return <ErrorState title="任务列表加载失败" description={jobsQuery.error?.message} onRetry={() => void jobsQuery.refetch()} />;
  }

  return (
    <div>
      {/* 页面标题 */}
      <div className="df-page-header">
        <h1 className="df-page-title">AI 生图工作台</h1>
        <p className="df-page-desc">创建和管理 AI 图像生成任务</p>
      </div>

      {/* 指标卡片 */}
      <div className="df-grid-4" style={{ marginBottom: "var(--df-space-6)" }}>
        <div className="df-metric-card">
          <div className="df-metric-icon blue"><FileImageOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">运行中任务</div>
            <div className="df-metric-value">{metrics.running}</div>
          </div>
        </div>
        <div className="df-metric-card">
          <div className="df-metric-icon green"><CheckCircleOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">今日生成</div>
            <div className="df-metric-value">{metrics.today}</div>
          </div>
        </div>
        <div className="df-metric-card">
          <div className="df-metric-icon purple"><FileImageOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">已完成</div>
            <div className="df-metric-value">{metrics.succeeded}</div>
          </div>
        </div>
        <div className="df-metric-card">
          <div className="df-metric-icon orange"><ClockCircleOutlined /></div>
          <div className="df-metric-body">
            <div className="df-metric-label">总任务数</div>
            <div className="df-metric-value">{metrics.total}</div>
          </div>
        </div>
      </div>

      {/* 创建任务表单 */}
      <Card style={{ marginBottom: "var(--df-space-6)" }}>
        <div className="df-section-header">
          <span className="df-section-title">创建新任务</span>
        </div>
        <Form
          form={form}
          layout="inline"
          onFinish={(values) => {
            setCreating(true);
            void createMutation.mutateAsync(values);
          }}
          style={{ flexWrap: "wrap", gap: "var(--df-space-3)" }}
        >
          <Form.Item name="taskName" rules={[{ required: true, message: "请输入任务名称" }]}>
            <Input placeholder="任务名称" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item name="toolCode" rules={[{ required: true, message: "请输入工具编码" }]}>
            <Input placeholder="工具编码 (如 dall-e-3)" style={{ width: 180 }} />
          </Form.Item>
          <Form.Item name="inputData">
            <Input placeholder="输入数据 (JSON，可选)" style={{ width: 300 }} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={creating} icon={<PlusOutlined />}>
              创建任务
            </Button>
          </Form.Item>
        </Form>
      </Card>

      {/* 任务列表 */}
      <Card>
        <div className="df-section-header">
          <span className="df-section-title">任务列表</span>
          <Button icon={<ReloadOutlined />} onClick={() => void jobsQuery.refetch()} loading={jobsQuery.isFetching}>
            刷新
          </Button>
        </div>
        <Table
          dataSource={jobs}
          columns={columns}
          rowKey="id"
          loading={jobsQuery.isPending}
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: <EmptyState title="暂无任务" description="创建第一个 AI 生图任务" compact /> }}
        />
      </Card>
    </div>
  );
}