import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Button, Card, Form, Input, Select, Space, Table, Tag, Typography, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useMemo, useState } from "react";
import { EmptyState, ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";
import type { CategoryVisualPolicy } from "../../services/types";
import { formatDateTime, parseJsonValue, parseListText, safeJsonStringify, textFromUnknown } from "./visualUtils";

const { TextArea } = Input;
const { Paragraph, Text } = Typography;

type PolicyFormValues = {
  categoryCode: string;
  categoryName: string;
  modelPolicy?: string;
  modelConsistencyLevel?: string;
  allowedShotTypes?: string;
  requiredMainImages?: string;
  detailScreenCountRange?: string;
  riskRules?: string;
  status?: string;
};

const statusOptions = [
  { label: "DRAFT", value: "DRAFT" },
  { label: "CONFIRMED", value: "CONFIRMED" },
  { label: "ARCHIVED", value: "ARCHIVED" }
];

const policyColumns: ColumnsType<CategoryVisualPolicy> = [
  { title: "类目编码", dataIndex: "categoryCode", width: 140, render: (v) => v ?? "--" },
  { title: "类目名称", dataIndex: "categoryName", width: 180, render: (v) => v ?? "--" },
  { title: "模型策略", dataIndex: "modelPolicy", width: 120, render: (v) => <Tag>{textFromUnknown(v)}</Tag> },
  { title: "Consistency", dataIndex: "modelConsistencyLevel", width: 120, render: (v) => <Tag color="blue">{textFromUnknown(v)}</Tag> },
  { title: "Shot Types", dataIndex: "allowedShotTypes", render: (v) => textFromUnknown(v) },
  { title: "Risk Rules", dataIndex: "riskRules", render: (v) => textFromUnknown(v) },
  { title: "状态", dataIndex: "status", width: 100, render: (v) => v ?? "--" },
  { title: "更新时间", dataIndex: "updateTime", width: 180, render: (v) => formatDateTime(v) }
];

export default function CategoryVisualPoliciesPage() {
  const [form] = Form.useForm<PolicyFormValues>();
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<{ categoryCode?: string; status?: string }>({});

  const query = useQuery({
    queryKey: ["visual", "category-policies", filters],
    queryFn: () =>
      api.visualCategoryPolicies.list({
        pageNum: 1,
        pageSize: 20,
        categoryCode: filters.categoryCode,
        status: filters.status
      })
  });

  const createMutation = useMutation({
    mutationFn: async (values: PolicyFormValues) => {
      const payload: CategoryVisualPolicy = {
        categoryCode: values.categoryCode.trim(),
        categoryName: values.categoryName.trim(),
        modelPolicy: values.modelPolicy?.trim() || undefined,
        modelConsistencyLevel: values.modelConsistencyLevel?.trim() || undefined,
        allowedShotTypes: parseListText(values.allowedShotTypes ?? ""),
        requiredMainImages: values.requiredMainImages ? parseJsonValue<Record<string, unknown>>(values.requiredMainImages, {}) : undefined,
        detailScreenCountRange: values.detailScreenCountRange
          ? parseJsonValue<Record<string, unknown>>(values.detailScreenCountRange, {})
          : undefined,
        riskRules: parseListText(values.riskRules ?? ""),
        status: values.status?.trim() || undefined
      };
      return api.visualCategoryPolicies.create(payload);
    },
    onSuccess: async () => {
      message.success("Policy submitted to backend");
      form.resetFields();
      await queryClient.invalidateQueries({ queryKey: ["visual", "category-policies"] });
    }
  });

  const confirmMutation = useMutation({
    mutationFn: (id: number | string) => api.visualCategoryPolicies.confirm(id),
    onSuccess: async () => {
      message.success("Policy confirmed");
      await queryClient.invalidateQueries({ queryKey: ["visual", "category-policies"] });
    }
  });

  const rows = query.data?.data ?? [];

  const summary = useMemo(
    () => [
      { label: "Policies", value: String(rows.length) },
      { label: "Category", value: filters.categoryCode || "--" },
      { label: "Status", value: filters.status || "--" }
    ],
    [filters.categoryCode, filters.status, rows.length]
  );

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <Card title="类目视觉策略">
        <Space wrap>
          {summary.map((item) => (
            <Tag key={item.label} color="blue">
              {item.label}: {item.value}
            </Tag>
          ))}
        </Space>
        <Paragraph style={{ marginTop: 12, marginBottom: 0 }}>
          Connected to real <Text code>/api/v1/category-visual-policies</Text> endpoints.
        </Paragraph>
      </Card>

      <Card title="New Policy">
        <Form form={form} layout="vertical" onFinish={(values) => void createMutation.mutateAsync(values)}>
          <Space direction="vertical" size={12} style={{ width: "100%" }}>
            <Space wrap style={{ width: "100%" }}>
              <Form.Item name="categoryCode" label="类目编码" rules={[{ required: true, message: "Required" }]} style={{ flex: 1, minWidth: 220 }}>
                <Input placeholder="e.g. WOMEN_DRESS" />
              </Form.Item>
              <Form.Item name="categoryName" label="类目名称" rules={[{ required: true, message: "Required" }]} style={{ flex: 1, minWidth: 220 }}>
                <Input placeholder="e.g. Dresses" />
              </Form.Item>
            </Space>
            <Space wrap style={{ width: "100%" }}>
              <Form.Item name="modelPolicy" label="模型策略" style={{ flex: 1, minWidth: 180 }}>
                <Select allowClear options={[{ label: "REQUIRED", value: "REQUIRED" }, { label: "OPTIONAL", value: "OPTIONAL" }, { label: "FORBIDDEN", value: "FORBIDDEN" }]} />
              </Form.Item>
              <Form.Item name="modelConsistencyLevel" label="Consistency Level" style={{ flex: 1, minWidth: 180 }}>
                <Select allowClear options={[{ label: "STRICT", value: "STRICT" }, { label: "LOOSE", value: "LOOSE" }, { label: "NONE", value: "NONE" }]} />
              </Form.Item>
              <Form.Item name="status" label="Status" style={{ flex: 1, minWidth: 180 }}>
                <Select allowClear options={statusOptions} />
              </Form.Item>
            </Space>
            <Form.Item name="allowedShotTypes" label="Allowed Shot Types">
              <TextArea rows={2} placeholder="One per line or comma-separated" />
            </Form.Item>
            <Form.Item name="riskRules" label="Risk Rules">
              <TextArea rows={2} placeholder="One risk rule per line" />
            </Form.Item>
            <Form.Item name="requiredMainImages" label="Main Images JSON">
              <TextArea rows={4} placeholder='{"1:1": 5, "3:4": 5}' />
            </Form.Item>
            <Form.Item name="detailScreenCountRange" label="Detail Screen Range JSON">
              <TextArea rows={4} placeholder='{"min": 6, "max": 10}' />
            </Form.Item>
            {createMutation.isError ? <Alert showIcon type="error" message="Submit failed" description={createMutation.error instanceof Error ? createMutation.error.message : "Save failed"} /> : null}
            <Space wrap>
              <Button type="primary" htmlType="submit" loading={createMutation.isPending}>
                Save Policy
              </Button>
              <Button onClick={() => form.resetFields()} disabled={createMutation.isPending}>
                Reset
              </Button>
            </Space>
          </Space>
        </Form>
      </Card>

      <Card
        title="Policy List"
        extra={
          <Space wrap>
            <Input
              allowClear
              placeholder="Category Code"
              value={filters.categoryCode}
              onChange={(event) => setFilters((value) => ({ ...value, categoryCode: event.target.value }))}
              style={{ width: 160 }}
            />
            <Select
              allowClear
              placeholder="Status"
              value={filters.status}
              onChange={(value) => setFilters((current) => ({ ...current, status: value }))}
              options={statusOptions}
              style={{ width: 140 }}
            />
            <Button onClick={() => void query.refetch()} loading={query.isFetching}>
              Refresh
            </Button>
          </Space>
        }
      >
        {query.isError ? (
          <ErrorState
            title="Load failed"
            description={query.error instanceof Error ? query.error.message : "Cannot read policies"}
            onRetry={() => void query.refetch()}
          />
        ) : query.isPending ? (
          <LoadingState title="Loading policies" description="GET /api/v1/category-visual-policies/list" />
        ) : rows.length === 0 ? (
          <EmptyState title="No policies yet" description="Backend returned empty list." />
        ) : (
          <Table
            rowKey={(record) => String(record.id ?? record.categoryCode ?? Math.random())}
            columns={[...policyColumns, {
              title: "Action",
              key: "action",
              width: 100,
              render: (_: unknown, record: CategoryVisualPolicy) => (
                <Button
                  type="link"
                  size="small"
                  disabled={!record.id || record.status !== "DRAFT" || confirmMutation.isPending}
                  onClick={() => { if (record.id) void confirmMutation.mutateAsync(record.id); }}
                >
                  Confirm
                </Button>
              )
            }]}
            dataSource={rows}
            pagination={false}
            scroll={{ x: 1200 }}
          />
        )}
      </Card>
    </Space>
  );
}
