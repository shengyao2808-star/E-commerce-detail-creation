import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Button,
  Card,
  Col,
  Drawer,
  Form,
  Input,
  message,
  Modal,
  Popconfirm,
  Row,
  Select,
  Space,
  Tag,
  Typography
} from "antd";
import { useCallback, useState } from "react";
import { api } from "../../services/api";
import type { PromptTemplate, PromptTemplateCreateRequest } from "../../services/types";

const { TextArea } = Input;
const { Paragraph, Text } = Typography;

const CATEGORIES = [
  { value: "PRODUCT_MAIN", label: "Product Main" },
  { value: "DETAIL_SCENE", label: "Detail Scene" },
  { value: "MODEL_SHOT", label: "Model Shot" },
  { value: "FLAT_LAY", label: "Flat Lay" },
  { value: "BACKGROUND", label: "Background" },
  { value: "LIFESTYLE", label: "Lifestyle" },
  { value: "BRAND_STORY", label: "Brand Story" }
];

const PLATFORMS = ["TAOBAO", "JD", "PINDUODUO", "DOUYIN", "AMAZON", "SHOPIFY", "GENERAL"];
const STYLES = ["MINIMALIST", "LUXURY", "CUTE", "TECH", "VINTAGE", "FRESH", "DARK", "BRIGHT"];
const SOURCES = ["SYSTEM", "COMMUNITY", "CUSTOM", "GITHUB"];

const categoryColor: Record<string, string> = {
  PRODUCT_MAIN: "blue",
  DETAIL_SCENE: "green",
  MODEL_SHOT: "purple",
  FLAT_LAY: "orange",
  BACKGROUND: "cyan",
  LIFESTYLE: "magenta",
  BRAND_STORY: "gold"
};

