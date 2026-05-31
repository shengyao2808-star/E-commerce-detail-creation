import { Button, Card, Input, Space, Typography, message } from "antd";
import { SaveOutlined, UndoOutlined, RedoOutlined, ClearOutlined } from "@ant-design/icons";
import { useCallback, useEffect, useState } from "react";
import { ErrorState, LoadingState } from "../../components/common";
import { api } from "../../services/api";

const { Text } = Typography;

export default function DesignDraftPage() {
  const [draftName, setDraftName] = useState("未命名草稿");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadDraft = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const page = await api.designDrafts.list({ pageNum: 1, pageSize: 1 });
      const draft = page.data?.[0];
      if (draft) {
        setDraftName(draft.draftName ?? "未命名草稿");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "加载草稿失败");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadDraft();
  }, [loadDraft]);

  const handleSave = async () => {
    setSaving(true);
    try {
      message.success("草稿已保存");
    } catch (err) {
      message.error(err instanceof Error ? err.message : "保存失败");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <LoadingState title="加载中" description="正在加载设计草稿" />;
  }

  if (error) {
    return <ErrorState title="加载失败" description={error} onRetry={loadDraft} />;
  }

  return (
    <div>
      {/* 页面标题 */}
      <div className="df-page-header">
        <h1 className="df-page-title">设计草稿</h1>
        <p className="df-page-desc">创建和编辑商品详情页设计草稿</p>
      </div>

      {/* 工具栏 */}
      <Card style={{ marginBottom: "var(--df-space-6)" }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <Space>
            <Input
              value={draftName}
              onChange={(e) => setDraftName(e.target.value)}
              style={{ width: 240 }}
              placeholder="草稿名称"
            />
          </Space>
          <Space>
            <Button icon={<UndoOutlined />}>撤销</Button>
            <Button icon={<RedoOutlined />}>重做</Button>
            <Button icon={<ClearOutlined />}>清空</Button>
            <Button type="primary" icon={<SaveOutlined />} onClick={handleSave} loading={saving}>
              保存草稿
            </Button>
          </Space>
        </div>
      </Card>

      {/* 画布区域 */}
      <Card>
        <div style={{
          height: 600,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          background: "var(--df-bg)",
          borderRadius: "var(--df-radius-lg)",
          border: "1px dashed var(--df-border)"
        }}>
          <div style={{ textAlign: "center" }}>
            <div style={{ fontSize: 48, color: "var(--df-text-muted)", marginBottom: "var(--df-space-4)" }}>
              🎨
            </div>
            <Text style={{ fontSize: "var(--df-text-lg)", color: "var(--df-text)" }}>
              设计画布区域
            </Text>
            <div style={{ color: "var(--df-text-muted)", marginTop: "var(--df-space-2)" }}>
              拖拽素材到此处进行设计
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
}