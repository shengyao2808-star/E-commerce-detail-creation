import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Button, Card, Form, Input, InputNumber, Select, Space, Table, Tabs, Tag, Typography, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useState } from "react";
import { EmptyState, ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";
import type { ModelProfile, SkcPolicy } from "../../services/types";
import { formatDateTime, parseListText, textFromUnknown } from "./visualUtils";

const { TextArea } = Input;
const { Paragraph, Text } = Typography;

const lifecycleStatusOptions = [
  { label: "草稿", value: "DRAFT" },
  { label: "已确认", value: "CONFIRMED" },
  { label: "已归档", value: "ARCHIVED" }
];

const modelColumns: ColumnsType<ModelProfile> = [
  { title: "显示名称", dataIndex: "displayName", render: (v) => v ?? "--" },
  { title: "版本", dataIndex: "version", width: 80, render: (v) => v ?? "--" },
  { title: "授权状态", dataIndex: "authorizationStatus", width: 120, render: (v) => v ?? "--" },
  { title: "状态", dataIndex: "status", width: 100, render: (v) => <Tag>{v ?? "--"}</Tag> },
  { title: "类目范围", dataIndex: "categoryScopes", render: (v) => textFromUnknown(v) },
  { title: "风格标签", dataIndex: "styleTags", render: (v) => textFromUnknown(v) },
  { title: "更新时间", dataIndex: "updateTime", width: 180, render: (v) => formatDateTime(v) }
];

const skcColumns: ColumnsType<SkcPolicy> = [
  { title: "策略名称", dataIndex: "name", render: (v) => v ?? "--" },
  { title: "类目", dataIndex: "categoryCode", width: 140, render: (v) => v ?? "--" },
  { title: "颜色数", dataIndex: "colorCount", width: 80, render: (v) => v ?? "--" },
  { title: "规格数", dataIndex: "specCount", width: 80, render: (v) => v ?? "--" },
  { title: "渲染模式", dataIndex: "renderMode", width: 120, render: (v) => v ?? "--" },
  { title: "状态", dataIndex: "status", width: 100, render: (v) => <Tag>{v ?? "--"}</Tag> },
  { title: "更新时间", dataIndex: "updateTime", width: 180, render: (v) => formatDateTime(v) }
];

function ModelProfilesPanel() {
  const [form] = Form.useForm();
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<{ displayName?: string; status?: string }>({});

  const query = useQuery({
    queryKey: ["visual", "model-profiles", filters],
    queryFn: () => api.visualModelProfiles.list({ pageNum: 1, pageSize: 20, displayName: filters.displayName, status: filters.status })
  });

  const mutation = useMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const payload: ModelProfile = {
        displayName: String(values.displayName ?? "").trim(),
        height: values.height as number | undefined,
        weight: values.weight as number | undefined,
        styleTags: parseListText(String(values.styleTags ?? "")),
        categoryScopes: parseListText(String(values.categoryScopes ?? "")),
        authorizationStatus: values.authorizationStatus ? String(values.authorizationStatus).trim() : undefined
      };
      return api.visualModelProfiles.create(payload);
    },
    onSuccess: async () => {
      message.success("Model profile submitted");
      form.resetFields();
      await queryClient.invalidateQueries({ queryKey: ["visual", "model-profiles"] });
    }
  });

  const confirmMutation = useMutation({
    mutationFn: (id: number | string) => api.visualModelProfiles.confirm(id),
    onSuccess: async () => {
      message.success("Model profile confirmed");
      await queryClient.invalidateQueries({ queryKey: ["visual", "model-profiles"] });
    }
  });

  const rows = query.data?.data ?? [];

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <Card title="模型档案">
        <Paragraph>Connected to <Text code>/api/v1/model-profiles</Text></Paragraph>
      </Card>
      <Card title="New Profile">
        <Form form={form} layout="vertical" onFinish={(values) => void mutation.mutateAsync(values)}>
          <Space direction="vertical" size={12} style={{ width: "100%" }}>
            <Form.Item name="displayName" label="显示名称" rules={[{ required: true }]}>
              <Input placeholder="e.g. Model A" />
            </Form.Item>
            <Space wrap>
              <Form.Item name="height" label="Height (cm)"><InputNumber style={{ width: 120 }} /></Form.Item>
              <Form.Item name="weight" label="Weight (kg)"><InputNumber style={{ width: 120 }} /></Form.Item>
            </Space>
            <Form.Item name="styleTags" label="风格标签"><TextArea rows={2} placeholder="clean, premium" /></Form.Item>
            <Form.Item name="categoryScopes" label="类目范围"><TextArea rows={2} placeholder="women-dress" /></Form.Item>
            {mutation.isError ? <Alert showIcon type="error" message="Failed" description={mutation.error instanceof Error ? mutation.error.message : "Error"} /> : null}
            <Space><Button type="primary" htmlType="submit" loading={mutation.isPending}>Save</Button><Button onClick={() => form.resetFields()}>Reset</Button></Space>
          </Space>
        </Form>
      </Card>
      <Card title="Profile List" extra={<Space><Input allowClear placeholder="Name" value={filters.displayName} onChange={(e) => setFilters((v) => ({ ...v, displayName: e.target.value }))} style={{ width: 140 }} /><Select allowClear placeholder="Status" value={filters.status} onChange={(v) => setFilters((c) => ({ ...c, status: v }))} options={lifecycleStatusOptions} style={{ width: 120 }} /><Button onClick={() => void query.refetch()} loading={query.isFetching}>Refresh</Button></Space>}>
        {query.isError ? <ErrorState title="Load failed" description={query.error instanceof Error ? query.error.message : "Error"} onRetry={() => void query.refetch()} /> : query.isPending ? <LoadingState title="Loading" description="GET /api/v1/model-profiles/list" /> : rows.length === 0 ? <EmptyState title="No profiles" description="Empty list" /> : (
          <Table rowKey={(r) => String(r.id ?? Math.random())} columns={[...modelColumns, { title: "Action", key: "action", width: 100, render: (_: unknown, r: ModelProfile) => <Button type="link" size="small" disabled={!r.id || r.status !== "DRAFT" || confirmMutation.isPending} onClick={() => { if (r.id) void confirmMutation.mutateAsync(r.id); }}>Confirm</Button> }]} dataSource={rows} pagination={false} scroll={{ x: 1200 }} />
        )}
      </Card>
    </Space>
  );
}

