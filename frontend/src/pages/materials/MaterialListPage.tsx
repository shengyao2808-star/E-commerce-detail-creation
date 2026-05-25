import { type FormEvent, useEffect, useMemo, useRef, useState } from 'react';

import { api } from '../../services/api';
import type { ProductMaterial } from '../../services/types';

type MaterialStatus = ProductMaterial['status'];

interface UploadForm {
  productName: string;
  brandName: string;
  category: string;
  sku: string;
  uploader: string;
  price: string;
  description: string;
  images: string;
  videos: string;
  documents: string;
}

const AI_PENDING_TEXT = '\u5f85\u63a5\u5165\u672c\u5730AI\u670d\u52a1';
const EMPTY_LIST_NOTICE = '\u5f53\u524d\u7b5b\u9009\u6761\u4ef6\u4e0b\u6682\u65e0\u5546\u54c1\u8d44\u6599\u3002';

const TEXT = {
  module: '\u5546\u54c1\u8d44\u6599\u6a21\u5757',
  title: '\u5546\u54c1\u8d44\u6599\u7ba1\u7406',
  subtitle:
    '\u4e0a\u4f20\u5546\u54c1\u57fa\u7840\u8d44\u6599\uff0c\u67e5\u770b\u5df2\u521b\u5efa\u8d44\u6599\uff0c\u5e76\u901a\u8fc7\u540e\u7aef\u63a5\u53e3\u5b8c\u6210\u67e5\u8be2\u3001\u7f16\u8f91\u548c\u5220\u9664\u3002',
  aiParse: 'AI \u89e3\u6790\u8d44\u6599',
  uploadProduct: '+ \u4e0a\u4f20\u5546\u54c1',
  collapseUpload: '\u6536\u8d77\u4e0a\u4f20',
  apiStatus: '\u63a5\u53e3\u72b6\u6001',
  listApiMissing: '\u8d44\u6599 CRUD \u63a5\u53e3',
  listApiDesc:
    '\u5217\u8868\u3001\u7f16\u8f91\u548c\u5220\u9664\u5747\u5df2\u63a5\u5165\u540e\u7aef\u771f\u5b9e\u63a5\u53e3\uff1b\u54c1\u724c\u3001\u7c7b\u76ee\u548c\u72b6\u6001\u7b5b\u9009\u5728\u5f53\u524d\u9875\u9762\u672c\u5730\u4e8c\u6b21\u8fc7\u6ee4\u3002',
  available: '\u53ef\u7528',
  filterArea: '\u7b5b\u9009\u533a',
  materialSearch: '\u8d44\u6599\u68c0\u7d22',
  filterPending: 'GET /api/v1/material/list',
  keyword: '\u5173\u952e\u8bcd',
  keywordPlaceholder: '\u5546\u54c1\u540d\u79f0 / SKU',
  brand: '\u54c1\u724c',
  brandPlaceholder: '\u54c1\u724c\u540d\u79f0',
  category: '\u7c7b\u76ee',
  categoryPlaceholder: '\u5546\u54c1\u7c7b\u76ee',
  status: '\u72b6\u6001',
  all: '\u5168\u90e8',
  query: '\u67e5\u8be2',
  reset: '\u91cd\u7f6e',
  realApi: '\u771f\u5b9e\u63a5\u53e3',
  uploadMaterial: '\u4e0a\u4f20\u5546\u54c1\u8d44\u6599',
  editMaterial: '\u7f16\u8f91\u5546\u54c1\u8d44\u6599',
  productName: '\u5546\u54c1\u540d\u79f0',
  productNameRequired: '\u5546\u54c1\u540d\u79f0 *',
  productNamePlaceholder: '\u8bf7\u8f93\u5165\u5546\u54c1\u540d\u79f0',
  sku: 'SKU',
  skuPlaceholder: '\u5546\u54c1 SKU',
  uploader: '\u4e0a\u4f20\u4eba',
  uploaderPlaceholder: '\u64cd\u4f5c\u4eba',
  price: '\u4ef7\u683c',
  description: '\u5546\u54c1\u63cf\u8ff0',
  descriptionPlaceholder: '\u8f93\u5165\u5546\u54c1\u5356\u70b9\u3001\u89c4\u683c\u6216\u539f\u59cb\u63cf\u8ff0',
  imageUrl: '\u56fe\u7247 URL',
  imageUrlPlaceholder: '\u6bcf\u884c\u4e00\u4e2a\u56fe\u7247 URL',
  videoUrl: '\u89c6\u9891 URL',
  videoUrlPlaceholder: '\u6bcf\u884c\u4e00\u4e2a\u89c6\u9891 URL',
  documentUrl: '\u6587\u6863 URL',
  documentUrlPlaceholder: '\u6bcf\u884c\u4e00\u4e2a\u6587\u6863 URL',
  chooseFiles: '\u9009\u62e9\u672c\u5730\u6587\u4ef6',
  binaryPending:
    '\u5f53\u524d\u4e0a\u4f20\u63a5\u53e3\u63a5\u6536 JSON \u8d44\u6599\uff1b\u6587\u4ef6\u4e8c\u8fdb\u5236\u4e0a\u4f20\u5f85\u516c\u5171 API \u5c42\u786e\u8ba4\u3002',
  submitMaterial: '\u63d0\u4ea4\u8d44\u6599',
  saveMaterial: '\u4fdd\u5b58\u8d44\u6599',
  uploading: '\u4e0a\u4f20\u4e2d...',
  saving: '\u4fdd\u5b58\u4e2d...',
  loading: '\u52a0\u8f7d\u4e2d...',
  listArea: '\u5217\u8868\u533a',
  materialList: '\u5546\u54c1\u8d44\u6599\u5217\u8868',
  visibleCountPrefix: '\u5171',
  visibleCountSuffix: '\u6761\u53ef\u5c55\u793a\u8d44\u6599',
  operation: '\u64cd\u4f5c',
  view: '\u67e5\u770b',
  edit: '\u7f16\u8f91',
  delete: '\u5220\u9664',
  noDescription: '\u6682\u65e0\u63cf\u8ff0',
  noListData: '\u6682\u65e0\u5217\u8868\u6570\u636e',
  viewDetail: '\u67e5\u770b\u8be6\u60c5',
  uploadTime: '\u4e0a\u4f20\u65f6\u95f4',
  prevPage: '\u4e0a\u4e00\u9875',
  nextPage: '\u4e0b\u4e00\u9875',
  fillProductName: '\u8bf7\u5148\u586b\u5199\u5546\u54c1\u540d\u79f0\u3002',
  uploadFailed: '\u4e0a\u4f20\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002',
  uploadSuccess: '\u4e0a\u4f20\u6210\u529f\uff0c\u8d44\u6599 ID',
  updateSuccess: '\u8d44\u6599\u5df2\u66f4\u65b0\u3002',
  deleteSuccess: '\u8d44\u6599\u5df2\u5220\u9664\u3002',
  loadFailed: '\u5217\u8868\u52a0\u8f7d\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002',
  updateFailed: '\u66f4\u65b0\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002',
  deleteFailed: '\u5220\u9664\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002',
  confirmDelete: '\u786e\u5b9a\u5220\u9664\u8be5\u5546\u54c1\u8d44\u6599\uff1f',
  cancelEdit: '\u53d6\u6d88\u7f16\u8f91',
  unfilledBrand: '\u672a\u586b\u5199\u54c1\u724c',
};

