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
  { value: "real-esrgan", label: "Real-ESRGAN (upscale)" },
  { value: "iopaint", label: "IOPaint (inpaint/cleanup)" },
  { value: "grounded-sam", label: "Grounded-SAM (segment)" },
  { value: "imagemagick", label: "ImageMagick (resize/crop/convert)" }
];

const OPERATION_OPTIONS = [
  { value: "upscale", label: "upscale" },
  { value: "inpaint", label: "inpaint" },
  { value: "cleanup-background", label: "cleanup-background" },
  { value: "remove-object", label: "remove-object" },
  { value: "segment", label: "segment" },
  { value: "crop", label: "crop" },
  { value: "resize", label: "resize" },
  { value: "convert", label: "convert" },
  { value: "restore-face", label: "restore-face" }
];

function formatBytes(bytes?: number): string {
  if (!bytes) return "-";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function PostProcessTasksPage() {
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
      messageApi.error("Failed to load post-process tasks");
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
      messageApi.success("Task retry submitted");
      fetchTasks();
    } catch {
      messageApi.error("Retry failed");
    }
  };

  const handleCancel = async (id: number | string) => {
    try {
      await api.postProcessTasks.cancel(id);
      messageApi.success("Task canceled");
      fetchTasks();
    } catch {
      messageApi.error("Cancel failed");
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
      messageApi.success("Post-process task created");
      setCreateOpen(false);
      form.resetFields();
      fetchTasks();
    } catch {
      messageApi.error("Create failed");
    } finally {
      setCreating(false);
    }
  };

  const columns = [
    {
      title: "ID",
      dataIndex: "id",
      key: "id",
      width: 80
    },
    {
      title: "Tool",
      dataIndex: "toolCode",
      key: "toolCode",
      width: 140
    },
    {
      title: "Operation",
      dataIndex: "operation",
      key: "operation",
      width: 160
    },
    {
      title: "Status",
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
      title: "Input",
      key: "input",
      width: 160,
      render: (_: unknown, record: PostProcessTask) => {
        const w = record.inputWidth;
        const h = record.inputHeight;
        if (w && h) return `${w}x${h}`;
        return formatBytes(record.inputFileSize);
      }
    },
    {
      title: "Output",
      key: "output",
      width: 160,
      render: (_: unknown, record: PostProcessTask) => {
        const w = record.outputWidth;
        const h = record.outputHeight;
        if (w && h) return `${w}x${h}`;
        return formatBytes(record.outputFileSize);
      }
    },
    {
      title: "Error",
      dataIndex: "errorMessage",
      key: "errorMessage",
      ellipsis: true,
      render: (msg: string) =>
        msg ? (
          <Typography.Text type="danger" ellipsis={{ tooltip: msg }}>
            {msg}
          </Typography.Text>
        ) : (
          "-"
        )
    },
    {
      title: "Created",
      dataIndex: "createTime",
      key: "createTime",
      width: 170
    },
    {
      title: "Actions",
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
                Retry
              </Button>
            )}
            {canCancel && (
              <Button
                size="small"
                danger
                icon={<StopOutlined />}
                onClick={() => handleCancel(record.id!)}
              >
                Cancel
              </Button>
            )}
            {terminal && !canRetry && <Tag>Done</Tag>}
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
            Post-Process Tasks
          </Title>
        </Col>
        <Col>
          <Space>
            <Button icon={<ReloadOutlined />} onClick={fetchTasks}>
              Refresh
            </Button>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setCreateOpen(true)}
            >
              New Task
            </Button>
          </Space>
        </Col>
      </Row>

      <Card size="small" style={{ marginBottom: 16 }}>
        <Space wrap>
          <Select
            allowClear
            placeholder="Filter by status"
            style={{ width: 160 }}
            value={statusFilter}
            onChange={setStatusFilter}
            options={[
              { value: "PENDING", label: "Pending" },
              { value: "RUNNING", label: "Running" },
              { value: "SUCCEEDED", label: "Succeeded" },
              { value: "FAILED", label: "Failed" },
              { value: "CANCELED", label: "Canceled" }
            ]}
          />
          <Select
            allowClear
            placeholder="Filter by tool"
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
        title="Create Post-Process Task"
        open={createOpen}
        onCancel={() => setCreateOpen(false)}
        onOk={handleCreate}
        confirmLoading={creating}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="toolCode"
            label="Tool"
            rules={[{ required: true, message: "Select a tool" }]}
          >
            <Select options={TOOL_OPTIONS} placeholder="Select tool" />
          </Form.Item>
          <Form.Item
            name="operation"
            label="Operation"
            rules={[{ required: true, message: "Select an operation" }]}
          >
            <Select options={OPERATION_OPTIONS} placeholder="Select operation" />
          </Form.Item>
          <Form.Item name="sourceImagePath" label="Source Image Path">
            <Input placeholder="e.g. exports/product.png" />
          </Form.Item>
          <Form.Item name="sourceGenerationResultId" label="Source Generation Result ID">
            <InputNumber style={{ width: "100%" }} placeholder="Optional" />
          </Form.Item>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="targetWidth" label="Target Width">
                <InputNumber min={1} style={{ width: "100%" }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="targetHeight" label="Target Height">
                <InputNumber min={1} style={{ width: "100%" }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="outputRatio" label="Output Ratio">
                <Input placeholder="e.g. 1:1" />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  );
}
