import { AuditOutlined, ExportOutlined, FileImageOutlined, ProductOutlined, RadarChartOutlined, ToolOutlined } from "@ant-design/icons";
import { Card, Col, Empty, Row, Space, Statistic, Tag, Typography } from "antd";
import { Link } from "react-router-dom";
import { ApiUnavailableState, ToolUnavailableState } from "../../components/common";

const { Paragraph, Text, Title } = Typography;

const quickEntries = [
  {
    title: "新建商品资料",
    description: "从商品基础信息和素材引用开始。",
    to: "/materials/new",
    icon: <ProductOutlined />
  },
  {
    title: "市场调研任务",
    description: "创建授权调研任务骨架。",
    to: "/research/new",
    icon: <RadarChartOutlined />
  },
  {
    title: "生图工作台",
    description: "查看提示词、合规预检和工具状态。",
    to: "/generate",
    icon: <FileImageOutlined />
  },
  {
    title: "工具中心",
    description: "查看 tool-adapter 配置状态。",
    to: "/tools",
    icon: <ToolOutlined />
  }
];

export default function HomeWorkbenchPage() {
  return (
    <main className="p0-page">
      <section className="p0-hero glass-panel">
        <div>
          <Text className="p0-eyebrow">电商商品视觉 AI 生产台</Text>
          <Title level={2}>首页工作台</Title>
          <Paragraph>
            这里展示当前前端 baseline 的真实接入状态。市场调研、竞品分析、生图、结果管理等模块保留骨架，但不展示伪造任务或伪造生成结果。
          </Paragraph>
        </div>
        <Space wrap>
          <Tag color="processing">/api/v1</Tag>
          <Tag color="warning">AI 待配置</Tag>
          <Tag color="default">工具默认关闭</Tag>
        </Space>
      </section>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card className="p0-card">
            <Statistic title="真实生成结果" value="--" />
            <Text type="secondary">未接入生图任务接口，不统计伪造数据。</Text>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="p0-card">
            <Statistic title="待审核内容" value="--" prefix={<AuditOutlined />} />
            <Text type="secondary">请进入审核中心查看真实接口数据。</Text>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="p0-card">
            <Statistic title="导出记录" value="--" prefix={<ExportOutlined />} />
            <Text type="secondary">导出列表会请求后端真实记录。</Text>
          </Card>
        </Col>
      </Row>

      <Card className="p0-card" title="快速入口">
        <div className="p0-entry-grid">
          {quickEntries.map((entry) => (
            <Link className="p0-entry" to={entry.to} key={entry.to}>
              <span className="p0-entry-icon">{entry.icon}</span>
              <strong>{entry.title}</strong>
              <Text type="secondary">{entry.description}</Text>
            </Link>
          ))}
        </div>
      </Card>

      <div className="p0-notice-grid">
        <ApiUnavailableState compact description="首页不拼接虚构指标，没有汇总接口的统计项不会展示。" />
        <ToolUnavailableState compact description="Crawl4AI、ComfyUI、LLaVA 等工具需要客户端私有化部署并配置 tool-adapter 后启用。" />
      </div>

      <Card className="p0-card" title="最近任务">
        <Empty description="暂无可展示的真实任务。接入 image-jobs / research-tasks 后展示任务队列。" />
      </Card>
    </main>
  );
}
