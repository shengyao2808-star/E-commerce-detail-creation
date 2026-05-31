import { useEffect, useMemo, useState } from "react";
import {
  Alert,
  Button,
  Card,
  Col,
  Form,
  Input,
  InputNumber,
  Modal,
  Row,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message
} from "antd";
import { ReloadOutlined, StopOutlined, PlusOutlined } from "@ant-design/icons";
import { api } from "../../services/api";
import { useLang } from "../../i18n";
import type {
  PostProcessTask,
  PostProcessTaskCreateRequest,
  PostProcessTaskListQuery
} from "../../services/types";

const { Title } = Typography;

const STATUS_COLORS: Record<string, string> = {
  PENDING: "default",
  RUNNING: "processing",
  SUCCEEDED: "success",
  FAILED: "error",
  CANCELED: "warning"
};

const TOOL_OPTIONS = [
  { value: "real-esrgan", label: "Real-ESRGAN (超分)" },
  { value: "iopaint", label: "IOPaint (修复/清理)" },
  { value: "grounded-sam", label: "Grounded-SAM (分割)" },
  { value: "imagemagick", label: "ImageMagick (缩放/裁剪/转换)" }
];

const OPERATION_OPTIONS = [
  { value: "upscale", label: "超分" },
  { value: "inpaint", label: "修复" },
  { value: "cleanup-background", label: "清理背景" },
  { value: "remove-object", label: "移除对象" },
  { value: "segment", label: "分割" },
  { value: "crop", label: "裁剪" },
  { value: "resize", label: "缩放" },
  { value: "convert", label: "转换" },
  { value: "restore-face", label: "人脸修复" }
];

