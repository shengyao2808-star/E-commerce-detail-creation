import { Alert, Button, Card, Descriptions, Empty, Input, Space, Switch, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useMemo, useState } from "react";
import { EmptyState, ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";
import type { GenerationResult, PageResult } from "../../services/types";
import { P0Scaffold } from "../p0/P0Scaffold";

const { Text, Paragraph } = Typography;
const RESULTS_REFRESH_INTERVAL_MS = 30_000;

type NoticeTone = "info" | "success" | "warning" | "error";

type Notice = {
  tone: NoticeTone;
  title: string;
  message: string;
};

function normalizeId(value?: number | string) {
  return value === undefined || value === null || value === "" ? "" : String(value);
}

function parseDetailId(value: string) {
  const trimmed = value.trim();
  if (!trimmed) {
    return null;
  }

  const parsed = Number(trimmed);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return null;
  }

  return parsed;
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

function safeJsonStringify(value: unknown) {
  if (value === undefined) {
    return "--";
  }

  if (typeof value === "string") {
    return value;
  }

  try {
    return JSON.stringify(value, null, 2);
  } catch {
    return String(value);
  }
}

function formatJsonSummary(value: unknown) {
  const text = safeJsonStringify(value);
  if (text === "--") {
    return text;
  }
  return text.length > 160 ? `${text.slice(0, 157)}...` : text;
}

function getComplianceMeta(status?: string): { label: string; color: string } {
  const normalized = (status ?? "").trim().toUpperCase();

  switch (normalized) {
    case "APPROVED":
    case "PASSED":
    case "PASS":
    case "COMPLIANT":
      return { label: "已通过", color: "success" };
    case "PENDING":
    case "PROCESSING":
    case "REVIEWING":
      return { label: "审核中", color: "warning" };
    case "REJECTED":
    case "FAILED":
    case "BLOCKED":
      return { label: "未通过", color: "error" };
    default:
      return { label: normalized || "未知", color: "default" };
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

function ResultPreviewImage({ src, alt }: { src?: string; alt: string }) {
  if (!src) {
    return <Empty description="暂无可显示的缩略图" />;
  }

  return <img src={src} alt={alt} style={{ maxWidth: "100%", borderRadius: 8, display: "block" }} />;
}

async function listGenerationResultsByVisualPlanId(visualPlanId: string, slot?: string): Promise<PageResult<GenerationResult>> {
  const grouped = await api.visualPlans.batchResults(visualPlanId, slot?.trim() || undefined);
  const slotGroups = grouped.slotGroups ?? {};
  const data = Object.values(slotGroups).flatMap((jobs) =>
    jobs.flatMap((job) => {
      const results = Array.isArray(job.results) ? job.results : [];
      return results as GenerationResult[];
    })
  );
  return { code: 200, message: "OK", data, pageNum: 1, pageSize: data.length || 20, total: data.length, pages: data.length > 0 ? 1 : 0 };
}

export default function ResultsPreviewPage() {
  const queryClient = useQueryClient();
  const [notice, setNotice] = useState<Notice>({
    tone: "info",
    title: "结果预览",
    message: "这里只读取 /api/v1/generation-results 的持久化记录；列表为空时就保持空状态。"
  });
  const [detailIdInput, setDetailIdInput] = useState("");
  const [visualPlanIdFilter, setVisualPlanIdFilter] = useState("");
  const [visualPlanSlotFilter, setVisualPlanSlotFilter] = useState("");
  const [selectedResultId, setSelectedResultId] = useState<string>("");
  const [busyResultId, setBusyResultId] = useState<string | null>(null);
  const normalizedVisualPlanIdFilter = visualPlanIdFilter.trim();
  const normalizedVisualPlanSlotFilter = visualPlanSlotFilter.trim();

  const resultsQuery = useQuery({
    queryKey: ["generation-results", { visualPlanId: normalizedVisualPlanIdFilter, slot: normalizedVisualPlanSlotFilter }],
    queryFn: async () =>
      normalizedVisualPlanIdFilter
        ? listGenerationResultsByVisualPlanId(normalizedVisualPlanIdFilter, normalizedVisualPlanSlotFilter)
        : api.generationResults.list({ pageNum: 1, pageSize: 20 }),
    staleTime: 15_000,
    refetchInterval: RESULTS_REFRESH_INTERVAL_MS
  });

  const selectionMutation = useMutation({
    mutationFn: async ({ id, selected }: { id: string; selected: boolean }) =>
      api.generationResults.updateSelection(id, { selected }),
    onSuccess: async (_, variables) => {
      setNotice({
        tone: "success",
        title: "选择状态已更新",
        message: `结果 #${variables.id} 的 selected=${variables.selected}`
      });
      await queryClient.invalidateQueries({ queryKey: ["generation-results"] });
    },
    onError: (error) => {
      setNotice({
        tone: "error",
        title: "更新失败",
        message: error instanceof Error ? error.message : "更新结果选择状态时发生错误"
      });
    }
  });

  const results = resultsQuery.data?.data ?? [];
  const selectedGenerationResultIds = useMemo(
    () =>
      results
        .filter((record) => Boolean(record.selected))
        .map((record) => Number(normalizeId(record.id)))
        .filter((value) => Number.isFinite(value) && value > 0),
    [results]
  );

  const applyGenerationResultsMutation = useMutation({
    mutationFn: async ({ detailId, generationResultIds }: { detailId: number; generationResultIds: number[] }) =>
      api.detail.applyGenerationResults(detailId, {
        generationResultIds
      }),
    onSuccess: async (_, variables) => {
      setNotice({
        tone: "success",
        title: "已应用到详情",
        message: `已将 ${variables.generationResultIds.length} 条已选结果应用到 detail #${variables.detailId}`
      });
      await queryClient.invalidateQueries({ queryKey: ["generation-results"] });
    },
    onError: (error) => {
      setNotice({
        tone: "error",
        title: "应用失败",
        message: error instanceof Error ? error.message : "应用 generation-results 到 detail 时发生错误"
      });
    }
  });

  useEffect(() => {
    if (results.length === 0) {
      setSelectedResultId("");
      return;
    }

    setSelectedResultId((current) => {
      if (current && results.some((item) => normalizeId(item.id) === current)) {
        return current;
      }

      return normalizeId(results[0]?.id);
    });
  }, [results]);

  const selectedResult = useMemo(
    () => results.find((item) => normalizeId(item.id) === selectedResultId) ?? results[0] ?? null,
    [results, selectedResultId]
  );

  const handleApplySelectedResults = () => {
    const detailId = parseDetailId(detailIdInput);
    if (detailId === null) {
      setNotice({
        tone: "error",
        title: "请输入真实 detail ID",
        message: "detail ID 不能为空，且必须是大于 0 的数字。"
      });
      return;
    }

    if (selectedGenerationResultIds.length === 0) {
      setNotice({
        tone: "warning",
        title: "没有可应用的结果",
        message: "请先将至少一条已持久化的 generation-result 标记为选中。"
      });
      return;
    }

    void applyGenerationResultsMutation.mutateAsync({
      detailId,
      generationResultIds: selectedGenerationResultIds
    });
  };

  const columns = useMemo<ColumnsType<GenerationResult>>(
    () => [
      {
        title: "结果 ID",
        dataIndex: "id",
        width: 100,
        render: (value: GenerationResult["id"]) => (value ? `#${value}` : "--")
      },
      {
        title: "生图任务 ID",
        dataIndex: "imageJobId",
        width: 120,
        render: (value: GenerationResult["imageJobId"]) => (value ? `#${value}` : "--")
      },
      {
        title: "缩略图 / 结果",
        key: "preview",
        render: (_, record) => {
          const previewSrc = record.thumbnailUrl || record.resultUrl;
          return previewSrc ? (
            <Space size={8}>
              <img
                src={previewSrc}
                alt={record.prompt || `result-${record.id ?? ""}`}
                style={{ width: 56, height: 56, objectFit: "cover", borderRadius: 6 }}
              />
              <a href={record.resultUrl || previewSrc} target="_blank" rel="noreferrer">
                查看
              </a>
            </Space>
          ) : (
            <Text type="secondary">--</Text>
          );
        }
      },
      {
        title: "提示词",
        dataIndex: "prompt",
        render: (value: string | undefined) => value || "--"
      },
      {
        title: "合规状态",
        dataIndex: "complianceStatus",
        width: 140,
        render: (value: string | undefined) => {
          const meta = getComplianceMeta(value);
          return <Tag color={meta.color}>{meta.label}</Tag>;
        }
      },
      {
        title: "已选中",
        dataIndex: "selected",
        width: 120,
        render: (_, record) => {
          const id = normalizeId(record.id);
          const checked = Boolean(record.selected);
          const busy = busyResultId === id || selectionMutation.isPending;

          return (
            <Switch
              checked={checked}
              loading={busy}
              disabled={!id || busy}
              onChange={(nextChecked) => {
                if (!id) {
                  return;
                }
                setBusyResultId(id);
                void selectionMutation
                  .mutateAsync({ id, selected: nextChecked })
                  .finally(() => setBusyResultId(null));
              }}
            />
          );
        }
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
      }
    ],
    [busyResultId, selectionMutation]
  );

  const detailPreviewSrc = selectedResult?.thumbnailUrl || selectedResult?.resultUrl;
  const selectedComplianceMeta = getComplianceMeta(selectedResult?.complianceStatus);

  return (
    <P0Scaffold
      eyebrow="结果"
      title="生成结果预览"
      description="这里只显示 generation-results 表里的持久化数据；列表为空就保持空状态，选择状态也只来自后端。"
      actions={[{ label: "返回生图工作台", to: "/generate" }]}
      flow={["生成结果", "选择状态", "后续处理"]}
      apiNotice={false}
      toolNotice={false}
      capabilities={[
        { title: "真实列表", description: "GET /api/v1/generation-results/list 读取持久化结果。", status: "available" },
        { title: "真实详情", description: "结果卡片只展示后端返回字段。", status: "available" },
        { title: "真实选择", description: "PUT /api/v1/generation-results/{id}/selection 写回 selected。", status: "available" }
      ]}
    >
      <NoticeBanner notice={notice} />
      <Card className="p0-card" title="应用到详情">
        <Space direction="vertical" size={12} style={{ width: "100%" }}>
          <Text type="secondary">
            这里只会把后台里已持久化且处于“已选中”状态的 generation-result ID 传给后端。
          </Text>
          <Space wrap>
            <Input
              value={detailIdInput}
              onChange={(event) => setDetailIdInput(event.target.value)}
              placeholder="detail ID"
              inputMode="numeric"
              style={{ width: 240 }}
            />
            <Button
              type="primary"
              loading={applyGenerationResultsMutation.isPending}
              disabled={selectedGenerationResultIds.length === 0}
              onClick={handleApplySelectedResults}
            >
              应用选中结果
            </Button>
          </Space>
          <Text type="secondary">
            已选中的持久化结果:{" "}
            {selectedGenerationResultIds.length > 0 ? selectedGenerationResultIds.map((id) => `#${id}`).join(", ") : "--"}
          </Text>
        </Space>
      </Card>

      <Card className="p0-card" title="Visual Plan Result Filter">
        <Space wrap>
          <Input
            allowClear
            placeholder="visual plan ID"
            value={visualPlanIdFilter}
            onChange={(event) => setVisualPlanIdFilter(event.target.value)}
            style={{ width: 180 }}
          />
          <Input
            allowClear
            placeholder="slot"
            value={visualPlanSlotFilter}
            onChange={(event) => setVisualPlanSlotFilter(event.target.value)}
            disabled={!normalizedVisualPlanIdFilter}
            style={{ width: 160 }}
          />
          <Button onClick={() => void resultsQuery.refetch()} loading={resultsQuery.isFetching}>
            Refresh
          </Button>
        </Space>
      </Card>

      <Card
        className="p0-card"
        title="结果列表"
        extra={
          <Button onClick={() => void resultsQuery.refetch()} loading={resultsQuery.isFetching}>
            刷新列表
          </Button>
        }
      >
        {resultsQuery.isError ? (
          <ErrorState
            title="结果列表加载失败"
            description={resultsQuery.error instanceof Error ? resultsQuery.error.message : "无法读取 generation-results 列表"}
            onRetry={() => void resultsQuery.refetch()}
          />
        ) : resultsQuery.isPending ? (
          <LoadingState title="正在加载结果列表" description="GET /api/v1/generation-results/list" />
        ) : results.length === 0 ? (
          <EmptyState
            title="暂无真实生成结果"
            description="后端返回空列表时，这里保持空状态。"
            action={
              <Button type="primary" onClick={() => void resultsQuery.refetch()}>
                刷新列表
              </Button>
            }
          />
        ) : (
          <Table
            rowKey={(record) => normalizeId(record.id) || `${record.imageJobId ?? ""}-${record.createTime ?? ""}`}
            columns={columns}
            dataSource={results}
            pagination={false}
            scroll={{ x: 1180 }}
            rowClassName={(record) => (normalizeId(record.id) === selectedResultId ? "table-row-selected" : "")}
            onRow={(record) => ({
              onClick: () => setSelectedResultId(normalizeId(record.id))
            })}
          />
        )}
      </Card>

      {selectedResult ? (
        <Card
          className="p0-card"
          title={`结果详情${selectedResult.id ? ` #${selectedResult.id}` : ""}`}
        >
          <Space direction="vertical" size={16} style={{ width: "100%" }}>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="结果 ID">{selectedResult.id ? `#${selectedResult.id}` : "--"}</Descriptions.Item>
              <Descriptions.Item label="生图任务 ID">
                {selectedResult.imageJobId ? `#${selectedResult.imageJobId}` : "--"}
              </Descriptions.Item>
              <Descriptions.Item label="合规状态">
                <Tag color={selectedComplianceMeta.color}>{selectedComplianceMeta.label}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="已选中">{selectedResult.selected ? "是" : "否"}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{formatDateTime(selectedResult.createTime)}</Descriptions.Item>
              <Descriptions.Item label="更新时间">{formatDateTime(selectedResult.updateTime)}</Descriptions.Item>
              <Descriptions.Item label="结果 URL">{selectedResult.resultUrl || "--"}</Descriptions.Item>
              <Descriptions.Item label="缩略图 URL">{selectedResult.thumbnailUrl || "--"}</Descriptions.Item>
            </Descriptions>

            <section
              style={{
                border: "1px solid #e2e8f0",
                borderRadius: 8,
                padding: 16
              }}
            >
              <Text strong>结果预览</Text>
              <div style={{ marginTop: 12 }}>
                <ResultPreviewImage src={detailPreviewSrc} alt={selectedResult.prompt || "generation-result"} />
              </div>
            </section>

            <section
              style={{
                border: "1px solid #e2e8f0",
                borderRadius: 8,
                padding: 16
              }}
            >
              <Space direction="vertical" size={12} style={{ width: "100%" }}>
                <div>
                  <Text strong>Prompt</Text>
                  <Paragraph style={{ marginBottom: 0, whiteSpace: "pre-wrap" }}>
                    {selectedResult.prompt || "--"}
                  </Paragraph>
                </div>
                <div>
                  <Text strong>Params</Text>
                  <Paragraph style={{ marginBottom: 0, whiteSpace: "pre-wrap" }}>
                    {formatJsonSummary(selectedResult.params ?? selectedResult.paramsJson)}
                  </Paragraph>
                </div>
              </Space>
            </section>
          </Space>
        </Card>
      ) : (
        <Empty description="请选择一条真实结果记录查看详情" />
      )}
    </P0Scaffold>
  );
}
