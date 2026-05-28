import { Alert, Button, Card, Descriptions, Space, Tag, Typography } from "antd";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { EmptyState, ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";
import type { ResearchTask } from "../../services/types";
import { P0Scaffold } from "../p0/P0Scaffold";

const { Text } = Typography;

const normalizeText = (value: unknown) => {
  if (value === null || value === undefined) {
    return "--";
  }
  if (typeof value === "string" && value.trim().length === 0) {
    return "--";
  }
  return String(value);
};

const formatJson = (value: unknown) => {
  if (!value || typeof value !== "object") {
    return "--";
  }
  return JSON.stringify(value, null, 2);
};

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
      setError(requestError instanceof Error ? requestError.message : "Failed to load research task detail");
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    void loadTask();
  }, [loadTask]);

  const title = useMemo(() => `Research Task Detail${id ? ` #${id}` : ""}`, [id]);

  return (
    <P0Scaffold
      eyebrow="Research"
      title={title}
      description="Loads persisted task detail from GET /api/v1/research/tasks/{id}. This page does not fabricate execution logs or generated findings."
      actions={[{ label: "Back to Tasks", to: "/research" }]}
      apiNotice={false}
      toolNotice={false}
      capabilities={[
        { title: "Task detail", description: "Reads persisted task fields from backend.", status: "available" },
        { title: "Result payload", description: "Displays stored result JSON as-is.", status: "available" },
        { title: "No fake logs", description: "No fabricated worker timeline is rendered.", status: "available" }
      ]}
    >
      <Card className="p0-card" title="Task Snapshot" extra={<Button onClick={() => void loadTask()} loading={loading}>Refresh</Button>}>
        {!id ? (
          <EmptyState title="Missing task id" description="Open this page with a real task id from the task list." />
        ) : error ? (
          <ErrorState title="Failed to load task detail" description={error} />
        ) : loading ? (
          <LoadingState title="Loading task detail" description={`GET /api/v1/research/tasks/${id}`} />
        ) : !task ? (
          <EmptyState title="Task not found" description="Backend returned no task payload for this id." />
        ) : (
          <Space direction="vertical" size={16} style={{ width: "100%" }}>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="Task ID">{normalizeText(task.id ?? id)}</Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag>{normalizeText(task.status)}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Task Name">{normalizeText(task.taskName)}</Descriptions.Item>
              <Descriptions.Item label="Category">{normalizeText(task.category)}</Descriptions.Item>
              <Descriptions.Item label="Owner">{normalizeText(task.owner)}</Descriptions.Item>
              <Descriptions.Item label="Created At">{normalizeText(task.createTime)}</Descriptions.Item>
              <Descriptions.Item label="Updated At">{normalizeText(task.updateTime)}</Descriptions.Item>
              <Descriptions.Item label="Report Link">
                {task.id ? <Link to={`/research/reports/${task.id}`}>Open Report</Link> : "--"}
              </Descriptions.Item>
            </Descriptions>

            <Alert showIcon type="info" message={`Backend endpoint: GET /api/v1/research/tasks/${id}`} />

            <Card size="small" title="Input Data">
              <pre style={{ margin: 0, whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
                {formatJson(task.inputData)}
              </pre>
            </Card>

            <Card size="small" title="Result Data">
              <pre style={{ margin: 0, whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
                {formatJson(task.resultData)}
              </pre>
            </Card>

            <Text type="secondary">
              Execution logs are intentionally omitted until a real backend execution-log endpoint exists.
            </Text>
          </Space>
        )}
      </Card>
    </P0Scaffold>
  );
}
