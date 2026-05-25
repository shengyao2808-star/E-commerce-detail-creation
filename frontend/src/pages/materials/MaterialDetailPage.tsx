import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { api } from '../../services/api';

type MaterialStatus = string | number;
type AssetKind = 'image' | 'video' | 'document';

interface ProductMaterial {
  id?: number | string;
  brandId?: number | string;
  brandName?: string;
  productName?: string;
  productSku?: string;
  sku?: string;
  category?: string;
  price?: number | string;
  description?: string;
  images?: string[];
  videos?: string[];
  documents?: string[];
  status?: MaterialStatus;
  originalFilePath?: string;
  fileType?: string;
  fileSize?: number;
  uploader?: string;
  uploadTime?: string;
  parseStatus?: number;
  parseError?: string;
  createTime?: string;
  updateTime?: string;
}

const AI_PENDING_TEXT = '\u5f85\u63a5\u5165\u672c\u5730AI\u670d\u52a1';

const TEXT = {
  detailEyebrow: '\u5546\u54c1\u8d44\u6599\u8be6\u60c5',
  detailTitle: '\u5546\u54c1\u8d44\u6599\u8be6\u60c5',
  subtitle:
    '\u5c55\u793a\u57fa\u7840\u8d44\u6599\u3001\u56fe\u7247\u3001\u89c6\u9891\u3001\u6587\u6863\u5217\u8868\uff0c\u5e76\u660e\u786e\u6807\u8bb0\u7f16\u8f91\u3001\u5220\u9664\u548c AI \u529f\u80fd\u7684\u5f85\u63a5\u5165\u72b6\u6001\u3002',
  backToList: '\u8fd4\u56de\u5217\u8868',
  aiGenerate: 'AI \u751f\u6210\u8be6\u60c5\u9875',
  realApi: '\u771f\u5b9e\u63a5\u53e3',
  apiDesc:
    '\u672c\u9875\u8bfb\u53d6\u5546\u54c1\u8d44\u6599\u8be6\u60c5\uff0c\u5220\u9664\u53ef\u4ee5\u76f4\u63a5\u8c03\u7528\u540e\u7aef\uff1b\u7f16\u8f91\u5165\u53e3\u5728\u5217\u8868\u9875\u5b8c\u6210\u3002',
  available: '\u53ef\u7528',
  loadingTitle: '\u6b63\u5728\u52a0\u8f7d\u5546\u54c1\u8d44\u6599',
  loadFailed: '\u52a0\u8f7d\u5931\u8d25',
  cannotLoad: '\u65e0\u6cd5\u83b7\u53d6\u5546\u54c1\u8d44\u6599',
  baseInfo: '\u57fa\u7840\u8d44\u6599',
  productInfo: '\u5546\u54c1\u4fe1\u606f',
  productName: '\u5546\u54c1\u540d\u79f0',
  brand: '\u54c1\u724c',
  category: '\u7c7b\u76ee',
  sku: 'SKU',
  price: '\u4ef7\u683c',
  uploader: '\u4e0a\u4f20\u4eba',
  uploadTime: '\u4e0a\u4f20\u65f6\u95f4',
  createTime: '\u521b\u5efa\u65f6\u95f4',
  updateTime: '\u66f4\u65b0\u65f6\u95f4',
  fileType: '\u6587\u4ef6\u7c7b\u578b',
  fileSize: '\u6587\u4ef6\u5927\u5c0f',
  parseStatus: '\u89e3\u6790\u72b6\u6001',
  description: '\u5546\u54c1\u63cf\u8ff0',
  noDescription: '\u6682\u65e0\u63cf\u8ff0',
  parseError: '\u89e3\u6790\u9519\u8bef',
  editMaterial: '\u7f16\u8f91\u8d44\u6599',
  deleteMaterial: '\u5220\u9664\u8d44\u6599',
  originalFile: '\u67e5\u770b\u539f\u59cb\u6587\u4ef6',
  assetFile: '\u8d44\u6599\u6587\u4ef6',
  images: '\u5546\u54c1\u56fe\u7247',
  videos: '\u5546\u54c1\u89c6\u9891',
  documents: '\u5546\u54c1\u6587\u6863',
  fileCountSuffix: '\u4e2a\u6587\u4ef6',
  noImages: '\u6682\u65e0\u5546\u54c1\u56fe\u7247',
  noVideos: '\u6682\u65e0\u5546\u54c1\u89c6\u9891',
  noDocuments: '\u6682\u65e0\u5546\u54c1\u6587\u6863',
  uploadHint: '\u53ef\u901a\u8fc7 POST /api/v1/material/upload \u4e0a\u4f20\u8d44\u6599\u540e\u5c55\u793a\u3002',
  videoUnsupported: '\u5f53\u524d\u6d4f\u89c8\u5668\u4e0d\u652f\u6301\u89c6\u9891\u9884\u89c8\u3002',
  open: '\u6253\u5f00',
  aiCapability: 'AI \u80fd\u529b',
  aiPanelDesc:
    'AI \u89e3\u6790\u8d44\u6599\u3001\u56fe\u7247 OCR\u3001\u6587\u6863\u7406\u89e3\u3001\u751f\u6210\u8be6\u60c5\u9875\u5185\u5bb9\u5747\u4fdd\u7559\u5165\u53e3\uff0c\u4f46\u4e0d\u8c03\u7528\u672a\u5b8c\u6210\u670d\u52a1\u3002',
  aiPrompt: '\u67e5\u770b AI \u63a5\u5165\u63d0\u793a',
  aiFeature: 'AI \u529f\u80fd',
  aiDisconnected: 'AI \u670d\u52a1\u672a\u8fde\u63a5',
  currentStatus: '\u5f53\u524d\u72b6\u6001',
  aiRecommend:
    '\u63a8\u8350\u65b9\u6848\uff1aOllama + Qwen2.5\uff0c\u672c\u5730\u90e8\u7f72\u5b8c\u6210\u540e\u518d\u542f\u7528 AI \u89e3\u6790\u3001\u751f\u6210\u8be6\u60c5\u9875\u548c\u6587\u6863\u7406\u89e3\u80fd\u529b\u3002',
  later: '\u7a0d\u540e\u63a5\u5165',
  close: '\u5173\u95ed',
  missingId: '\u7f3a\u5c11\u5546\u54c1\u8d44\u6599 ID\uff0c\u65e0\u6cd5\u8c03\u7528 GET /api/v1/material/{id}\u3002',
  loadFailedMessage: '\u5546\u54c1\u8d44\u6599\u8be6\u60c5\u52a0\u8f7d\u5931\u8d25\u3002',
  deleteConfirm: '\u786e\u5b9a\u5220\u9664\u8be5\u5546\u54c1\u8d44\u6599\uff1f',
  deleting: '\u5220\u9664\u4e2d...',
  deleteSuccess: '\u8d44\u6599\u5df2\u5220\u9664\uff0c\u6b63\u5728\u8fd4\u56de\u5217\u8868\u3002',
};