export default function PromptTemplatePage() {
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<{
    category?: string;
    platform?: string;
    style?: string;
    source?: string;
    keyword?: string;
  }>({});
  const [pageNum, setPageNum] = useState(1);
  const [detailDrawer, setDetailDrawer] = useState<PromptTemplate | null>(null);
  const [editModal, setEditModal] = useState<{ open: boolean; editing?: PromptTemplate }>({ open: false });
  const [createForm] = Form.useForm<PromptTemplateCreateRequest>();

  const { data, isLoading } = useQuery({
    queryKey: ["prompt-templates", filters, pageNum],
    queryFn: () => api.promptTemplates.list({ ...filters, pageNum, pageSize: 20 })
  });

  const createMutation = useMutation({
    mutationFn: (values: PromptTemplateCreateRequest) =>
      editModal.editing
        ? api.promptTemplates.update(editModal.editing.id!, values)
        : api.promptTemplates.create(values),
    onSuccess: () => {
      message.success(editModal.editing ? "Template updated" : "Template created");
      setEditModal({ open: false });
      createForm.resetFields();
      queryClient.invalidateQueries({ queryKey: ["prompt-templates"] });
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.promptTemplates.delete(id),
    onSuccess: () => {
      message.success("Template deleted");
      queryClient.invalidateQueries({ queryKey: ["prompt-templates"] });
    }
  });

  const duplicateMutation = useMutation({
    mutationFn: (id: number) => api.promptTemplates.duplicate(id),
    onSuccess: () => {
      message.success("Template duplicated");
      queryClient.invalidateQueries({ queryKey: ["prompt-templates"] });
    }
  });

  const openEdit = useCallback(
    (template?: PromptTemplate) => {
      if (template) {
        createForm.setFieldsValue({
          templateName: template.templateName,
          category: template.category,
          sceneType: template.sceneType,
          platform: template.platform,
          style: template.style,
          positivePrompt: template.positivePrompt,
          negativePrompt: template.negativePrompt,
          styleTags: template.styleTags,
          constraints: template.constraints,
          description: template.description,
          language: template.language,
          author: template.author,
          tags: template.tags
        });
      } else {
        createForm.resetFields();
      }
      setEditModal({ open: true, editing: template });
    },
    [createForm]
  );

  const templates: PromptTemplate[] = data?.data ?? [];

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <Card title="Prompt Template Library">
        <Space wrap style={{ marginBottom: 16, width: "100%" }}>
          <Select
            allowClear
            placeholder="Category"
            style={{ width: 160 }}
            options={CATEGORIES}
            value={filters.category}
            onChange={(v) => { setFilters((f) => ({ ...f, category: v })); setPageNum(1); }}
          />
          <Select
            allowClear
            placeholder="Platform"
            style={{ width: 140 }}
            options={PLATFORMS.map((p) => ({ value: p, label: p }))}
            value={filters.platform}
            onChange={(v) => { setFilters((f) => ({ ...f, platform: v })); setPageNum(1); }}
          />
          <Select
            allowClear
            placeholder="Style"
            style={{ width: 140 }}
            options={STYLES.map((s) => ({ value: s, label: s }))}
            value={filters.style}
            onChange={(v) => { setFilters((f) => ({ ...f, style: v })); setPageNum(1); }}
          />
          <Select
            allowClear
            placeholder="Source"
            style={{ width: 140 }}
            options={SOURCES.map((s) => ({ value: s, label: s }))}
            value={filters.source}
            onChange={(v) => { setFilters((f) => ({ ...f, source: v })); setPageNum(1); }}
          />
          <Input.Search
            placeholder="Search templates..."
            style={{ width: 240 }}
            allowClear
            onSearch={(v) => { setFilters((f) => ({ ...f, keyword: v || undefined })); setPageNum(1); }}
          />
          <Button type="primary" onClick={() => openEdit()}>
            + New Template
          </Button>
        </Space>

        {isLoading ? (
          <div style={{ textAlign: "center", padding: 40 }}>Loading...</div>
        ) : templates.length === 0 ? (
          <div style={{ textAlign: "center", padding: 40, color: "#999" }}>No templates found</div>
        ) : (
          <Row gutter={[16, 16]}>
            {templates.map((t) => (
              <Col key={t.id} xs={24} sm={12} md={8} lg={6}>
                <Card
                  hoverable
                  size="small"
                  style={{ height: "100%" }}
                  onClick={() => setDetailDrawer(t)}
                  actions={[
                    <span key="use" onClick={(e) => { e.stopPropagation(); api.promptTemplates.use(t.id!).then(() => message.success("Usage recorded")); }}>
                      Use
                    </span>,
                    <span key="dup" onClick={(e) => { e.stopPropagation(); duplicateMutation.mutate(t.id!); }}>
                      Duplicate
                    </span>,
                    <span key="edit" onClick={(e) => { e.stopPropagation(); openEdit(t); }}>
                      Edit
                    </span>
                  ]}
                >
                  <Space direction="vertical" size={4} style={{ width: "100%" }}>
                    <Text strong ellipsis style={{ fontSize: 13 }}>
                      {t.templateName}
                    </Text>
                    <Space size={4} wrap>
                      {t.category && <Tag color={categoryColor[t.category] ?? "default"}>{t.category}</Tag>}
                      {t.platform && <Tag>{t.platform}</Tag>}
                      {t.style && <Tag>{t.style}</Tag>}
                    </Space>
                    <Paragraph
                      ellipsis={{ rows: 2 }}
                      style={{ fontSize: 12, color: "#666", marginBottom: 0, minHeight: 36 }}
                    >
                      {t.positivePrompt}
                    </Paragraph>
                    <Space size={8}>
                      <Text type="secondary" style={{ fontSize: 11 }}>
                        Used: {t.usageCount ?? 0}
                      </Text>
                      <Text type="secondary" style={{ fontSize: 11 }}>
                        Rating: {t.rating ?? 0}
                      </Text>
                      <Text type="secondary" style={{ fontSize: 11 }}>
                        {t.source}
                      </Text>
                    </Space>
                  </Space>
                </Card>
              </Col>
            ))}
          </Row>
        )}

        {data && data.total > 20 && (
          <div style={{ textAlign: "center", marginTop: 16 }}>
            <Space>
              <Button disabled={pageNum <= 1} onClick={() => setPageNum((p) => p - 1)}>
                Previous
              </Button>
              <Text>
                Page {pageNum} / {Math.ceil((data.total ?? 0) / 20)}
              </Text>
              <Button
                disabled={pageNum * 20 >= (data.total ?? 0)}
                onClick={() => setPageNum((p) => p + 1)}
              >
                Next
              </Button>
            </Space>
          </div>
        )}
      </Card>

      {/* Detail Drawer */}
      <Drawer
        title={detailDrawer?.templateName ?? "Template Detail"}
        open={!!detailDrawer}
        onClose={() => setDetailDrawer(null)}
        width={560}
      >
        {detailDrawer && (
          <Space direction="vertical" size={12} style={{ width: "100%" }}>
            <Space wrap>
              {detailDrawer.category && <Tag color={categoryColor[detailDrawer.category] ?? "default"}>{detailDrawer.category}</Tag>}
              {detailDrawer.platform && <Tag>{detailDrawer.platform}</Tag>}
              {detailDrawer.style && <Tag>{detailDrawer.style}</Tag>}
              {detailDrawer.sceneType && <Tag>{detailDrawer.sceneType}</Tag>}
              {detailDrawer.source && <Tag color="geekblue">{detailDrawer.source}</Tag>}
            </Space>
            {detailDrawer.description && <Paragraph>{detailDrawer.description}</Paragraph>}
            <Card size="small" title="Positive Prompt">
              <Paragraph style={{ whiteSpace: "pre-wrap", marginBottom: 0 }}>
                {detailDrawer.positivePrompt}
              </Paragraph>
            </Card>
            {detailDrawer.negativePrompt && (
              <Card size="small" title="Negative Prompt">
                <Paragraph style={{ whiteSpace: "pre-wrap", marginBottom: 0 }}>
                  {detailDrawer.negativePrompt}
                </Paragraph>
              </Card>
            )}
            {detailDrawer.styleTags && detailDrawer.styleTags.length > 0 && (
              <div>
                <Text strong>Style Tags: </Text>
                {detailDrawer.styleTags.map((tag) => (
                  <Tag key={tag}>{tag}</Tag>
                ))}
              </div>
            )}
            {detailDrawer.constraints && detailDrawer.constraints.length > 0 && (
              <div>
                <Text strong>Constraints: </Text>
                {detailDrawer.constraints.map((c) => (
                  <Tag key={c}>{c}</Tag>
                ))}
              </div>
            )}
            {detailDrawer.tags && detailDrawer.tags.length > 0 && (
              <div>
                <Text strong>Tags: </Text>
                {detailDrawer.tags.map((tag) => (
                  <Tag key={tag} color="blue">{tag}</Tag>
                ))}
              </div>
            )}
            <Space split="|">
              <Text type="secondary">Used: {detailDrawer.usageCount ?? 0}</Text>
              <Text type="secondary">Rating: {detailDrawer.rating ?? 0}</Text>
              <Text type="secondary">Lang: {detailDrawer.language}</Text>
              {detailDrawer.author && <Text type="secondary">By: {detailDrawer.author}</Text>}
            </Space>
            {detailDrawer.sourceRef && (
              <Text type="secondary" style={{ fontSize: 12 }}>
                Source: {detailDrawer.sourceRef}
              </Text>
            )}
            <Space>
              <Popconfirm title="Delete this template?" onConfirm={() => { deleteMutation.mutate(detailDrawer.id!); setDetailDrawer(null); }}>
                <Button danger size="small">Delete</Button>
              </Popconfirm>
            </Space>
          </Space>
        )}
      </Drawer>

      {/* Create / Edit Modal */}
      <Modal
        title={editModal.editing ? "Edit Template" : "New Template"}
        open={editModal.open}
        onCancel={() => setEditModal({ open: false })}
        onOk={() => createForm.submit()}
        confirmLoading={createMutation.isPending}
        width={680}
      >
        <Form form={createForm} layout="vertical" onFinish={(v) => createMutation.mutate(v)}>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="templateName" label="Name" rules={[{ required: true }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="category" label="Category" rules={[{ required: true }]}>
                <Select options={CATEGORIES} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="platform" label="Platform">
                <Select allowClear options={PLATFORMS.map((p) => ({ value: p, label: p }))} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="style" label="Style">
                <Select allowClear options={STYLES.map((s) => ({ value: s, label: s }))} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="sceneType" label="Scene Type">
                <Input placeholder="INDOOR / STUDIO..." />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="positivePrompt" label="Positive Prompt" rules={[{ required: true }]}>
            <TextArea rows={4} />
          </Form.Item>
          <Form.Item name="negativePrompt" label="Negative Prompt">
            <TextArea rows={3} />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="description" label="Description">
                <TextArea rows={2} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="tags" label="Tags">
                <Select mode="tags" placeholder="Enter tags" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="author" label="Author">
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="language" label="Language" initialValue="zh-CN">
                <Input />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </Space>
  );
}