const emptyUploadForm: UploadForm = {
  productName: '',
  brandName: '',
  category: '',
  sku: '',
  uploader: '',
  price: '',
  description: '',
  images: '',
  videos: '',
  documents: '',
};

const columns = [
  TEXT.productName,
  TEXT.sku,
  TEXT.brand,
  TEXT.category,
  TEXT.uploader,
  TEXT.status,
  TEXT.operation,
];

function splitLines(value: string): string[] {
  return value
    .split('\n')
    .map((item) => item.trim())
    .filter(Boolean);
}

function normalizeUploadPayload(form: UploadForm) {
  return {
    productName: form.productName.trim(),
    brandName: form.brandName.trim() || TEXT.unfilledBrand,
    category: form.category.trim(),
    sku: form.sku.trim(),
    uploader: form.uploader.trim(),
    price: form.price ? Number(form.price) : undefined,
    description: form.description.trim(),
    images: splitLines(form.images),
    videos: splitLines(form.videos),
    documents: splitLines(form.documents),
  };
}

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

function buildDetailHref(id?: number | string) {
  return id ? `/materials/${id}` : '#';
}

function formFromMaterial(item: ProductMaterial): UploadForm {
  return {
    productName: item.productName ?? '',
    brandName: item.brandName ?? item.brand ?? '',
    category: item.category ?? '',
    sku: item.productSku ?? item.sku ?? '',
    uploader: item.uploader ?? '',
    price: item.price !== undefined && item.price !== null ? String(item.price) : '',
    description: item.description ?? '',
    images: Array.isArray(item.images) ? item.images.join('\n') : '',
    videos: Array.isArray(item.videos) ? item.videos.join('\n') : '',
    documents: Array.isArray(item.documents) ? item.documents.join('\n') : '',
  };
}

