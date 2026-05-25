import { Tag } from "antd";

type RiskTagProps = {
  level?: string | number | null;
  value?: string | number | null;
  children?: string;
};

const riskMap: Record<string, { color: string; text: string }> = {
  "0": { color: "#52C41A", text: "低风险" },
  "1": { color: "#FAAD14", text: "中风险" },
  "2": { color: "#F5222D", text: "高风险" },
  "3": { color: "#722ED1", text: "极高风险" },
  LOW: { color: "#52C41A", text: "低风险" },
  MEDIUM: { color: "#FAAD14", text: "中风险" },
  HIGH: { color: "#F5222D", text: "高风险" },
  EXTREME: { color: "#722ED1", text: "极高风险" },
  CRITICAL: { color: "#722ED1", text: "极高风险" }
};

export const RiskTag = ({ level, value, children }: RiskTagProps) => {
  const source = level ?? value ?? children ?? "UNKNOWN";
  const key = String(source).toUpperCase();
  const meta = riskMap[key] ?? { color: "default", text: "未评估" };

  return <Tag color={meta.color}>{meta.text}</Tag>;
};
