import { Button, Card, Descriptions, Space, Tag, Typography } from "antd";
import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeftOutlined, SettingOutlined } from "@ant-design/icons";
import { ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";
import type { ToolAdapterInfo } from "../../services/types";

const { Text } = Typography;

export default function ToolDetailPage() {
  const { toolCode = "" } = useParams<{ toolCode: string }>();
  const [tool, setTool] = useState<ToolAdapterInfo | null>(null);
  const [loading, setLoading] = useState(Boolean(toolCode));
  const [error, setError] = useState<string | null>(null);

  const loadTool = async () => {
    if (!toolCode) {
      setError("缺少工具编码");
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      setTool(await api.tools.get(toolCode));
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "工具详情加载失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadTool();
  }, [toolCode]);

  if (loading) {
    return <LoadingState title="加载中" description="正在加载工具详情" />;
  }

  if (error) {
    return <ErrorState title="加载失败" description={error} onRetry={loadTool} />;
  }

  if (!tool) {
    return <ErrorState title="工具不存在" description="未找到该工具" />;
  }

  return (
    <div>
      {/* 页面标题 */}
      <div className="df-page-header">
        <Space>
          <Link to="/tools">
            <Button type="text" icon={<ArrowLeftOutlined />}>返回</Button>
          </Link>
          <div>
            <h1 className="df-page-title">工具详情：{tool.name || toolCode}</h1>
            <p className="df-page-desc">查看工具适配器的配置和状态</p>
          </div>
        </Space>
      </div>

      {/* 基本信息 */}
      <Card style={{ marginBottom: "var(--df-space-6)" }}>
        <div className="df-section-header">
          <span className="df-section-title">基本信息</span>
          <Tag color={tool.configured ? "green" : "orange"}>
            {tool.configured ? "已配置" : "待配置"}
          </Tag>
        </div>
        <Descriptions column={2}>
          <Descriptions.Item label="名称">{tool.name || "--"}</Descriptions.Item>
          <Descriptions.Item label="编码">{tool.code || "--"}</Descriptions.Item>
          <Descriptions.Item label="分类">{tool.category || "--"}</Descriptions.Item>
          <Descriptions.Item label="接入方式">{tool.integrationMode || "--"}</Descriptions.Item>
          <Descriptions.Item label="默认操作">{tool.defaultOperation || "--"}</Descriptions.Item>
          <Descriptions.Item label="默认路径">{tool.defaultPath || "--"}</Descriptions.Item>
          <Descriptions.Item label="状态">{tool.status || "--"}</Descriptions.Item>
          <Descriptions.Item label="仓库">{tool.repository || "--"}</Descriptions.Item>
        </Descriptions>
      </Card>

      {/* 操作集合 */}
      {tool.operations && tool.operations.length > 0 && (
        <Card style={{ marginBottom: "var(--df-space-6)" }}>
          <div className="df-section-header">
            <span className="df-section-title">操作集合</span>
          </div>
          <Space wrap>
            {tool.operations.map((op) => (
              <Tag key={op} icon={<SettingOutlined />}>{op}</Tag>
            ))}
          </Space>
        </Card>
      )}

      {/* 商业信息 */}
      <Card>
        <div className="df-section-header">
          <span className="df-section-title">商业信息</span>
        </div>
        <Descriptions column={2}>
          <Descriptions.Item label="许可证">{tool.license || "--"}</Descriptions.Item>
          <Descriptions.Item label="商业策略">{tool.commercialPolicy || "--"}</Descriptions.Item>
          <Descriptions.Item label="星标">{tool.stars ?? "--"}</Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
}