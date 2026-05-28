import { Button, Card, Form, Input, InputNumber, Space, Typography, message } from "antd";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ApiUnavailableState, ToolUnavailableState } from "../../components/common";
import { api } from "../../services/api";
import { useWorkbenchStore } from "../../stores/workbenchStore";

const { TextArea } = Input;
const { Paragraph, Text, Title } = Typography;

type MaterialForm = {
  productName: string;
  brandName?: string;
  category?: string;
  sku?: string;
  uploader?: string;
  price?: number;
  description?: string;
};

export default function MaterialCreatePage() {
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();
  const [form] = Form.useForm<MaterialForm>();
  const setCurrentProduct = useWorkbenchStore((state) => state.setCurrentProduct);

  const submit = async (values: MaterialForm) => {
    setSubmitting(true);
    try {
      const id = await api.material.upload({
        ...values,
        images: [],
        videos: [],
        documents: []
      });
      setCurrentProduct({
        id: String(id ?? values.productName),
        name: values.productName,
        sku: values.sku,
        category: values.category,
        brandName: values.brandName
      });
      message.success(`商品资料已创建：${id ?? "-"}`);
      navigate(id ? `/materials/${id}` : "/materials");
    } catch (error) {
      message.error(error instanceof Error ? error.message : "商品资料创建失败");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="p0-page">
      <section className="p0-hero glass-panel">
        <div>
          <Text className="p0-eyebrow">商品资料</Text>
          <Title level={2}>新建商品资料</Title>
          <Paragraph>
            继续调用真实的 `POST /api/v1/material/upload` 创建基础资料。Excel / PDF / CSV 的本地预览入口放在工具中心，不在这里伪造解析结果。
          </Paragraph>
        </div>
        <Space wrap>
          <Link to="/tools/imports">
            <Button type="primary">打开导入工具</Button>
          </Link>
          <Link to="/tools/design-draft">
            <Button>设计草稿</Button>
          </Link>
        </Space>
      </section>

      <div className="p0-notice-grid">
        <ApiUnavailableState compact title="部分资料能力待接入" description="文件解析、OCR、PDF 预览和批量导入入口已转到前端工具页，不会在这里伪造结果。" />
        <ToolUnavailableState compact title="AI 解析待配置" description="LLaVA、OCR 与文档理解工具仍以待接入状态保留入口。" />
      </div>

      <Card className="p0-card" title="基础信息">
        <Form form={form} layout="vertical" onFinish={submit}>
          <Form.Item name="productName" label="商品名称" rules={[{ required: true, message: "请输入商品名称" }]}>
            <Input placeholder="请输入商品名称" />
          </Form.Item>
          <Form.Item name="brandName" label="品牌">
            <Input placeholder="请输入品牌名称" />
          </Form.Item>
          <Form.Item name="category" label="类目">
            <Input placeholder="请输入商品类目" />
          </Form.Item>
          <Form.Item name="sku" label="SKU">
            <Input placeholder="请输入 SKU" />
          </Form.Item>
          <Form.Item name="uploader" label="上传人">
            <Input placeholder="请输入操作人" />
          </Form.Item>
          <Form.Item name="price" label="价格">
            <InputNumber min={0} precision={2} style={{ width: "100%" }} placeholder="0.00" />
          </Form.Item>
          <Form.Item name="description" label="商品描述">
            <TextArea rows={5} placeholder="请输入真实商品描述、规格或卖点" />
          </Form.Item>
          <Space wrap>
            <Button type="primary" htmlType="submit" loading={submitting}>
              提交资料
            </Button>
            <Link to="/materials">
              <Button>返回列表</Button>
            </Link>
          </Space>
        </Form>
      </Card>
    </main>
  );
}
