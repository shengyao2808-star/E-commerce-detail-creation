import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { AiPendingNotice, EmptyState, ErrorState, GlassPanel, LoadingState, RiskTag, StatusTag } from "../../components/common";
import { SortableModuleBoard } from "../../components/dnd/SortableModuleBoard";
import { api } from "../../services/api";
import { useLang } from "../../i18n";
import type {
  DetailComposition,
  DetailCompositionQualityCheck,
  DetailDeliveryManifest,
  ProductContentTask,
  ProductContentTaskApplyRequest,
  ProductDetail
} from "../../services/types";

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
      return { label: "待处理", color: "warning" };
    case "RUNNING":
    case "PROCESSING":
      return { label: "处理中", color: "processing" };
    case "SUCCEEDED":
    case "SUCCESS":
      return { label: "成功", color: "success" };
    case "FAILED":
    case "ERROR":
      return { label: "失败", color: "error" };
    case "CANCELED":
    case "CANCELLED":
      return { label: "已取消", color: "default" };
    default:
      return { label: normalized || "未知", color: "default" };
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

export default function DetailEditorPage() {
  const { t } = useLang();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();

  const [draft, setDraft] = useState<EditableDetail>({
    title: "",
    subtitle: "",
    sellingPoints: [],
    seoKeywords: []
  });
  const [saving, setSaving] = useState(false);
  const [showComposition, setShowComposition] = useState(false);

  const detailQuery = useQuery({
    queryKey: ["detail", id],
    queryFn: () => api.detail.get(id!),
    enabled: !!id
  });

  const detail = detailQuery.data;

  const compositionQuery = useQuery({
    queryKey: ["composition", id],
    queryFn: () => api.detailCompositions.get(id!),
    enabled: !!id && showComposition,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return isTerminalCompositionStatus(status) ? false : COMPOSITION_REFRESH_INTERVAL_MS;
    }
  });

  const composition = compositionQuery.data;

  const contentTaskQuery = useQuery({
    queryKey: ["contentTask", id],
    queryFn: () => api.productContentTasks.get(id!),
    enabled: !!id
  });

  const contentTask = contentTaskQuery.data;

  useEffect(() => {
    if (detail) {
      setDraft(buildEditableDetail(detail));
    }
  }, [detail]);

  const saveMutation = useMutation({
    mutationFn: async () => {
      if (!id) return;
      setSaving(true);
      try {
        await api.detail.update(id, {
          title: draft.title,
          subtitle: draft.subtitle,
          sellingPoints: JSON.stringify(draft.sellingPoints),
          seoKeywords: JSON.stringify(draft.seoKeywords)
        });
        await queryClient.invalidateQueries({ queryKey: ["detail", id] });
      } finally {
        setSaving(false);
      }
    }
  });

  const generateMutation = useMutation({
    mutationFn: async () => {
      if (!id) return;
      await api.detail.generate({ materialId: Number(id) });
      await queryClient.invalidateQueries({ queryKey: ["contentTask", id] });
    }
  });

  const applyMutation = useMutation({
    mutationFn: async () => {
      if (!id || !contentTask) return;
      await api.productContentTasks.apply(id, { fields: CONTENT_APPLY_FIELDS });
      await queryClient.invalidateQueries({ queryKey: ["detail", id] });
      await queryClient.invalidateQueries({ queryKey: ["contentTask", id] });
    }
  });

  const composeMutation = useMutation({
    mutationFn: async () => {
      if (!id) return;
      await api.detailCompositions.create({ productDetailId: Number(id) });
      setShowComposition(true);
      await queryClient.invalidateQueries({ queryKey: ["composition", id] });
    }
  });

  const handleApplyContent = useCallback(() => {
    if (!contentTask) return;
    setDraft({
      title: contentTask.title ?? draft.title,
      subtitle: contentTask.subtitle ?? draft.subtitle,
      sellingPoints: toList(contentTask.sellingPoints) || draft.sellingPoints,
      seoKeywords: toList(contentTask.seoKeywords) || draft.seoKeywords
    });
  }, [contentTask, draft]);

  const previewContent = useMemo(() => {
    if (!contentTask?.outputText) return "";
    return formatAiContent(contentTask.outputText);
  }, [contentTask?.outputText]);

  const compositionStatusMeta = useMemo(
    () => getCompositionStatusMeta(composition?.status),
    [composition?.status]
  );

  if (detailQuery.isLoading) {
    return <LoadingState title={t("common.loading")} />;
  }

  if (detailQuery.isError) {
    return <ErrorState title={t("common.error")} />;
  }

  if (!detail) {
    return (
      <EmptyState
        title={t("detail.noProduct")}
        description={t("detail.noProduct.desc")}
      />
    );
  }

  return (
    <main className="flex flex-col gap-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">{t("detail.title")}</h1>
          <p className="text-sm text-slate-500">{t("detail.desc")}</p>
        </div>
        <div className="flex gap-2">
          <button
            type="button"
            onClick={() => saveMutation.mutate()}
            disabled={saving}
            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {saving ? t("common.loading") : t("detail.save")}
          </button>
          <button
            type="button"
            onClick={() => generateMutation.mutate()}
            disabled={generateMutation.isPending}
            className="rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 disabled:opacity-50"
          >
            {generateMutation.isPending ? t("common.loading") : "生成内容"}
          </button>
          <button
            type="button"
            onClick={() => composeMutation.mutate()}
            disabled={composeMutation.isPending}
            className="rounded-md bg-purple-600 px-4 py-2 text-sm font-medium text-white hover:bg-purple-700 disabled:opacity-50"
          >
            {composeMutation.isPending ? t("common.loading") : "合成详情"}
          </button>
        </div>
      </div>

      <section className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <GlassPanel title={t("detail.content")} subtitle="编辑商品详情页的基本信息">
            <div className="grid gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700">标题</label>
                <input
                  type="text"
                  value={draft.title}
                  onChange={(e) => setDraft({ ...draft, title: e.target.value })}
                  className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  placeholder="输入商品标题"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700">副标题</label>
                <input
                  type="text"
                  value={draft.subtitle}
                  onChange={(e) => setDraft({ ...draft, subtitle: e.target.value })}
                  className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  placeholder="输入副标题"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700">卖点</label>
                <textarea
                  value={toMultiline(draft.sellingPoints)}
                  onChange={(e) => setDraft({ ...draft, sellingPoints: fromMultiline(e.target.value) })}
                  rows={4}
                  className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  placeholder="每行一个卖点"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700">SEO 关键词</label>
                <textarea
                  value={toMultiline(draft.seoKeywords)}
                  onChange={(e) => setDraft({ ...draft, seoKeywords: fromMultiline(e.target.value) })}
                  rows={3}
                  className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  placeholder="每行一个关键词"
                />
              </div>
            </div>
          </GlassPanel>

          {contentTask && (
            <GlassPanel title="AI 生成内容" subtitle="后端返回的 AI 生成内容">
              <div className="grid gap-4">
                {contentTask.title && (
                  <div>
                    <label className="block text-sm font-medium text-slate-700">AI 标题</label>
                    <p className="mt-1 text-sm text-slate-900">{contentTask.title}</p>
                  </div>
                )}
                {contentTask.subtitle && (
                  <div>
                    <label className="block text-sm font-medium text-slate-700">AI 副标题</label>
                    <p className="mt-1 text-sm text-slate-900">{contentTask.subtitle}</p>
                  </div>
                )}
                {previewContent && (
                  <div>
                    <label className="block text-sm font-medium text-slate-700">AI 内容预览</label>
                    <pre className="mt-1 max-h-[200px] overflow-auto whitespace-pre-wrap break-words rounded-lg bg-slate-50 p-3 text-xs text-slate-700">
                      {previewContent}
                    </pre>
                  </div>
                )}
                <button
                  type="button"
                  onClick={() => applyMutation.mutate()}
                  disabled={applyMutation.isPending}
                  className="rounded-md bg-orange-600 px-4 py-2 text-sm font-medium text-white hover:bg-orange-700 disabled:opacity-50"
                >
                  {applyMutation.isPending ? t("common.loading") : "应用 AI 内容"}
                </button>
              </div>
            </GlassPanel>
          )}

          {showComposition && composition && (
            <GlassPanel title="合成状态" subtitle="详情页合成进度和结果">
              <div className="grid gap-4">
                <div className="flex items-center gap-2">
                  <span className="text-sm font-medium text-slate-700">状态:</span>
                  <span className={`rounded-full px-2 py-1 text-xs font-medium ${
                    compositionStatusMeta.color === "success" ? "bg-green-100 text-green-800" :
                    compositionStatusMeta.color === "error" ? "bg-red-100 text-red-800" :
                    compositionStatusMeta.color === "warning" ? "bg-yellow-100 text-yellow-800" :
                    compositionStatusMeta.color === "processing" ? "bg-blue-100 text-blue-800" :
                    "bg-gray-100 text-gray-800"
                  }`}>
                    {compositionStatusMeta.label}
                  </span>
                </div>
              </div>
            </GlassPanel>
          )}
        </div>

        <aside className="flex min-w-0 flex-col gap-6">
          <GlassPanel title="记录状态" subtitle="直接反映持久化的详情元数据和审核字段">
            <dl className="grid gap-3 text-sm">
              <div className="flex items-center justify-between gap-4">
                <dt className="text-slate-500">审核状态</dt>
                <dd>
                  <StatusTag value={detail?.auditStatus} />
                </dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-slate-500">风险等级</dt>
                <dd>
                  <RiskTag value={detail?.riskLevel} />
                </dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-slate-500">素材 ID</dt>
                <dd className="font-medium text-slate-800">{detail?.materialId ?? "--"}</dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-slate-500">更新时间</dt>
                <dd className="font-medium text-slate-800">{formatTime(detail?.updateTime)}</dd>
              </div>
              <div className="grid gap-1">
                <dt className="text-slate-500">审核备注</dt>
                <dd className="rounded-md bg-slate-50 p-3 text-slate-700">{detail?.auditComment || "暂无审核备注"}</dd>
              </div>
            </dl>
          </GlassPanel>

          <GlassPanel title="AI 内容预览" subtitle="仅显示持久化的后端内容，从不伪造本地输出">
            {previewContent ? (
              <pre className="max-h-[520px] overflow-auto whitespace-pre-wrap break-words rounded-lg bg-slate-950 p-4 text-xs leading-6 text-slate-50">
                {previewContent}
              </pre>
            ) : (
              <EmptyState
                title="暂无 AI 内容"
                description="当后端未返回生成内容时，此面板保持为空。"
              />
            )}
            <button
              type="button"
              className="mt-4 w-full cursor-not-allowed rounded-md border border-dashed border-slate-300 bg-slate-50 px-4 py-2 text-sm font-medium text-slate-500"
              disabled
              title="POST /api/v1/detail/generate 此页面不会调用"
            >
              本地 AI 服务待接入
            </button>
          </GlassPanel>

          <GlassPanel title="草稿预览" subtitle="反映保存前的当前本地草稿">
            <article className="grid gap-4">
              <div>
                <h2 className="text-xl font-semibold leading-8 text-slate-950">{draft.title || "暂无标题"}</h2>
                <p className="mt-1 text-sm leading-6 text-slate-500">{draft.subtitle || "暂无副标题"}</p>
              </div>

              <section>
                <h3 className="text-sm font-semibold text-slate-800">卖点</h3>
                {draft.sellingPoints.length ? (
                  <ul className="mt-2 grid gap-2">
                    {draft.sellingPoints.map((point, index) => (
                      <li key={`${point}-${index}`} className="rounded-md bg-slate-50 px-3 py-2 text-sm text-slate-700">
                        {point}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="mt-2 text-sm text-slate-500">暂无卖点</p>
                )}
              </section>

              <section>
                <h3 className="text-sm font-semibold text-slate-800">SEO 关键词</h3>
                {draft.seoKeywords.length ? (
                  <div className="mt-2 flex flex-wrap gap-2">
                    {draft.seoKeywords.map((keyword) => (
                      <span key={keyword} className="rounded-full bg-blue-50 px-2.5 py-1 text-xs font-medium text-blue-700">
                        {keyword}
                      </span>
                    ))}
                  </div>
                ) : (
                  <p className="mt-2 text-sm text-slate-500">暂无 SEO 关键词</p>
                )}
              </section>
            </article>
          </GlassPanel>
        </aside>
      </section>
    </main>
  );
}