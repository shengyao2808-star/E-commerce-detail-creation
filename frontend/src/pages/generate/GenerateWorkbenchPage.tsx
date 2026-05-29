import { Button, Card, Form, Input, Space, Table, Tag, Typography, Alert, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { EmptyState, ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";
import type { ImageJob, ImageJobCreateRequest } from "../../services/types";
import { P0Scaffold } from "../p0/P0Scaffold";

const { TextArea } = Input;
const { Text, Paragraph } = Typography;
const JOB_REFRESH_INTERVAL_MS = 30_000;

type JobFormValues = {
  taskName: string;
  toolCode: string;
  inputDataJson?: string;
};

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

function safeJsonStringify(value: unknown) {
  if (value === undefined) {
    return "--";
  }

  if (typeof value === "string") {
    return value;
  }

  try {
    return JSON.stringify(value);
  } catch {
    return String(value);
  }
}

function parseJsonPayload(raw: string): unknown {
  const trimmed = raw.trim();
  if (!trimmed) {
    return undefined;
  }

  try {
    return JSON.parse(trimmed);
  } catch {
    throw new Error("输入的 JSON 无法解析");
  }
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

export default function GenerateWorkbenchPage() {
  const [form] = Form.useForm<JobFormValues>();
  const queryClient = useQueryClient();
  const [notice, setNotice] = useState<Notice>({
    tone: "info",
    title: "真实生图任务工作台",
    message: "GET /api/v1/image-jobs/list 与 POST /api/v1/image-jobs 直接返回后端状态；空列表就保持为空。"
  });
  const [busyJobId, setBusyJobId] = useState<string | null>(null);
  const [visualPlanIdFilter, setVisualPlanIdFilter] = useState("");
  const normalizedVisualPlanIdFilter = visualPlanIdFilter.trim();

  const jobsQuery = useQuery({
    queryKey: ["image-jobs", { visualPlanId: normalizedVisualPlanIdFilter }],
    queryFn: async () =>
      api.imageJobs.list({
        pageNum: 1,
        pageSize: 20,
        visualPlanId: normalizedVisualPlanIdFilter || undefined
      }),
    staleTime: 15_000,
    refetchInterval: (query) => {
      const jobs = query.state.data?.data ?? [];
      return jobs.some((job) => !isTerminalStatus(job.status)) ? JOB_REFRESH_INTERVAL_MS : false;
    }
  });

  const createMutation = useMutation({
    mutationFn: async (values: JobFormValues) => {
      const taskName = values.taskName.trim();
      const toolCode = values.toolCode.trim();
      const inputDataJson = values.inputDataJson?.trim() ?? "";

      const payload: ImageJobCreateRequest = {
        taskName,
        toolCode
      };

      if (inputDataJson) {
        payload.inputJson = inputDataJson;
        payload.inputData = parseJsonPayload(inputDataJson);
      }

      return api.imageJobs.create(payload);
    },
    onSuccess: async (jobId) => {
      setNotice({
        tone: "success",
        title: "任务已创建",
        message: `后端返回任务 ID：#${jobId}`
      });
      form.resetFields();
      await queryClient.invalidateQueries({ queryKey: ["image-jobs"] });
    },
    onError: (error) => {
      setNotice({
        tone: "error",
        title: "创建失败",
        message: error instanceof Error ? error.message : "创建 image-job 时发生未知错误"
      });
    }
  });

  const retryMutation = useMutation({
    mutationFn: async (job: ImageJob) => api.imageJobs.retry(job.id ?? ""),
    onSuccess: async (_, job) => {
      setNotice({
        tone: "success",
        title: "任务已重试",
        message: `#${job.id ?? "--"} 已提交重试请求`
      });
      await queryClient.invalidateQueries({ queryKey: ["image-jobs"] });
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
    mutationFn: async (job: ImageJob) => api.imageJobs.cancel(job.id ?? ""),
    onSuccess: async (_, job) => {
      setNotice({
        tone: "success",
        title: "任务已取消",
        message: `#${job.id ?? "--"} 已提交取消请求`
      });
      await queryClient.invalidateQueries({ queryKey: ["image-jobs"] });
    },
    onError: (error) => {
      setNotice({
        tone: "error",
        title: "取消失败",
        message: error instanceof Error ? error.message : "取消请求失败"
      });
    }
  });

  const jobs = jobsQuery.data?.data ?? [];

  const columns = useMemo<ColumnsType<ImageJob>>(
    () => [
      {
        title: "任务 ID",
        dataIndex: "id",
        width: 100,
        render: (value: ImageJob["id"]) => (value ? `#${value}` : "--")
      },
      {
        title: "任务名称",
        dataIndex: "taskName",
        render: (value: string | undefined) => value || "--"
      },
      {
        title: "工具编码",
        dataIndex: "toolCode",
        width: 140,
        render: (value: string | undefined) => value || "--"
      },
      {
        title: "状态",
        dataIndex: "status",
        width: 120,
        render: (value: string | undefined) => {
          const meta = getStatusMeta(value);
          return <Tag color={meta.color}>{meta.label}</Tag>;
        }
      },
      {
        title: "进度",
        dataIndex: "progress",
        width: 100,
        render: (value: number | undefined) => formatProgress(value)
      },
      {
        title: "外部任务 ID",
        dataIndex: "externalJobId",
        width: 180,
        render: (value: string | undefined) => value || "--"
      },
      {
        title: "错误信息",
        dataIndex: "errorMessage",
        render: (value: string | undefined) =>
          value ? <Text type="danger">{value}</Text> : <Text type="secondary">--</Text>
      },
      {
        title: "创建时间",
        dataIndex: "createTime",
        width: 180,
        render: (value: string | undefined) => formatDateTime(value)
      },
      {
        title: "更新时间",
        dataIndex: "updateTime",
        width: 180,
        render: (value: string | undefined) => formatDateTime(value)
      },
      {
        title: "操作",
        key: "actions",
        width: 180,
        render: (_, record) => {
          const jobId = String(record.id ?? "");
          const busy = busyJobId === jobId || createMutation.isPending || retryMutation.isPending || cancelMutation.isPending;

          return (
            <Space size={0} wrap>
              <Link to={`/generate/${jobId}`}>详情</Link>
              <Button
                type="link"
                size="small"
                disabled={!record.id || busy}
                onClick={() => {
                  if (!record.id) {
                    return;
                  }
                  setBusyJobId(jobId);
                  void retryMutation.mutateAsync(record).finally(() => setBusyJobId(null));
                }}
              >
                重试
              </Button>
              <Button
                type="link"
                size="small"
                danger
                disabled={!record.id || busy}
                onClick={() => {
                  if (!record.id) {
                    return;
                  }
                  setBusyJobId(jobId);
                  void cancelMutation.mutateAsync(record).finally(() => setBusyJobId(null));
                }}
              >
                取消
              </Button>
            </Space>
          );
        }
      }
    ],
    [busyJobId, cancelMutation, createMutation.isPending, retryMutation, retryMutation.isPending, cancelMutation.isPending]
  );

  const onCreate = async (values: JobFormValues) => {
    try {
      await createMutation.mutateAsync(values);
    } catch {
      // handled by mutation state and banner
    }
  };

  return (
    <P0Scaffold
      eyebrow="生成"
      title="生图工作台"
      description="这里只读取和提交真实的 image-jobs 后端状态。没有列表时就显示空状态，后端报错就直接展示消息。"
      actions={[{ label: "结果预览", to: "/results" }]}
      flow={["提交任务", "任务列表", "状态轮询", "结果预览"]}
      apiNotice={false}
      toolNotice={false}
      capabilities={[
        { title: "真实列表", description: "GET /api/v1/image-jobs/list 直接读取持久化记录。", status: "available" },
        { title: "真实创建", description: "POST /api/v1/image-jobs 失败时直接显示后端消息。", status: "available" },
        { title: "重试与取消", description: "对已保存任务发起 retry / cancel 请求。", status: "available" }
      ]}
    >
      <NoticeBanner notice={notice} />

      <Card className="p0-card" title="创建任务">
        <Form form={form} layout="vertical" onFinish={(values) => void onCreate(values)}>
          <Form.Item label="任务名称" name="taskName" rules={[{ required: true, message: "请输入任务名称" }]}>
            <Input placeholder="请输入任务名称" maxLength={120} />
          </Form.Item>
          <Form.Item label="工具编码" name="toolCode" rules={[{ required: true, message: "请输入工具编码" }]}>
            <Input placeholder="请输入工具编码" maxLength={80} />
          </Form.Item>
          <Form.Item label="输入数据 JSON" name="inputDataJson">
            <TextArea rows={5} placeholder="可选，留空则不提交 inputData" />
          </Form.Item>
          {createMutation.isError ? (
            <Alert
              showIcon
              type="error"
              message="创建失败"
              description={createMutation.error instanceof Error ? createMutation.error.message : "创建 image-job 时发生错误"}
              style={{ marginBottom: 16 }}
            />
          ) : null}
          <Space wrap>
            <Button type="primary" htmlType="submit" loading={createMutation.isPending}>
              创建任务
            </Button>
            <Button onClick={() => form.resetFields()} disabled={createMutation.isPending}>
              重置
            </Button>
            <Button onClick={() => void jobsQuery.refetch()} loading={jobsQuery.isFetching}>
              刷新列表
            </Button>
          </Space>
        </Form>
      </Card>

      <Card className="p0-card" title="Visual Plan Filter">
        <Space wrap>
          <Input
            allowClear
            placeholder="visual plan ID"
            value={visualPlanIdFilter}
            onChange={(event) => setVisualPlanIdFilter(event.target.value)}
            style={{ width: 180 }}
          />
          <Button onClick={() => void jobsQuery.refetch()} loading={jobsQuery.isFetching}>
            Refresh
          </Button>
        </Space>
      </Card>

      <Card className="p0-card" title="任务列表">
        {jobsQuery.isError ? (
          <EmptyState
            title="暂无生图任务"
            description="后端未连接或暂无数据"
            action={
              <Button onClick={() => void jobsQuery.refetch()}>
                重试
              </Button>
            }
          />
        ) : jobsQuery.isPending ? (
          <LoadingState title="正在加载任务列表" description="GET /api/v1/image-jobs/list" />
        ) : jobs.length === 0 ? (
          <EmptyState
            title="暂无真实生图任务"
            description="后端返回空列表时，这里保持空状态。"
            action={
              <Button type="primary" onClick={() => void jobsQuery.refetch()}>
                刷新列表
              </Button>
            }
          />
        ) : (
          <Table
            rowKey={(record) => String(record.id ?? `${record.taskName ?? ""}-${record.createTime ?? ""}`)}
            columns={columns}
            dataSource={jobs}
            pagination={false}
            scroll={{ x: 1320 }}
          />
        )}
      </Card>
    </P0Scaffold>
  );
}