function textIncludes(value: string | number | undefined, keyword: string) {
  if (!keyword) {
    return true;
  }
  return String(value ?? '').toLowerCase().includes(keyword.toLowerCase());
}

function StatusPill({ status }: { status?: MaterialStatus }) {
  const meta = getStatusMeta(status);

  return <span className={`material-status material-status--${meta.className}`}>{meta.label}</span>;
}

function MissingApiNotice() {
  return (
    <section className="material-panel material-panel--notice" aria-label={TEXT.listApiMissing}>
      <div>
        <p className="material-eyebrow">{TEXT.apiStatus}</p>
        <h2>{TEXT.listApiMissing}</h2>
        <p>
          <code>GET /api/v1/material/list</code> / <code>PUT /api/v1/material/{'{id}'}</code> /{' '}
          <code>DELETE /api/v1/material/{'{id}'}</code> {TEXT.listApiDesc}
        </p>
      </div>
      <div className="material-api-grid">
        <span>{TEXT.available}: POST /api/v1/material/upload</span>
        <span>{TEXT.available}: GET /api/v1/material/{'{id}'}</span>
        <span>{TEXT.available}: PUT /api/v1/material/{'{id}'}</span>
        <span>{TEXT.available}: DELETE /api/v1/material/{'{id}'}</span>
      </div>
    </section>
  );
}