function getStatusMeta(status?: MaterialStatus) {
  switch (String(status ?? '')) {
    case '0':
      return { label: '\u8349\u7a3f', className: 'neutral' };
    case '1':
      return { label: '\u5df2\u63d0\u4ea4', className: 'info' };
    case '2':
      return { label: '\u5ba1\u6838\u4e2d', className: 'warning' };
    case '3':
      return { label: '\u5ba1\u6838\u901a\u8fc7', className: 'success' };
    case '4':
      return { label: '\u5ba1\u6838\u62d2\u7edd', className: 'danger' };
    default:
      return { label: '\u6682\u65e0\u72b6\u6001', className: 'neutral' };
  }
}

function getParseStatusLabel(status?: number) {
  switch (status) {
    case 0:
      return '\u5f85\u89e3\u6790';
    case 1:
      return '\u89e3\u6790\u4e2d';
    case 2:
      return '\u89e3\u6790\u6210\u529f';
    case 3:
      return '\u89e3\u6790\u5931\u8d25';
    default:
      return '\u6682\u65e0\u89e3\u6790\u72b6\u6001';
  }
}

function formatDateTime(value?: string) {
  if (!value) {
    return '-';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function formatFileSize(bytes?: number) {
  if (!bytes && bytes !== 0) {
    return '-';
  }

  if (bytes < 1024) {
    return `${bytes} B`;
  }

  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`;
  }

  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

function getFileName(path: string) {
  const clean = path.split('?')[0];
  const segments = clean.split(/[\\/]/);
  return segments[segments.length - 1] || path;
}

function normalizeAssets(items?: string[]) {
  return Array.isArray(items) ? items.filter(Boolean) : [];
}

function StatusPill({ status }: { status?: MaterialStatus }) {
  const meta = getStatusMeta(status);

  return <span className={`material-status material-status--${meta.className}`}>{meta.label}</span>;
}

function AiPendingNotice({ onClose }: { onClose?: () => void }) {
  return (
    <div className="material-modal-backdrop" role="presentation">
      <section className="material-modal" role="dialog" aria-modal="true" aria-label={TEXT.aiDisconnected}>
        <header>
          <div>
            <p className="material-eyebrow">{TEXT.aiFeature}</p>
            <h2>{TEXT.aiDisconnected}</h2>
          </div>
          <button className="material-icon-button" type="button" onClick={onClose} aria-label={TEXT.close}>
            x
          </button>
        </header>
        <p>
          {TEXT.currentStatus}: {AI_PENDING_TEXT}
        </p>
        <p>{TEXT.aiRecommend}</p>
        <footer>
          <button className="material-secondary-button" type="button" onClick={onClose}>
            {TEXT.later}
          </button>
          <button className="material-primary-button" type="button" onClick={onClose}>
            {TEXT.close}
          </button>
        </footer>
      </section>
    </div>
  );
}

function AssetList({ title, kind, items }: { title: string; kind: AssetKind; items: string[] }) {
  const emptyText = {
    image: TEXT.noImages,
    video: TEXT.noVideos,
    document: TEXT.noDocuments,
  }[kind];

  return (
    <section className="material-panel">
      <div className="material-section-heading">
        <div>
          <p className="material-eyebrow">{TEXT.assetFile}</p>
          <h2>{title}</h2>
        </div>
        <span className="material-muted">
          {items.length} {TEXT.fileCountSuffix}
        </span>
      </div>

      {items.length ? (
        <div className={`material-assets material-assets--${kind}`}>
          {items.map((item) => (
            <article className="material-asset-card" key={item}>
              {kind === 'image' && <img src={item} alt={getFileName(item)} loading="lazy" />}
              {kind === 'video' && (
                <video controls preload="metadata">
                  <source src={item} />
                  {TEXT.videoUnsupported}
                </video>
              )}
              {kind === 'document' && (
                <div className="material-doc-icon" aria-hidden="true">
                  DOC
                </div>
              )}
              <div>
                <strong title={item}>{getFileName(item)}</strong>
                <span title={item}>{item}</span>
              </div>
              <a className="material-link-button" href={item} target="_blank" rel="noreferrer">
                {TEXT.open}
              </a>
            </article>
          ))}
        </div>
      ) : (
        <div className="material-empty">
          <strong>{emptyText}</strong>
          <p>{TEXT.uploadHint}</p>
        </div>
      )}
    </section>
  );
}

export default function MaterialDetailPage() {
  const { id: materialId = '' } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [material, setMaterial] = useState<ProductMaterial | null>(null);
  const [isLoading, setIsLoading] = useState(Boolean(materialId));
  const [errorMessage, setErrorMessage] = useState('');
  const [isAiNoticeOpen, setIsAiNoticeOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    if (!materialId) {
      setIsLoading(false);
      setErrorMessage(TEXT.missingId);
      return;
    }

    const controller = new AbortController();

    async function loadMaterial() {
      setIsLoading(true);
      setErrorMessage('');

      try {
        const nextMaterial = await api.material.get(materialId);
        setMaterial(nextMaterial);
      } catch (error) {
        if (error instanceof DOMException && error.name === 'AbortError') {
          return;
        }

        setErrorMessage(error instanceof Error ? error.message : TEXT.loadFailedMessage);
      } finally {
        setIsLoading(false);
      }
    }

    void loadMaterial();

    return () => controller.abort();
  }, [materialId]);

  async function handleDelete() {
    if (!materialId || !material?.id) {
      return;
    }

    if (!window.confirm(TEXT.deleteConfirm)) {
      return;
    }

    setIsDeleting(true);
    setErrorMessage('');

    try {
      await api.material.remove(materialId);
      navigate('/materials', { replace: true });
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : TEXT.loadFailedMessage);
    } finally {
      setIsDeleting(false);
    }
  }

  const images = normalizeAssets(material?.images);
  const videos = normalizeAssets(material?.videos);
  const documents = normalizeAssets(material?.documents);

  return (
    <main className="material-page">
      <style>{styles}</style>

      <header className="material-hero">
        <div>
          <p className="material-eyebrow">{TEXT.detailEyebrow}</p>
          <h1>{material?.productName || TEXT.detailTitle}</h1>
          <p>{TEXT.subtitle}</p>
        </div>
        <div className="material-actions">
          <a className="material-secondary-button" href="/materials">
            {TEXT.backToList}
          </a>
          <button className="material-ai-button" type="button" title={AI_PENDING_TEXT} onClick={() => setIsAiNoticeOpen(true)}>
            {TEXT.aiGenerate} ({AI_PENDING_TEXT})
          </button>
        </div>
      </header>

      <section className="material-panel material-panel--notice" aria-label="api status">
        <div>
          <p className="material-eyebrow">{TEXT.realApi}</p>
          <h2>GET /api/v1/material/{'{id}'}</h2>
          <p>{TEXT.apiDesc}</p>
        </div>
        <div className="material-api-grid">
          <span>{TEXT.available}: GET /api/v1/material/{'{id}'}</span>
          <span>{TEXT.available}: POST /api/v1/material/upload</span>
          <span>{TEXT.available}: PUT /api/v1/material/{'{id}'}</span>
          <span>{TEXT.available}: DELETE /api/v1/material/{'{id}'}</span>
          <span>AI: {AI_PENDING_TEXT}</span>
        </div>
      </section>

      {isLoading && (
        <section className="material-panel">
          <div className="material-empty">
            <strong>{TEXT.loadingTitle}</strong>
            <p>GET /api/v1/material/{materialId}</p>
          </div>
        </section>
      )}

      {errorMessage && !isLoading && (
        <section className="material-panel material-panel--error" role="alert">
          <p className="material-eyebrow">{TEXT.loadFailed}</p>
          <h2>{TEXT.cannotLoad}</h2>
          <p>{errorMessage}</p>
        </section>
      )}

      {!isLoading && !errorMessage && material && (
        <>
          <section className="material-panel" aria-label={TEXT.baseInfo}>
            <div className="material-section-heading">
              <div>
                <p className="material-eyebrow">{TEXT.baseInfo}</p>
                <h2>{TEXT.productInfo}</h2>
              </div>
              <StatusPill status={material.status} />
            </div>

            <div className="material-info-grid">
              <article>
                <span>{TEXT.productName}</span>
                <strong>{material.productName || '-'}</strong>
              </article>
              <article>
                <span>{TEXT.brand}</span>
                <strong>{material.brandName || '-'}</strong>
              </article>
              <article>
                <span>{TEXT.category}</span>
                <strong>{material.category || '-'}</strong>
              </article>
              <article>
                <span>{TEXT.sku}</span>
                <strong>{material.productSku || material.sku || '-'}</strong>
              </article>
              <article>
                <span>{TEXT.price}</span>
                <strong>{material.price ?? '-'}</strong>
              </article>
              <article>
                <span>{TEXT.uploader}</span>
                <strong>{material.uploader || '-'}</strong>
              </article>
              <article>
                <span>{TEXT.uploadTime}</span>
                <strong>{formatDateTime(material.uploadTime)}</strong>
              </article>
              <article>
                <span>{TEXT.createTime}</span>
                <strong>{formatDateTime(material.createTime)}</strong>
              </article>
              <article>
                <span>{TEXT.updateTime}</span>
                <strong>{formatDateTime(material.updateTime)}</strong>
              </article>
              <article>
                <span>{TEXT.fileType}</span>
                <strong>{material.fileType || '-'}</strong>
              </article>
              <article>
                <span>{TEXT.fileSize}</span>
                <strong>{formatFileSize(material.fileSize)}</strong>
              </article>
              <article>
                <span>{TEXT.parseStatus}</span>
                <strong>{getParseStatusLabel(material.parseStatus)}</strong>
              </article>
            </div>

            <div className="material-description">
              <span>{TEXT.description}</span>
              <p>{material.description || TEXT.noDescription}</p>
            </div>

            {material.parseError && (
              <div className="material-inline-warning">
                <strong>{TEXT.parseError}</strong>
                <p>{material.parseError}</p>
              </div>
            )}

            <div className="material-footer-actions">
              <button className="material-secondary-button" type="button" onClick={() => navigate('/materials')}>
                {TEXT.backToList}
              </button>
              <button className="material-ghost-button" type="button" onClick={() => void handleDelete()} disabled={isDeleting}>
                {isDeleting ? TEXT.deleting : TEXT.deleteMaterial}
              </button>
              {material.originalFilePath ? (
                <a className="material-secondary-button" href={material.originalFilePath} target="_blank" rel="noreferrer">
                  {TEXT.originalFile}
                </a>
              ) : (
                <button className="material-disabled-button" type="button" disabled>
                  {TEXT.originalFile}
                </button>
              )}
            </div>
          </section>

          <AssetList title={TEXT.images} kind="image" items={images} />
          <AssetList title={TEXT.videos} kind="video" items={videos} />
          <AssetList title={TEXT.documents} kind="document" items={documents} />

          <section className="material-panel material-ai-panel" aria-label={TEXT.aiCapability}>
            <div>
              <p className="material-eyebrow">{TEXT.aiCapability}</p>
              <h2>{AI_PENDING_TEXT}</h2>
              <p>{TEXT.aiPanelDesc}</p>
            </div>
            <button className="material-ai-button" type="button" onClick={() => setIsAiNoticeOpen(true)}>
              {TEXT.aiPrompt}
            </button>
          </section>
        </>
      )}

      {isAiNoticeOpen && <AiPendingNotice onClose={() => setIsAiNoticeOpen(false)} />}
    </main>
  );
}

const styles = `
.material-page {
  min-height: 100%;
  padding: 24px;
  color: #172033;
  background:
    radial-gradient(circle at 12% 8%, rgba(24, 144, 255, 0.14), transparent 28%),
    linear-gradient(135deg, #f7fbff 0%, #eef4f8 42%, #f8fafc 100%);
}

.material-hero,
.material-panel {
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: rgba(255, 255, 255, 0.68);
  box-shadow: 0 22px 60px rgba(31, 45, 61, 0.12);
  backdrop-filter: blur(18px);
}

.material-hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
  padding: 28px;
  border-radius: 8px;
}

.material-hero h1,
.material-panel h2,
.material-modal h2 {
  margin: 0;
  letter-spacing: 0;
  color: #111827;
}

.material-hero h1 {
  font-size: 30px;
  line-height: 1.2;
}

.material-hero p,
.material-panel p,
.material-modal p {
  margin: 8px 0 0;
  color: #5f6b7a;
  line-height: 1.7;
}

.material-eyebrow {
  margin: 0 0 8px !important;
  color: #1890ff !important;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

.material-actions,
.material-footer-actions,
.material-modal footer {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.material-primary-button,
.material-secondary-button,
.material-disabled-button,
.material-link-button,
.material-ai-button,
.material-icon-button {
  min-height: 36px;
  border-radius: 8px;
  border: 1px solid transparent;
  padding: 8px 14px;
  font: inherit;
  text-decoration: none;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.material-primary-button {
  color: #fff;
  background: #1890ff;
  box-shadow: 0 10px 20px rgba(24, 144, 255, 0.25);
}

.material-secondary-button,
.material-link-button {
  color: #1769aa;
  background: rgba(255, 255, 255, 0.82);
  border-color: rgba(24, 144, 255, 0.36);
}

.material-ai-button {
  color: #666;
  background: rgba(255, 255, 255, 0.48);
  border-color: rgba(140, 140, 140, 0.6);
  border-style: dashed;
}

.material-disabled-button {
  color: #8c8c8c;
  background: rgba(217, 217, 217, 0.54);
  border-color: rgba(140, 140, 140, 0.22);
  box-shadow: none;
  cursor: not-allowed;
}

.material-icon-button {
  width: 36px;
  padding: 0;
  color: #64748b;
  background: rgba(255, 255, 255, 0.72);
  border-color: rgba(148, 163, 184, 0.24);
}

.material-panel {
  margin-top: 18px;
  padding: 22px;
  border-radius: 8px;
}

.material-panel--notice {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 0.65fr);
  gap: 18px;
  align-items: center;
  border-color: rgba(250, 173, 20, 0.42);
  background: rgba(255, 251, 230, 0.68);
}

.material-panel--error {
  border-color: rgba(245, 34, 45, 0.32);
  background: rgba(255, 241, 240, 0.72);
}

.material-api-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}

.material-api-grid span {
  padding: 9px 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.62);
  color: #3f4856;
  font-size: 13px;
}

.material-section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.material-muted {
  color: #64748b;
  font-size: 13px;
}

.material-info-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 12px;
}

.material-info-grid article,
.material-description,
.material-inline-warning {
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 8px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.52);
}

.material-info-grid span,
.material-description span {
  display: block;
  color: #64748b;
  font-size: 12px;
  margin-bottom: 6px;
}

.material-info-grid strong {
  color: #1f2937;
  word-break: break-word;
}

.material-description,
.material-inline-warning,
.material-footer-actions {
  margin-top: 14px;
}

.material-description p,
.material-inline-warning p {
  margin: 0;
}

.material-inline-warning {
  border-color: rgba(250, 173, 20, 0.38);
  background: rgba(255, 251, 230, 0.7);
}

.material-status {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  border-radius: 999px;
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 700;
}

.material-status--neutral {
  color: #595959;
  background: rgba(217, 217, 217, 0.54);
}

.material-status--info {
  color: #0958d9;
  background: rgba(24, 144, 255, 0.12);
}

.material-status--warning {
  color: #ad6800;
  background: rgba(250, 173, 20, 0.18);
}

.material-status--success {
  color: #237804;
  background: rgba(82, 196, 26, 0.16);
}

.material-status--danger {
  color: #a8071a;
  background: rgba(245, 34, 45, 0.12);
}

.material-assets {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}

.material-assets--document {
  grid-template-columns: 1fr;
}

.material-asset-card {
  display: grid;
  gap: 12px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 8px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.56);
}

