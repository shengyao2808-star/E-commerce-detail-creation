import { Card, Descriptions, Empty, Tag } from "antd";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { ErrorState, LoadingState, ToolUnavailableState } from "../../components/common";
import { api } from "../../services/api";
import type { ToolAdapterInfo } from "../../services/types";
import { P0Scaffold } from "../p0/P0Scaffold";

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

  return (
    <P0Scaffold
      eyebrow="工具中心"
      title={`工具详情${toolCode ? `：${toolCode}` : ""}`}
      description="查看工具适配器的契约与配置状态。前端不直接调用第三方工具，未配置时保持不可用。"
      actions={[{ label: "返回工具中心", to: "/tools" }]}
      apiNotice={false}
      toolNotice={false}
    >
      {loading && <LoadingState title="正在加载工具详情" description={`GET /api/v1/tool-adapters/${toolCode}`} />}
      {error && !loading && <ErrorState title="工具详情加载失败" description={error} onRetry={loadTool} />}
      {!loading && !error && tool && (
        <>
          {!tool.configured && (
            <ToolUnavailableState compact description="该工具未启用或未配置 base-url，调用入口保持不可用。" />
          )}
          <Card className="p0-card" title="工具信息">
            <Descriptions bordered column={1}>
              <Descriptions.Item label="名称">{tool.name}</Descriptions.Item>
              <Descriptions.Item label="编码">{tool.code}</Descriptions.Item>
              <Descriptions.Item label="分类">{tool.category ?? "--"}</Descriptions.Item>
              <Descriptions.Item label="接入方式">{tool.integrationMode ?? "--"}</Descriptions.Item>
              <Descriptions.Item label="配置状态">
                <Tag color={tool.configured ? "success" : "warning"}>{tool.configured ? "已配置" : "待配置 / 不可用"}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="默认操作">{tool.defaultOperation ?? "--"}</Descriptions.Item>
              <Descriptions.Item label="默认路径">{tool.defaultPath ?? "--"}</Descriptions.Item>
              <Descriptions.Item label="操作集合">{tool.operations?.join(", ") || "--"}</Descriptions.Item>
              <Descriptions.Item label="仓库">{tool.repository ?? "--"}</Descriptions.Item>
              <Descriptions.Item label="许可证">{tool.license ?? "--"}</Descriptions.Item>
              <Descriptions.Item label="商业策略">{tool.commercialPolicy ?? "--"}</Descriptions.Item>
              <Descriptions.Item label="当前状态">{tool.status ?? "--"}</Descriptions.Item>
              <Descriptions.Item label="星标">{tool.stars ?? "--"}</Descriptions.Item>
            </Descriptions>
          </Card>
        </>
      )}
      {!loading && !error && !tool && (
        <Card className="p0-card">
          <Empty description="暂无工具详情。" />
        </Card>
      )}
    </P0Scaffold>
  );
}