function SkcPoliciesPanel() {
  const [form] = Form.useForm();
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<{ categoryCode?: string; status?: string }>({});

  const query = useQuery({
    queryKey: ["visual", "skc-policies", filters],
    queryFn: () => api.visualSkcPolicies.list({ pageNum: 1, pageSize: 20, categoryCode: filters.categoryCode, status: filters.status })
  });

  const mutation = useMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const payload: SkcPolicy = {
        policyName: String(values.policyName ?? "").trim(),
        categoryCode: values.categoryCode ? String(values.categoryCode).trim() : undefined,
        colorCount: values.colorCount as number | undefined,
        specCount: values.specCount as number | undefined,
        renderMode: values.renderMode ? String(values.renderMode) : undefined
      };
      return api.visualSkcPolicies.create(payload);
    },
    onSuccess: async () => {
      message.success("SKC policy submitted");
      form.resetFields();
      await queryClient.invalidateQueries({ queryKey: ["visual", "skc-policies"] });
    }
  });

  const confirmMutation = useMutation({
    mutationFn: (id: number | string) => api.visualSkcPolicies.confirm(id),
    onSuccess: async () => {
      message.success("SKC policy confirmed");
      await queryClient.invalidateQueries({ queryKey: ["visual", "skc-policies"] });
    }
  });

  const rows = query.data?.data ?? [];

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <Card title="SKC 策略"><Paragraph>Connected to <Text code>/api/v1/skc-policies</Text></Paragraph></Card>
      <Card title="New SKC Policy">
        <Form form={form} layout="vertical" onFinish={(values) => void mutation.mutateAsync(values)}>
          <Space direction="vertical" size={12} style={{ width: "100%" }}>
            <Form.Item name="policyName" label="Policy Name" rules={[{ required: true }]}><Input /></Form.Item>
            <Space wrap>
              <Form.Item name="categoryCode" label="Category"><Input style={{ width: 200 }} /></Form.Item>
              <Form.Item name="colorCount" label="Colors"><InputNumber style={{ width: 100 }} /></Form.Item>
              <Form.Item name="specCount" label="Specs"><InputNumber style={{ width: 100 }} /></Form.Item>
              <Form.Item name="renderMode" label="渲染模式"><Select allowClear options={[{ label: "MODEL", value: "MODEL" }, { label: "FLAT_LAY", value: "FLAT_LAY" }, { label: "REAL_PRODUCT", value: "REAL_PRODUCT" }, { label: "MIXED", value: "MIXED" }]} style={{ width: 160 }} /></Form.Item>
            </Space>
            {mutation.isError ? <Alert showIcon type="error" message="Failed" description={mutation.error instanceof Error ? mutation.error.message : "Error"} /> : null}
            <Space><Button type="primary" htmlType="submit" loading={mutation.isPending}>Save</Button><Button onClick={() => form.resetFields()}>Reset</Button></Space>
          </Space>
        </Form>
      </Card>
      <Card title="Policy List" extra={<Space><Input allowClear placeholder="Category" value={filters.categoryCode} onChange={(e) => setFilters((v) => ({ ...v, categoryCode: e.target.value }))} style={{ width: 140 }} /><Select allowClear placeholder="Status" value={filters.status} onChange={(v) => setFilters((c) => ({ ...c, status: v }))} options={lifecycleStatusOptions} style={{ width: 120 }} /><Button onClick={() => void query.refetch()} loading={query.isFetching}>Refresh</Button></Space>}>
        {query.isError ? <ErrorState title="Load failed" description={query.error instanceof Error ? query.error.message : "Error"} onRetry={() => void query.refetch()} /> : query.isPending ? <LoadingState title="Loading" description="GET /api/v1/skc-policies/list" /> : rows.length === 0 ? <EmptyState title="No policies" description="Empty list" /> : (
          <Table rowKey={(r) => String(r.id ?? Math.random())} columns={[...skcColumns, { title: "Action", key: "action", width: 100, render: (_: unknown, r: SkcPolicy) => <Button type="link" size="small" disabled={!r.id || r.status !== "DRAFT" || confirmMutation.isPending} onClick={() => { if (r.id) void confirmMutation.mutateAsync(r.id); }}>Confirm</Button> }]} dataSource={rows} pagination={false} scroll={{ x: 1100 }} />
        )}
      </Card>
    </Space>
  );
}

export default function ModelProfilesPage() {
  return <Tabs items={[{ key: "profiles", label: "模型档案", children: <ModelProfilesPanel /> }, { key: "skc", label: "SKC 策略", children: <SkcPoliciesPanel /> }]} />;
}