.material-asset-card img,
.material-asset-card video {
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: cover;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.08);
}

.material-asset-card strong,
.material-asset-card span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.material-asset-card strong {
  color: #1f2937;
}

.material-asset-card span {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.material-doc-icon {
  display: grid;
  place-items: center;
  width: 54px;
  height: 64px;
  border-radius: 8px;
  color: #1769aa;
  background: rgba(24, 144, 255, 0.12);
  font-weight: 800;
}

.material-empty {
  display: grid;
  place-items: center;
  gap: 8px;
  min-height: 140px;
  padding: 22px;
  text-align: center;
  color: #64748b;
}

.material-empty strong {
  color: #334155;
}

.material-ai-panel {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
  border-color: rgba(140, 140, 140, 0.38);
}

.material-modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: grid;
  place-items: center;
  padding: 18px;
  background: rgba(15, 23, 42, 0.34);
}

.material-modal {
  width: min(560px, 100%);
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 8px;
  padding: 22px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 22px 70px rgba(15, 23, 42, 0.24);
  backdrop-filter: blur(18px);
}

.material-modal header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.material-modal footer {
  justify-content: flex-end;
  margin-top: 18px;
}

@media (max-width: 920px) {
  .material-page {
    padding: 16px;
  }

  .material-hero,
  .material-section-heading,
  .material-panel--notice,
  .material-ai-panel {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }

  .material-info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .material-hero {
    padding: 20px;
  }

  .material-hero h1 {
    font-size: 24px;
  }

  .material-info-grid,
  .material-assets {
    grid-template-columns: 1fr;
  }

  .material-actions,
  .material-footer-actions {
    width: 100%;
  }

  .material-actions > *,
  .material-footer-actions > *,
  .material-ai-panel > button {
    width: 100%;
    justify-content: center;
    text-align: center;
  }
}
`;
