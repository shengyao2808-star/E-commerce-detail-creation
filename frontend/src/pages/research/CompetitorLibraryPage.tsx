import { Button, Card, Empty, Space } from "antd";
import { Link } from "react-router-dom";
import { PlusOutlined } from "@ant-design/icons";

export default function CompetitorLibraryPage() {
  return (
    <div>
      {/* 页面标题 */}
      <div className="df-page-header">
        <h1 className="df-page-title">竞品库</h1>
        <p className="df-page-desc">管理竞品信息，分析竞品策略</p>
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
            🏢
          </div>
          <h3 style={{ fontSize: "var(--df-text-lg)", color: "var(--df-text)", marginBottom: "var(--df-space-2)" }}>
            暂无竞品数据
          </h3>
          <p style={{ color: "var(--df-text-muted)", marginBottom: "var(--df-space-5)", maxWidth: 320 }}>
            竞品库功能开发中，敬请期待
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