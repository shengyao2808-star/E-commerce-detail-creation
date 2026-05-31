import { Button, Card, Descriptions, Space, Tag, Typography } from "antd";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeftOutlined, ReloadOutlined } from "@ant-design/icons";
import { ErrorState, LoadingState } from "../../components/common";
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

export default function ResearchTaskDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [task, setTask] = useState<ResearchTask | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadTask = useCallback(async () => {
    if (!id) {
      setTask(null);
      setError(null);
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const detail = await api.research.get(id);
      setTask(detail);
    } catch (requestError) {
      setTask(null);
      setError(requestError instanceof Error ? requestError.message : "加载调研任务详情失败");
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    void loadTask();
  }, [loadTask]);

  if (loading) {
    return <LoadingState title="加载中" description="正在加载调研任务详情" />;
  }

  if (error) {
    return <ErrorState title="加载失败" description={error} onRetry={loadTask} />;
  }

  if (!task) {
    return <ErrorState title="任务不存在" description="未找到该调研任务" />;
  }

  return (
    <div>
      {/* 页面标题 */}
      <div className="df-page-header">
        <Space>
          <Link to="/research">
            <Button type="text" icon={<ArrowLeftOutlined />}>返回</Button>
          </Link>
          <div>
            <h1 className="df-page-title">调研任务详情 #{id}</h1>
            <p className="df-page-desc">{task.taskName || "市场调研任务"}</p>
          </div>
        </Space>
      </div>

      {/* 基本信息 */}
      <Card style={{ marginBottom: "var(--df-space-6)" }}>
        <div className="df-section-header">
          <span className="df-section-title">基本信息</span>
          <Space>
            <Tag color={getStatusColor(task.status)}>{task.status || "PENDING"}</Tag>
            <Button icon={<ReloadOutlined />} onClick={loadTask} loading={loading}>刷新</Button>
          </Space>
        </div>
        <Descriptions column={2}>
          <Descriptions.Item label="任务 ID">{task.id || id}</Descriptions.Item>
          <Descriptions.Item label="任务名称">{task.taskName || "--"}</Descriptions.Item>
          <Descriptions.Item label="类目">{task.category || "--"}</Descriptions.Item>
          <Descriptions.Item label="负责人">{task.owner || "--"}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{formatDateTime(task.createTime)}</Descriptions.Item>
          <Descriptions.Item label="更新时间">{formatDateTime(task.updateTime)}</Descriptions.Item>
        </Descriptions>
      </Card>

      {/* 输入数据 */}
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
          {task.inputData ? JSON.stringify(task.inputData, null, 2) : "--"}
        </pre>
      </Card>

      {/* 结果数据 */}
      <Card>
        <div className="df-section-header">
          <span className="df-section-title">结果数据</span>
        </div>
        <pre style={{
          background: "var(--df-bg)",
          padding: "var(--df-space-4)",
          borderRadius: "var(--df-radius-md)",
          overflow: "auto",
          fontSize: "var(--df-text-sm)",
          maxHeight: 300
        }}>
          {task.resultData ? JSON.stringify(task.resultData, null, 2) : "--"}
        </pre>
      </Card>
    </div>
  );
}