function formatBytes(bytes?: number): string {
  if (!bytes) return "-";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function PostProcessTasksPage() {
  const { t } = useLang();
  const [tasks, setTasks] = useState<PostProcessTask[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [statusFilter, setStatusFilter] = useState<string | undefined>();
  const [toolFilter, setToolFilter] = useState<string | undefined>();
  const [createOpen, setCreateOpen] = useState(false);
  const [creating, setCreating] = useState(false);
  const [form] = Form.useForm();
  const [messageApi, contextHolder] = message.useMessage();

  const query: PostProcessTaskListQuery = useMemo(
    () => ({ pageNum, pageSize, status: statusFilter, toolCode: toolFilter }),
    [pageNum, pageSize, statusFilter, toolFilter]
  );

  const fetchTasks = async () => {
    setLoading(true);
    try {
      const res = await api.postProcessTasks.list(query);
      const pageData = res as unknown as {
        data?: PostProcessTask[];
        pageNum?: number;
        total?: number;
      };
      setTasks(pageData.data ?? []);
      setTotal(pageData.total ?? 0);
    } catch (err) {
      messageApi.error(t("postprocess.loadFailed"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTasks();
  }, [query]);

  const handleRetry = async (id: number | string) => {
    try {
      await api.postProcessTasks.retry(id);
      messageApi.success(t("postprocess.retrySuccess"));
      fetchTasks();
    } catch {
      messageApi.error(t("postprocess.retryFailed"));
    }
  };

  const handleCancel = async (id: number | string) => {
    try {
      await api.postProcessTasks.cancel(id);
      messageApi.success(t("postprocess.cancelSuccess"));
      fetchTasks();
    } catch {
      messageApi.error(t("postprocess.cancelFailed"));
    }
  };

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      setCreating(true);
      const payload: PostProcessTaskCreateRequest = {
        toolCode: values.toolCode,
        operation: values.operation,
        sourceImagePath: values.sourceImagePath || undefined,
        sourceGenerationResultId: values.sourceGenerationResultId || undefined,
        targetWidth: values.targetWidth || undefined,
        targetHeight: values.targetHeight || undefined,
        outputRatio: values.outputRatio || undefined
      };
      await api.postProcessTasks.create(payload);
      messageApi.success(t("postprocess.createSuccess"));
      setCreateOpen(false);
      form.resetFields();
      fetchTasks();
    } catch {
      messageApi.error(t("postprocess.createFailed"));
    } finally {
      setCreating(false);
    }
  };

  const columns = [
    {
      title: t("postprocess.taskId"),
      dataIndex: "id",
      key: "id",
      width: 80
    },
    {
      title: t("postprocess.tool"),
      dataIndex: "toolCode",
      key: "toolCode",
      width: 140
    },
    {
      title: t("postprocess.operation"),
      dataIndex: "operation",
      key: "operation",
      width: 160
    },
    {
      title: t("postprocess.status"),
      dataIndex: "status",
      key: "status",
      width: 110,
      render: (status: string) => (
        <Tag color={STATUS_COLORS[status] ?? "default"}>{status}</Tag>
      )
    },
    {
      title: "Progress",
      dataIndex: "progress",
      key: "progress",
      width: 80,
      render: (v: number) => `${v ?? 0}%`
    },
    {
      title: t("postprocess.input"),
      dataIndex: "sourceImagePath",
      key: "sourceImagePath",
      ellipsis: true
    },
    {
      title: t("postprocess.output"),
      dataIndex: "outputPath",
      key: "outputPath",
      ellipsis: true
    },
    {
      title: t("postprocess.size"),
      dataIndex: "outputFileSize",
      key: "outputFileSize",
      width: 100,
      render: (v: number) => formatBytes(v)
    },
    {
      title: t("postprocess.time"),
      dataIndex: "durationMs",
      key: "durationMs",
      width: 100,
      render: (v: number) => (v ? `${(v / 1000).toFixed(1)}s` : "-")
    },
    {
      title: t("postprocess.actions"),
      key: "actions",
      width: 120,
      render: (_: unknown, record: PostProcessTask) => {
        const terminal =
          record.status === "SUCCEEDED" ||
          record.status === "FAILED" ||
          record.status === "CANCELED";
        const canRetry =
          record.status === "FAILED" || record.status === "CANCELED";
        const canCancel =
          record.status === "PENDING" || record.status === "RUNNING";
        return (
          <Space size="small">
            {canRetry && (
              <Button
                size="small"
                icon={<ReloadOutlined />}
                onClick={() => handleRetry(record.id!)}
              >
                {t("postprocess.retry")}
              </Button>
            )}
            {canCancel && (
              <Button
                size="small"
                danger
                icon={<StopOutlined />}
                onClick={() => handleCancel(record.id!)}
              >
                {t("postprocess.cancel")}
              </Button>
            )}
            {terminal && !canRetry && <Tag>{t("status.done")}</Tag>}
          </Space>
        );
      }
    }
  ];

  return (
    <div style={{ padding: 24 }}>
      {contextHolder}
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col>
          <Title level={4} style={{ margin: 0 }}>
            {t("postprocess.title")}
          </Title>
        </Col>
        <Col>
          <Space>
            <Button icon={<ReloadOutlined />} onClick={fetchTasks}>
              {t("postprocess.refresh")}
            </Button>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setCreateOpen(true)}
            >
              {t("postprocess.create")}
            </Button>
          </Space>
        </Col>
      </Row>

      <Card size="small" style={{ marginBottom: 16 }}>
        <Space wrap>
          <Select
            allowClear
            placeholder={t("postprocess.filter.status")}
            style={{ width: 160 }}
            value={statusFilter}
            onChange={setStatusFilter}
            options={[
              { value: "PENDING", label: t("status.pending") },
              { value: "RUNNING", label: t("status.running") },
              { value: "SUCCEEDED", label: t("status.done") },
              { value: "FAILED", label: t("common.failed") },
              { value: "CANCELED", label: t("common.canceled") }
            ]}
          />
          <Select
            allowClear
            placeholder={t("postprocess.filter.tool")}
            style={{ width: 200 }}
            value={toolFilter}
            onChange={setToolFilter}
            options={TOOL_OPTIONS}
          />
        </Space>
      </Card>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={tasks}
        loading={loading}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          onChange: (p, ps) => {
            setPageNum(p);
            setPageSize(ps);
          }
        }}
        scroll={{ x: 1200 }}
        size="small"
      />

      <Modal
        title={t("postprocess.createTitle")}
        open={createOpen}
        onCancel={() => setCreateOpen(false)}
        onOk={handleCreate}
        confirmLoading={creating}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="toolCode"
            label={t("postprocess.form.tool")}
            rules={[{ required: true, message: t("common.required") }]}
          >
            <Select options={TOOL_OPTIONS} placeholder={t("postprocess.form.tool")} />
          </Form.Item>
          <Form.Item
            name="operation"
            label={t("postprocess.form.operation")}
            rules={[{ required: true, message: t("common.required") }]}
          >
            <Select options={OPERATION_OPTIONS} placeholder={t("postprocess.form.operation")} />
          </Form.Item>
          <Form.Item name="sourceImagePath" label={t("postprocess.input")}>
            <Input placeholder="exports/product.png" />
          </Form.Item>
          <Form.Item name="sourceGenerationResultId" label={t("postprocess.form.assetId")}>
            <InputNumber style={{ width: "100%" }} placeholder={t("common.optional")} />
          </Form.Item>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="targetWidth" label="宽度">
                <InputNumber min={1} style={{ width: "100%" }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="targetHeight" label="高度">
                <InputNumber min={1} style={{ width: "100%" }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="outputRatio" label="输出比例">
                <Input placeholder="1:1" />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  );
}