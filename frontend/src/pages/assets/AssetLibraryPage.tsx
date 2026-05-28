import { Button, Card, Empty, Space, Tag, Typography } from "antd";
import { UploadOutlined } from "@ant-design/icons";
import { useEffect, useMemo, useRef, useState } from "react";
import { Link } from "react-router-dom";
import Tesseract from "tesseract.js";
import tesseractWorkerSrc from "tesseract.js/dist/worker.min.js?url";
import { EmptyState } from "../../components/common";
import { api } from "../../services/api";
import { useWorkbenchStore } from "../../stores/workbenchStore";
import { P0Scaffold } from "../p0/P0Scaffold";

const { Paragraph, Text } = Typography;

export default function AssetLibraryPage() {
  const selectedAssets = useWorkbenchStore((state) => state.selectedAssets);
  const setSelectedAssets = useWorkbenchStore((state) => state.setSelectedAssets);
  const setToolState = useWorkbenchStore((state) => state.setToolState);

  const [imageFile, setImageFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string>("");
  const [ocrText, setOcrText] = useState("");
  const [ocrError, setOcrError] = useState<string | null>(null);
  const [ocrProgress, setOcrProgress] = useState<string>("Waiting");
  const [ocrLoading, setOcrLoading] = useState(false);
  const [ocrTaskId, setOcrTaskId] = useState<number | null>(null);
  const inputRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    if (!imageFile) {
      setPreviewUrl("");
      return;
    }

    const nextPreviewUrl = URL.createObjectURL(imageFile);
    setPreviewUrl(nextPreviewUrl);

    return () => URL.revokeObjectURL(nextPreviewUrl);
  }, [imageFile]);

  useEffect(() => {
    if (!imageFile || !previewUrl) {
      return;
    }

    setSelectedAssets([
      {
        id: imageFile.name,
        name: imageFile.name,
        type: "image",
        url: previewUrl
      }
    ]);
  }, [imageFile, previewUrl, setSelectedAssets]);

  const ocrStatus = useMemo(() => {
    if (ocrLoading) {
      return "Recognizing";
    }
    if (ocrError) {
      return "Failed";
    }
    if (ocrText) {
      return "Done";
    }
    return "Idle";
  }, [ocrError, ocrLoading, ocrText]);

  const handlePickFile = (file: File | null) => {
    if (!file) {
      return;
    }

    setImageFile(file);
    setOcrText("");
    setOcrError(null);
    setOcrProgress("Image selected, waiting for OCR");
    setOcrTaskId(null);
  };

  const persistOcrStatus = async (taskId: number, status: string, progress: number, errorMessage?: string) => {
    await api.ocrTasks.updateStatus(taskId, { status, progress, errorMessage });
  };

  const runOcr = async () => {
    if (!imageFile) {
      setOcrError("Please choose a real image first.");
      return;
    }

    setToolState({ status: "loading", activeTool: "ocr", message: "Running OCR" });
    setOcrLoading(true);
    setOcrError(null);
    setOcrText("");
    setOcrProgress("Creating OCR task");

    let taskId: number | null = null;
    try {
      taskId = Number(
        await api.ocrTasks.create({
          assetName: imageFile.name,
          assetType: imageFile.type || "image",
          language: "eng",
          status: "RUNNING",
          progress: 0
        })
      );
      setOcrTaskId(taskId);
    } catch (requestError) {
      const messageText = requestError instanceof Error ? requestError.message : "Failed to create OCR task";
      setOcrError(messageText);
      setToolState({ status: "error", activeTool: "ocr", message: messageText, lastSyncedAt: new Date().toISOString() });
      setOcrLoading(false);
      return;
    }

    try {
      const worker = await Tesseract.createWorker("eng", 1, {
        workerPath: tesseractWorkerSrc,
        langPath: "https://tessdata.projectnaptha.com/4.0.0",
        logger: (entry) => {
          if (entry.status) {
            setOcrProgress(`${entry.status} ${(entry.progress * 100).toFixed(0)}%`);
          }
        }
      });

      const result = await worker.recognize(imageFile);
      await worker.terminate();

      const text = result.data.text.trim();
      const confidence = Number((result.data.confidence ?? 0).toFixed(2));

      setOcrText(text);
      setOcrProgress("OCR complete");

      if (taskId !== null) {
        await api.ocrTasks.updateResult(taskId, {
          ocrText: text,
          confidence,
          progress: 100
        });
      }

      setToolState({ status: "ready", activeTool: "ocr", message: "OCR completed", lastSyncedAt: new Date().toISOString() });
    } catch (requestError) {
      const messageText = requestError instanceof Error ? requestError.message : "OCR failed";
      setOcrError(messageText);

      if (taskId !== null) {
        await persistOcrStatus(taskId, "FAILED", 0, messageText);
      }

      setToolState({ status: "error", activeTool: "ocr", message: messageText, lastSyncedAt: new Date().toISOString() });
    } finally {
      setOcrLoading(false);
    }
  };

  return (
    <P0Scaffold
      eyebrow="Assets"
      title="Asset Library"
      description="This page only persists real OCR output after Tesseract completes. No fake OCR text is generated."
      actions={[
        { label: "Design Draft", to: "/tools/design-draft" },
        { label: "Import Data", to: "/tools/imports" }
      ]}
      flow={["Select asset", "Run OCR", "Persist result", "Review"]}
      apiNotice={false}
      toolNotice={false}
    >
      <div className="p0-notice-grid">
        <Card className="p0-card" title="OCR Entry">
          <Space direction="vertical" size={12} style={{ width: "100%" }}>
            <Paragraph type="secondary">Upload a real image first. When OCR finishes, the result is persisted to the backend task table.</Paragraph>
            <input
              ref={inputRef}
              type="file"
              hidden
              accept="image/*"
              onChange={(event) => handlePickFile(event.target.files?.[0] ?? null)}
            />
            <Space wrap>
              <Button icon={<UploadOutlined />} onClick={() => inputRef.current?.click()}>
                Pick Image
              </Button>
              <Button type="primary" onClick={() => void runOcr()} loading={ocrLoading} disabled={!imageFile}>
                Run OCR
              </Button>
              <Tag>{ocrStatus}</Tag>
            </Space>
            <Text type="secondary">{ocrProgress}</Text>
            {ocrTaskId !== null ? <Text type="secondary">Task ID: {ocrTaskId}</Text> : null}
            {ocrError && <EmptyState title="OCR failed" description={ocrError} />}
          </Space>
        </Card>

        <Card className="p0-card" title="Selected Assets">
          {selectedAssets.length ? (
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
            <EmptyState compact title="No asset selected" description="Select a real image to populate the selected-asset state." />
          )}
        </Card>
      </div>

      <div className="design-draft-layout">
        <aside className="design-draft-sidebar">
          <Card className="p0-card" title="Image Preview">
            {previewUrl ? <img className="asset-preview-image" src={previewUrl} alt={imageFile?.name ?? "preview"} /> : <Empty description="No image selected" />}
          </Card>

          <Card className="p0-card" title="OCR Result">
            {ocrText ? <pre className="ocr-result-block">{ocrText}</pre> : <EmptyState compact title="Waiting for OCR" description="The recognized text appears here after a real OCR run." />}
          </Card>
        </aside>

        <section className="design-draft-canvas-shell">
          <Card className="p0-card" title="Asset Library Placeholder">
            <Empty description="Backend asset inventory is not wired yet. This page keeps the entry point and OCR persistence only." />
            <div style={{ marginTop: 16 }}>
              <Link to="/tools/design-draft">Open design draft page</Link>
            </div>
          </Card>
        </section>
      </div>
    </P0Scaffold>
  );
}