export default function MaterialListPage() {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [filters, setFilters] = useState({
    keyword: '',
    brand: '',
    category: '',
    status: 'all',
  });
  const [uploadForm, setUploadForm] = useState<UploadForm>(emptyUploadForm);
  const [isUploadOpen, setIsUploadOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [uploadMessage, setUploadMessage] = useState('');
  const [listMessage, setListMessage] = useState('');
  const [materials, setMaterials] = useState<ProductMaterial[]>([]);
  const [isLoadingList, setIsLoadingList] = useState(true);
  const [editingMaterialId, setEditingMaterialId] = useState<number | string | null>(null);

  const canSubmit = useMemo(() => uploadForm.productName.trim().length > 0, [uploadForm.productName]);
  const isEditing = editingMaterialId !== null;
  const tableRows = useMemo(
    () =>
      materials.filter((item) => {
        const brandMatch = !filters.brand.trim() || textIncludes(item.brandName ?? item.brand, filters.brand.trim());
        const categoryMatch = !filters.category.trim() || textIncludes(item.category, filters.category.trim());
        const statusMatch = filters.status === 'all' || String(item.status ?? '') === filters.status;
        return brandMatch && categoryMatch && statusMatch;
      }),
    [materials, filters.brand, filters.category, filters.status]
  );

  useEffect(() => {
    void loadMaterials('');
  }, []);

  async function loadMaterials(keyword: string) {
    setIsLoadingList(true);
    setListMessage('');

    try {
      const result = await api.material.list({
        pageNum: 1,
        pageSize: 50,
        keyword: keyword.trim() || undefined,
      });

      const nextMaterials = result.data ?? [];
      setMaterials(nextMaterials);
      setListMessage(nextMaterials.length ? '' : EMPTY_LIST_NOTICE);
    } catch (error) {
      setMaterials([]);
      setListMessage(error instanceof Error ? error.message : TEXT.loadFailed);
    } finally {
      setIsLoadingList(false);
    }
  }

  function beginEdit(item: ProductMaterial) {
    setEditingMaterialId(item.id ?? null);
    setUploadForm(formFromMaterial(item));
    setIsUploadOpen(true);
    setUploadMessage('');
  }

  function cancelEdit() {
    setEditingMaterialId(null);
    setUploadForm(emptyUploadForm);
    setUploadMessage('');
  }

  async function handleUploadSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!canSubmit) {
      setUploadMessage(TEXT.fillProductName);
      return;
    }

    setIsSubmitting(true);
    setUploadMessage('');

    try {
      const payload = normalizeUploadPayload(uploadForm);
      if (isEditing && editingMaterialId !== null) {
        await api.material.update(editingMaterialId, payload);
        setUploadMessage(TEXT.updateSuccess);
      } else {
        const result = await api.material.upload(payload);
        setUploadMessage(`${TEXT.uploadSuccess}: ${result ?? '-'}`);
      }

      setUploadForm(emptyUploadForm);
      setEditingMaterialId(null);
      setIsUploadOpen(false);
      await loadMaterials(filters.keyword);
    } catch (error) {
      setUploadMessage(error instanceof Error ? error.message : isEditing ? TEXT.updateFailed : TEXT.uploadFailed);
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete(item: ProductMaterial) {
    if (item.id === undefined || item.id === null) {
      return;
    }

    if (!window.confirm(TEXT.confirmDelete)) {
      return;
    }

    try {
      await api.material.remove(item.id);
      if (editingMaterialId === item.id) {
        cancelEdit();
      }
      setUploadMessage(TEXT.deleteSuccess);
      await loadMaterials(filters.keyword);
    } catch (error) {
      setListMessage(error instanceof Error ? error.message : TEXT.deleteFailed);
    }
  }

  function handleQuery() {
    void loadMaterials(filters.keyword);
  }

  function handleReset() {
    const nextFilters = { keyword: '', brand: '', category: '', status: 'all' };
    setFilters(nextFilters);
    void loadMaterials('');
  }

  return (
    <main className="material-page">
      <style>{styles}</style>

      <header className="material-hero">
        <div>
          <p className="material-eyebrow">{TEXT.module}</p>
          <h1>{TEXT.title}</h1>
          <p>{TEXT.subtitle}</p>
        </div>
        <div className="material-actions">
          <button className="material-ai-button" type="button" title={AI_PENDING_TEXT}>
            {TEXT.aiParse} ({AI_PENDING_TEXT})
          </button>
          <button
            className="material-primary-button"
            type="button"
            onClick={() =>
              setIsUploadOpen((value) => {
                const nextValue = !value;
                if (!nextValue) {
                  cancelEdit();
                }
                return nextValue;
              })
            }
          >
            {isUploadOpen ? TEXT.collapseUpload : TEXT.uploadProduct}
          </button>
        </div>
      </header>

      <MissingApiNotice />

      <section className="material-panel" aria-label={TEXT.filterArea}>
        <div className="material-section-heading">
          <div>
            <p className="material-eyebrow">{TEXT.filterArea}</p>
            <h2>{TEXT.materialSearch}</h2>
          </div>
          <span className="material-muted">{TEXT.filterPending}</span>
        </div>

        <form className="material-filter-grid">
          <label>
            <span>{TEXT.keyword}</span>
            <input
              value={filters.keyword}
              placeholder={TEXT.keywordPlaceholder}
              onChange={(event) => setFilters((value) => ({ ...value, keyword: event.target.value }))}
            />
          </label>
          <label>
            <span>{TEXT.brand}</span>
            <input
              value={filters.brand}
              placeholder={TEXT.brandPlaceholder}
              onChange={(event) => setFilters((value) => ({ ...value, brand: event.target.value }))}
            />
          </label>
          <label>
            <span>{TEXT.category}</span>
            <input
              value={filters.category}
              placeholder={TEXT.categoryPlaceholder}
              onChange={(event) => setFilters((value) => ({ ...value, category: event.target.value }))}
            />
          </label>
          <label>
            <span>{TEXT.status}</span>
            <select value={filters.status} onChange={(event) => setFilters((value) => ({ ...value, status: event.target.value }))}>
              <option value="all">{TEXT.all}</option>
              <option value="0">{getStatusMeta(0).label}</option>
              <option value="1">{getStatusMeta(1).label}</option>
              <option value="2">{getStatusMeta(2).label}</option>
              <option value="3">{getStatusMeta(3).label}</option>
              <option value="4">{getStatusMeta(4).label}</option>
            </select>
          </label>
          <button className="material-secondary-button" type="button" onClick={handleQuery}>
            {TEXT.query}
          </button>
          <button className="material-ghost-button" type="button" onClick={handleReset}>
            {TEXT.reset}
          </button>
        </form>
      </section>

      {isUploadOpen && (
        <section className="material-panel" aria-label={TEXT.uploadMaterial}>
          <div className="material-section-heading">
            <div>
              <p className="material-eyebrow">{TEXT.realApi}</p>
              <h2>{isEditing ? TEXT.editMaterial : TEXT.uploadMaterial}</h2>
            </div>
            <span className="material-muted">{isEditing ? `PUT /api/v1/material/{id}` : 'POST /api/v1/material/upload'}</span>
          </div>

          <form className="material-upload-grid" onSubmit={handleUploadSubmit}>
            <label>
              <span>{TEXT.productNameRequired}</span>
              <input
                value={uploadForm.productName}
                onChange={(event) => setUploadForm((value) => ({ ...value, productName: event.target.value }))}
                placeholder={TEXT.productNamePlaceholder}
              />
            </label>
            <label>
              <span>{TEXT.brand}</span>
              <input
                value={uploadForm.brandName}
                onChange={(event) => setUploadForm((value) => ({ ...value, brandName: event.target.value }))}
                placeholder={TEXT.brandPlaceholder}
              />
            </label>
            <label>
              <span>{TEXT.category}</span>
              <input
                value={uploadForm.category}
                onChange={(event) => setUploadForm((value) => ({ ...value, category: event.target.value }))}
                placeholder={TEXT.categoryPlaceholder}
              />
            </label>
            <label>
              <span>{TEXT.sku}</span>
              <input
                value={uploadForm.sku}
                onChange={(event) => setUploadForm((value) => ({ ...value, sku: event.target.value }))}
                placeholder={TEXT.skuPlaceholder}
              />
            </label>
            <label>
              <span>{TEXT.uploader}</span>
              <input
                value={uploadForm.uploader}
                onChange={(event) => setUploadForm((value) => ({ ...value, uploader: event.target.value }))}
                placeholder={TEXT.uploaderPlaceholder}
              />
            </label>
            <label>
              <span>{TEXT.price}</span>
              <input
                type="number"
                min="0"
                step="0.01"
                value={uploadForm.price}
                onChange={(event) => setUploadForm((value) => ({ ...value, price: event.target.value }))}
                placeholder="0.00"
              />
            </label>
            <label className="material-field-wide">
              <span>{TEXT.description}</span>
              <textarea
                value={uploadForm.description}
                onChange={(event) => setUploadForm((value) => ({ ...value, description: event.target.value }))}
                placeholder={TEXT.descriptionPlaceholder}
              />
            </label>
            <label>
              <span>{TEXT.imageUrl}</span>
              <textarea
                value={uploadForm.images}
                onChange={(event) => setUploadForm((value) => ({ ...value, images: event.target.value }))}
                placeholder={TEXT.imageUrlPlaceholder}
              />
            </label>
            <label>
              <span>{TEXT.videoUrl}</span>
              <textarea
                value={uploadForm.videos}
                onChange={(event) => setUploadForm((value) => ({ ...value, videos: event.target.value }))}
                placeholder={TEXT.videoUrlPlaceholder}
              />
            </label>
            <label>
              <span>{TEXT.documentUrl}</span>
              <textarea
                value={uploadForm.documents}
                onChange={(event) => setUploadForm((value) => ({ ...value, documents: event.target.value }))}
                placeholder={TEXT.documentUrlPlaceholder}
              />
            </label>
            <input ref={fileInputRef} type="file" hidden multiple />
            <div className="material-upload-actions">
              <button className="material-ghost-button" type="button" onClick={() => fileInputRef.current?.click()}>
                {TEXT.chooseFiles}
              </button>
              {isEditing && (
                <button className="material-secondary-button" type="button" onClick={cancelEdit}>
                  {TEXT.cancelEdit}
                </button>
              )}
              <span className="material-muted">{TEXT.binaryPending}</span>
              <button className="material-primary-button" type="submit" disabled={!canSubmit || isSubmitting}>
                {isSubmitting ? (isEditing ? TEXT.saving : TEXT.uploading) : isEditing ? TEXT.saveMaterial : TEXT.submitMaterial}
              </button>
            </div>
          </form>
        </section>
      )}

      {uploadMessage && (
        <div className="material-feedback" role="status">
          {uploadMessage}
        </div>
      )}

      <section className="material-panel" aria-label={TEXT.listArea}>
        <div className="material-section-heading">
          <div>
            <p className="material-eyebrow">{TEXT.listArea}</p>
            <h2>{TEXT.materialList}</h2>
          </div>
          <span className="material-muted">{isLoadingList ? TEXT.loading : `${TEXT.visibleCountPrefix} ${tableRows.length} ${TEXT.visibleCountSuffix}`}</span>
        </div>

        {listMessage && (
          <div className="mt-2 rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-600">{listMessage}</div>
        )}

        <div className="material-table-wrap">
          <table className="material-table">
            <thead>
              <tr>
                {columns.map((column) => (
                  <th key={column}>{column}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {tableRows.map((item) => (
                <tr key={item.id ?? item.productName}>
                  <td>
                    <strong>{item.productName || '-'}</strong>
                    <span>{item.description || TEXT.noDescription}</span>
                  </td>
                  <td>{item.productSku || item.sku || '-'}</td>
                  <td>{item.brandName || '-'}</td>
                  <td>{item.category || '-'}</td>
                  <td>{item.uploader || '-'}</td>
                  <td>
                    <StatusPill status={item.status} />
                  </td>
                  <td>
                    <div className="material-row-actions">
                      <a className="material-link-button" href={buildDetailHref(item.id)}>
                        {TEXT.view}
                      </a>
                      <button className="material-secondary-button" type="button" onClick={() => beginEdit(item)}>
                        {TEXT.edit}
                      </button>
                      <button className="material-ghost-button" type="button" onClick={() => void handleDelete(item)}>
                        {TEXT.delete}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {!tableRows.length && !isLoadingList && (
                <tr>
                  <td colSpan={columns.length}>
                    <div className="material-empty">
                      <strong>{TEXT.noListData}</strong>
                      <p>{EMPTY_LIST_NOTICE}</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="material-mobile-list">
          {tableRows.map((item) => (
            <article className="material-mobile-card" key={item.id ?? item.productName}>
              <div>
                <strong>{item.productName || '-'}</strong>
                <StatusPill status={item.status} />
              </div>
              <p>{item.description || TEXT.noDescription}</p>
              <dl>
                <div>
                  <dt>{TEXT.sku}</dt>
                  <dd>{item.productSku || item.sku || '-'}</dd>
                </div>
                <div>
                  <dt>{TEXT.brand}</dt>
                  <dd>{item.brandName || '-'}</dd>
                </div>
                <div>
                  <dt>{TEXT.category}</dt>
                  <dd>{item.category || '-'}</dd>
                </div>
                <div>
                  <dt>{TEXT.uploadTime}</dt>
                  <dd>{formatDateTime(item.uploadTime)}</dd>
                </div>
              </dl>
              <div className="material-row-actions">
                <a className="material-link-button" href={buildDetailHref(item.id)}>
                  {TEXT.viewDetail}
                </a>
                <button className="material-secondary-button" type="button" onClick={() => beginEdit(item)}>
                  {TEXT.edit}
                </button>
                <button className="material-ghost-button" type="button" onClick={() => void handleDelete(item)}>
                  {TEXT.delete}
                </button>
              </div>
            </article>
          ))}
          {!tableRows.length && !isLoadingList && (
            <div className="material-empty">
              <strong>{TEXT.noListData}</strong>
              <p>{EMPTY_LIST_NOTICE}</p>
            </div>
          )}
        </div>

        <footer className="material-pagination" aria-label="pagination">
          <button type="button" disabled>
            {TEXT.prevPage}
          </button>
          <span>1 / 1</span>
          <button type="button" disabled>
            {TEXT.nextPage}
          </button>
        </footer>
      </section>
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
.material-panel,
.material-feedback {
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
.material-panel h2 {
  margin: 0;
  letter-spacing: 0;
  color: #111827;
}

.material-hero h1 {
  font-size: 30px;
  line-height: 1.2;
}

.material-hero p,
.material-panel p {
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
.material-row-actions,
.material-upload-actions,
.material-pagination {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.material-primary-button,
.material-secondary-button,
.material-ghost-button,
.material-disabled-button,
.material-link-button,
.material-ai-button,
.material-pagination button {
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

.material-ghost-button,
.material-pagination button {
  color: #475569;
  background: rgba(255, 255, 255, 0.58);
  border-color: rgba(148, 163, 184, 0.32);
}

.material-ai-button {
  color: #666;
  background: rgba(255, 255, 255, 0.48);
  border-color: rgba(140, 140, 140, 0.6);
  border-style: dashed;
}

.material-disabled-button,
.material-primary-button:disabled,
.material-secondary-button:disabled,
.material-pagination button:disabled {
  color: #8c8c8c;
  background: rgba(217, 217, 217, 0.54);
  border-color: rgba(140, 140, 140, 0.22);
  box-shadow: none;
  cursor: not-allowed;
}

.material-panel,
.material-feedback {
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

.material-filter-grid,
.material-upload-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 14px;
  align-items: end;
}

.material-upload-grid {
  grid-template-columns: repeat(3, minmax(180px, 1fr));
}

.material-filter-grid label,
.material-upload-grid label {
  display: grid;
  gap: 7px;
  color: #334155;
  font-size: 13px;
  font-weight: 600;
}

.material-filter-grid input,
.material-filter-grid select,
.material-upload-grid input,
.material-upload-grid textarea {
  width: 100%;
  min-height: 38px;
  box-sizing: border-box;
  border: 1px solid rgba(148, 163, 184, 0.32);
  border-radius: 8px;
  padding: 9px 11px;
  color: #172033;
  background: rgba(255, 255, 255, 0.78);
  outline: none;
}

.material-upload-grid textarea {
  min-height: 92px;
  resize: vertical;
}

.material-field-wide,
.material-upload-actions {
  grid-column: 1 / -1;
}

.material-feedback {
  color: #1f5f3b;
  background: rgba(240, 253, 244, 0.72);
  border-color: rgba(82, 196, 26, 0.3);
}

.material-table-wrap {
  overflow-x: auto;
}

.material-table {
  width: 100%;
  min-width: 920px;
  border-collapse: collapse;
  overflow: hidden;
  border-radius: 8px;
}

.material-table th,
.material-table td {
  padding: 14px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
  text-align: left;
  vertical-align: middle;
}

.material-table th {
  color: #475569;
  font-size: 13px;
  background: rgba(248, 250, 252, 0.86);
}

.material-table td {
  color: #263445;
  background: rgba(255, 255, 255, 0.42);
}

.material-table td strong,
.material-table td span {
  display: block;
}

.material-table td span {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
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

.material-empty {
  display: grid;
  place-items: center;
  gap: 8px;
  min-height: 160px;
  padding: 22px;
  text-align: center;
  color: #64748b;
}

.material-empty strong {
  color: #334155;
}

.material-mobile-list {
  display: none;
}

.material-mobile-card {
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 8px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.56);
}

.material-mobile-card + .material-mobile-card {
  margin-top: 12px;
}

.material-mobile-card > div:first-child {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.material-mobile-card dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin: 14px 0;
}

.material-mobile-card dt {
  color: #64748b;
  font-size: 12px;
}

.material-mobile-card dd {
  margin: 2px 0 0;
  color: #1f2937;
}

.material-pagination {
  justify-content: flex-end;
  margin-top: 18px;
  color: #64748b;
}

@media (max-width: 920px) {
  .material-page {
    padding: 16px;
  }

  .material-hero,
  .material-section-heading,
  .material-panel--notice {
    grid-template-columns: 1fr;
    flex-direction: column;
  }

  .material-filter-grid,
  .material-upload-grid {
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

  .material-filter-grid,
  .material-upload-grid {
    grid-template-columns: 1fr;
  }

  .material-table-wrap {
    display: none;
  }

  .material-mobile-list {
    display: block;
  }

  .material-actions,
  .material-upload-actions,
  .material-row-actions {
    width: 100%;
  }

  .material-actions > *,
  .material-upload-actions > * {
    width: 100%;
    justify-content: center;
    text-align: center;
  }
}
`;
