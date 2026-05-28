import { ArrowRightOutlined } from "@ant-design/icons";
import { Button, Card, Empty, List, Space, Tag, Typography } from "antd";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { ApiUnavailableState, ToolUnavailableState } from "../../components/common";

const { Paragraph, Text, Title } = Typography;

export type P0Capability = {
  title: string;
  description: string;
  status?: "available" | "pending" | "disabled";
};

export type P0Action = {
  label: string;
  to?: string;
  disabled?: boolean;
};

type P0ScaffoldProps = {
  eyebrow: string;
  title: string;
  description: string;
  actions?: P0Action[];
  capabilities?: P0Capability[];
  flow?: string[];
  children?: ReactNode;
  apiNotice?: ReactNode | boolean;
  toolNotice?: ReactNode | boolean;
};

const statusMeta = {
  available: { color: "success", text: "已接入" },
  pending: { color: "warning", text: "待接入" },
  disabled: { color: "default", text: "不可用" }
} as const;

export const P0Scaffold = ({
  eyebrow,
  title,
  description,
  actions = [],
  capabilities = [],
  flow = [],
  children,
  apiNotice,
  toolNotice
}: P0ScaffoldProps) => (
  <main className="p0-page">
    <section className="p0-hero glass-panel">
      <div>
        <Text className="p0-eyebrow">{eyebrow}</Text>
        <Title level={2}>{title}</Title>
        <Paragraph>{description}</Paragraph>
      </div>
      {actions.length > 0 && (
        <Space wrap>
          {actions.map((action, index) =>
            action.to && !action.disabled ? (
              <Link key={action.label} to={action.to}>
                <Button type={index === 0 ? "primary" : "default"} icon={<ArrowRightOutlined />}>
                  {action.label}
                </Button>
              </Link>
            ) : (
              <Button key={action.label} disabled={action.disabled} type={index === 0 ? "primary" : "default"}>
                {action.label}
              </Button>
            )
          )}
        </Space>
      )}
    </section>

    {flow.length > 0 && (
      <Card className="p0-card" title="生产链路">
        <div className="p0-flow">
          {flow.map((item, index) => (
            <span key={item} className={index === 0 ? "p0-flow-node p0-flow-node--active" : "p0-flow-node"}>
              {item}
            </span>
          ))}
        </div>
      </Card>
    )}

    {(apiNotice || toolNotice) && (
      <div className="p0-notice-grid">
        {apiNotice === true || apiNotice === undefined ? (
          <ApiUnavailableState compact description="该模块业务接口尚未补齐，页面只保留入口、字段结构和禁用态。" />
        ) : (
          apiNotice
        )}
        {toolNotice === true || toolNotice === undefined ? (
          <ToolUnavailableState compact description="相关工具必须通过 /api/v1/tool-adapters 配置后才可启用。" />
        ) : (
          toolNotice
        )}
      </div>
    )}

    {capabilities.length > 0 && (
      <Card className="p0-card" title="能力状态">
        <List
          dataSource={capabilities}
          renderItem={(item) => {
            const meta = statusMeta[item.status ?? "pending"];
            return (
              <List.Item>
                <List.Item.Meta title={item.title} description={item.description} />
                <Tag color={meta.color}>{meta.text}</Tag>
              </List.Item>
            );
          }}
        />
      </Card>
    )}

    {children ?? (
      <Card className="p0-card">
        <Empty description="暂无真实数据。接口接入前不会展示占位任务、占位结果或虚构指标。" />
      </Card>
    )}
  </main>
);
