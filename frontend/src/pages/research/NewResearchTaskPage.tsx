import { Button, Card, Form, Input, Space, message } from "antd";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowLeftOutlined } from "@ant-design/icons";
import { api } from "../../services/api";
import type { ResearchTask } from "../../services/types";

const { TextArea } = Input;

type TaskFormValues = {
  taskName: string;
  category?: string;
  owner?: string;
  authorizationSource?: string;
  goals?: string;
  deliveryRequirements?: string;
};

export default function NewResearchTaskPage() {
  const navigate = useNavigate();
  const [form] = Form.useForm<TaskFormValues>();
  const [submitting, setSubmitting] = useState(false);

  const onSubmit = async (values: TaskFormValues) => {
    setSubmitting(true);
    try {
      const payload: ResearchTask = {
        taskName: values.taskName.trim(),
        category: values.category?.trim() || undefined,
        owner: values.owner?.trim() || undefined,
        status: "PENDING",
        inputData: {
          authorizationSource: values.authorizationSource?.trim() || "",
          goals: values.goals?.trim() || "",
          deliveryRequirements: values.deliveryRequirements?.trim() || ""
        }
      };
      const taskId = await api.research.create(payload);
      message.success("调研任务已创建");
      navigate(`/research/tasks/${taskId}`);
    } catch (requestError) {
      message.error(requestError instanceof Error ? requestError.message : "创建调研任务失败");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      {/* 页面标题 */}
      <div className="df-page-header">
        <Space>
          <Link to="/research">
            <Button type="text" icon={<ArrowLeftOutlined />}>返回</Button>
          </Link>
          <div>
            <h1 className="df-page-title">新建调研任务</h1>
            <p className="df-page-desc">创建市场调研任务，分析竞品和用户需求</p>
          </div>
        </Space>
      </div>

      {/* 表单 */}
      <Card>
        <Form
          form={form}
          layout="vertical"
          onFinish={(values) => void onSubmit(values)}
          style={{ maxWidth: 640 }}
        >
          <Form.Item
            label="任务名称"
            name="taskName"
            rules={[{ required: true, message: "请输入任务名称" }]}
          >
            <Input placeholder="夏季活动竞品扫描" maxLength={120} />
          </Form.Item>

          <Form.Item label="类目" name="category">
            <Input placeholder="家居装饰 / 厨房工具 / 时尚配饰" maxLength={120} />
          </Form.Item>

          <Form.Item label="负责人" name="owner">
            <Input placeholder="负责人姓名" maxLength={80} />
          </Form.Item>

          <Form.Item label="授权来源" name="authorizationSource">
            <TextArea rows={3} placeholder="客户授权 URL、公开数据源列表或导入说明" />
          </Form.Item>

          <Form.Item label="调研目标" name="goals">
            <TextArea rows={4} placeholder="价格区间、视觉趋势、卖点分析、详情页结构" />
          </Form.Item>

          <Form.Item label="交付要求" name="deliveryRequirements">
            <TextArea rows={4} placeholder="期望的报告格式和验收标准" />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={submitting}>
                创建任务
              </Button>
              <Button onClick={() => form.resetFields()} disabled={submitting}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}