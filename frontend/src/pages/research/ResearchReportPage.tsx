import { Button, Card, Empty, Space } from "antd";
import { Link, useParams } from "react-router-dom";
import { ArrowLeftOutlined, FileTextOutlined } from "@ant-design/icons";

export default function ResearchReportPage() {
  const { id } = useParams<{ id: string }>();

  return (
    <div>
      {/* 页面标题 */}
      <div className="df-page-header">
        <Space>
          <Link to="/research">
            <Button type="text" icon={<ArrowLeftOutlined />}>返回</Button>
          </Link>
          <div>
            <h1 className="df-page-title">调研报告 #{id || ""}</h1>
            <p className="df-page-desc">查看市场调研报告</p>
          </div>
        </Space>
      </div>

      {/* 空状态 */}
      <Card>
        <div style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          padding: "var(--df-space-8) var(--df-space-6)",
          textAlign: "center"
        }}>
          <div style={{ fontSize: 48, color: "var(--df-text-muted)", marginBottom: "var(--df-space-4)" }}>
            <FileTextOutlined />
          </div>
          <h3 style={{ fontSize: "var(--df-text-lg)", color: "var(--df-text)", marginBottom: "var(--df-space-2)" }}>
            暂无报告内容
          </h3>
          <p style={{ color: "var(--df-text-muted)", marginBottom: "var(--df-space-5)", maxWidth: 320 }}>
            调研报告功能开发中，敬请期待
          </p>
          <Space>
            <Link to="/research">
              <Button>返回调研中心</Button>
            </Link>
          </Space>
        </div>
      </Card>
    </div>
  );
}