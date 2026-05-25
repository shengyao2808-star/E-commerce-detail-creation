import { Tag } from "antd";

type StatusTagProps = {
  status?: string | number | null;
  value?: string | number | null;
  children?: string;
};

const statusMap: Record<string, { color: string; text: string }> = {
  "0": { color: "warning", text: "待审核" },
  "1": { color: "success", text: "已通过" },
  "2": { color: "error", text: "已驳回" },
  "3": { color: "default", text: "已退回" },
  DRAFT: { color: "default", text: "草稿" },
  PENDING: { color: "warning", text: "待审核" },
  APPROVED: { color: "success", text: "已通过" },
  REJECTED: { color: "error", text: "已驳回" },
  RETURNED: { color: "default", text: "已退回" },
  PROCESSING: { color: "processing", text: "处理中" },
  SUCCESS: { color: "success", text: "成功" },
  FAILED: { color: "error", text: "失败" },
  DISABLED: { color: "default", text: "未接入" }
};

export const StatusTag = ({ status, value, children }: StatusTagProps) => {
  const source = status ?? value ?? children ?? "DISABLED";
  const key = String(source).toUpperCase();
  const meta = statusMap[key] ?? {
    color: "default",
    text: String(source ?? "未知")
  };

  return <Tag color={meta.color}>{meta.text}</Tag>;
};
