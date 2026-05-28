import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { AiPendingNotice, EmptyState, ErrorState, GlassPanel, LoadingState, RiskTag, StatusTag } from "../../components/common";
import { SortableModuleBoard } from "../../components/dnd/SortableModuleBoard";
import { api } from "../../services/api";
import type {
  DetailComposition,
  DetailCompositionQualityCheck,
  DetailDeliveryManifest,
  ProductContentTask,
  ProductContentTaskApplyRequest,
  ProductDetail
} from "../../services/types";
import { DEFAULT_DETAIL_MODULE_ORDER, useWorkbenchStore } from "../../stores/workbenchStore";

type EditableDetail = {
  title: string;
  subtitle: string;
  sellingPoints: string[];
  seoKeywords: string[];
};

type AttachmentSummaryItem = {
  label: string;
  value: number;
  hint: string;
};

type CompositionStatusMeta = {
  label: string;
  color: string;
};

type ManifestSummaryItem = {
  label: string;
  value: string;
};

const COMPOSITION_REFRESH_INTERVAL_MS = 15_000;
const terminalCompositionStatuses = new Set(["SUCCEEDED", "FAILED", "CANCELED", "CANCELLED"]);
const CONTENT_APPLY_FIELDS: ProductContentTaskApplyRequest["fields"] = [
  "title",
  "subtitle",
  "sellingPoints",
  "seoKeywords",
  "aiGeneratedContent"
];

const toList = (value: string[] | string | null | undefined): string[] => {
  if (!value) {
    return [];
  }

  if (Array.isArray(value)) {
    return value.map((item) => String(item).trim()).filter(Boolean);
  }

  const trimmed = value.trim();
  if (!trimmed) {
    return [];
  }

  try {
    const parsed = JSON.parse(trimmed);
    if (Array.isArray(parsed)) {
      return parsed.map((item) => String(item).trim()).filter(Boolean);
    }
  } catch {
    // Keep the fallback parser for existing comma-separated values.
  }

  return trimmed
    .split(/[\n,]/)
    .map((item) => item.trim())
    .filter(Boolean);
};

const toMultiline = (items: string[]): string => items.join("\n");

const fromMultiline = (value: string): string[] =>
  value
    .split(/\n/)
    .map((item) => item.trim())
    .filter(Boolean);

const formatTime = (value: string | null | undefined): string => {
  if (!value) {
    return "--";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
};

const formatAiContent = (value: string | null | undefined): string => {
  if (!value?.trim()) {
    return "";
  }

  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return value;
  }
};

const buildEditableDetail = (detail: ProductDetail): EditableDetail => ({
  title: detail.title ?? "",
  subtitle: detail.subtitle ?? "",
  sellingPoints: toList(detail.sellingPoints),
  seoKeywords: toList(detail.seoKeywords)
});

const normalizeStatus = (status?: string) => (status ?? "").trim().toUpperCase();

const isTerminalCompositionStatus = (status?: string) => terminalCompositionStatuses.has(normalizeStatus(status));

const getCompositionStatusMeta = (status?: string): CompositionStatusMeta => {
  const normalized = normalizeStatus(status);

  switch (normalized) {
    case "PENDING":
      return { label: "Pending", color: "warning" };
    case "RUNNING":
    case "PROCESSING":
      return { label: "Running", color: "processing" };
    case "SUCCEEDED":
    case "SUCCESS":
      return { label: "Succeeded", color: "success" };
    case "FAILED":
    case "ERROR":
      return { label: "Failed", color: "error" };
    case "CANCELED":
    case "CANCELLED":
      return { label: "Canceled", color: "default" };
    default:
      return { label: normalized || "Unknown", color: "default" };
  }
};

const formatBytes = (value?: number) => {
  if (value === undefined || value === null || Number.isNaN(value)) {
    return "--";
  }

  if (value < 1024) {
    return `${value} B`;
  }
  if (value < 1024 * 1024) {
    return `${(value / 1024).toFixed(1)} KB`;
  }
  return `${(value / (1024 * 1024)).toFixed(1)} MB`;
};

const formatToolchain = (toolchain?: string[]) => {
  if (!toolchain || toolchain.length === 0) {
    return "--";
  }

  return toolchain.join(" -> ");
};

const buildManifestSummary = (manifest?: DetailDeliveryManifest | null): ManifestSummaryItem[] => [
  { label: "Deliverable", value: manifest?.deliverable ? "Yes" : "No" },
  { label: "Status", value: manifest?.compositionStatus || "--" },
  { label: "Output path", value: manifest?.outputPath || "--" },
  { label: "Output size", value: formatBytes(manifest?.outputFileSize) },
  {
    label: "Dimensions",
    value:
      manifest?.outputWidth && manifest?.outputHeight ? `${manifest.outputWidth} x ${manifest.outputHeight}` : "--"
  },
  { label: "Generated at", value: manifest?.generatedAt ? String(manifest.generatedAt) : "--" },
  { label: "Toolchain", value: formatToolchain(manifest?.toolchain) }
];

const buildCompositionDetailData = (detail: ProductDetail, draft: EditableDetail, moduleOrder: string[]) => ({
  productId: detail.id,
  materialId: detail.materialId,
  brandId: detail.brandId,
  productName: detail.productName ?? detail.title,
  title: draft.title || detail.title,
  subtitle: draft.subtitle || detail.subtitle,
  sellingPoints: draft.sellingPoints.length ? draft.sellingPoints : toList(detail.sellingPoints),
  seoKeywords: draft.seoKeywords.length ? draft.seoKeywords : toList(detail.seoKeywords),
  moduleOrder,
  imageTemplateId: detail.imageTemplateId,
  sku: detail.sku,
  category: detail.category,
  price: detail.price,
  description: detail.description,
  aiGeneratedContent: detail.aiGeneratedContent,
  images: toList(detail.images),
  videos: toList(detail.videos),
  documents: toList(detail.documents),
  creator: detail.creator
});

