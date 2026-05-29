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

const AI_PENDING_TEXT = '待接入本地AI服务';

const TEXT = {
  detailEyebrow: '商品资料详情',
  detailTitle: '商品资料详情',
  subtitle:
    '展示基础资料、图片、视频、文档列表，并明确标记编辑、删除和 AI 功能的待接入状态。',
  backToList: '返回列表',
  aiGenerate: 'AI 生成详情页',
  realApi: '真实接口',
  apiDesc:
    '本页读取商品资料详情，删除可以直接调用后端；编辑入口在列表页完成。',
  available: '可用',
  loadingTitle: '正在加载商品资料',
  loadFailed: '加载失败',
  cannotLoad: '无法获取商品资料',
  baseInfo: '基础资料',
  productInfo: '商品信息',
  productName: '商品名称',
  brand: '品牌',
  category: '类目',
  sku: 'SKU',
  price: '价格',
  uploader: '上传人',
  uploadTime: '上传时间',
  createTime: '创建时间',
  updateTime: '更新时间',
  fileType: '文件类型',
  fileSize: '文件大小',
  parseStatus: '解析状态',
  description: '商品描述',
  noDescription: '暂无描述',
  parseError: '解析错误',
  editMaterial: '编辑资料',
  deleteMaterial: '删除资料',
  originalFile: '查看原始文件',
  assetFile: '资料文件',
  images: '商品图片',
  videos: '商品视频',
  documents: '商品文档',
  fileCountSuffix: '个文件',
  noImages: '暂无商品图片',
  noVideos: '暂无商品视频',
  noDocuments: '暂无商品文档',
  uploadHint: '可通过 POST /api/v1/material/upload 上传资料后展示。',
  videoUnsupported: '当前浏览器不支持视频预览。',
  open: '打开',
  aiCapability: 'AI 能力',
  aiPanelDesc:
    'AI 解析资料、图片 OCR、文档理解、生成详情页内容均保留入口，但不调用未完成服务。',
  aiPrompt: '查看 AI 接入提示',
  aiFeature: 'AI 功能',
  aiDisconnected: 'AI 服务未连接',
  currentStatus: '当前状态',
  aiRecommend:
    '推荐方案：Ollama + Qwen2.5，本地部署完成后再启用 AI 解析、生成详情页和文档理解能力。',
  later: '稍后接入',
  close: '关闭',
  missingId: '缺少商品资料 ID，无法调用 GET /api/v1/material/{id}。',
  loadFailedMessage: '商品资料详情加载失败。',
  deleteConfirm: '确定删除该商品资料？',
  deleting: '删除中...',
  deleteSuccess: '资料已删除，正在返回列表。',
};

function getStatusMeta(status?: MaterialStatus) {
  switch (String(status ?? '')) {
    case '0':
      return { label: '草稿', className: 'neutral' };
    case '1':
      return { label: '已提交', className: 'info' };
    case '2':
      return { label: '审核中', className: 'warning' };
    case '3':
      return { label: '审核通过', className: 'success' };
    case '4':
      return { label: '审核拒绝', className: 'danger' };
    default:
      return { label: '暂无状态', className: 'neutral' };
  }
}

function getParseStatusLabel(status?: number) {
  switch (status) {
    case 0:
      return '待解析';
    case 1:
      return '解析中';
    case 2:
      return '解析成功';
    case 3:
      return '解析失败';
    default:
      return '暂无解析状态';
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
