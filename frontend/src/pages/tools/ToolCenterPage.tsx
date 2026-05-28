import { Button, Card, Empty, List, Space, Tag, Typography } from "antd";
import { useEffect } from "react";
import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { ApiUnavailableState, ErrorState, LoadingState, ToolUnavailableState } from "../../components/common";
import { api } from "../../services/api";
import type { ToolAdapterInfo } from "../../services/types";
import { useWorkbenchStore } from "../../stores/workbenchStore";

const { Text, Title, Paragraph } = Typography;

export default function ToolCenterPage() {
  const setToolState = useWorkbenchStore((state) => state.setToolState);

  const toolQuery = useQuery<ToolAdapterInfo[]>({
    queryKey: ["tool-adapters"],
    queryFn: async () => api.tools.list(),
    retry: 2,
    refetchInterval: 30_000,
    staleTime: 15_000
  });

  useEffect(() => {
    if (toolQuery.isPending) {
      setToolState({ status: "loading", activeTool: "tool-center", message: "正在加载工具清单" });
      return;
    }

    if (toolQuery.isError) {
      setToolState({
        status: "error",
        activeTool: "tool-center",
        message: toolQuery.error instanceof Error ? toolQuery.error.message : "工具清单加载失败"
      });
      return;
    }

    if (toolQuery.data) {
      setToolState({
        status: "ready",
        activeTool: "tool-center",
        message: `已加载 ${toolQuery.data.length} 个工具适配器`,
        lastSyncedAt: new Date().toISOString()
      });
    }
  }, [setToolState, toolQuery.data, toolQuery.error, toolQuery.isError, toolQuery.isPending]);

  return (
    <main className="p0-page">
      <section className="p0-hero glass-panel">
        <div>
          <Text className="p0-eyebrow">工具中心</Text>
          <Title level={2}>工具中心</Title>
          <Paragraph>
            这里继续读取真实的 <code>/api/v1/tool-adapters</code>，并补充前端导入、OCR、设计草稿等本地工具入口。
          </Paragraph>
        </div>
        <Space wrap>
          <Button onClick={() => void toolQuery.refetch()} loading={toolQuery.isFetching}>
            刷新工具
          </Button>
          <Link to="/tools/imports">
            <Button type="primary">资料导入</Button>
          </Link>
          <Link to="/tools/design-draft">
            <Button>设计草稿</Button>
          </Link>
        </Space>
      </section>

      <div className="p0-notice-grid">
        <ApiUnavailableState compact title="工具适配器接入" description="这里只请求后端工具适配器列表，不直接调用第三方服务。" />
        <ToolUnavailableState compact title="默认全部关闭" description="未配置的工具保持待配置或不可用状态，不伪造可执行能力。" />
      </div>

      <div className="p0-notice-grid">
        <Card className="p0-card" title="前端工具入口">
          <Space direction="vertical" size={8}>
            <Link to="/tools/imports">资料导入工具</Link>
            <Link to="/tools/design-draft">设计草稿 / 素材批注</Link>
            <Link to="/assets">图片 OCR 素材库</Link>
          </Space>
        </Card>
        <Card className="p0-card" title="工具状态">
          <Space direction="vertical" size={8}>
            <Text type="secondary">
              {toolQuery.data ? `当前加载 ${toolQuery.data.length} 个工具适配器。` : "等待后端返回工具适配器列表。"}
            </Text>
            <Tag color={toolQuery.isError ? "error" : toolQuery.isPending ? "processing" : "success"}>
              {toolQuery.isError ? "失败" : toolQuery.isPending ? "加载中" : "已连接"}
            </Tag>
          </Space>
        </Card>
      </div>

      {toolQuery.isPending && <LoadingState title="正在加载工具清单" description="GET /api/v1/tool-adapters" />}
      {toolQuery.isError && (
        <ErrorState
          title="工具清单加载失败"
          description={toolQuery.error instanceof Error ? toolQuery.error.message : "请稍后重试"}
          onRetry={() => void toolQuery.refetch()}
        />
      )}
      {toolQuery.data && (
        <Card className="p0-card" title="工具清单">
          <List
            dataSource={toolQuery.data}
            locale={{ emptyText: <Empty description="工具清单为空。请确认后端 tool-adapter 接口可用。" /> }}
            renderItem={(tool) => (
              <List.Item
                actions={[
                  <Link key="detail" to={`/tools/${tool.code}`}>
                    查看详情
                  </Link>
                ]}
              >
                <List.Item.Meta
                  title={
                    <Space wrap>
                      <span>{tool.name}</span>
                      <Tag>{tool.code}</Tag>
                      <Tag color={tool.configured ? "success" : "warning"}>
                        {tool.configured ? "已配置" : "待配置 / 不可用"}
                      </Tag>
                    </Space>
                  }
                  description={
                    <Space direction="vertical" size={2}>
                      <Text type="secondary">
                        {tool.category ?? "未分类"} | {tool.integrationMode ?? "未说明接入方式"}
                      </Text>
                      <Text type="secondary">{tool.repository ?? "暂无仓库信息"}</Text>
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        </Card>
      )}
    </main>
  );
}
