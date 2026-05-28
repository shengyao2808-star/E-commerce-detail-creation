import { Alert, Button, Card, Form, Input, Space, message } from "antd";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ErrorState } from "../../components/common";
import { api } from "../../services/api";
import type { ResearchTask } from "../../services/types";
import { P0Scaffold } from "../p0/P0Scaffold";

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
  const [createError, setCreateError] = useState<string | null>(null);

  const onSubmit = async (values: TaskFormValues) => {
    setSubmitting(true);
    setCreateError(null);

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
      message.success("Research task created");
      navigate(`/research/tasks/${taskId}`);
    } catch (requestError) {
      setCreateError(requestError instanceof Error ? requestError.message : "Failed to create research task");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <P0Scaffold
      eyebrow="Research"
      title="New Research Task"
      description="Create a persisted task through POST /api/v1/research/tasks with real backend responses only."
      apiNotice={false}
      toolNotice={false}
      capabilities={[
        { title: "Task persistence", description: "Creates a real research task record.", status: "available" },
        { title: "Honest failure state", description: "Shows backend error when create fails.", status: "available" },
        { title: "No fake AI", description: "Stores only user-provided task input.", status: "available" }
      ]}
    >
      <Card className="p0-card" title="Task Information">
        {createError ? <ErrorState title="Failed to create task" description={createError} /> : null}
        <Form form={form} layout="vertical" onFinish={(values) => void onSubmit(values)}>
          <Alert
            showIcon
            type="info"
            style={{ marginBottom: 16 }}
            message="Backend endpoint: POST /api/v1/research/tasks"
          />
          <Form.Item label="Task Name" name="taskName" rules={[{ required: true, message: "Task name is required" }]}>
            <Input placeholder="Summer campaign competitor scan" maxLength={120} />
          </Form.Item>
          <Form.Item label="Category" name="category">
            <Input placeholder="Home decor / kitchen tools / fashion accessories" maxLength={120} />
          </Form.Item>
          <Form.Item label="Owner" name="owner">
            <Input placeholder="Operator name or owner ID" maxLength={80} />
          </Form.Item>
          <Form.Item label="Authorization Source" name="authorizationSource">
            <TextArea rows={3} placeholder="Client authorization URL, public source list, or import note." />
          </Form.Item>
          <Form.Item label="Research Goals" name="goals">
            <TextArea rows={4} placeholder="Pricing bands, visual trends, selling points, detail-page structure." />
          </Form.Item>
          <Form.Item label="Delivery Requirements" name="deliveryRequirements">
            <TextArea rows={4} placeholder="Expected report format and acceptance criteria." />
          </Form.Item>
          <Space wrap>
            <Button type="primary" htmlType="submit" loading={submitting}>
              Create Task
            </Button>
            <Button onClick={() => form.resetFields()} disabled={submitting}>
              Reset
            </Button>
          </Space>
        </Form>
      </Card>
    </P0Scaffold>
  );
}
