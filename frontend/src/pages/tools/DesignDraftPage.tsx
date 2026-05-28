import { Button, Card, Empty, Input, Space, Tag, Typography } from "antd";
import { Excalidraw } from "@excalidraw/excalidraw";
import "@excalidraw/excalidraw/index.css";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { EmptyState } from "../../components/common";
import { api } from "../../services/api";
import { useWorkbenchStore } from "../../stores/workbenchStore";
import { P0Scaffold } from "../p0/P0Scaffold";

const { Paragraph, Text } = Typography;

type SceneData = {
  elements: unknown[];
  appState: Record<string, unknown>;
  files: Record<string, unknown>;
};

const createBlankScene = (): SceneData => ({
  elements: [],
  appState: {},
  files: {}
});

const parseScene = (sceneJson: string | undefined | null): SceneData => {
  if (!sceneJson) {
    return createBlankScene();
  }

  try {
    const parsed = JSON.parse(sceneJson) as Partial<SceneData>;
    return {
      elements: Array.isArray(parsed.elements) ? parsed.elements : [],
      appState: parsed.appState && typeof parsed.appState === "object" ? parsed.appState : {},
      files: parsed.files && typeof parsed.files === "object" ? parsed.files : {}
    };
  } catch {
    return createBlankScene();
  }
};

export default function DesignDraftPage() {
  const selectedAssets = useWorkbenchStore((state) => state.selectedAssets);
  const clearSelectedAssets = useWorkbenchStore((state) => state.clearSelectedAssets);
  const currentProduct = useWorkbenchStore((state) => state.currentProduct);
  const productDetailId = currentProduct?.id ? Number(currentProduct.id) : undefined;
  const hasProductDetailId = typeof productDetailId === "number" && Number.isFinite(productDetailId);

  const [draftId, setDraftId] = useState<number | null>(null);
  const [draftName, setDraftName] = useState("Untitled Draft");
  const [scene, setScene] = useState<SceneData>(createBlankScene());
  const [sceneKey, setSceneKey] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const assetCount = useMemo(() => selectedAssets.length, [selectedAssets.length]);
  const sceneJson = useMemo(() => JSON.stringify(scene), [scene]);

  const loadDraft = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const query = hasProductDetailId ? { pageNum: 1, pageSize: 1, productDetailId } : { pageNum: 1, pageSize: 1 };
      const page = await api.designDrafts.list(query);
      const draft = page.data?.[0] ?? null;

      if (draft) {
        setDraftId(draft.id ?? null);
        setDraftName(draft.draftName ?? "Untitled Draft");
        setScene(parseScene(draft.sceneJson));
      } else {
        setDraftId(null);
        setDraftName("Untitled Draft");
        setScene(createBlankScene());
      }
      setSceneKey((value) => value + 1);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to load draft");
    } finally {
      setLoading(false);
    }
  }, [currentProduct?.id]);

  useEffect(() => {
    void loadDraft();
  }, [loadDraft]);

  const handleSceneChange = useCallback((elements: unknown[], appState: Record<string, unknown>, files: Record<string, unknown>) => {
    setScene({ elements, appState, files });
    setMessage(null);
  }, []);

  const handleSave = async () => {
    setSaving(true);
    setMessage(null);
    setError(null);

    try {
      const payload = {
        draftName: draftName.trim() || "Untitled Draft",
        sceneJson,
        selectedAssets: selectedAssets.map((asset) => ({
          id: asset.id,
          name: asset.name,
          type: asset.type,
          url: asset.url
        })),
        productDetailId: hasProductDetailId ? productDetailId : undefined,
        status: "PENDING"
      };

      if (draftId) {
        await api.designDrafts.update(draftId, payload);
      } else {
        const createdId = await api.designDrafts.create(payload);
        setDraftId(Number(createdId));
      }

      await loadDraft();
      setMessage("Draft saved to backend.");
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to save draft");
    } finally {
      setSaving(false);
    }
  };

  return (
    <P0Scaffold
      eyebrow="Tools"
      title="Design Draft"
      description="This page saves and loads a real Excalidraw scene JSON plus selected assets through the backend."
      actions={[
        { label: "Back to Tools", to: "/tools" },
        { label: "Import Data", to: "/tools/imports" }
      ]}
      flow={["Select assets", "Edit draft", "Persist scene", "Reuse draft"]}
      apiNotice={false}
      toolNotice={false}
    >
      {error ? <ErrorBanner message={error} /> : null}

      <div className="design-draft-layout">
        <aside className="design-draft-sidebar">
          <Card className="p0-card" title="Current Product">
            {currentProduct ? (
              <Space direction="vertical" size={8}>
                <Text strong>{currentProduct.name ?? "-"}</Text>
                <Text type="secondary">SKU: {currentProduct.sku ?? "-"}</Text>
                <Text type="secondary">Category: {currentProduct.category ?? "-"}</Text>
              </Space>
            ) : (
              <Empty description="No product selected" />
            )}
          </Card>

          <Card
            className="p0-card"
            title="Selected Assets"
            extra={
              <Button size="small" onClick={() => clearSelectedAssets()} disabled={!assetCount}>
                Clear
              </Button>
            }
          >
            {assetCount ? (
              <Space direction="vertical" size={8} style={{ width: "100%" }}>
                {selectedAssets.map((asset) => (
                  <Card key={asset.id} size="small">
                    <Space direction="vertical" size={4}>
                      <Text strong>{asset.name}</Text>
                      <Tag>{asset.type ?? "asset"}</Tag>
                      {asset.url && <Text type="secondary">{asset.url}</Text>}
                    </Space>
                  </Card>
                ))}
              </Space>
            ) : (
              <EmptyState compact title="No selected assets" description="Choose a real asset first if you want to persist references." />
            )}
          </Card>

          <Card className="p0-card" title="Draft Metadata">
            <Space direction="vertical" size={8} style={{ width: "100%" }}>
              <label className="grid gap-2">
                <span className="text-sm font-medium text-slate-700">Draft Name</span>
                <Input value={draftName} onChange={(event) => setDraftName(event.target.value)} />
              </label>
              <Text type="secondary">Draft ID: {draftId ?? "new"}</Text>
              <Text type="secondary">Scene JSON length: {sceneJson.length}</Text>
            </Space>
          </Card>

          <Card className="p0-card" title="Usage Note">
            <Paragraph type="secondary">The editor loads the latest draft for the current product when available, otherwise it starts from a blank scene.</Paragraph>
            <Link to="/assets">Open asset library</Link>
          </Card>
        </aside>

        <section className="design-draft-canvas-shell">
          <div className="flex items-center justify-between gap-3">
            <Space>
              <Tag>{draftId ? "Loaded" : "New"}</Tag>
              <Text type="secondary">{message ?? (loading ? "Loading draft..." : "Ready")}</Text>
            </Space>
            <Space>
              <Button onClick={() => void loadDraft()} loading={loading}>
                Reload
              </Button>
              <Button type="primary" onClick={() => void handleSave()} loading={saving}>
                Save Draft
              </Button>
            </Space>
          </div>

          <div className="mt-4 min-h-[720px]">
            <Excalidraw
              key={`${draftId ?? "new"}-${sceneKey}`}
              initialData={scene as any}
              onChange={handleSceneChange as any}
            />
          </div>
        </section>
      </div>
    </P0Scaffold>
  );
}

function ErrorBanner({ message }: { message: string }) {
  return (
    <Card className="p0-card" title="Draft Error">
      <EmptyState title="Failed to load or save the draft" description={message} />
    </Card>
  );
}
