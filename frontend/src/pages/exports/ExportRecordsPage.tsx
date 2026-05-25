import { FormEvent, useEffect, useMemo, useState } from "react";

import { api } from "../../services/api";
import type { ExportRecord as BackendExportRecord } from "../../services/types";

type SupportedExportFormat = "WORD" | "MARKDOWN" | "JSON" | "HTML" | "TXT";
type ExportFormat = SupportedExportFormat | "PDF";
type FormatFilter = "ALL" | SupportedExportFormat;
type NoticeTone = "info" | "success" | "warning" | "error";

type ExportRecord = {
  id: string;
  productDetailId: string;
  exportFormat: string;
  filePath?: string;
  fileName?: string;
  fileSize?: number;
  exportStatus?: number | string;
  errorMessage?: string;
  exporter?: string;
  exportTime?: string;
  createTime?: string;
};

type Notice = {
  tone: NoticeTone;
  title: string;
  message: string;
};

type RecordActionProps = {
  busyRecordId: string;
  isLoadingDetail: boolean;
  onSelect: (id: string) => Promise<void>;
  onReexport: (record: ExportRecord) => Promise<void>;
  onRemove: (record: ExportRecord) => Promise<void>;
  onDownload: (record: ExportRecord) => Promise<void>;
};

const supportedFormats: Array<{
  value: SupportedExportFormat;
  label: string;
  extension: string;
}> = [
  { value: "WORD", label: "Word", extension: ".docx" },
  { value: "MARKDOWN", label: "Markdown", extension: ".md" },
  { value: "JSON", label: "JSON", extension: ".json" },
  { value: "HTML", label: "HTML", extension: ".html" },
  { value: "TXT", label: "TXT", extension: ".txt" },
];

const allFormats: Array<{
  value: ExportFormat;
  label: string;
  extension: string;
  available: boolean;
}> = [
  ...supportedFormats.map((format) => ({ ...format, available: true })),
  { value: "PDF", label: "PDF", extension: ".pdf", available: false },
];

function normalizeRecord(record: BackendExportRecord): ExportRecord {
  return {
    id: String(record.id ?? ""),
    productDetailId: String(record.productDetailId ?? ""),
    exportFormat: String(record.exportFormat ?? record.format ?? "").toUpperCase(),
    filePath: record.filePath,
    fileName: record.fileName,
    fileSize: parseOptionalNumber(record.fileSize),
    exportStatus: record.exportStatus ?? record.status,
    errorMessage: record.errorMessage,
    exporter: record.exporter,
    exportTime: record.exportTime,
    createTime: record.createTime,
  };
}

function parseOptionalNumber(value: unknown): number | undefined {
  if (value === undefined || value === null || value === "") {
    return undefined;
  }

  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}

function upsertRecord(records: ExportRecord[], nextRecord: ExportRecord): ExportRecord[] {
  const withoutCurrent = records.filter((record) => record.id !== nextRecord.id);
  return [nextRecord, ...withoutCurrent].sort((a, b) => getRecordTime(b) - getRecordTime(a));
}

function getRecordTime(record: ExportRecord): number {
  const date = record.exportTime || record.createTime;
  return date ? new Date(date).getTime() || 0 : 0;
}

function formatFileSize(size?: number): string {
  if (!size) {
    return "-";
  }

  if (size < 1024) {
    return `${size} B`;
  }

  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }

  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}

function formatDateTime(value?: string): string {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("zh-CN", { hour12: false });
}

function getStatusMeta(record: Pick<ExportRecord, "exportStatus" | "errorMessage">): {
  label: string;
  tone: NoticeTone;
} {
  const rawStatus = record.exportStatus;
  const status = typeof rawStatus === "string" ? rawStatus.toUpperCase() : rawStatus;

  if (record.errorMessage) {
    return { label: "失败", tone: "error" };
  }

  if (status === 0 || status === "0" || status === "PENDING" || status === "PROCESSING") {
    return { label: "导出中", tone: "warning" };
  }

  if (status === 1 || status === "1" || status === "SUCCESS" || status === "DONE") {
    return { label: "成功", tone: "success" };
  }

  if (status === 2 || status === "2" || status === "FAILED" || status === "ERROR") {
    return { label: "失败", tone: "error" };
  }

  return { label: rawStatus === undefined || rawStatus === "" ? "未知" : String(rawStatus), tone: "info" };
}

