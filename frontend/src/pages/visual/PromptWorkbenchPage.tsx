import { useMutation } from "@tanstack/react-query";
import { Alert, Button, Card, Descriptions, Form, Input, InputNumber, Space, Tabs, Typography, message } from "antd";
import { useMemo } from "react";
import { EmptyState, ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";
import type {
  PromptWorkbenchExpandRequest,
  PromptWorkbenchGuidedRequest,
  PromptWorkbenchImageToPromptRequest,
  PromptWorkbenchResult
} from "../../services/types";
import { parseListText, safeJsonStringify, textFromUnknown } from "./visualUtils";

const { TextArea } = Input;
const { Paragraph, Text } = Typography;

type GuidedFormValues = PromptWorkbenchGuidedRequest & {
  materialId?: number;
  skcPolicyId?: number;
  modelProfileId?: number;
};

type ExpandFormValues = PromptWorkbenchExpandRequest & {
  positivePrompt: string;
  styleTagsText?: string;
  constraintsText?: string;
};

type ImageToPromptFormValues = PromptWorkbenchImageToPromptRequest & {
  outputText?: string;
};

const ResultPanel = ({
  data,
  isError,
  error,
  isPending,
  title
}: {
  title: string;
  data?: PromptWorkbenchResult;
  isError?: boolean;
  error?: unknown;
  isPending?: boolean;
}) => {
  if (isPending) {
    return <LoadingState title={`${title} generating`} description="等待后端返回真实结果。" compact />;
  }

  if (isError) {
    return (
      <ErrorState
        title={`${title} failed`}
        description={error instanceof Error ? error.message : "提示词接口调用失败"}
        compact
      />
    );
  }

  if (!data) {
    return <EmptyState title="暂无结果" description="提交真实请求后，这里才会显示后端返回内容。" compact />;
  }

  return (
    <Space direction="vertical" size={12} style={{ width: "100%" }}>
      <Descriptions bordered size="small" column={1}>
        {["positivePrompt", "negativePrompt", "shotScript", "composition", "lighting", "camera"].map((key) => {
          const value = data[key as keyof PromptWorkbenchResult];
          return (
            <Descriptions.Item key={key} label={key}>
              {textFromUnknown(value)}
            </Descriptions.Item>
          );
        })}
        <Descriptions.Item label="styleTags">{textFromUnknown(data.styleTags)}</Descriptions.Item>
        <Descriptions.Item label="riskWarnings">{textFromUnknown(data.riskWarnings)}</Descriptions.Item>
      </Descriptions>
      <Alert showIcon type="info" message="原始返回" description={<pre style={{ margin: 0, whiteSpace: "pre-wrap" }}>{safeJsonStringify(data)}</pre>} />
    </Space>
  );
};

export default function PromptWorkbenchPage() {
  const guidedForm = Form.useForm<GuidedFormValues>()[0];
  const expandForm = Form.useForm<ExpandFormValues>()[0];
  const imageForm = Form.useForm<ImageToPromptFormValues>()[0];

  const guidedMutation = useMutation({
    mutationFn: (values: GuidedFormValues) =>
      api.visualPromptWorkbench.guided({
        materialId: values.materialId,
        categoryCode: values.categoryCode?.trim() || undefined,
        brandGuideline: values.brandGuideline?.trim() || undefined,
        platformRequirement: values.platformRequirement?.trim() || undefined,
        ratio: values.ratio?.trim() || undefined,
        skcPolicyId: values.skcPolicyId,
        modelProfileId: values.modelProfileId,
        referenceNotes: values.referenceNotes?.trim() || undefined
      }),
    onSuccess: () => message.success("已调用 guided 接口")
  });

  const expandMutation = useMutation({
    mutationFn: (values: ExpandFormValues) =>
      api.visualPromptWorkbench.expand({
        positivePrompt: values.positivePrompt.trim(),
        negativePrompt: values.negativePrompt?.trim() || undefined,
        styleTags: parseListText(values.styleTagsText ?? ""),
        constraints: parseListText(values.constraintsText ?? "")
      }),
    onSuccess: () => message.success("已调用 expand 接口")
  });

  const imageMutation = useMutation({
    mutationFn: (values: ImageToPromptFormValues) =>
      api.visualPromptWorkbench.imageToPrompt({
        imageUrl: values.imageUrl.trim(),
        language: values.language?.trim() || undefined,
        output: parseListText(values.outputText ?? "")
      }),
    onSuccess: () => message.success("已调用 image-to-prompt 接口")
  });

  const tabs = useMemo(
    () => [
      {
        key: "guided",
        label: "引导生成",
        children: (
          <Card title="引导 prompt">
            <Form form={guidedForm} layout="vertical" onFinish={(values) => void guidedMutation.mutateAsync(values)}>
              <Space direction="vertical" size={12} style={{ width: "100%" }}>
                <Space wrap style={{ width: "100%" }}>
                  <Form.Item name="materialId" label="商品素材 ID" style={{ flex: 1, minWidth: 180 }}>
                    <InputNumber style={{ width: "100%" }} />
                  </Form.Item>
                  <Form.Item name="skcPolicyId" label="SKC 策略 ID" style={{ flex: 1, minWidth: 180 }}>
                    <InputNumber style={{ width: "100%" }} />
                  </Form.Item>
                  <Form.Item name="modelProfileId" label="模特档案 ID" style={{ flex: 1, minWidth: 180 }}>
                    <InputNumber style={{ width: "100%" }} />
                  </Form.Item>
                  <Form.Item name="ratio" label="画幅" style={{ flex: 1, minWidth: 120 }}>
                    <Input placeholder="1:1 / 3:4" />
                  </Form.Item>
                </Space>
                <Form.Item name="categoryCode" label="类目编号">
                  <Input placeholder="如 WOMEN_DRESS" />
                </Form.Item>
                <Form.Item name="brandGuideline" label="品牌约束">
                  <TextArea rows={3} placeholder="品牌风格、禁用项、平台限制" />
                </Form.Item>
                <Form.Item name="platformRequirement" label="平台要求">
                  <TextArea rows={3} placeholder="平台尺寸、卖点、版式约束" />
                </Form.Item>
                <Form.Item name="referenceNotes" label="参考说明">
                  <TextArea rows={3} placeholder="可选的参考背景说明" />
                </Form.Item>
                <Space wrap>
                  <Button type="primary" htmlType="submit" loading={guidedMutation.isPending}>
                    生成
                  </Button>
                  <Button onClick={() => guidedForm.resetFields()} disabled={guidedMutation.isPending}>
                    重置
                  </Button>
                </Space>
                <ResultPanel
                  title="guided"
                  data={guidedMutation.data}
                  isError={guidedMutation.isError}
                  error={guidedMutation.error}
                  isPending={guidedMutation.isPending}
                />
              </Space>
            </Form>
          </Card>
        )
      },
      {
        key: "expand",
        label: "扩写 prompt",
        children: (
          <Card title="扩写 prompt">
            <Form form={expandForm} layout="vertical" onFinish={(values) => void expandMutation.mutateAsync(values)}>
              <Space direction="vertical" size={12} style={{ width: "100%" }}>
                <Form.Item
                  name="positivePrompt"
                  label="正向 prompt"
                  rules={[{ required: true, message: "请输入正向 prompt" }]}
                >
                  <TextArea rows={4} />
                </Form.Item>
                <Form.Item name="negativePrompt" label="反向 prompt">
                  <TextArea rows={3} />
                </Form.Item>
                <Form.Item name="styleTagsText" label="风格标签">
                  <TextArea rows={2} placeholder="一行一个或逗号分隔" />
                </Form.Item>
                <Form.Item name="constraintsText" label="约束条件">
                  <TextArea rows={2} placeholder="一行一个或逗号分隔" />
                </Form.Item>
                <Space wrap>
                  <Button type="primary" htmlType="submit" loading={expandMutation.isPending}>
                    扩写
                  </Button>
                  <Button onClick={() => expandForm.resetFields()} disabled={expandMutation.isPending}>
                    重置
                  </Button>
                </Space>
                <ResultPanel
                  title="expand"
                  data={expandMutation.data}
                  isError={expandMutation.isError}
                  error={expandMutation.error}
                  isPending={expandMutation.isPending}
                />
              </Space>
            </Form>
          </Card>
        )
      },
      {
        key: "image",
        label: "图转 prompt",
        children: (
          <Card title="图片转 prompt">
            <Form form={imageForm} layout="vertical" onFinish={(values) => void imageMutation.mutateAsync(values)}>
              <Space direction="vertical" size={12} style={{ width: "100%" }}>
                <Form.Item
                  name="imageUrl"
                  label="图片地址"
                  rules={[{ required: true, message: "请输入图片地址" }]}
                >
                  <Input placeholder="file:// 或 http(s):// 地址" />
                </Form.Item>
                <Space wrap style={{ width: "100%" }}>
                  <Form.Item name="language" label="语言" style={{ flex: 1, minWidth: 180 }}>
                    <Input placeholder="zh-CN" />
                  </Form.Item>
                  <Form.Item name="outputText" label="输出字段" style={{ flex: 2, minWidth: 280 }}>
                    <Input placeholder="caption, styleTags, positivePrompt, negativePrompt" />
                  </Form.Item>
                </Space>
                <Space wrap>
                  <Button type="primary" htmlType="submit" loading={imageMutation.isPending}>
                    转换
                  </Button>
                  <Button onClick={() => imageForm.resetFields()} disabled={imageMutation.isPending}>
                    重置
                  </Button>
                </Space>
                <ResultPanel
                  title="image-to-prompt"
                  data={imageMutation.data}
                  isError={imageMutation.isError}
                  error={imageMutation.error}
                  isPending={imageMutation.isPending}
                />
              </Space>
            </Form>
          </Card>
        )
      }
    ],
    [expandForm, expandMutation, guidedForm, guidedMutation, imageForm, imageMutation]
  );

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <Card title="提示词工作台">
        <Paragraph style={{ marginBottom: 0 }}>
          只连接真实的 <Text code>/api/v1/prompt-workbench/*</Text> 接口。接口失败时直接显示后端错误，不伪造结果。
        </Paragraph>
      </Card>
      <Tabs items={tabs} />
    </Space>
  );
}
