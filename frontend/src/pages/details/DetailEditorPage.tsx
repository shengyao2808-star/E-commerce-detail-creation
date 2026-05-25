import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";

import { AiPendingNotice, EmptyState, GlassPanel, RiskTag, StatusTag } from "../../components/common";
import { api } from "../../services/api";

type EditableDetail = {
  title: string;
  subtitle: string;
  sellingPoints: string[];
  seoKeywords: string[];
};

type DetailRecord = {
  id?: number;
  materialId?: number;
  productName?: string;
  brandName?: string;
  title?: string;
  subtitle?: string;
  sellingPoints?: string[] | string | null;
  seoKeywords?: string[] | string | null;
  aiGeneratedContent?: string | null;
  description?: string | null;
  images?: string[] | string | null;
  videos?: string[] | string | null;
  documents?: string[] | string | null;
  riskLevel?: string | number | null;
  riskDescription?: string | null;
  auditStatus?: number | string | null;
  auditComment?: string | null;
  creator?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
};

type AttachmentSummaryItem = {
  label: string;
  value: number;
  hint: string;
};

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
    // Backend comments say JSON arrays, but current service also writes comma-separated strings.
  }

  return trimmed
    .split(/[,，\n]/)
    .map((item) => item.trim())
    .filter(Boolean);
};

const toMultiline = (items: string[]): string => items.join("\n");

const fromMultiline = (value: string): string[] =>
  value
    .split(/\n/)
    .map((item) => item.trim())
    .filter(Boolean);

const normalizeStatusLabel = (status: string | number | null | undefined): string => {
  const statusText = String(status ?? "").toUpperCase();
  const statusMap: Record<string, string> = {
    "0": "待审核",
    "1": "审核中",
    "2": "已通过",
    "3": "已驳回",
    "4": "需修改",
    PENDING: "待审核",
    APPROVED: "已通过",
    REJECTED: "已驳回",
    RETURNED: "需修改",
  };

  return statusMap[statusText] ?? "未提交";
};

const normalizeRiskLabel = (riskLevel: string | number | null | undefined): string => {
  const riskText = String(riskLevel ?? "").toUpperCase();
  const riskMap: Record<string, string> = {
    "1": "低风险",
    "2": "中风险",
    "3": "高风险",
    LOW: "低风险",
    MEDIUM: "中风险",
    HIGH: "高风险",
    CRITICAL: "极高风险",
    EXTREME: "极高风险",
  };

  return riskMap[riskText] ?? "未检测";
};