function getFormatLabel(format: string): string {
  const normalized = format.toUpperCase();
  return allFormats.find((item) => item.value === normalized)?.label || format || "-";
}

function triggerBrowserDownload(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

async function fetchExportRecord(id: string): Promise<ExportRecord> {
  const detail = await api.export.get(encodeURIComponent(id));
  return normalizeRecord(detail);
}

export default function ExportRecordsPage() {
  const [records, setRecords] = useState<ExportRecord[]>([]);
  const [formatFilter, setFormatFilter] = useState<FormatFilter>("ALL");
  const [productDetailId, setProductDetailId] = useState("");
  const [exporter, setExporter] = useState("");
  const [exportFormat, setExportFormat] = useState<SupportedExportFormat>("WORD");
  const [lookupId, setLookupId] = useState("");
  const [selectedRecord, setSelectedRecord] = useState<ExportRecord | null>(null);
  const [notice, setNotice] = useState<Notice>({
    tone: "info",
    title: "导出列表已接入",
    message: "导出列表、删除、重新导出和下载均通过 services/api 调用后端接口。",
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoadingDetail, setIsLoadingDetail] = useState(false);
  const [isLoadingList, setIsLoadingList] = useState(false);
  const [busyRecordId, setBusyRecordId] = useState("");
  const [pagination, setPagination] = useState({ pageNum: 1, pageSize: 20, total: 0, pages: 0 });

  useEffect(() => {
    void loadList();
    // 初次进入页面时加载导出记录列表。
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const filteredRecords = useMemo(() => {
    if (formatFilter === "ALL") {
      return records;
    }

    return records.filter((record) => record.exportFormat.toUpperCase() === formatFilter);
  }, [formatFilter, records]);

  async function loadList() {
    setIsLoadingList(true);

    try {
      const page = await api.export.list({
        pageNum: 1,
        pageSize: pagination.pageSize,
      });
      const nextRecords = (page.data ?? []).map(normalizeRecord);
      setRecords(nextRecords);
      setPagination({
        pageNum: page.pageNum,
        pageSize: page.pageSize,
        total: page.total,
        pages: page.pages,
      });
      setSelectedRecord((current) => {
        if (current && nextRecords.some((record) => record.id === current.id)) {
          return current;
        }
        return nextRecords[0] ?? null;
      });
      setNotice({ tone: "success", title: "列表已刷新", message: `已加载 ${page.total} 条导出记录。` });
    } catch (error) {
      setNotice({
        tone: "error",
        title: "列表加载失败",
        message: error instanceof Error ? error.message : "加载导出列表时出现未知错误。",
      });
    } finally {
      setIsLoadingList(false);
    }
  }

  async function handleStartExport(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const normalizedDetailId = productDetailId.trim();
    const normalizedExporter = exporter.trim();

    if (!normalizedDetailId) {
      setNotice({ tone: "warning", title: "缺少商品详情 ID", message: "请先输入 productDetailId，再发起导出。" });
      return;
    }

    if (!normalizedExporter) {
      setNotice({ tone: "warning", title: "缺少导出人", message: "请填写导出人，后端 ExportDTO 要求 exporter 非空。" });
      return;
    }

    setIsSubmitting(true);

    try {
      const exportId = await api.export.create({
        productDetailId: Number(normalizedDetailId),
        exportFormat,
        exporter: normalizedExporter,
      });

      const detail = await fetchExportRecord(String(exportId));
      setRecords((current) => upsertRecord(current, detail));
      setSelectedRecord(detail);
      setLookupId(detail.id);
      setNotice({
        tone: "success",
        title: "导出已发起",
        message: `已通过 services/api 发起导出，并查询到记录 #${detail.id}。`,
      });
    } catch (error) {
      setNotice({
        tone: "error",
        title: "导出失败",
        message: error instanceof Error ? error.message : "发起导出时出现未知错误。",
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleLookup(event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault();

    const id = lookupId.trim();
    if (!id) {
      setNotice({ tone: "warning", title: "缺少导出记录 ID", message: "请输入导出记录 ID 后再查询详情。" });
      return;
    }

    await loadDetail(id);
  }

  async function loadDetail(id: string) {
    setIsLoadingDetail(true);

    try {
      const detail = await fetchExportRecord(id);
      setRecords((current) => upsertRecord(current, detail));
      setSelectedRecord(detail);
      setLookupId(detail.id);
      setNotice({ tone: "success", title: "详情已更新", message: `已通过 services/api 查询导出记录 #${detail.id}。` });
    } catch (error) {
      setNotice({
        tone: "error",
        title: "查询失败",
        message: error instanceof Error ? error.message : "查询导出详情时出现未知错误。",
      });
    } finally {
      setIsLoadingDetail(false);
    }
  }

  async function handleReexport(record: ExportRecord) {
    setBusyRecordId(record.id);

    try {
      await api.export.reexport(record.id);
      const detail = await fetchExportRecord(record.id);
      setRecords((current) => upsertRecord(current, detail));
      setSelectedRecord(detail);
      setNotice({ tone: "success", title: "已重新导出", message: `记录 #${record.id} 已重新导出并刷新详情。` });
    } catch (error) {
      setNotice({
        tone: "error",
        title: "重新导出失败",
        message: error instanceof Error ? error.message : "重新导出时出现未知错误。",
      });
    } finally {
      setBusyRecordId("");
    }
  }

  async function handleRemove(record: ExportRecord) {
    setBusyRecordId(record.id);

    try {
      await api.export.remove(record.id);
      setRecords((current) => current.filter((item) => item.id !== record.id));
      setSelectedRecord((current) => (current?.id === record.id ? null : current));
      setNotice({ tone: "success", title: "记录已删除", message: `记录 #${record.id} 已从后端删除。` });
    } catch (error) {
      setNotice({
        tone: "error",
        title: "删除失败",
        message: error instanceof Error ? error.message : "删除导出记录时出现未知错误。",
      });
    } finally {
      setBusyRecordId("");
    }
  }

  async function handleDownload(record: ExportRecord) {
    if (record.exportFormat.toUpperCase() === "PDF") {
      setNotice({ tone: "warning", title: "PDF 未实现", message: "PDF 导出仍未实现，不能伪装为可下载。" });
      return;
    }

    setBusyRecordId(record.id);

    try {
      const file = await api.export.download(record.id);
      triggerBrowserDownload(file.blob, file.fileName || record.fileName || `export-${record.id}`);
      const nextRecord = { ...record, fileName: file.fileName || record.fileName };
      setRecords((current) => upsertRecord(current, nextRecord));
      setSelectedRecord((current) => (current?.id === record.id ? nextRecord : current));

      setNotice({
        tone: "success",
        title: "Download started",
        message: `Export #${record.id} downloaded from backend file stream: ${file.fileName || record.fileName || "export"}`,
      });
    } catch (error) {
      setNotice({
        tone: "error",
        title: "下载失败",
        message: error instanceof Error ? error.message : "下载导出文件时出现未知错误。",
      });
    } finally {
      setBusyRecordId("");
    }
  }

  return (
    <main className="exports-page">
      <style>{styles}</style>

      <section className="exports-hero">
        <div>
          <p className="eyebrow">导出记录</p>
          <h1>导出记录与详情预览</h1>
          <p className="hero-copy">
            导出列表、下载、删除和重新导出均通过 services/api 调用后端；PDF 与 CMS 对接仍按未实现能力显示。
          </p>
        </div>
        <div className="hero-actions">
          <button className="secondary-button" type="button" disabled={isLoadingList} onClick={() => void loadList()}>
            {isLoadingList ? "刷新中..." : "刷新列表"}
          </button>
          <button className="secondary-button" type="button" disabled title="CMS 对接未实现">
            CMS 对接未实现
          </button>
        </div>
      </section>

      <NoticeBanner notice={notice} />

      <section className="exports-grid">
        <form className="panel export-form" onSubmit={handleStartExport}>
          <div className="panel-heading">
            <div>
            <p className="section-kicker">services/api export.create</p>
              <h2>发起导出</h2>
            </div>
            <span className="state-pill state-pill-info">真实接口</span>
          </div>

          <label className="field">
            <span>商品详情 ID</span>
            <input
              inputMode="numeric"
              min="1"
              placeholder="例如：1001"
              type="number"
              value={productDetailId}
              onChange={(event) => setProductDetailId(event.target.value)}
            />
          </label>

          <label className="field">
            <span>导出人</span>
            <input placeholder="例如：admin" value={exporter} onChange={(event) => setExporter(event.target.value)} />
          </label>

          <label className="field">
            <span>导出格式</span>
            <select value={exportFormat} onChange={(event) => setExportFormat(event.target.value as SupportedExportFormat)}>
              {supportedFormats.map((format) => (
                <option key={format.value} value={format.value}>
                  {format.label} {format.extension}
                </option>
              ))}
              <option disabled value="PDF">
                PDF .pdf - 未实现
              </option>
            </select>
          </label>

          <button className="primary-button" disabled={isSubmitting} type="submit">
            {isSubmitting ? "发起中..." : "发起导出"}
          </button>

          <p className="muted">
            TXT 按前端需求展示为支持；若当前后端版本尚未接收 TXT，会显示真实接口错误。
          </p>
        </form>

        <section className="panel">
          <div className="panel-heading">
            <div>
            <p className="section-kicker">services/api export.get</p>
              <h2>查询导出详情</h2>
            </div>
            <span className="state-pill state-pill-info">真实接口</span>
          </div>

          <form className="lookup-form" onSubmit={handleLookup}>
            <label className="field">
              <span>导出记录 ID</span>
              <input
                inputMode="numeric"
                min="1"
                placeholder="输入记录 ID"
                type="number"
                value={lookupId}
                onChange={(event) => setLookupId(event.target.value)}
              />
            </label>
            <button className="secondary-button" disabled={isLoadingDetail} type="submit">
              {isLoadingDetail ? "查询中..." : "查询详情"}
            </button>
          </form>

          <div className="capability-list">
            <CapabilityItem label="下载文件" state="已接入" text="通过 services/api export.download 下载后端返回的文件流。" />
            <CapabilityItem label="删除记录" state="已接入" text="通过 services/api export.remove 删除后端导出记录。" />
            <CapabilityItem label="CMS 对接" state="未实现" text="仅显示提示，不发起任何 CMS 请求。" />
          </div>
        </section>
      </section>

      <section className="panel">
        <div className="panel-heading align-end">
          <div>
            <p className="section-kicker">格式筛选</p>
            <h2>导出格式支持</h2>
          </div>
          <div className="format-filter" aria-label="导出格式筛选">
            <button
              className={formatFilter === "ALL" ? "filter-chip active" : "filter-chip"}
              type="button"
              onClick={() => setFormatFilter("ALL")}
            >
              全部
            </button>
            {supportedFormats.map((format) => (
              <button
                className={formatFilter === format.value ? "filter-chip active" : "filter-chip"}
                key={format.value}
                type="button"
                onClick={() => setFormatFilter(format.value)}
              >
                {format.label}
              </button>
            ))}
            <button className="filter-chip disabled" type="button" disabled title="PDF 导出未实现">
              PDF 未实现
            </button>
          </div>
        </div>

        <div className="format-grid">
          {allFormats.map((format) => (
            <div className={format.available ? "format-card" : "format-card unavailable"} key={format.value}>
              <strong>{format.label}</strong>
              <span>{format.extension}</span>
              <span className={format.available ? "state-pill state-pill-success" : "state-pill state-pill-neutral"}>
                {format.available ? "支持" : "未实现"}
              </span>
            </div>
          ))}
        </div>
      </section>

      <section className="records-layout">
        <section className="panel records-panel">
          <div className="panel-heading">
            <div>
              <p className="section-kicker">导出列表</p>
              <h2>导出记录列表</h2>
            </div>
            <span className="state-pill state-pill-info">共 {pagination.total} 条</span>
          </div>

          {filteredRecords.length > 0 ? (
            <>
              <div className="table-wrap">
                <table className="exports-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>商品详情</th>
                      <th>格式</th>
                      <th>状态</th>
                      <th>文件</th>
                      <th>导出人</th>
                      <th>时间</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredRecords.map((record) => (
                      <ExportRecordRow
                        isLoadingDetail={isLoadingDetail}
                        key={record.id}
                        record={record}
                        selected={selectedRecord?.id === record.id}
                        busyRecordId={busyRecordId}
                        onDownload={handleDownload}
                        onRemove={handleRemove}
                        onReexport={handleReexport}
                        onSelect={loadDetail}
                      />
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="export-cards">
                {filteredRecords.map((record) => (
                  <ExportRecordCard
                    isLoadingDetail={isLoadingDetail}
                    key={record.id}
                    record={record}
                    selected={selectedRecord?.id === record.id}
                    busyRecordId={busyRecordId}
                    onDownload={handleDownload}
                    onRemove={handleRemove}
                    onReexport={handleReexport}
                    onSelect={loadDetail}
                  />
                ))}
              </div>
            </>
          ) : (
            <div className="empty-state">
              <h3>暂无可展示记录</h3>
              <p>当前筛选条件下没有后端返回的导出记录。可发起导出或按 ID 查询单条记录。</p>
            </div>
          )}
        </section>

        <aside className="panel preview-panel">
          <div className="panel-heading">
            <div>
              <p className="section-kicker">详情预览</p>
              <h2>导出详情</h2>
            </div>
            {selectedRecord ? <StatusPill record={selectedRecord} /> : null}
          </div>

          {selectedRecord ? (
            <ExportDetailPreview
              busyRecordId={busyRecordId}
              record={selectedRecord}
              onDownload={handleDownload}
              onRefresh={loadDetail}
              onRemove={handleRemove}
              onReexport={handleReexport}
              isRefreshing={isLoadingDetail}
            />
          ) : (
            <div className="empty-state compact">
              <h3>未选择记录</h3>
              <p>点击列表中的“详情”，或按 ID 查询导出记录。</p>
            </div>
          )}
        </aside>
      </section>
    </main>
  );
}

function NoticeBanner({ notice }: { notice: Notice }) {
  return (
    <section className={`notice notice-${notice.tone}`} role={notice.tone === "error" ? "alert" : "status"}>
      <strong>{notice.title}</strong>
      <span>{notice.message}</span>
    </section>
  );
}

function CapabilityItem({ label, state, text }: { label: string; state: string; text: string }) {
  return (
    <div className="capability-item">
      <div>
        <strong>{label}</strong>
        <p>{text}</p>
      </div>
      <span className="state-pill state-pill-neutral">{state}</span>
    </div>
  );
}

function StatusPill({ record }: { record: ExportRecord }) {
  const status = getStatusMeta(record);
  return <span className={`state-pill state-pill-${status.tone}`}>{status.label}</span>;
}

function ExportRecordRow({
  record,
  selected,
  busyRecordId,
  isLoadingDetail,
  onDownload,
  onRemove,
  onReexport,
  onSelect,
}: {
  record: ExportRecord;
  selected: boolean;
} & RecordActionProps) {
  return (
    <tr className={selected ? "selected-row" : undefined}>
      <td>#{record.id}</td>
      <td>{record.productDetailId || "-"}</td>
      <td>{getFormatLabel(record.exportFormat)}</td>
      <td>
        <StatusPill record={record} />
      </td>
      <td>
        <div className="file-cell">
          <span>{record.fileName || record.filePath || "-"}</span>
          <small>{formatFileSize(record.fileSize)}</small>
        </div>
      </td>
      <td>{record.exporter || "-"}</td>
      <td>{formatDateTime(record.exportTime || record.createTime)}</td>
      <td>
        <RecordActions
          busyRecordId={busyRecordId}
          isLoadingDetail={isLoadingDetail}
          record={record}
          onDownload={onDownload}
          onRemove={onRemove}
          onReexport={onReexport}
          onSelect={onSelect}
        />
      </td>
    </tr>
  );
}

function ExportRecordCard({
  record,
  selected,
  busyRecordId,
  isLoadingDetail,
  onDownload,
  onRemove,
  onReexport,
  onSelect,
}: {
  record: ExportRecord;
  selected: boolean;
} & RecordActionProps) {
  return (
    <article className={selected ? "record-card selected-card" : "record-card"}>
      <div className="record-card-heading">
        <strong>#{record.id}</strong>
        <StatusPill record={record} />
      </div>
      <dl>
        <div>
          <dt>商品详情</dt>
          <dd>{record.productDetailId || "-"}</dd>
        </div>
        <div>
          <dt>格式</dt>
          <dd>{getFormatLabel(record.exportFormat)}</dd>
        </div>
        <div>
          <dt>文件</dt>
          <dd>{record.fileName || record.filePath || "-"}</dd>
        </div>
        <div>
          <dt>时间</dt>
          <dd>{formatDateTime(record.exportTime || record.createTime)}</dd>
        </div>
      </dl>
      <RecordActions
        busyRecordId={busyRecordId}
        isLoadingDetail={isLoadingDetail}
        record={record}
        onDownload={onDownload}
        onRemove={onRemove}
        onReexport={onReexport}
        onSelect={onSelect}
      />
    </article>
  );
}

function RecordActions({
  record,
  busyRecordId,
  isLoadingDetail,
  onDownload,
  onRemove,
  onReexport,
  onSelect,
}: {
  record: ExportRecord;
} & RecordActionProps) {
  const isBusy = busyRecordId === record.id;
  const isPdf = record.exportFormat.toUpperCase() === "PDF";

  return (
    <div className="record-actions">
      <button className="link-button" disabled={isLoadingDetail || isBusy} type="button" onClick={() => void onSelect(record.id)}>
        详情
      </button>
      <button className="link-button" type="button" disabled={isBusy} onClick={() => void onReexport(record)}>
        {isBusy ? "处理中..." : "重新导出"}
      </button>
      <button
        className={isPdf ? "link-button disabled" : "link-button"}
        type="button"
        disabled={isBusy || isPdf}
        title={isPdf ? "PDF 导出未实现" : "下载后端返回的文件流"}
        onClick={() => void onDownload(record)}
      >
        {isPdf ? "PDF 未实现" : "下载"}
      </button>
      <button className="link-button" type="button" disabled={isBusy} onClick={() => void onRemove(record)}>
        删除
      </button>
    </div>
  );
}

function ExportDetailPreview({
  record,
  busyRecordId,
  isRefreshing,
  onDownload,
  onRemove,
  onReexport,
  onRefresh,
}: {
  record: ExportRecord;
  isRefreshing: boolean;
  onRefresh: (id: string) => Promise<void>;
  busyRecordId: string;
  onReexport: (record: ExportRecord) => Promise<void>;
  onRemove: (record: ExportRecord) => Promise<void>;
  onDownload: (record: ExportRecord) => Promise<void>;
}) {
  const isBusy = busyRecordId === record.id;
  const isPdf = record.exportFormat.toUpperCase() === "PDF";

  return (
    <div className="detail-preview">
      <dl className="detail-list">
        <DetailRow label="记录 ID" value={`#${record.id}`} />
        <DetailRow label="商品详情 ID" value={record.productDetailId || "-"} />
        <DetailRow label="导出格式" value={getFormatLabel(record.exportFormat)} />
        <DetailRow label="导出人" value={record.exporter || "-"} />
        <DetailRow label="文件名" value={record.fileName || "-"} />
        <DetailRow label="文件路径" value={record.filePath || "-"} />
        <DetailRow label="文件大小" value={formatFileSize(record.fileSize)} />
        <DetailRow label="导出时间" value={formatDateTime(record.exportTime || record.createTime)} />
        <DetailRow label="错误信息" value={record.errorMessage || "-"} danger={Boolean(record.errorMessage)} />
      </dl>

      <div className="preview-actions">
        <button className="secondary-button" disabled={isRefreshing || isBusy} type="button" onClick={() => void onRefresh(record.id)}>
          {isRefreshing ? "刷新中..." : "刷新详情"}
        </button>
        <button className="secondary-button" type="button" disabled={isBusy} onClick={() => void onReexport(record)}>
          {isBusy ? "处理中..." : "重新导出"}
        </button>
        <button
          className="secondary-button"
          type="button"
          disabled={isBusy || isPdf}
          title={isPdf ? "PDF 导出未实现" : "下载后端返回的文件流"}
          onClick={() => void onDownload(record)}
        >
          {isPdf ? "PDF 未实现" : "下载文件"}
        </button>
        <button className="secondary-button" type="button" disabled={isBusy} onClick={() => void onRemove(record)}>
          删除记录
        </button>
      </div>

      <div className="cms-note">
        <strong>CMS 对接未实现</strong>
        <p>当前页面不向 CMS 发布导出文件，仅展示后端导出记录和接口缺口。</p>
      </div>
    </div>
  );
}

function DetailRow({ label, value, danger = false }: { label: string; value: string; danger?: boolean }) {
  return (
    <div>
      <dt>{label}</dt>
      <dd className={danger ? "danger-text" : undefined}>{value}</dd>
    </div>
  );
}

const styles = `
.exports-page {
  --surface: #ffffff;
  --surface-muted: #f6f8fb;
  --border: #d9e2ef;
  --text: #1f2937;
  --muted: #667085;
  --primary: #1b66d2;
  --primary-strong: #1552aa;
  --success: #1f8a4c;
  --warning: #a16207;
  --error: #c2410c;
  --neutral: #64748b;
  color: var(--text);
  display: grid;
  gap: 18px;
  padding: 24px;
}

.exports-hero,
.panel,
.notice {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 8px;
}

.exports-hero {
  align-items: flex-end;
  display: flex;
  gap: 18px;
  justify-content: space-between;
  padding: 22px;
}

.eyebrow,
.section-kicker {
  color: var(--muted);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
  margin: 0 0 6px;
  text-transform: uppercase;
}

h1,
h2,
h3,
p {
  margin-top: 0;
}

h1 {
  font-size: 28px;
  line-height: 1.2;
  margin-bottom: 8px;
}

h2 {
  font-size: 18px;
  line-height: 1.3;
  margin: 0;
}

h3 {
  font-size: 16px;
  line-height: 1.35;
  margin-bottom: 6px;
}

.hero-copy,
.muted,
.empty-state p,
.capability-item p,
.cms-note p {
  color: var(--muted);
  line-height: 1.6;
}

.hero-copy {
  margin-bottom: 0;
  max-width: 760px;
}

.hero-actions,
.format-filter,
.record-actions,
.preview-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.notice {
  display: grid;
  gap: 4px;
  line-height: 1.5;
  padding: 14px 16px;
}

.notice-info {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.notice-success {
  background: #ecfdf3;
  border-color: #bbf7d0;
}

.notice-warning {
  background: #fff7ed;
  border-color: #fed7aa;
}

.notice-error {
  background: #fff1f2;
  border-color: #fecdd3;
}

.exports-grid {
  display: grid;
  gap: 18px;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 0.85fr);
}

.records-layout {
  align-items: start;
  display: grid;
  gap: 18px;
  grid-template-columns: minmax(0, 1.45fr) minmax(320px, 0.75fr);
}

.panel {
  min-width: 0;
  padding: 18px;
}

.panel-heading {
  align-items: center;
  display: flex;
  gap: 12px;
  justify-content: space-between;
  margin-bottom: 16px;
}

.align-end {
  align-items: flex-end;
}

.export-form,
.lookup-form {
  display: grid;
  gap: 14px;
}

.field {
  display: grid;
  gap: 6px;
}

.field span {
  color: #344054;
  font-size: 13px;
  font-weight: 700;
}

input,
select {
  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  color: var(--text);
  font: inherit;
  min-height: 40px;
  padding: 8px 10px;
}

input:focus,
select:focus,
button:focus-visible {
  outline: 3px solid #bfdbfe;
  outline-offset: 2px;
}

button {
  border-radius: 6px;
  cursor: pointer;
  font: inherit;
  min-height: 36px;
  padding: 8px 12px;
}

button:disabled {
  cursor: not-allowed;
}

.primary-button {
  background: var(--primary);
  border: 1px solid var(--primary);
  color: #ffffff;
  font-weight: 700;
}

.primary-button:hover:not(:disabled) {
  background: var(--primary-strong);
}

.secondary-button {
  background: #ffffff;
  border: 1px solid #b8c7dc;
  color: var(--primary);
  font-weight: 700;
}

.secondary-button:disabled,
.link-button.disabled,
.filter-chip.disabled {
  background: #f1f5f9;
  border-color: #d8dee8;
  color: #94a3b8;
}

.link-button {
  background: transparent;
  border: 1px solid transparent;
  color: var(--primary);
  min-height: 28px;
  padding: 3px 6px;
}

.link-button:hover:not(:disabled) {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.filter-chip {
  background: #ffffff;
  border: 1px solid #cbd5e1;
  color: #334155;
}

.filter-chip.active {
  background: #eaf2ff;
  border-color: var(--primary);
  color: var(--primary);
  font-weight: 700;
}

.state-pill {
  align-items: center;
  border-radius: 999px;
  display: inline-flex;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  min-height: 24px;
  padding: 5px 9px;
  white-space: nowrap;
}

.state-pill-info {
  background: #eff6ff;
  color: #1d4ed8;
}

.state-pill-success {
  background: #dcfce7;
  color: var(--success);
}

.state-pill-warning {
  background: #fef3c7;
  color: var(--warning);
}

.state-pill-error {
  background: #fee2e2;
  color: var(--error);
}

.state-pill-neutral {
  background: #f1f5f9;
  color: var(--neutral);
}

.capability-list {
  display: grid;
  gap: 10px;
}

.capability-item {
  align-items: center;
  background: var(--surface-muted);
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  display: flex;
  gap: 12px;
  justify-content: space-between;
  padding: 12px;
}

.capability-item p {
  font-size: 13px;
  margin: 4px 0 0;
}

.format-grid {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(6, minmax(110px, 1fr));
}

.format-card {
  background: var(--surface-muted);
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  display: grid;
  gap: 8px;
  min-height: 106px;
  padding: 12px;
}

.format-card span:not(.state-pill) {
  color: var(--muted);
}

.format-card.unavailable {
  background: #f8fafc;
}

.table-wrap {
  overflow-x: auto;
}

.exports-table {
  border-collapse: collapse;
  min-width: 880px;
  width: 100%;
}

.exports-table th,
.exports-table td {
  border-bottom: 1px solid #e2e8f0;
  padding: 12px 10px;
  text-align: left;
  vertical-align: middle;
}

.exports-table th {
  color: #475467;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}

.selected-row {
  background: #f8fbff;
}

.file-cell {
  display: grid;
  gap: 4px;
  max-width: 260px;
}

.file-cell span,
.detail-list dd {
  overflow-wrap: anywhere;
}

.file-cell small {
  color: var(--muted);
}

.export-cards {
  display: none;
}

.record-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  display: grid;
  gap: 12px;
  padding: 14px;
}

.selected-card {
  border-color: var(--primary);
}

.record-card-heading {
  align-items: center;
  display: flex;
  justify-content: space-between;
}

.record-card dl,
.detail-list {
  display: grid;
  gap: 10px;
  margin: 0;
}

.record-card dl div,
.detail-list div {
  display: grid;
  gap: 4px;
}

dt {
  color: var(--muted);
  font-size: 12px;
  font-weight: 700;
}

dd {
  margin: 0;
}

.detail-preview {
  display: grid;
  gap: 16px;
}

.detail-list div {
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 10px;
}

.danger-text {
  color: var(--error);
}

.cms-note {
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  padding: 12px;
}

.cms-note p {
  margin: 6px 0 0;
}

.empty-state {
  align-items: center;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  display: grid;
  justify-items: center;
  min-height: 220px;
  padding: 28px;
  text-align: center;
}

.empty-state.compact {
  min-height: 180px;
}

@media (max-width: 1120px) {
  .exports-grid,
  .records-layout {
    grid-template-columns: 1fr;
  }

  .format-grid {
    grid-template-columns: repeat(3, minmax(110px, 1fr));
  }
}

@media (max-width: 720px) {
  .exports-page {
    padding: 14px;
  }

  .exports-hero,
  .panel-heading,
  .capability-item {
    align-items: stretch;
    flex-direction: column;
  }

  .hero-actions,
  .format-filter,
  .record-actions,
  .preview-actions {
    width: 100%;
  }

  .hero-actions button,
  .format-filter button,
  .record-actions button,
  .preview-actions button,
  .primary-button,
  .secondary-button {
    flex: 1 1 auto;
  }

  h1 {
    font-size: 24px;
  }

  .format-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .table-wrap {
    display: none;
  }

  .export-cards {
    display: grid;
    gap: 12px;
  }
}
`;