export default function DetailEditorPage() {
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const [detail, setDetail] = useState<ProductDetail | null>(null);
  const [draft, setDraft] = useState<EditableDetail>({
    title: "",
    subtitle: "",
    sellingPoints: [],
    seoKeywords: []
  });
  const [moduleOrder, setModuleOrder] = useState<string[]>([...DEFAULT_DETAIL_MODULE_ORDER]);
  const [persistedModuleOrder, setPersistedModuleOrder] = useState<string[]>([...DEFAULT_DETAIL_MODULE_ORDER]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [saveMessage, setSaveMessage] = useState<string | null>(null);
  const [hasLocalChanges, setHasLocalChanges] = useState(false);
  const [selectedContentTaskId, setSelectedContentTaskId] = useState<string>("");
  const [selectedCompositionId, setSelectedCompositionId] = useState<string>("");
  const [selectedQualityCheckId, setSelectedQualityCheckId] = useState<string>("");
  const [compositionPreviewUrl, setCompositionPreviewUrl] = useState<string>("");
  const [compositionPreviewError, setCompositionPreviewError] = useState<string | null>(null);
  const [compositionPreviewLoading, setCompositionPreviewLoading] = useState(false);
  const [manifestDownloadError, setManifestDownloadError] = useState<string | null>(null);
  const setDetailDraft = useWorkbenchStore((state) => state.setDetailDraft);
  const storedModuleOrder = useWorkbenchStore((state) => state.detailDraft.moduleOrder);

  const syncWorkbenchDraft = useCallback(
    (nextDraft: EditableDetail, nextModuleOrder: string[], detailId?: string | number) => {
      setDetailDraft({
        id: String(detailId ?? id ?? ""),
        title: nextDraft.title,
        subtitle: nextDraft.subtitle,
        sellingPoints: nextDraft.sellingPoints,
        seoKeywords: nextDraft.seoKeywords,
        moduleOrder: nextModuleOrder
      });
    },
    [id, setDetailDraft]
  );

  const loadDetail = useCallback(async () => {
    if (!id) {
      setError("Missing detail ID");
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const nextDetail = await api.detail.get(id);
      const editable = buildEditableDetail(nextDetail);
      const persistedOrderFromApi = await api.detail.getModuleOrder(id);
      const fallbackModuleOrder = toList(nextDetail.moduleOrder);
      const nextModuleOrder =
        persistedOrderFromApi.length > 0
          ? persistedOrderFromApi
          : fallbackModuleOrder.length > 0
            ? fallbackModuleOrder
            : [...DEFAULT_DETAIL_MODULE_ORDER];

      setDetail(nextDetail);
      setDraft(editable);
      setModuleOrder(nextModuleOrder);
      setPersistedModuleOrder(nextModuleOrder);
      setHasLocalChanges(false);
      setSaveMessage(null);
      syncWorkbenchDraft(editable, nextModuleOrder, nextDetail.id ?? id);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Failed to load detail");
    } finally {
      setLoading(false);
    }
  }, [id, syncWorkbenchDraft]);

  useEffect(() => {
    void loadDetail();
  }, [loadDetail]);

  useEffect(() => {
    if (storedModuleOrder.length > 0) {
      setModuleOrder(storedModuleOrder);
    }
  }, [id, storedModuleOrder]);

  const contentTaskQuery = useQuery({
    queryKey: ["product-content-tasks", id],
    queryFn: async () =>
      api.productContentTasks.list({
        pageNum: 1,
        pageSize: 10,
        productDetailId: Number(id)
      }),
    enabled: Boolean(id)
  });

  const contentTaskRecords = contentTaskQuery.data?.data ?? [];
  const selectedContentTask = useMemo(
    () => contentTaskRecords.find((record) => String(record.id ?? "") === selectedContentTaskId) ?? contentTaskRecords[0] ?? null,
    [contentTaskRecords, selectedContentTaskId]
  );

  useEffect(() => {
    if (contentTaskRecords.length === 0) {
      setSelectedContentTaskId("");
      return;
    }

    setSelectedContentTaskId((current) => {
      if (current && contentTaskRecords.some((record) => String(record.id ?? "") === current)) {
        return current;
      }
      return String(contentTaskRecords[0]?.id ?? "");
    });
  }, [contentTaskRecords]);

  const compositionQuery = useQuery({
    queryKey: ["detail-compositions", id],
    queryFn: async () =>
      api.detailCompositions.list({
        pageNum: 1,
        pageSize: 10,
        productDetailId: Number(id)
      }),
    enabled: Boolean(id),
    refetchInterval: (query) => {
      const records = query.state.data?.data ?? [];
      return records.some((record) => !isTerminalCompositionStatus(record.status)) ? COMPOSITION_REFRESH_INTERVAL_MS : false;
    }
  });

  const compositionRecords = compositionQuery.data?.data ?? [];
  const selectedComposition = useMemo(
    () => compositionRecords.find((record) => String(record.id ?? "") === selectedCompositionId) ?? compositionRecords[0] ?? null,
    [compositionRecords, selectedCompositionId]
  );

  useEffect(() => {
    if (compositionRecords.length === 0) {
      setSelectedCompositionId("");
      return;
    }

    setSelectedCompositionId((current) => {
      if (current && compositionRecords.some((record) => String(record.id ?? "") === current)) {
        return current;
      }
      return String(compositionRecords[0]?.id ?? "");
    });
  }, [compositionRecords]);

  const qualityChecksQuery = useQuery({
    queryKey: ["detail-composition-quality-checks", selectedComposition?.id],
    queryFn: async () => {
      if (!selectedComposition?.id) {
        throw new Error("Missing composition ID");
      }
      return api.detailCompositions.listQualityChecks(selectedComposition.id, {
        pageNum: 1,
        pageSize: 20
      });
    },
    enabled: Boolean(selectedComposition?.id),
    refetchInterval: (query) => {
      const records = query.state.data?.data ?? [];
      return records.some((record) => !isTerminalCompositionStatus(record.status)) ? COMPOSITION_REFRESH_INTERVAL_MS : false;
    }
  });

  const qualityCheckRecords = qualityChecksQuery.data?.data ?? [];
  const latestQualityCheck = qualityCheckRecords[0] ?? null;
  const selectedQualityCheck = useMemo(
    () => qualityCheckRecords.find((record) => String(record.id ?? "") === selectedQualityCheckId) ?? latestQualityCheck,
    [latestQualityCheck, qualityCheckRecords, selectedQualityCheckId]
  );

  useEffect(() => {
    if (qualityCheckRecords.length === 0) {
      setSelectedQualityCheckId("");
      return;
    }

    setSelectedQualityCheckId((current) => {
      if (current && qualityCheckRecords.some((record) => String(record.id ?? "") === current)) {
        return current;
      }
      return String(qualityCheckRecords[0]?.id ?? "");
    });
  }, [qualityCheckRecords]);

  useEffect(() => {
    setManifestDownloadError(null);
  }, [selectedComposition?.id]);

  const manifestQuery = useQuery({
    queryKey: ["detail-composition-manifest", selectedComposition?.id],
    queryFn: async () => {
      if (!selectedComposition?.id) {
        throw new Error("Missing composition ID");
      }
      return api.detailCompositions.getDeliveryManifest(selectedComposition.id);
    },
    enabled: Boolean(selectedComposition?.id),
    staleTime: 10_000,
    refetchInterval: () => {
      const qualityCheckRecordsPending = qualityChecksQuery.data?.data ?? [];
      const compositionPending = !isTerminalCompositionStatus(selectedComposition?.status);
      return compositionPending || qualityCheckRecordsPending.some((record) => !isTerminalCompositionStatus(record.status))
        ? COMPOSITION_REFRESH_INTERVAL_MS
        : false;
    }
  });
  const deliveryManifest = manifestQuery.data ?? null;
  const hasRealCompositionOutput = Boolean(
    selectedComposition?.status === "SUCCEEDED" &&
      selectedComposition.outputPath &&
      (selectedComposition.outputFileSize ?? 0) > 0
  );
  const canPreviewCompositionFile = Boolean(
    deliveryManifest?.deliverable &&
      deliveryManifest.outputPath &&
      (deliveryManifest.outputFileSize ?? 0) > 0
  );

  useEffect(() => {
    if (!selectedComposition?.id || !canPreviewCompositionFile) {
      setCompositionPreviewUrl("");
      setCompositionPreviewError(null);
      setCompositionPreviewLoading(false);
      return;
    }

    let active = true;
    let nextPreviewUrl = "";
    setCompositionPreviewError(null);
    setCompositionPreviewLoading(true);

    void api.detailCompositions
      .download(selectedComposition.id)
      .then(({ blob }) => {
        if (!active) {
          return;
        }
        nextPreviewUrl = URL.createObjectURL(blob);
        setCompositionPreviewUrl(nextPreviewUrl);
      })
      .catch((error) => {
        if (!active) {
          return;
        }
        setCompositionPreviewUrl("");
        setCompositionPreviewError(error instanceof Error ? error.message : "Failed to load composition preview");
      })
      .finally(() => {
        if (active) {
          setCompositionPreviewLoading(false);
        }
      });

    return () => {
      active = false;
      if (nextPreviewUrl) {
        URL.revokeObjectURL(nextPreviewUrl);
      }
    };
  }, [canPreviewCompositionFile, selectedComposition?.id]);

  const createCompositionMutation = useMutation({
    mutationFn: async () => {
      if (!id || !detail) {
        throw new Error("Missing detail data");
      }

      return api.detailCompositions.create({
        productDetailId: Number(id),
        taskName: draft.title || detail.title || detail.productName || "detail composition",
        toolCode: "imagemagick",
        detailData: buildCompositionDetailData(detail, draft, moduleOrder),
        moduleOrder
      });
    },
    onSuccess: async (createdId) => {
      setSelectedCompositionId(String(createdId));
      await queryClient.invalidateQueries({ queryKey: ["detail-compositions", id] });
      await compositionQuery.refetch();
    }
  });

  const createQualityCheckMutation = useMutation({
    mutationFn: async (compositionId: number) => api.detailCompositions.createQualityCheck(compositionId),
    onSuccess: async (_, compositionId) => {
      setManifestDownloadError(null);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["detail-composition-quality-checks", compositionId] }),
        queryClient.invalidateQueries({ queryKey: ["detail-composition-manifest", compositionId] }),
        queryClient.invalidateQueries({ queryKey: ["detail-compositions", id] })
      ]);
      await qualityChecksQuery.refetch();
      await manifestQuery.refetch();
    },
    onError: (error) => {
      setManifestDownloadError(error instanceof Error ? error.message : "Failed to create quality check");
    }
  });

  const createContentTaskMutation = useMutation({
    mutationFn: async () => {
      if (!id || !detail) {
        throw new Error("Missing detail data");
      }

      return api.productContentTasks.create({
        productDetailId: Number(id),
        materialId: detail.materialId,
        taskName: draft.title || detail.title || detail.productName || "product content task",
        toolCode: "ai-relay",
        inputData: {
          sourceType: "DETAIL_DRAFT",
          productName: detail.productName ?? detail.title,
          category: detail.category,
          sku: detail.sku,
          price: detail.price,
          brandId: detail.brandId,
          brandName: detail.brandName,
          currentDraft: {
            title: draft.title,
            subtitle: draft.subtitle,
            sellingPoints: draft.sellingPoints,
            seoKeywords: draft.seoKeywords,
            description: detail.description,
            moduleOrder
          }
        }
      });
    },
    onSuccess: async (task) => {
      setSelectedContentTaskId(String(task.id ?? ""));
      await queryClient.invalidateQueries({ queryKey: ["product-content-tasks", id] });
      await contentTaskQuery.refetch();
    }
  });

  const applyContentTaskMutation = useMutation({
    mutationFn: async (task: ProductContentTask) => {
      if (!task.id) {
        throw new Error("Missing content task ID");
      }
      return api.productContentTasks.apply(task.id, {
        fields: CONTENT_APPLY_FIELDS
      });
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["product-content-tasks", id] });
      await contentTaskQuery.refetch();
      await loadDetail();
    }
  });

  const attachmentSummary = useMemo<AttachmentSummaryItem[]>(() => {
    const images = toList(detail?.images);
    const videos = toList(detail?.videos);
    const documents = toList(detail?.documents);

    return [
      { label: "Images", value: images.length, hint: "Only counts backend-returned references. OCR is handled separately." },
      { label: "Videos", value: videos.length, hint: "Video references remain read-only in this editor." },
      { label: "Documents", value: documents.length, hint: "PDF and attachment preview stay in placeholder mode when backend data is empty." }
    ];
  }, [detail?.documents, detail?.images, detail?.videos]);

  const previewContent = useMemo(() => formatAiContent(detail?.aiGeneratedContent), [detail?.aiGeneratedContent]);

  const updateDraft = <Key extends keyof EditableDetail>(key: Key, value: EditableDetail[Key]) => {
    const nextDraft = { ...draft, [key]: value };
    setDraft(nextDraft);
    setHasLocalChanges(true);
    setSaveMessage(null);
    syncWorkbenchDraft(nextDraft, moduleOrder, detail?.id);
  };

  const handleModuleOrderChange = (nextOrder: string[]) => {
    setModuleOrder(nextOrder);
    setHasLocalChanges(true);
    setSaveMessage(null);
    syncWorkbenchDraft(draft, nextOrder, detail?.id);
  };

  const handleResetDraft = () => {
    if (!detail) {
      return;
    }

    const nextDraft = buildEditableDetail(detail);
    setDraft(nextDraft);
    setModuleOrder(persistedModuleOrder);
    setHasLocalChanges(false);
    setSaveMessage(null);
    syncWorkbenchDraft(nextDraft, persistedModuleOrder, detail.id);
  };

  const handleSaveDraft = async () => {
    if (!id) {
      setSaveMessage("Missing detail ID");
      return;
    }

    setSaving(true);
    setSaveMessage(null);

    try {
      await api.detail.update(id, {
        title: draft.title,
        subtitle: draft.subtitle,
        sellingPoints: draft.sellingPoints,
        seoKeywords: draft.seoKeywords
      });
      await api.detail.updateModuleOrder(id, moduleOrder);
      await loadDetail();
      setSaveMessage("Saved to backend");
    } catch (requestError) {
      setSaveMessage(requestError instanceof Error ? requestError.message : "Failed to save draft");
    } finally {
      setSaving(false);
    }
  };

  const handleCreateComposition = async () => {
    if (!id || !detail) {
      return;
    }

    await createCompositionMutation.mutateAsync();
  };

  const handleDownloadComposition = async () => {
    if (!selectedComposition?.id || !canPreviewCompositionFile) {
      return;
    }

    try {
      const download = await api.detailCompositions.download(selectedComposition.id);
      const link = document.createElement("a");
      const objectUrl = URL.createObjectURL(download.blob);
      link.href = objectUrl;
      link.download = download.fileName;
      document.body.append(link);
      link.click();
      link.remove();
      window.setTimeout(() => URL.revokeObjectURL(objectUrl), 0);
    } catch (error) {
          setCompositionPreviewError(error instanceof Error ? error.message : "Failed to download composition output");
    }
  };

  const handleCreateQualityCheck = async () => {
    if (!selectedComposition?.id) {
      return;
    }

    setManifestDownloadError(null);
    await createQualityCheckMutation.mutateAsync(selectedComposition.id);
  };

  const handleDownloadManifest = async () => {
    if (!selectedComposition?.id) {
      return;
    }

    try {
      const manifest = deliveryManifest ?? (await api.detailCompositions.getDeliveryManifest(selectedComposition.id));
      const blob = new Blob([JSON.stringify(manifest, null, 2)], {
        type: "application/json;charset=utf-8"
      });
      const objectUrl = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = objectUrl;
      link.download = `detail-composition-${selectedComposition.id}-manifest.json`;
      document.body.append(link);
      link.click();
      link.remove();
      window.setTimeout(() => URL.revokeObjectURL(objectUrl), 0);
    } catch (error) {
      setManifestDownloadError(error instanceof Error ? error.message : "Failed to download delivery manifest");
    }
  };

  if (loading) {
    return (
      <main className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
        <GlassPanel>
          <div className="h-32 animate-pulse rounded-lg bg-slate-100" />
        </GlassPanel>
      </main>
    );
  }

  if (error) {
    return (
      <main className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
        <GlassPanel title="Detail Editor" subtitle={`Detail ID: ${id ?? "-"}`}>
          <EmptyState title="Failed to load detail" description={error} />
          <button
            type="button"
            className="mt-4 rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700"
            onClick={() => void loadDetail()}
          >
            Reload
          </button>
        </GlassPanel>
      </main>
    );
  }

  return (
    <main className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
      <header className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <p className="text-sm font-medium text-slate-500">Detail editor</p>
          <h1 className="mt-1 text-2xl font-semibold text-slate-950">
            {draft.title || detail?.productName || "Untitled detail"}
          </h1>
          <p className="mt-2 max-w-3xl text-sm text-slate-500">
            Reads real detail content from `GET /api/v1/detail/{id}` and persists editable fields plus module order
            through backend `PUT` endpoints.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <StatusTag value={detail?.auditStatus} />
          <RiskTag value={detail?.riskLevel} />
        </div>
      </header>

      <AiPendingNotice
        title="Local AI service pending"
        message="The page shows real backend content only. It does not call unconfigured generation services or create fake AI output."
      />

      <section className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(360px,0.85fr)]">
        <div className="flex min-w-0 flex-col gap-6">
          <GlassPanel title="Basic content" subtitle="Edits here are saved through the existing detail backend endpoint.">
            <div className="grid gap-4">
              <label className="grid gap-2">
                <span className="text-sm font-medium text-slate-700">Title</span>
                <input
                  className="min-h-10 rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-950 outline-none focus:border-blue-500"
                  value={draft.title}
                  onChange={(event) => updateDraft("title", event.target.value)}
                  placeholder="Enter detail title"
                />
              </label>

              <label className="grid gap-2">
                <span className="text-sm font-medium text-slate-700">Subtitle</span>
                <input
                  className="min-h-10 rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-950 outline-none focus:border-blue-500"
                  value={draft.subtitle}
                  onChange={(event) => updateDraft("subtitle", event.target.value)}
                  placeholder="Enter subtitle"
                />
              </label>

              <label className="grid gap-2">
                <span className="text-sm font-medium text-slate-700">Selling points</span>
                <textarea
                  className="min-h-36 resize-y rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-950 outline-none focus:border-blue-500"
                  value={toMultiline(draft.sellingPoints)}
                  onChange={(event) => updateDraft("sellingPoints", fromMultiline(event.target.value))}
                  placeholder="One selling point per line"
                />
              </label>

              <label className="grid gap-2">
                <span className="text-sm font-medium text-slate-700">SEO keywords</span>
                <textarea
                  className="min-h-24 resize-y rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-950 outline-none focus:border-blue-500"
                  value={toMultiline(draft.seoKeywords)}
                  onChange={(event) => updateDraft("seoKeywords", fromMultiline(event.target.value))}
                  placeholder="One keyword per line"
                />
              </label>
            </div>

            <div className="mt-5 flex flex-col gap-3 border-t border-slate-200 pt-4 sm:flex-row sm:items-center sm:justify-between">
              <p className="text-sm text-slate-500">
                {saveMessage ?? (hasLocalChanges ? "Local draft has unsaved changes." : "Draft matches current backend data.")}
              </p>
              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600"
                  onClick={handleResetDraft}
                >
                  Reset local draft
                </button>
                <button
                  type="button"
                  className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-500"
                  disabled={!hasLocalChanges || saving}
                  title="PUT /api/v1/detail/{id}"
                  onClick={() => void handleSaveDraft()}
                >
                  {saving ? "Saving..." : "Save draft"}
                </button>
              </div>
            </div>
          </GlassPanel>

          <GlassPanel
            title="Product content tasks"
            subtitle="Creates and applies persisted AI relay content tasks. Empty or failed relay output stays visible as real task state."
          >
            <div className="flex flex-col gap-4">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <p className="text-sm text-slate-500">
                  The request snapshot includes the current detail draft and product metadata. Applying a task only updates
                  selected fields and keeps other manual content untouched.
                </p>
                <div className="flex flex-wrap gap-2">
                  <button
                    type="button"
                    className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600"
                    onClick={() => void contentTaskQuery.refetch()}
                  >
                    Refresh
                  </button>
                  <button
                    type="button"
                    className="rounded-md bg-slate-950 px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-500"
                    disabled={createContentTaskMutation.isPending || !detail}
                    onClick={() => void createContentTaskMutation.mutateAsync()}
                  >
                    {createContentTaskMutation.isPending ? "Creating..." : "Create content task"}
                  </button>
                </div>
              </div>

              {createContentTaskMutation.isError ? (
                <div className="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
                  {createContentTaskMutation.error instanceof Error
                    ? createContentTaskMutation.error.message
                    : "Failed to create content task"}
                </div>
              ) : null}
              {applyContentTaskMutation.isError ? (
                <div className="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
                  {applyContentTaskMutation.error instanceof Error
                    ? applyContentTaskMutation.error.message
                    : "Failed to apply content task"}
                </div>
              ) : null}

              {contentTaskQuery.isError ? (
                <ErrorState
                  title="Content task list failed"
                  description={contentTaskQuery.error instanceof Error ? contentTaskQuery.error.message : "Failed to load product content tasks"}
                  onRetry={() => void contentTaskQuery.refetch()}
                />
              ) : contentTaskQuery.isPending ? (
                <LoadingState title="Loading content tasks" description="GET /api/v1/product-content-tasks" />
              ) : contentTaskRecords.length === 0 ? (
                <EmptyState
                  title="No content tasks yet"
                  description="Create a real content task. If AI relay is not configured, the backend will persist CANCELED instead of fake copy."
                  action={
                    <button
                      type="button"
                      className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600"
                      disabled={createContentTaskMutation.isPending || !detail}
                      onClick={() => void createContentTaskMutation.mutateAsync()}
                    >
                      Create content task
                    </button>
                  }
                />
              ) : (
                <div className="grid gap-4 xl:grid-cols-[minmax(220px,0.7fr)_minmax(0,1fr)]">
                  <section className="rounded-lg border border-slate-200 bg-slate-50 p-4">
                    <div className="flex items-center justify-between gap-3">
                      <h3 className="text-sm font-medium text-slate-950">Task history</h3>
                      <span className="text-xs text-slate-500">{contentTaskRecords.length} record(s)</span>
                    </div>
                    <div className="mt-3 grid gap-2">
                      {contentTaskRecords.map((record) => {
                        const meta = getCompositionStatusMeta(record.status);
                        const isSelected = String(record.id ?? "") === selectedContentTaskId;
                        return (
                          <button
                            key={String(record.id ?? `${record.productDetailId ?? "content"}-${record.createTime ?? ""}`)}
                            type="button"
                            className={`flex w-full flex-col gap-1 rounded-lg border px-4 py-3 text-left transition ${
                              isSelected ? "border-blue-500 bg-blue-50" : "border-slate-200 bg-white hover:border-slate-300"
                            }`}
                            onClick={() => setSelectedContentTaskId(String(record.id ?? ""))}
                          >
                            <div className="flex items-center justify-between gap-3">
                              <p className="truncate text-sm font-medium text-slate-950">
                                #{record.id ?? "--"} {record.taskName || "content task"}
                              </p>
                              <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-700">
                                {meta.label}
                              </span>
                            </div>
                            <p className="truncate text-xs text-slate-500">
                              {record.errorMessage || record.title || record.outputText || "Waiting for persisted output"}
                            </p>
                          </button>
                        );
                      })}
                    </div>
                  </section>

                  <section className="rounded-lg border border-slate-200 bg-white p-4">
                    {selectedContentTask ? (
                      <div className="grid gap-4">
                        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                          <div>
                            <p className="text-sm font-medium text-slate-950">{selectedContentTask.title || "No title output"}</p>
                            <p className="mt-1 text-sm text-slate-500">{selectedContentTask.subtitle || "No subtitle output"}</p>
                          </div>
                          <button
                            type="button"
                            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-500"
                            disabled={selectedContentTask.status !== "SUCCEEDED" || applyContentTaskMutation.isPending}
                            onClick={() => void applyContentTaskMutation.mutateAsync(selectedContentTask)}
                          >
                            {applyContentTaskMutation.isPending ? "Applying..." : "Apply selected fields"}
                          </button>
                        </div>

                        <div className="grid gap-3 sm:grid-cols-2">
                          <div className="rounded-md bg-slate-50 p-3">
                            <p className="text-xs font-medium uppercase text-slate-500">Selling points</p>
                            <ul className="mt-2 grid gap-1 text-sm text-slate-700">
                              {(selectedContentTask.sellingPoints ?? []).map((item) => (
                                <li key={item}>{item}</li>
                              ))}
                            </ul>
                          </div>
                          <div className="rounded-md bg-slate-50 p-3">
                            <p className="text-xs font-medium uppercase text-slate-500">SEO keywords</p>
                            <p className="mt-2 text-sm text-slate-700">
                              {(selectedContentTask.seoKeywords ?? []).join(", ") || "No keywords persisted"}
                            </p>
                          </div>
                        </div>

                        <div className="rounded-md bg-slate-50 p-3">
                          <p className="text-xs font-medium uppercase text-slate-500">Risk warnings</p>
                          <p className="mt-2 text-sm text-slate-700">
                            {(selectedContentTask.riskWarnings ?? []).join("; ") || "No risk warnings persisted"}
                          </p>
                        </div>

                        <pre className="max-h-72 overflow-auto whitespace-pre-wrap break-words rounded-md bg-slate-950 p-3 text-xs leading-6 text-slate-50">
                          {JSON.stringify(selectedContentTask.outputData ?? {}, null, 2)}
                        </pre>
                      </div>
                    ) : (
                      <EmptyState title="No content task selected" description="Select a persisted task to inspect its real output." />
                    )}
                  </section>
                </div>
              )}
            </div>
          </GlassPanel>

          <GlassPanel
            title="Module order"
            subtitle="The sortable list reads and saves the real backend module order. No fake layout result is generated."
          >
            <SortableModuleBoard
              items={moduleOrder}
              onChange={handleModuleOrderChange}
              emptyText="No modules available"
              description="Order persists to PUT /api/v1/detail/{id}/module-order when you save the draft."
            />
          </GlassPanel>

          <GlassPanel title="Attachment summary" subtitle="Counts are based only on backend-returned asset references.">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
              {attachmentSummary.map((item) => (
                <div key={item.label} className="rounded-lg border border-slate-200 bg-slate-50 p-4">
                  <p className="text-sm text-slate-500">{item.label}</p>
                  <p className="mt-2 text-2xl font-semibold text-slate-950">{item.value}</p>
                  <p className="mt-2 text-xs leading-5 text-slate-500">{item.hint}</p>
                </div>
              ))}
            </div>
          </GlassPanel>

          <GlassPanel
            title="Detail composition"
            subtitle="Creates a real stitched PNG job from the current draft and only shows persisted backend results."
          >
            <div className="flex flex-col gap-4">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <p className="text-sm text-slate-500">
                  This job uses the current title, subtitle, selling points, SEO keywords, and module order as the input
                  snapshot. No local preview or success state is fabricated.
                </p>
                <div className="flex flex-wrap gap-2">
                  <button
                    type="button"
                    className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600"
                    onClick={() => void compositionQuery.refetch()}
                  >
                    Refresh
                  </button>
                  <button
                    type="button"
                    className="rounded-md bg-slate-950 px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-500"
                    disabled={createCompositionMutation.isPending || !detail}
                    onClick={() => void handleCreateComposition()}
                  >
                    {createCompositionMutation.isPending ? "Creating..." : "Create composition"}
                  </button>
                </div>
              </div>

              {compositionQuery.isError ? (
                <ErrorState
                  title="Composition list failed"
                  description={compositionQuery.error instanceof Error ? compositionQuery.error.message : "Failed to load detail compositions"}
                  onRetry={() => void compositionQuery.refetch()}
                />
              ) : compositionQuery.isPending ? (
                <LoadingState title="Loading detail compositions" description="GET /api/v1/detail-compositions/list" />
              ) : compositionRecords.length === 0 ? (
                <EmptyState
                  title="No composition jobs yet"
                  description="Create a real composition job to generate a persisted stitched PNG output."
                  action={
                    <button
                      type="button"
                      className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600"
                      onClick={() => void handleCreateComposition()}
                    >
                      Create composition
                    </button>
                  }
                />
              ) : (
                <div className="grid gap-2">
                  {compositionRecords.map((record) => {
                    const meta = getCompositionStatusMeta(record.status);
                    const isSelected = String(record.id ?? "") === selectedCompositionId;
                    return (
                      <button
                        key={String(record.id ?? `${record.productDetailId ?? "composition"}-${record.createTime ?? ""}`)}
                        type="button"
                        className={`flex w-full flex-col gap-1 rounded-lg border px-4 py-3 text-left transition ${
                          isSelected ? "border-blue-500 bg-blue-50" : "border-slate-200 bg-white hover:border-slate-300"
                        }`}
                        onClick={() => setSelectedCompositionId(String(record.id ?? ""))}
                      >
                        <div className="flex items-center justify-between gap-4">
                          <div className="min-w-0">
                            <p className="truncate text-sm font-medium text-slate-950">
                              #{record.id ?? "--"} {record.taskName || "Untitled composition"}
                            </p>
                            <p className="truncate text-xs text-slate-500">
                              {record.outputPath || record.errorMessage || "Waiting for persisted output"}
                            </p>
                          </div>
                          <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${meta.color === "success" ? "bg-emerald-50 text-emerald-700" : meta.color === "error" ? "bg-rose-50 text-rose-700" : meta.color === "processing" ? "bg-blue-50 text-blue-700" : "bg-slate-100 text-slate-700"}`}>
                            {meta.label}
                          </span>
                        </div>
                      </button>
                    );
                  })}
                </div>
              )}

              {selectedComposition ? (
                <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(320px,0.9fr)]">
                  <section className="rounded-lg border border-slate-200 bg-slate-50 p-4">
                    <dl className="grid gap-3 text-sm">
                      <div className="flex items-center justify-between gap-4">
                        <dt className="text-slate-500">Status</dt>
                        <dd className="font-medium text-slate-800">{getCompositionStatusMeta(selectedComposition.status).label}</dd>
                      </div>
                      <div className="flex items-center justify-between gap-4">
                        <dt className="text-slate-500">Progress</dt>
                        <dd className="font-medium text-slate-800">{selectedComposition.progress ?? 0}%</dd>
                      </div>
                      <div className="flex items-center justify-between gap-4">
                        <dt className="text-slate-500">Tool</dt>
                        <dd className="font-medium text-slate-800">{selectedComposition.toolCode || "--"}</dd>
                      </div>
                      <div className="flex items-center justify-between gap-4">
                        <dt className="text-slate-500">External job ID</dt>
                        <dd className="font-medium text-slate-800">{selectedComposition.externalJobId || "--"}</dd>
                      </div>
                      <div className="flex items-center justify-between gap-4">
                        <dt className="text-slate-500">Output path</dt>
                        <dd className="max-w-[240px] truncate font-medium text-slate-800">{selectedComposition.outputPath || "--"}</dd>
                      </div>
                      <div className="flex items-center justify-between gap-4">
                        <dt className="text-slate-500">Output size</dt>
                        <dd className="font-medium text-slate-800">{formatBytes(selectedComposition.outputFileSize)}</dd>
                      </div>
                      <div className="flex items-center justify-between gap-4">
                        <dt className="text-slate-500">Dimensions</dt>
                        <dd className="font-medium text-slate-800">
                          {selectedComposition.outputWidth && selectedComposition.outputHeight
                            ? `${selectedComposition.outputWidth} × ${selectedComposition.outputHeight}`
                            : "--"}
                        </dd>
                      </div>
                      <div className="grid gap-1">
                        <dt className="text-slate-500">Error</dt>
                        <dd className="rounded-md bg-white p-3 text-slate-700">
                          {selectedComposition.errorMessage || "No backend error"}
                        </dd>
                      </div>
                      <div className="grid gap-1">
                        <dt className="text-slate-500">Input snapshot</dt>
                        <dd className="max-h-64 overflow-auto rounded-md bg-white p-3 text-xs leading-6 text-slate-700">
                          <pre className="whitespace-pre-wrap break-words">
                            {JSON.stringify(selectedComposition.inputData ?? {}, null, 2)}
                          </pre>
                        </dd>
                      </div>
                    </dl>
                  </section>

                  <section className="rounded-lg border border-slate-200 bg-white p-4">
                    <div className="flex items-center justify-between gap-3">
                      <div>
                        <p className="text-sm font-medium text-slate-950">Preview</p>
                        <p className="text-xs text-slate-500">
                          {canPreviewCompositionFile
                            ? "Renders the real PNG blob returned by the backend."
                            : "Preview becomes available only after the backend marks the manifest deliverable and a real PNG exists."}
                        </p>
                      </div>
                      <button
                        type="button"
                        className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600 disabled:cursor-not-allowed disabled:text-slate-400"
                        disabled={!canPreviewCompositionFile}
                        onClick={() => void handleDownloadComposition()}
                      >
                        Download
                      </button>
                    </div>

                    <div className="mt-4 min-h-[260px] rounded-lg border border-dashed border-slate-200 bg-slate-50 p-4">
                      {compositionPreviewLoading ? (
                        <LoadingState title="Loading preview" description="Fetching the real PNG blob from /detail-compositions/{id}/download" />
                      ) : compositionPreviewError ? (
                        <EmptyState title="Preview unavailable" description={compositionPreviewError} />
                      ) : compositionPreviewUrl ? (
                        <img
                          src={compositionPreviewUrl}
                          alt={selectedComposition.taskName || "detail composition"}
                          className="block max-h-[640px] w-full rounded-md object-contain"
                        />
                      ) : (
                        <EmptyState
                          title="No persisted preview"
                          description="A real PNG is required before the preview can be rendered."
                        />
                      )}
                    </div>
                  </section>
                </div>
              ) : null}

              {selectedComposition ? (
                <section className="rounded-lg border border-slate-200 bg-white p-4">
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                      <p className="text-sm font-medium text-slate-950">Visual QA and manifest</p>
                      <p className="text-xs text-slate-500">
                        Creates a real Playwright QA job for the selected composition and reads only persisted QA and
                        delivery-manifest data.
                      </p>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      <button
                        type="button"
                        className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600"
                        onClick={() => void qualityChecksQuery.refetch()}
                      >
                        Refresh QA
                      </button>
                      <button
                        type="button"
                        className="rounded-md bg-slate-950 px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-500"
                        disabled={
                          createQualityCheckMutation.isPending ||
                          !hasRealCompositionOutput
                        }
                        onClick={() => void handleCreateQualityCheck()}
                      >
                        {createQualityCheckMutation.isPending ? "Creating QA..." : "Create QA"}
                      </button>
                      <button
                        type="button"
                        className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600 disabled:cursor-not-allowed disabled:text-slate-400"
                        disabled={!deliveryManifest || manifestQuery.isFetching}
                        onClick={() => void handleDownloadManifest()}
                      >
                        Download manifest
                      </button>
                    </div>
                  </div>

                  {manifestDownloadError ? (
                    <div className="mt-3 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
                      {manifestDownloadError}
                    </div>
                  ) : null}

                  <div className="mt-4">
                    {qualityChecksQuery.isError ? (
                      <ErrorState
                        title="QA history failed"
                        description={
                          qualityChecksQuery.error instanceof Error
                            ? qualityChecksQuery.error.message
                            : "Failed to load QA history"
                        }
                        onRetry={() => void qualityChecksQuery.refetch()}
                      />
                    ) : qualityChecksQuery.isPending ? (
                      <LoadingState title="Loading QA history" description="GET /api/v1/detail-compositions/{id}/quality-checks/list" />
                    ) : qualityCheckRecords.length === 0 ? (
                      <EmptyState
                        title="No QA records yet"
                        description="Create a real QA job after a stitched PNG exists. The page stays empty until persisted rows are available."
                        action={
                          <button
                            type="button"
                            className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600 disabled:cursor-not-allowed disabled:text-slate-400"
                            disabled={createQualityCheckMutation.isPending || !hasRealCompositionOutput}
                            onClick={() => void handleCreateQualityCheck()}
                          >
                            Create QA
                          </button>
                        }
                      />
                    ) : (
                      <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(320px,0.9fr)]">
                        <section className="rounded-lg border border-slate-200 bg-slate-50 p-4">
                          <div className="flex items-center justify-between gap-3">
                            <h3 className="text-sm font-medium text-slate-950">QA history</h3>
                            <span className="text-xs text-slate-500">{qualityCheckRecords.length} record(s)</span>
                          </div>
                          <div className="mt-3 grid gap-2">
                            {qualityCheckRecords.map((record) => {
                              const meta = getCompositionStatusMeta(record.status);
                              const isSelected = String(record.id ?? "") === selectedQualityCheckId;
                              return (
                                <button
                                  key={String(record.id ?? `${record.detailCompositionId ?? "qa"}-${record.createTime ?? ""}`)}
                                  type="button"
                                  className={`flex w-full flex-col gap-1 rounded-lg border px-4 py-3 text-left transition ${
                                    isSelected ? "border-blue-500 bg-blue-50" : "border-slate-200 bg-white hover:border-slate-300"
                                  }`}
                                  onClick={() => setSelectedQualityCheckId(String(record.id ?? ""))}
                                >
                                  <div className="flex items-center justify-between gap-4">
                                    <div className="min-w-0">
                                      <p className="truncate text-sm font-medium text-slate-950">
                                        #{record.id ?? "--"} {record.toolCode || "QA job"}
                                      </p>
                                      <p className="truncate text-xs text-slate-500">
                                        {record.screenshotPath || record.errorMessage || "Waiting for persisted QA output"}
                                      </p>
                                    </div>
                                    <span
                                      className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                                        meta.color === "success"
                                          ? "bg-emerald-50 text-emerald-700"
                                          : meta.color === "error"
                                            ? "bg-rose-50 text-rose-700"
                                            : meta.color === "processing"
                                              ? "bg-blue-50 text-blue-700"
                                              : "bg-slate-100 text-slate-700"
                                      }`}
                                    >
                                      {meta.label}
                                    </span>
                                  </div>
                                </button>
                              );
                            })}
                          </div>
                        </section>

                        <section className="rounded-lg border border-slate-200 bg-white p-4">
                          {selectedQualityCheck ? (
                            <dl className="grid gap-3 text-sm">
                              <div className="flex items-center justify-between gap-4">
                                <dt className="text-slate-500">QA status</dt>
                                <dd className="font-medium text-slate-800">{getCompositionStatusMeta(selectedQualityCheck.status).label}</dd>
                              </div>
                              <div className="flex items-center justify-between gap-4">
                                <dt className="text-slate-500">Issue count</dt>
                                <dd className="font-medium text-slate-800">{selectedQualityCheck.issueCount ?? 0}</dd>
                              </div>
                              <div className="flex items-center justify-between gap-4">
                                <dt className="text-slate-500">Tool</dt>
                                <dd className="font-medium text-slate-800">{selectedQualityCheck.toolCode || "--"}</dd>
                              </div>
                              <div className="flex items-center justify-between gap-4">
                                <dt className="text-slate-500">Screenshot path</dt>
                                <dd className="max-w-[240px] truncate font-medium text-slate-800">{selectedQualityCheck.screenshotPath || "--"}</dd>
                              </div>
                              <div className="grid gap-1">
                                <dt className="text-slate-500">Issues</dt>
                                <dd className="rounded-md bg-slate-50 p-3 text-slate-700">
                                  {selectedQualityCheck.issues?.length
                                    ? selectedQualityCheck.issues.join("；")
                                    : "No QA issues persisted"}
                                </dd>
                              </div>
                              <div className="grid gap-1">
                                <dt className="text-slate-500">Error</dt>
                                <dd className="rounded-md bg-slate-50 p-3 text-slate-700">
                                  {selectedQualityCheck.errorMessage || "No QA error"}
                                </dd>
                              </div>
                              <div className="flex items-center justify-between gap-4">
                                <dt className="text-slate-500">Check time</dt>
                                <dd className="font-medium text-slate-800">{formatTime(selectedQualityCheck.checkTime)}</dd>
                              </div>
                            </dl>
                          ) : (
                            <EmptyState title="No QA record selected" description="Select a QA record to inspect persisted issues and screenshot metadata." />
                          )}

                          <div className="mt-4 border-t border-slate-200 pt-4">
                            {manifestQuery.isError ? (
                              <ErrorState
                                title="Manifest load failed"
                                description={manifestQuery.error instanceof Error ? manifestQuery.error.message : "Failed to load delivery manifest"}
                                onRetry={() => void manifestQuery.refetch()}
                              />
                            ) : manifestQuery.isPending ? (
                              <LoadingState title="Loading manifest" description="GET /api/v1/detail-compositions/{id}/delivery-manifest" />
                            ) : deliveryManifest ? (
                              <div className="grid gap-3">
                                <div className="flex items-center justify-between gap-4">
                                  <h3 className="text-sm font-medium text-slate-950">Delivery manifest</h3>
                                  <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${deliveryManifest.deliverable ? "bg-emerald-50 text-emerald-700" : "bg-slate-100 text-slate-700"}`}>
                                    {deliveryManifest.deliverable ? "Deliverable" : "Not deliverable"}
                                  </span>
                                </div>
                                <div className="grid gap-2 text-sm">
                                  {buildManifestSummary(deliveryManifest).map((item) => (
                                    <div key={item.label} className="flex items-start justify-between gap-4">
                                      <dt className="text-slate-500">{item.label}</dt>
                                      <dd className="max-w-[220px] truncate text-right font-medium text-slate-800">{item.value}</dd>
                                    </div>
                                  ))}
                                </div>
                                <div className="grid gap-1">
                                  <dt className="text-slate-500">Generation results</dt>
                                  <dd className="rounded-md bg-slate-50 p-3 text-xs leading-6 text-slate-700">
                                    <pre className="whitespace-pre-wrap break-words">
                                      {JSON.stringify(deliveryManifest.generationResults ?? [], null, 2)}
                                    </pre>
                                  </dd>
                                </div>
                              </div>
                            ) : (
                              <EmptyState
                                title="No manifest yet"
                                description="The backend has not persisted a delivery manifest for this composition yet."
                              />
                            )}
                          </div>
                        </section>
                      </div>
                    )}
                  </div>
                </section>
              ) : null}
            </div>
          </GlassPanel>
        </div>

        <aside className="flex min-w-0 flex-col gap-6">
          <GlassPanel title="Record status" subtitle="Directly reflects persisted detail metadata and audit fields.">
            <dl className="grid gap-3 text-sm">
              <div className="flex items-center justify-between gap-4">
                <dt className="text-slate-500">Audit status</dt>
                <dd>
                  <StatusTag value={detail?.auditStatus} />
                </dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-slate-500">Risk level</dt>
                <dd>
                  <RiskTag value={detail?.riskLevel} />
                </dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-slate-500">Material ID</dt>
                <dd className="font-medium text-slate-800">{detail?.materialId ?? "--"}</dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-slate-500">Updated at</dt>
                <dd className="font-medium text-slate-800">{formatTime(detail?.updateTime)}</dd>
              </div>
              <div className="grid gap-1">
                <dt className="text-slate-500">Audit comment</dt>
                <dd className="rounded-md bg-slate-50 p-3 text-slate-700">{detail?.auditComment || "No audit comment"}</dd>
              </div>
            </dl>
          </GlassPanel>

          <GlassPanel title="AI content preview" subtitle="Displays only persisted backend content and never fabricates local output.">
            {previewContent ? (
              <pre className="max-h-[520px] overflow-auto whitespace-pre-wrap break-words rounded-lg bg-slate-950 p-4 text-xs leading-6 text-slate-50">
                {previewContent}
              </pre>
            ) : (
              <EmptyState
                title="No AI content"
                description="When the backend returns no generated content, this panel stays empty."
              />
            )}
            <button
              type="button"
              className="mt-4 w-full cursor-not-allowed rounded-md border border-dashed border-slate-300 bg-slate-50 px-4 py-2 text-sm font-medium text-slate-500"
              disabled
              title="POST /api/v1/detail/generate is intentionally not called from this page"
            >
              Local AI service pending
            </button>
          </GlassPanel>

          <GlassPanel title="Draft preview" subtitle="Reflects the current local draft before save.">
            <article className="grid gap-4">
              <div>
                <h2 className="text-xl font-semibold leading-8 text-slate-950">{draft.title || "No title yet"}</h2>
                <p className="mt-1 text-sm leading-6 text-slate-500">{draft.subtitle || "No subtitle yet"}</p>
              </div>

              <section>
                <h3 className="text-sm font-semibold text-slate-800">Selling points</h3>
                {draft.sellingPoints.length ? (
                  <ul className="mt-2 grid gap-2">
                    {draft.sellingPoints.map((point, index) => (
                      <li key={`${point}-${index}`} className="rounded-md bg-slate-50 px-3 py-2 text-sm text-slate-700">
                        {point}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="mt-2 text-sm text-slate-500">No selling points</p>
                )}
              </section>

              <section>
                <h3 className="text-sm font-semibold text-slate-800">SEO keywords</h3>
                {draft.seoKeywords.length ? (
                  <div className="mt-2 flex flex-wrap gap-2">
                    {draft.seoKeywords.map((keyword) => (
                      <span key={keyword} className="rounded-full bg-blue-50 px-2.5 py-1 text-xs font-medium text-blue-700">
                        {keyword}
                      </span>
                    ))}
                  </div>
                ) : (
                  <p className="mt-2 text-sm text-slate-500">No SEO keywords</p>
                )}
              </section>
            </article>
          </GlassPanel>
        </aside>
      </section>
    </main>
  );
}