const formatTime = (value: string | null | undefined): string => {
  if (!value) {
    return "暂无";
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
    minute: "2-digit",
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

const buildEditableDetail = (detail: DetailRecord): EditableDetail => ({
  title: detail.title ?? "",
  subtitle: detail.subtitle ?? "",
  sellingPoints: toList(detail.sellingPoints),
  seoKeywords: toList(detail.seoKeywords),
});

export default function DetailEditorPage() {
  const { id } = useParams<{ id: string }>();
  const [detail, setDetail] = useState<DetailRecord | null>(null);
  const [draft, setDraft] = useState<EditableDetail>({
    title: "",
    subtitle: "",
    sellingPoints: [],
    seoKeywords: [],
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [saveMessage, setSaveMessage] = useState<string | null>(null);
  const [hasLocalChanges, setHasLocalChanges] = useState(false);

  const loadDetail = useCallback(async () => {
    if (!id) {
      setError("缺少详情页 ID");
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const nextDetail = (await api.detail.get(id)) as DetailRecord;
      setDetail(nextDetail);
      setDraft(buildEditableDetail(nextDetail));
      setHasLocalChanges(false);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "详情页加载失败");
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    void loadDetail();
  }, [loadDetail]);

  const attachmentSummary = useMemo<AttachmentSummaryItem[]>(() => {
    const images = toList(detail?.images);
    const videos = toList(detail?.videos);
    const documents = toList(detail?.documents);

    return [
      { label: "图片", value: images.length, hint: "仅展示附件数量，图片 OCR 未实现" },
      { label: "视频", value: videos.length, hint: "视频仅作为素材引用，不做 CMS 分发" },
      { label: "文档", value: documents.length, hint: "PDF 解析与导出未实现" },
    ];
  }, [detail]);

  const previewContent = useMemo(() => formatAiContent(detail?.aiGeneratedContent), [detail?.aiGeneratedContent]);

  const updateDraft = <Key extends keyof EditableDetail>(key: Key, value: EditableDetail[Key]) => {
    setDraft((current) => ({ ...current, [key]: value }));
    setHasLocalChanges(true);
    setSaveMessage(null);
  };

  const handleSaveDraft = async () => {
    if (!id) {
      setSaveMessage("缺少详情页 ID，无法保存。");
      return;
    }

    setSaving(true);
    setSaveMessage(null);

    try {
      await api.detail.update(id, {
        title: draft.title,
        subtitle: draft.subtitle,
        sellingPoints: draft.sellingPoints,
        seoKeywords: draft.seoKeywords,
      });
      await loadDetail();
      setSaveMessage("已保存到后端。");
    } catch (requestError) {
      setSaveMessage(requestError instanceof Error ? requestError.message : "保存失败，请稍后重试。");
    } finally {
      setSaving(false);
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
        <GlassPanel title="详情页编辑器" subtitle={`详情 ID：${id ?? "-"}`}>
          <EmptyState title="详情加载失败" description={error} />
          <button
            type="button"
            className="mt-4 rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700"
            onClick={() => void loadDetail()}
          >
            重新加载
          </button>
        </GlassPanel>
      </main>
    );
  }

  return (
    <main className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
      <header className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <p className="text-sm font-medium text-slate-500">详情页编辑器</p>
          <h1 className="mt-1 text-2xl font-semibold text-slate-950">{draft.title || detail?.productName || "未命名详情页"}</h1>
          <p className="mt-2 max-w-3xl text-sm text-slate-500">
            通过真实接口 GET /api/v1/detail/{id} 读取内容，并通过 PUT /api/v1/detail/{id} 保存可编辑字段。
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <StatusTag value={detail?.auditStatus}>{normalizeStatusLabel(detail?.auditStatus)}</StatusTag>
          <RiskTag value={detail?.riskLevel}>{normalizeRiskLabel(detail?.riskLevel)}</RiskTag>
        </div>
      </header>

      <AiPendingNotice
        title="待接入本地AI服务"
        message="POST /api/v1/detail/generate 接口存在，但 AIUtil 未实现。页面不会调用该接口，也不会生成假内容。"
      />

      <section className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(360px,0.85fr)]">
        <div className="flex min-w-0 flex-col gap-6">
          <GlassPanel title="基础内容" subtitle="编辑区修改会保存到后端详情记录">
            <div className="grid gap-4">
              <label className="grid gap-2">
                <span className="text-sm font-medium text-slate-700">标题</span>
                <input
                  className="min-h-10 rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-950 outline-none focus:border-blue-500"
                  value={draft.title}
                  onChange={(event) => updateDraft("title", event.target.value)}
                  placeholder="输入商品详情页标题"
                />
              </label>

              <label className="grid gap-2">
                <span className="text-sm font-medium text-slate-700">副标题</span>
                <input
                  className="min-h-10 rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-950 outline-none focus:border-blue-500"
                  value={draft.subtitle}
                  onChange={(event) => updateDraft("subtitle", event.target.value)}
                  placeholder="输入副标题"
                />
              </label>

              <label className="grid gap-2">
                <span className="text-sm font-medium text-slate-700">核心卖点</span>
                <textarea
                  className="min-h-36 resize-y rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-950 outline-none focus:border-blue-500"
                  value={toMultiline(draft.sellingPoints)}
                  onChange={(event) => updateDraft("sellingPoints", fromMultiline(event.target.value))}
                  placeholder="每行一个卖点"
                />
              </label>

              <label className="grid gap-2">
                <span className="text-sm font-medium text-slate-700">SEO 关键词</span>
                <textarea
                  className="min-h-24 resize-y rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-950 outline-none focus:border-blue-500"
                  value={toMultiline(draft.seoKeywords)}
                  onChange={(event) => updateDraft("seoKeywords", fromMultiline(event.target.value))}
                  placeholder="每行一个关键词"
                />
              </label>
            </div>

            <div className="mt-5 flex flex-col gap-3 border-t border-slate-200 pt-4 sm:flex-row sm:items-center sm:justify-between">
              <p className="text-sm text-slate-500">
                {saveMessage ?? (hasLocalChanges ? "已有草稿变更，保存后会写入后端。" : "当前内容与接口返回数据一致。")}
              </p>
              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600"
                  onClick={() => {
                    if (detail) {
                      setDraft(buildEditableDetail(detail));
                      setHasLocalChanges(false);
                    }
                  }}
                >
                  放弃本地草稿
                </button>
                <button
                  type="button"
                  className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-500"
                  disabled={!hasLocalChanges || saving}
                  title="PUT /api/v1/detail/{id}"
                  onClick={() => void handleSaveDraft()}
                >
                  {saving ? "保存中..." : "保存草稿"}
                </button>
              </div>
            </div>
          </GlassPanel>

          <GlassPanel title="素材附件摘要" subtitle="只展示已返回的附件引用，不做 OCR、CMS 或 PDF 处理">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
              {attachmentSummary.map((item) => (
                <div key={item.label} className="rounded-lg border border-slate-200 bg-slate-50 p-4">
                  <p className="text-sm text-slate-500">{item.label}</p>
                  <p className="mt-2 text-2xl font-semibold text-slate-950">{item.value}</p>
                  <p className="mt-2 text-xs leading-5 text-slate-500">{item.hint}</p>
                </div>
              ))}
            </div>

            <div className="mt-4 rounded-lg border border-dashed border-slate-300 bg-white p-4 text-sm text-slate-500">
              PDF 解析、图片 OCR、CMS 发布、SSO 权限联动均未实现，当前页面仅提供能力缺口提示。
            </div>
          </GlassPanel>
        </div>

        <aside className="flex min-w-0 flex-col gap-6">
          <GlassPanel title="审核状态" subtitle="来自详情记录的审核与风险字段">
            <dl className="grid gap-3 text-sm">
              <div className="flex items-center justify-between gap-4">
                <dt className="text-slate-500">审核状态</dt>
                <dd>
                  <StatusTag value={detail?.auditStatus}>{normalizeStatusLabel(detail?.auditStatus)}</StatusTag>
                </dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-slate-500">风险等级</dt>
                <dd>
                  <RiskTag value={detail?.riskLevel}>{normalizeRiskLabel(detail?.riskLevel)}</RiskTag>
                </dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-slate-500">资料 ID</dt>
                <dd className="font-medium text-slate-800">{detail?.materialId ?? "暂无"}</dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-slate-500">更新于</dt>
                <dd className="font-medium text-slate-800">{formatTime(detail?.updateTime)}</dd>
              </div>
              <div className="grid gap-1">
                <dt className="text-slate-500">审核意见</dt>
                <dd className="rounded-md bg-slate-50 p-3 text-slate-700">{detail?.auditComment || "暂无审核意见"}</dd>
              </div>
            </dl>
          </GlassPanel>

          <GlassPanel title="AI 生成内容预览" subtitle="接口返回内容只做展示，不进行本地生成">
            {previewContent ? (
              <pre className="max-h-[520px] overflow-auto whitespace-pre-wrap break-words rounded-lg bg-slate-950 p-4 text-xs leading-6 text-slate-50">
                {previewContent}
              </pre>
            ) : (
              <EmptyState title="暂无 AI 内容" description="本地 AI 服务未接入前不会生成占位内容。" />
            )}
            <button
              type="button"
              className="mt-4 w-full cursor-not-allowed rounded-md border border-dashed border-slate-300 bg-slate-50 px-4 py-2 text-sm font-medium text-slate-500"
              disabled
              title="POST /api/v1/detail/generate 当前不可用"
            >
              待接入本地AI服务
            </button>
          </GlassPanel>

          <GlassPanel title="预览摘要" subtitle="按当前本地草稿实时展示">
            <article className="grid gap-4">
              <div>
                <h2 className="text-xl font-semibold leading-8 text-slate-950">{draft.title || "暂无标题"}</h2>
                <p className="mt-1 text-sm leading-6 text-slate-500">{draft.subtitle || "暂无副标题"}</p>
              </div>

              <section>
                <h3 className="text-sm font-semibold text-slate-800">核心卖点</h3>
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
                  <p className="mt-2 text-sm text-slate-500">暂无关键词</p>
                )}
              </section>
            </article>
          </GlassPanel>
        </aside>
      </section>
    </main>
  );
}
