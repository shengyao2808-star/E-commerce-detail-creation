import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Button, Card, Descriptions, Form, Input, InputNumber, Modal, Progress, Space, Table, Tag, Typography, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useMemo, useState } from "react";
import { EmptyState, ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";
import type { ImageJobCreateRequest, VisualPlan, VisualPlanBatchResultJob, VisualPlanBatchStatus, VisualPlanCreateRequest } from "../../services/types";
import { formatDateTime, parseJsonValue, safeJsonStringify } from "./visualUtils";

const { TextArea } = Input;
const { Paragraph, Text } = Typography;
const BATCH_REFRESH_INTERVAL_MS = 10_000;
const terminalBatchStatuses = new Set(["SUCCEEDED", "FAILED", "CANCELED", "CANCELLED", "PARTIAL_SUCCEEDED"]);

function isTerminalBatchStatus(status?: string) {
  return terminalBatchStatuses.has((status ?? "").trim().toUpperCase());
}

function canRetryJob(status?: string) {
  const normalized = (status ?? "").trim().toUpperCase();
  return normalized === "FAILED" || normalized === "CANCELED" || normalized === "CANCELLED";
}

function canCancelJob(status?: string) {
  const normalized = (status ?? "").trim().toUpperCase();
  return normalized === "PENDING" || normalized === "RUNNING";
}

const columns: ColumnsType<VisualPlan> = [
  { title: "ID", dataIndex: "id", width: 80, render: (v) => v ?? "--" },
  { title: "Detail ID", dataIndex: "productDetailId", width: 100, render: (v) => v ?? "--" },
  { title: "Name", dataIndex: "planName", render: (v) => v ?? "--" },
  { title: "Status", dataIndex: "status", width: 100, render: (v) => <Tag color={v === "CONFIRMED" ? "green" : undefined}>{v ?? "--"}</Tag> },
  { title: "Model", dataIndex: "modelProfileId", width: 100, render: (v) => v ?? "--" },
  { title: "SKC", dataIndex: "skcPolicyId", width: 100, render: (v) => v ?? "--" },
  { title: "Updated", dataIndex: "updateTime", width: 180, render: (v) => formatDateTime(v) }
];

export default function VisualPlansPage() {
  const [form] = Form.useForm();
  const [dispatchForm] = Form.useForm();
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<{ productDetailId?: string; status?: string }>({});
  const [detailPlan, setDetailPlan] = useState<VisualPlan | null>(null);
  const [selectedBatchSlot, setSelectedBatchSlot] = useState("");

  const query = useQuery({
    queryKey: ["visual", "plans", filters],
    queryFn: () =>
      api.visualPlans.list({
        pageNum: 1,
        pageSize: 20,
        productDetailId: filters.productDetailId ? Number(filters.productDetailId) : undefined,
        status: filters.status
      })
  });

  const createMutation = useMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const payload: VisualPlanCreateRequest = {
        productDetailId: values.productDetailId as number,
        planName: values.planName ? String(values.planName).trim() : undefined,
        categoryCode: values.categoryCode ? String(values.categoryCode).trim() : undefined,
        modelProfileId: values.modelProfileId as number | undefined,
        skcPolicyId: values.skcPolicyId as number | undefined,
        promptContext: values.promptContext ? parseJsonValue<Record<string, unknown>>(String(values.promptContext), {}) : undefined
      };
      return api.visualPlans.create(payload);
    },
    onSuccess: async () => {
      message.success("Visual plan created");
      form.resetFields();
      await queryClient.invalidateQueries({ queryKey: ["visual", "plans"] });
    }
  });

  const confirmMutation = useMutation({
    mutationFn: (id: number | string) => api.visualPlans.confirm(id),
    onSuccess: async () => {
      message.success("Visual plan confirmed - now immutable");
      await queryClient.invalidateQueries({ queryKey: ["visual", "plans"] });
    }
  });

  const batchStatusQuery = useQuery({
    queryKey: ["visual", "plans", detailPlan?.id, "batch-status"],
    queryFn: () => api.visualPlans.batchStatus(detailPlan?.id ?? ""),
    enabled: Boolean(detailPlan?.id && detailPlan.status === "CONFIRMED"),
    refetchInterval: (query) => {
      const status = query.state.data as VisualPlanBatchStatus | undefined;
      return status && !isTerminalBatchStatus(status.aggregatedStatus) ? BATCH_REFRESH_INTERVAL_MS : false;
    }
  });

  const batchResultsQuery = useQuery({
    queryKey: ["visual", "plans", detailPlan?.id, "batch-results", selectedBatchSlot],
    queryFn: () => api.visualPlans.batchResults(detailPlan?.id ?? "", selectedBatchSlot.trim() || undefined),
    enabled: Boolean(detailPlan?.id && detailPlan.status === "CONFIRMED"),
    refetchInterval: () => {
      const status = batchStatusQuery.data?.aggregatedStatus;
      return status && !isTerminalBatchStatus(status) ? BATCH_REFRESH_INTERVAL_MS : false;
    }
  });

  const dispatchMutation = useMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      if (!detailPlan?.id) {
        throw new Error("Missing visual plan ID");
      }
      if (detailPlan.status !== "CONFIRMED") {
        throw new Error("Only confirmed visual plans can dispatch image jobs");
      }
      const jobs = parseJsonValue<ImageJobCreateRequest[]>(String(values.jobsJson ?? ""), []);
      if (!Array.isArray(jobs) || jobs.length === 0) {
        throw new Error("Job list must be a non-empty JSON array");
      }
      return api.visualPlans.dispatch(detailPlan.id, jobs);
    },
    onSuccess: async (ids) => {
      message.success(`Dispatched ${ids.length} image job(s)`);
      dispatchForm.resetFields();
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["visual", "plans", detailPlan?.id, "batch-status"] }),
        queryClient.invalidateQueries({ queryKey: ["visual", "plans", detailPlan?.id, "batch-results"] }),
        queryClient.invalidateQueries({ queryKey: ["image-jobs"] })
      ]);
    }
  });

  const retryBatchMutation = useMutation({
    mutationFn: async () => {
      if (!detailPlan?.id) {
        throw new Error("Missing visual plan ID");
      }
      return api.visualPlans.batchRetry(detailPlan.id);
    },
    onSuccess: async (count) => {
      message.success(`Retried ${count} failed/canceled job(s)`);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["visual", "plans", detailPlan?.id, "batch-status"] }),
        queryClient.invalidateQueries({ queryKey: ["visual", "plans", detailPlan?.id, "batch-results"] })
      ]);
    }
  });

  const cancelBatchMutation = useMutation({
    mutationFn: async () => {
      if (!detailPlan?.id) {
        throw new Error("Missing visual plan ID");
      }
      return api.visualPlans.batchCancel(detailPlan.id);
    },
    onSuccess: async (count) => {
      message.success(`Canceled ${count} pending/running job(s)`);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["visual", "plans", detailPlan?.id, "batch-status"] }),
        queryClient.invalidateQueries({ queryKey: ["visual", "plans", detailPlan?.id, "batch-results"] })
      ]);
    }
  });

  const retryJobMutation = useMutation({
    mutationFn: async (job: VisualPlanBatchResultJob) => {
      if (!job.id) {
        throw new Error("Missing image job ID");
      }
      return api.imageJobs.retry(job.id, { retryReason: `Retry from visual plan #${detailPlan?.id ?? ""}`.trim() });
    },
    onSuccess: async () => {
      message.success("Image job retry submitted");
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["visual", "plans", detailPlan?.id, "batch-status"] }),
        queryClient.invalidateQueries({ queryKey: ["visual", "plans", detailPlan?.id, "batch-results"] }),
        queryClient.invalidateQueries({ queryKey: ["image-jobs"] })
      ]);
    }
  });

  const cancelJobMutation = useMutation({
    mutationFn: async (job: VisualPlanBatchResultJob) => {
      if (!job.id) {
        throw new Error("Missing image job ID");
      }
      return api.imageJobs.cancel(job.id);
    },
    onSuccess: async () => {
      message.success("Image job canceled");
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["visual", "plans", detailPlan?.id, "batch-status"] }),
        queryClient.invalidateQueries({ queryKey: ["visual", "plans", detailPlan?.id, "batch-results"] }),
        queryClient.invalidateQueries({ queryKey: ["image-jobs"] })
      ]);
    }
  });

  const rows = query.data?.data ?? [];
  const batchStatus = batchStatusQuery.data;
  const batchJobs = batchStatus?.jobSummaries ?? [];
  const totalJobs = batchStatus?.totalJobs ?? 0;
  const completedJobs = (batchStatus?.succeededJobs ?? 0) + (batchStatus?.failedJobs ?? 0) + (batchStatus?.canceledJobs ?? 0);
  const batchCompletionPercent = totalJobs > 0 ? Math.round((completedJobs / totalJobs) * 100) : 0;
  const batchSlotGroups = batchResultsQuery.data?.slotGroups ?? {};
  const batchSlots = Object.keys(batchSlotGroups).sort();
  const batchResultRows = Object.entries(batchSlotGroups).flatMap(([slot, jobs]) =>
    jobs.map((job) => ({ ...job, slot: job.slot ?? slot }))
  );
  const realResultCount = batchResultRows.reduce((count, job) => count + (job.results?.length ?? 0), 0);

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <Card title="Visual Plans">
        <Paragraph>Connected to <Text code>/api/v1/visual-plans</Text>. Draft plans can be edited; confirmed plans are frozen with a snapshot.</Paragraph>
      </Card>

      <Card title="Create Plan">
        <Form form={form} layout="vertical" onFinish={(values) => void createMutation.mutateAsync(values)}>
          <Space direction="vertical" size={12} style={{ width: "100%" }}>
            <Space wrap>
              <Form.Item name="productDetailId" label="Detail ID" rules={[{ required: true }]}><InputNumber style={{ width: 180 }} /></Form.Item>
              <Form.Item name="planName" label="Plan Name" style={{ minWidth: 240 }}><Input placeholder="Optional" /></Form.Item>
              <Form.Item name="categoryCode" label="Category Code" style={{ minWidth: 180 }}><Input placeholder="Optional" /></Form.Item>
            </Space>
            <Space wrap>
              <Form.Item name="modelProfileId" label="Model Profile ID"><InputNumber style={{ width: 180 }} /></Form.Item>
              <Form.Item name="skcPolicyId" label="SKC Policy ID"><InputNumber style={{ width: 180 }} /></Form.Item>
            </Space>
            <Form.Item name="promptContext" label="Plan Context JSON"><TextArea rows={4} placeholder='{"mainImages": 10}' /></Form.Item>
            {createMutation.isError ? <Alert showIcon type="error" message="Failed" description={createMutation.error instanceof Error ? createMutation.error.message : "Error"} /> : null}
            <Space>
              <Button type="primary" htmlType="submit" loading={createMutation.isPending}>Save Plan</Button>
              <Button onClick={() => form.resetFields()}>Reset</Button>
            </Space>
          </Space>
        </Form>
      </Card>

      <Card title="Plan List" extra={
        <Space wrap>
          <Input allowClear placeholder="Detail ID" value={filters.productDetailId} onChange={(e) => setFilters((v) => ({ ...v, productDetailId: e.target.value }))} style={{ width: 120 }} />
          <Input allowClear placeholder="Status" value={filters.status} onChange={(e) => setFilters((v) => ({ ...v, status: e.target.value }))} style={{ width: 120 }} />
          <Button onClick={() => void query.refetch()} loading={query.isFetching}>Refresh</Button>
        </Space>
      }>
        {query.isError ? <ErrorState title="Load failed" description={query.error instanceof Error ? query.error.message : "Error"} onRetry={() => void query.refetch()} /> : query.isPending ? <LoadingState title="Loading" description="GET /api/v1/visual-plans/list" /> : rows.length === 0 ? <EmptyState title="No plans" description="Empty list" /> : (
          <Table
            rowKey={(r) => String(r.id ?? Math.random())}
            columns={[...columns, {
              title: "Actions", key: "actions", width: 160,
              render: (_: unknown, record: VisualPlan) => (
                <Space size="small">
                  <Button type="link" size="small" disabled={!record.id || record.status === "CONFIRMED" || confirmMutation.isPending} onClick={() => { if (record.id) void confirmMutation.mutateAsync(record.id); }}>Confirm</Button>
                  <Button type="link" size="small" disabled={!record.id} onClick={() => { if (record.id) void api.visualPlans.get(record.id).then((plan) => { setDetailPlan(plan); setSelectedBatchSlot(""); dispatchForm.resetFields(); }); }}>View</Button>
                </Space>
              )
            }]}
            dataSource={rows}
            pagination={false}
            scroll={{ x: 1200 }}
            expandable={{ expandedRowRender: (record) => <pre style={{ margin: 0, whiteSpace: "pre-wrap" }}>{safeJsonStringify(record.planData)}</pre> }}
          />
        )}
      </Card>

      <Modal open={detailPlan !== null} title={detailPlan ? `Plan #${detailPlan.id}` : ""} onCancel={() => setDetailPlan(null)} footer={null} width={720}>
        {detailPlan && (
          <Space direction="vertical" size={16} style={{ width: "100%" }}>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="ID">{detailPlan.id}</Descriptions.Item>
              <Descriptions.Item label="Status"><Tag color={detailPlan.status === "CONFIRMED" ? "green" : undefined}>{detailPlan.status}</Tag></Descriptions.Item>
              <Descriptions.Item label="Detail ID">{detailPlan.productDetailId}</Descriptions.Item>
              <Descriptions.Item label="Name">{detailPlan.planName}</Descriptions.Item>
              <Descriptions.Item label="Category Policy">{detailPlan.categoryVisualPolicyId}</Descriptions.Item>
              <Descriptions.Item label="Model Profile">{detailPlan.modelProfileId}</Descriptions.Item>
              <Descriptions.Item label="SKC Policy">{detailPlan.skcPolicyId}</Descriptions.Item>
              <Descriptions.Item label="Version">{detailPlan.version}</Descriptions.Item>
              <Descriptions.Item label="Confirmed">{detailPlan.confirmedTime ? formatDateTime(detailPlan.confirmedTime) : "--"}</Descriptions.Item>
              <Descriptions.Item label="Created">{formatDateTime(detailPlan.createTime)}</Descriptions.Item>
              <Descriptions.Item label="Plan Data" span={2}><pre style={{ margin: 0, whiteSpace: "pre-wrap", fontSize: 12 }}>{safeJsonStringify(detailPlan.planData)}</pre></Descriptions.Item>
              {detailPlan.snapshotData && Object.keys(detailPlan.snapshotData).length > 0 && (
                <Descriptions.Item label="Snapshot" span={2}><pre style={{ margin: 0, whiteSpace: "pre-wrap", fontSize: 12, color: "#888" }}>{safeJsonStringify(detailPlan.snapshotData)}</pre></Descriptions.Item>
              )}
            </Descriptions>

            {detailPlan.status === "CONFIRMED" ? (
              <Card size="small" title="Image Job Batch">
                <Space direction="vertical" size={12} style={{ width: "100%" }}>
                  {batchStatusQuery.isError ? (
                    <ErrorState title="Batch status failed" description={batchStatusQuery.error instanceof Error ? batchStatusQuery.error.message : "Error"} onRetry={() => void batchStatusQuery.refetch()} />
                  ) : batchStatusQuery.isPending ? (
                    <LoadingState title="Loading batch status" description="GET /api/v1/visual-plans/{id}/batch-status" />
                  ) : (
                    <Space direction="vertical" size={8} style={{ width: "100%" }}>
                      <Descriptions bordered size="small" column={3}>
                        <Descriptions.Item label="Status"><Tag>{batchStatus?.aggregatedStatus ?? "--"}</Tag></Descriptions.Item>
                        <Descriptions.Item label="Total">{totalJobs}</Descriptions.Item>
                        <Descriptions.Item label="Completed">{completedJobs}</Descriptions.Item>
                        <Descriptions.Item label="Succeeded">{batchStatus?.succeededJobs ?? 0}</Descriptions.Item>
                        <Descriptions.Item label="Running">{batchStatus?.runningJobs ?? 0}</Descriptions.Item>
                        <Descriptions.Item label="Pending">{batchStatus?.pendingJobs ?? 0}</Descriptions.Item>
                        <Descriptions.Item label="Failed">{batchStatus?.failedJobs ?? 0}</Descriptions.Item>
                        <Descriptions.Item label="Canceled">{batchStatus?.canceledJobs ?? 0}</Descriptions.Item>
                        <Descriptions.Item label="Persisted results">{realResultCount}</Descriptions.Item>
                      </Descriptions>
                      <Progress percent={batchCompletionPercent} status={(batchStatus?.failedJobs ?? 0) > 0 ? "exception" : undefined} />
                    </Space>
                  )}

                  <Form form={dispatchForm} layout="vertical" onFinish={(values) => void dispatchMutation.mutateAsync(values)}>
                    <Form.Item name="jobsJson" label="Dispatch Jobs JSON" rules={[{ required: true }]}>
                      <TextArea rows={4} placeholder='[{"taskName":"main image","toolCode":"comfyui","slot":"main","ratio":"1:1","promptVersion":1}]' />
                    </Form.Item>
                    {(dispatchMutation.isError || retryBatchMutation.isError || cancelBatchMutation.isError || retryJobMutation.isError || cancelJobMutation.isError) ? (
                      <Alert
                        showIcon
                        type="error"
                        message="Batch action failed"
                        description={
                          dispatchMutation.error instanceof Error ? dispatchMutation.error.message :
                            retryBatchMutation.error instanceof Error ? retryBatchMutation.error.message :
                              cancelBatchMutation.error instanceof Error ? cancelBatchMutation.error.message :
                                retryJobMutation.error instanceof Error ? retryJobMutation.error.message :
                                  cancelJobMutation.error instanceof Error ? cancelJobMutation.error.message : "Error"
                        }
                      />
                    ) : null}
                    <Space wrap>
                      <Button type="primary" htmlType="submit" loading={dispatchMutation.isPending}>Dispatch Jobs</Button>
                      <Button loading={retryBatchMutation.isPending} onClick={() => void retryBatchMutation.mutateAsync()}>Retry Failed</Button>
                      <Button danger loading={cancelBatchMutation.isPending} onClick={() => void cancelBatchMutation.mutateAsync()}>Cancel Running</Button>
                      <Button onClick={() => void batchStatusQuery.refetch()} loading={batchStatusQuery.isFetching}>Refresh</Button>
                    </Space>
                  </Form>

                  {batchJobs.length === 0 ? (
                    <EmptyState title="No image jobs for this plan" description="No persisted image jobs are linked to this confirmed plan." />
                  ) : (
                    <Table
                      size="small"
                      rowKey={(r) => String(r.id ?? `${r.slot}-${r.taskName}`)}
                      dataSource={batchJobs}
                      pagination={false}
                      columns={[
                        { title: "Job", dataIndex: "id", width: 80 },
                        { title: "Task", dataIndex: "taskName" },
                        { title: "Slot", dataIndex: "slot", width: 120 },
                        { title: "Ratio", dataIndex: "ratio", width: 90 },
                        { title: "Status", dataIndex: "status", width: 120, render: (v) => <Tag>{v ?? "--"}</Tag> },
                        { title: "Progress", dataIndex: "progress", width: 90, render: (v) => v ?? "--" },
                        { title: "Error", dataIndex: "errorMessage", width: 220, render: (v) => v || "--" },
                        {
                          title: "Job actions",
                          key: "jobActions",
                          width: 170,
                          render: (_: unknown, record) => (
                            <Space size="small">
                              <Button
                                size="small"
                                disabled={!record.id || !canRetryJob(record.status)}
                                loading={retryJobMutation.isPending}
                                onClick={() => void retryJobMutation.mutateAsync(record)}
                              >
                                Retry
                              </Button>
                              <Button
                                size="small"
                                danger
                                disabled={!record.id || !canCancelJob(record.status)}
                                loading={cancelJobMutation.isPending}
                                onClick={() => void cancelJobMutation.mutateAsync(record)}
                              >
                                Cancel
                              </Button>
                            </Space>
                          )
                        }
                      ]}
                    />
                  )}

                  <Card size="small" title="Slot Results">
                    <Space direction="vertical" size={12} style={{ width: "100%" }}>
                      <Space wrap>
                        <Input
                          allowClear
                          placeholder="slot filter"
                          value={selectedBatchSlot}
                          onChange={(event) => setSelectedBatchSlot(event.target.value)}
                          style={{ width: 180 }}
                        />
                        <Button onClick={() => void batchResultsQuery.refetch()} loading={batchResultsQuery.isFetching}>Refresh Results</Button>
                        <Text type="secondary">Slots: {batchSlots.length > 0 ? batchSlots.join(", ") : "--"}</Text>
                      </Space>
                      {batchResultsQuery.isError ? (
                        <ErrorState title="Batch results failed" description={batchResultsQuery.error instanceof Error ? batchResultsQuery.error.message : "Error"} onRetry={() => void batchResultsQuery.refetch()} />
                      ) : batchResultsQuery.isPending ? (
                        <LoadingState title="Loading batch results" description="GET /api/v1/visual-plans/{id}/batch-results" />
                      ) : batchResultRows.length === 0 ? (
                        <EmptyState title="No real results for this slot" description="The backend returned no persisted generation_result rows for the current plan/slot." />
                      ) : (
                        <Table
                          size="small"
                          rowKey={(r) => String(r.id ?? `${r.slot}-${r.taskName}`)}
                          dataSource={batchResultRows}
                          pagination={false}
                          columns={[
                            { title: "Slot", dataIndex: "slot", width: 120 },
                            { title: "Job", dataIndex: "id", width: 80, render: (v) => v ?? "--" },
                            { title: "Task", dataIndex: "taskName", render: (v) => v ?? "--" },
                            { title: "Status", dataIndex: "status", width: 120, render: (v) => <Tag>{v ?? "--"}</Tag> },
                            { title: "Result rows", key: "resultRows", width: 110, render: (_: unknown, record) => record.results?.length ?? 0 },
                            {
                              title: "Results",
                              key: "results",
                              render: (_: unknown, record) => {
                                const results = record.results ?? [];
                                return results.length === 0 ? (
                                  <Text type="secondary">--</Text>
                                ) : (
                                  <Space wrap>
                                    {results.map((result, index) => {
                                      const url = result.thumbnailUrl || result.resultUrl;
                                      return url ? (
                                        <a key={String(result.id ?? result.resultUrl)} href={result.resultUrl || url} target="_blank" rel="noreferrer">
                                          <img src={url} alt={result.prompt || `result-${result.id ?? ""}`} style={{ width: 48, height: 48, objectFit: "cover", borderRadius: 6 }} />
                                        </a>
                                      ) : (
                                        <Tag key={String(result.id ?? index)}>#{result.id ?? "--"}</Tag>
                                      );
                                    })}
                                  </Space>
                                );
                              }
                            }
                          ]}
                        />
                      )}
                    </Space>
                  </Card>
                </Space>
              </Card>
            ) : (
              <Alert showIcon type="info" message="Batch dispatch requires a confirmed visual plan." />
            )}
          </Space>
        )}
      </Modal>
    </Space>
  );
}
