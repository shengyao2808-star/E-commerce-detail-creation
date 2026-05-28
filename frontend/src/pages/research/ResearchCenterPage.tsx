import { Button, Card, Descriptions, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { PlaceholderChart } from "../../components/charts/PlaceholderChart";
import { EmptyState, ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";
import type { ResearchChartDatum, ResearchTask, ResearchTaskCharts } from "../../services/types";
import { useWorkbenchStore } from "../../stores/workbenchStore";
import { P0Scaffold } from "../p0/P0Scaffold";

const { Text } = Typography;

const toBarData = (items: ResearchChartDatum[] | undefined) =>
  (items ?? []).map((item) => ({
    label: String(item.label ?? item.name ?? ""),
    value: Number(item.value ?? 0)
  }));

const toScatterData = (items: ResearchChartDatum[] | undefined) =>
  (items ?? []).map((item) => ({
    name: String(item.name ?? item.label ?? ""),
    x: Number(item.x ?? 0),
    y: Number(item.y ?? 0)
  }));

export default function ResearchCenterPage() {
  const setCurrentResearchTask = useWorkbenchStore((state) => state.setCurrentResearchTask);
  const [tasks, setTasks] = useState<ResearchTask[]>([]);
  const [selectedTaskId, setSelectedTaskId] = useState<number | null>(null);
  const [charts, setCharts] = useState<ResearchTaskCharts | null>(null);
  const [loadingTasks, setLoadingTasks] = useState(true);
  const [loadingCharts, setLoadingCharts] = useState(false);
  const [taskError, setTaskError] = useState<string | null>(null);
  const [chartError, setChartError] = useState<string | null>(null);

  const selectedTask = useMemo(
    () => tasks.find((task) => task.id === selectedTaskId) ?? null,
    [selectedTaskId, tasks]
  );

  const loadTasks = useCallback(async () => {
    setLoadingTasks(true);
    setTaskError(null);

    try {
      const page = await api.research.list({ pageNum: 1, pageSize: 20 });
      setTasks(page.data ?? []);
      setSelectedTaskId((current) => current ?? page.data?.[0]?.id ?? null);
    } catch (requestError) {
      setTaskError(requestError instanceof Error ? requestError.message : "Failed to load research tasks");
    } finally {
      setLoadingTasks(false);
    }
  }, []);

  useEffect(() => {
    void loadTasks();
  }, [loadTasks]);

  useEffect(() => {
    if (!selectedTask) {
      setCharts(null);
      setChartError(null);
      setCurrentResearchTask(null);
      return;
    }

    setCurrentResearchTask({
      id: String(selectedTask.id ?? ""),
      name: selectedTask.taskName ?? selectedTask.category ?? `Task ${selectedTask.id ?? ""}`,
      status: selectedTask.status,
      updatedAt: selectedTask.updateTime ?? selectedTask.createTime
    });

    const loadCharts = async () => {
      setLoadingCharts(true);
      setChartError(null);
      try {
        setCharts(await api.research.charts(selectedTask.id ?? ""));
      } catch (requestError) {
        setCharts(null);
        setChartError(requestError instanceof Error ? requestError.message : "Failed to load chart data");
      } finally {
        setLoadingCharts(false);
      }
    };

    void loadCharts();
  }, [selectedTask, setCurrentResearchTask]);

  const columns = useMemo<ColumnsType<ResearchTask>>(
    () => [
      { title: "Task ID", dataIndex: "id", width: 96 },
      { title: "Task Name", dataIndex: "taskName" },
      { title: "Category", dataIndex: "category" },
      { title: "Owner", dataIndex: "owner" },
      {
        title: "Status",
        dataIndex: "status",
        width: 120,
        render: (value: string | undefined) => <Tag>{value ?? "PENDING"}</Tag>
      },
      { title: "Updated At", dataIndex: "updateTime", width: 180 }
    ],
    []
  );

  const chartDescription = selectedTask
    ? loadingCharts
      ? "Reading real chart results from backend..."
      : chartError
        ? chartError
        : "Backend returned no chart data."
    : "Select a real task first.";
  const chartEndpoint = selectedTaskId ? `/api/v1/research/tasks/${selectedTaskId}/charts` : "/api/v1/research/tasks/{id}/charts";

  return (
    <P0Scaffold
      eyebrow="Research"
      title="Research Center"
      description="Only real backend tasks and results are shown. No fake task data, prices, or competitor conclusions are generated here."
      actions={[
        { label: "New Research Task", to: "/research/new" },
        { label: "Import Data", to: "/tools/imports" }
      ]}
      flow={["Tasks", "Real Results", "Chart Placeholder", "Report Output"]}
      apiNotice={false}
      toolNotice={false}
    >
      <div className="p0-notice-grid">
        <Card className="p0-card" title="Task Overview">
          <Space direction="vertical" size={6}>
            <Text type="secondary">Backend: GET /api/v1/research/tasks/list</Text>
            <Text type="secondary">Charts: GET {chartEndpoint}</Text>
          </Space>
        </Card>
        <Card className="p0-card" title="Task Entry">
          <Space direction="vertical" size={8}>
            <Button disabled>Filter Tasks (pending)</Button>
            <Link to="/research/new">Create Real Research Task</Link>
          </Space>
        </Card>
      </div>

      <Card className="p0-card" title="Real Task List" extra={<Button onClick={() => void loadTasks()} loading={loadingTasks}>Refresh</Button>}>
        {taskError ? (
          <ErrorState title="Failed to load research tasks" description={taskError} />
        ) : loadingTasks ? (
          <LoadingState title="Loading research tasks" description="GET /api/v1/research/tasks/list" />
        ) : tasks.length === 0 ? (
          <EmptyState
            title="No real research tasks yet"
            description="When the backend returns no data, the list stays empty."
            action={<Link to="/research/new">Create Task</Link>}
          />
        ) : (
          <Table
            rowKey="id"
            columns={columns}
            dataSource={tasks}
            pagination={false}
            rowClassName={(record) => (record.id === selectedTaskId ? "table-row-selected" : "")}
            onRow={(record) => ({
              onClick: () => setSelectedTaskId(record.id ?? null)
            })}
          />
        )}
      </Card>

      {selectedTask ? (
        <Card className="p0-card" title="Task Detail">
          <Descriptions bordered column={2}>
            <Descriptions.Item label="Task Name">{selectedTask.taskName ?? "--"}</Descriptions.Item>
            <Descriptions.Item label="Status">{selectedTask.status ?? "PENDING"}</Descriptions.Item>
            <Descriptions.Item label="Category">{selectedTask.category ?? "--"}</Descriptions.Item>
            <Descriptions.Item label="Owner">{selectedTask.owner ?? "--"}</Descriptions.Item>
            <Descriptions.Item label="Created At">{selectedTask.createTime ?? "--"}</Descriptions.Item>
            <Descriptions.Item label="Updated At">{selectedTask.updateTime ?? "--"}</Descriptions.Item>
          </Descriptions>
        </Card>
      ) : null}

      <div className="research-chart-grid">
        <PlaceholderChart title="Price Bands" description={chartDescription} mode="bar" data={toBarData(charts?.priceBands)} />
        <PlaceholderChart title="Keyword Ranking" description={chartDescription} mode="bar" data={toBarData(charts?.keywordRanking)} />
        <PlaceholderChart title="Pain Point Ranking" description={chartDescription} mode="bar" data={toBarData(charts?.painPointRanking)} />
        <PlaceholderChart title="Competitor Matrix" description={chartDescription} mode="scatter" data={toScatterData(charts?.competitorMatrix)} />
      </div>
    </P0Scaffold>
  );
}
