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
import { useLang } from "../../i18n";
import type { PromptTemplate, PromptTemplateCreateRequest } from "../../services/types";

const { TextArea } = Input;
const { Paragraph, Text } = Typography;

const CATEGORIES = [
  { value: "PRODUCT_MAIN", label: "商品主图" },
  { value: "DETAIL_SCENE", label: "详情场景" },
  { value: "MODEL_SHOT", label: "模特拍摄" },
  { value: "FLAT_LAY", label: "平铺拍摄" },
  { value: "BACKGROUND", label: "背景" },
  { value: "LIFESTYLE", label: "生活方式" },
  { value: "BRAND_STORY", label: "品牌故事" }
];

const PLATFORMS = ["淘宝", "京东", "拼多多", "抖音", "亚马逊", "Shopify", "通用"];
const STYLES = ["极简", "奢华", "可爱", "科技", "复古", "清新", "暗黑", "明亮"];
const SOURCES = ["系统", "社区", "自定义", "GitHub"];

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
  const { t } = useLang();
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
      message.success(editModal.editing ? t("common.success") : t("common.success"));
      setEditModal({ open: false });
      createForm.resetFields();
      queryClient.invalidateQueries({ queryKey: ["prompt-templates"] });
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.promptTemplates.delete(id),
    onSuccess: () => {
      message.success(t("common.success"));
      queryClient.invalidateQueries({ queryKey: ["prompt-templates"] });
    }
  });

  const duplicateMutation = useMutation({
    mutationFn: (id: number) => api.promptTemplates.duplicate(id),
    onSuccess: () => {
      message.success(t("common.success"));
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
      <Card title={t("template.title")}>
        <Space wrap style={{ marginBottom: 16, width: "100%" }}>
          <Select
            allowClear
            placeholder={t("template.filter.category")}
            style={{ width: 160 }}
            options={CATEGORIES}
            value={filters.category}
            onChange={(v) => { setFilters((f) => ({ ...f, category: v })); setPageNum(1); }}
          />
          <Select
            allowClear
            placeholder={t("template.filter.platform")}
            style={{ width: 140 }}
            options={PLATFORMS.map((p) => ({ value: p, label: p }))}
            value={filters.platform}
            onChange={(v) => { setFilters((f) => ({ ...f, platform: v })); setPageNum(1); }}
          />
          <Select
            allowClear
            placeholder={t("template.filter.style")}
            style={{ width: 140 }}
            options={STYLES.map((s) => ({ value: s, label: s }))}
            value={filters.style}
            onChange={(v) => { setFilters((f) => ({ ...f, style: v })); setPageNum(1); }}
          />
          <Select
            allowClear
            placeholder={t("template.filter.source")}
            style={{ width: 140 }}
            options={SOURCES.map((s) => ({ value: s, label: s }))}
            value={filters.source}
            onChange={(v) => { setFilters((f) => ({ ...f, source: v })); setPageNum(1); }}
          />
          <Input.Search
            placeholder={t("template.search")}
            style={{ width: 220 }}
            value={filters.keyword}
            onChange={(e) => setFilters((f) => ({ ...f, keyword: e.target.value }))}
            onSearch={() => setPageNum(1)}
          />
          <Button type="primary" onClick={() => openEdit()}>
            {t("template.create")}
          </Button>
        </Space>

        {isLoading ? (
          <div style={{ textAlign: "center", padding: 40 }}>{t("common.loading")}</div>
        ) : templates.length === 0 ? (
          <div style={{ textAlign: "center", padding: 40 }}>
            <div>{t("template.noTemplates")}</div>
            <div style={{ color: "#999", marginTop: 8 }}>{t("template.noTemplates.desc")}</div>
          </div>
        ) : (
          <Row gutter={[16, 16]}>
            {templates.map((template) => (
              <Col key={template.id} xs={24} sm={12} md={8} lg={6}>
                <Card
                  size="small"
                  hoverable
                  onClick={() => setDetailDrawer(template)}
                  actions={[
                    <Button key="edit" type="link" size="small" onClick={(e) => { e.stopPropagation(); openEdit(template); }}>
                      {t("template.edit")}
                    </Button>,
                    <Button key="duplicate" type="link" size="small" onClick={(e) => { e.stopPropagation(); duplicateMutation.mutate(template.id!); }}>
                      {t("template.duplicate")}
                    </Button>,
                    <Popconfirm key="delete" title={t("template.deleteConfirm")} onConfirm={() => deleteMutation.mutate(template.id!)}>
                      <Button type="link" size="small" danger onClick={(e) => e.stopPropagation()}>
                        {t("template.delete")}
                      </Button>
                    </Popconfirm>
                  ]}
                >
                  <div style={{ marginBottom: 8 }}>
                    <Text strong>{template.templateName}</Text>
                  </div>
                  <div style={{ marginBottom: 8 }}>
                    <Tag color={template.category ? categoryColor[template.category] : "default"}>
                      {CATEGORIES.find((c) => c.value === template.category)?.label ?? template.category}
                    </Tag>
                    {template.platform && <Tag>{template.platform}</Tag>}
                    {template.style && <Tag>{template.style}</Tag>}
                  </div>
                  <Paragraph
                    type="secondary"
                    ellipsis={{ rows: 2 }}
                    style={{ marginBottom: 8, fontSize: 12 }}
                  >
                    {template.description || template.positivePrompt}
                  </Paragraph>
                  <Space split="|">
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {t("template.usage")}: {template.usageCount ?? 0}
                    </Text>
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {t("template.rating")}: {template.rating ?? 0}
                    </Text>
                  </Space>
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </Card>

      {/* Detail Drawer */}
      <Drawer
        title={detailDrawer?.templateName}
        open={!!detailDrawer}
        onClose={() => setDetailDrawer(null)}
        width={480}
      >
        {detailDrawer && (
          <Space direction="vertical" size={16} style={{ width: "100%" }}>
            <div>
              <Tag color={detailDrawer.category ? categoryColor[detailDrawer.category] : "default"}>
                {CATEGORIES.find((c) => c.value === detailDrawer.category)?.label ?? detailDrawer.category}
              </Tag>
              {detailDrawer.platform && <Tag>{detailDrawer.platform}</Tag>}
              {detailDrawer.style && <Tag>{detailDrawer.style}</Tag>}
              {detailDrawer.source && <Tag>{detailDrawer.source}</Tag>}
            </div>
            {detailDrawer.description && <Paragraph>{detailDrawer.description}</Paragraph>}
            <Card size="small" title={t("template.positivePrompt")}>
              <Paragraph style={{ whiteSpace: "pre-wrap", marginBottom: 0 }}>
                {detailDrawer.positivePrompt}
              </Paragraph>
            </Card>
            {detailDrawer.negativePrompt && (
              <Card size="small" title={t("template.negativePrompt")}>
                <Paragraph style={{ whiteSpace: "pre-wrap", marginBottom: 0 }}>
                  {detailDrawer.negativePrompt}
                </Paragraph>
              </Card>
            )}
            {detailDrawer.styleTags && detailDrawer.styleTags.length > 0 && (
              <div>
                <Text strong>{t("template.styleTags")}: </Text>
                {detailDrawer.styleTags.map((tag) => (
                  <Tag key={tag}>{tag}</Tag>
                ))}
              </div>
            )}
            {detailDrawer.constraints && detailDrawer.constraints.length > 0 && (
              <div>
                <Text strong>{t("template.constraints")}: </Text>
                {detailDrawer.constraints.map((c) => (
                  <Tag key={c}>{c}</Tag>
                ))}
              </div>
            )}
            {detailDrawer.tags && detailDrawer.tags.length > 0 && (
              <div>
                <Text strong>{t("template.tags")}: </Text>
                {detailDrawer.tags.map((tag) => (
                  <Tag key={tag} color="blue">{tag}</Tag>
                ))}
              </div>
            )}
            <Space split="|">
              <Text type="secondary">{t("template.usage")}: {detailDrawer.usageCount ?? 0}</Text>
              <Text type="secondary">{t("template.rating")}: {detailDrawer.rating ?? 0}</Text>
              <Text type="secondary">{t("template.language")}: {detailDrawer.language}</Text>
              {detailDrawer.author && <Text type="secondary">{t("template.author")}: {detailDrawer.author}</Text>}
            </Space>
            {detailDrawer.sourceRef && (
              <Text type="secondary" style={{ fontSize: 12 }}>
                {t("template.source")}: {detailDrawer.sourceRef}
              </Text>
            )}
            <Space>
              <Popconfirm title={t("template.deleteConfirm")} onConfirm={() => { deleteMutation.mutate(detailDrawer.id!); setDetailDrawer(null); }}>
                <Button danger size="small">{t("template.delete")}</Button>
              </Popconfirm>
            </Space>
          </Space>
        )}
      </Drawer>

      {/* Create / Edit Modal */}
      <Modal
        title={editModal.editing ? t("template.edit") : t("template.create")}
        open={editModal.open}
        onCancel={() => setEditModal({ open: false })}
        onOk={() => createForm.submit()}
        confirmLoading={createMutation.isPending}
        width={680}
      >
        <Form form={createForm} layout="vertical" onFinish={(v) => createMutation.mutate(v)}>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="templateName" label={t("template.name")} rules={[{ required: true }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="category" label={t("template.category")} rules={[{ required: true }]}>
                <Select options={CATEGORIES} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="platform" label={t("template.platform")}>
                <Select allowClear options={PLATFORMS.map((p) => ({ value: p, label: p }))} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="style" label={t("template.style")}>
                <Select allowClear options={STYLES.map((s) => ({ value: s, label: s }))} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="sceneType" label="场景类型">
                <Input placeholder="室内 / 棚拍..." />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="positivePrompt" label="正向提示词" rules={[{ required: true }]}>
            <TextArea rows={4} />
          </Form.Item>
          <Form.Item name="negativePrompt" label="反向提示词">
            <TextArea rows={3} />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="description" label="描述">
                <TextArea rows={2} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="tags" label="标签">
                <Select mode="tags" placeholder="输入标签" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="author" label="作者">
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="language" label="语言" initialValue="zh-CN">
                <Input />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </Space>
  );
}