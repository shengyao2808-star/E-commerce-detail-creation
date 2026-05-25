import React, { type FormEvent, useEffect, useMemo, useState } from 'react';

import { api } from '../../services/api';
import type { AuditRecord, AuditSubmitRequest } from '../../services/types';

type AuditStatusCode = 0 | 1 | 2 | 3 | 4;
type RiskLevelCode = 1 | 2 | 3 | 4;

type StatusMeta = {
  code: AuditStatusCode;
  label: string;
  className: string;
};

type RiskMeta = {
  code?: RiskLevelCode;
  label: string;
  className: string;
};

const STATUS_META: Record<AuditStatusCode, StatusMeta> = {
  0: { code: 0, label: '待审核', className: 'audit-status-pending' },
  1: { code: 1, label: '审核中', className: 'audit-status-reviewing' },
  2: { code: 2, label: '通过', className: 'audit-status-approved' },
  3: { code: 3, label: '驳回', className: 'audit-status-rejected' },
  4: { code: 4, label: '需修改', className: 'audit-status-returned' },
};

const RISK_META: Record<RiskLevelCode, RiskMeta> = {
  1: { code: 1, label: '低风险', className: 'audit-risk-low' },
  2: { code: 2, label: '中风险', className: 'audit-risk-medium' },
  3: { code: 3, label: '高风险', className: 'audit-risk-high' },
  4: { code: 4, label: '极高风险', className: 'audit-risk-critical' },
};

const listReadyMessage = '审核列表与审核操作已接入 services/api，数据来自后端分页接口。';
const aiPendingMessage = '待接入本地AI服务';

const initialFilters = {
  productDetailId: '',
  status: 'ALL',
  riskLevel: 'ALL',
  auditor: '',
};

const initialSubmitForm = {
  productDetailId: '',
  auditor: '',
  submitter: '',
  riskLevel: '2',
  auditComment: '',
};

function normalizeAuditStatus(status: AuditRecord['auditStatus']): AuditStatusCode {
  if (typeof status === 'number' && status in STATUS_META) {
    return status as AuditStatusCode;
  }

  const value = String(status ?? '').trim().toUpperCase();
  if (value === '0' || value === 'PENDING') return 0;
  if (value === '1' || value === 'REVIEWING' || value === 'IN_REVIEW' || value === 'PROCESSING') return 1;
  if (value === '2' || value === 'APPROVED' || value === 'PASSED' || value === 'PASS') return 2;
  if (value === '3' || value === 'REJECTED' || value === 'REJECT') return 3;
  if (value === '4' || value === 'RETURNED' || value === 'NEED_MODIFICATION' || value === 'NEEDS_CHANGE') return 4;
  return 0;
}

function normalizeRiskLevel(riskLevel: AuditRecord['riskLevel']): RiskMeta {
  if (typeof riskLevel === 'number' && riskLevel in RISK_META) {
    return RISK_META[riskLevel as RiskLevelCode];
  }

  const value = String(riskLevel ?? '').trim().toUpperCase();
  if (value === '1' || value === 'LOW') return RISK_META[1];
  if (value === '2' || value === 'MEDIUM') return RISK_META[2];
  if (value === '3' || value === 'HIGH') return RISK_META[3];
  if (value === '4' || value === 'CRITICAL' || value === 'EXTREME') return RISK_META[4];
  return { label: '未评估', className: 'audit-risk-empty' };
}

function getRecordKey(record: AuditRecord): string {
  return String(record.id ?? `detail-${record.productDetailId ?? 'unknown'}`);
}

function mergeAuditRecord(records: AuditRecord[], nextRecord: AuditRecord): AuditRecord[] {
  const nextKey = getRecordKey(nextRecord);
  const nextProductDetailId = nextRecord.productDetailId;
  const nextRecords = records.filter((record) => {
    if (getRecordKey(record) === nextKey) return false;
    return record.productDetailId == null || record.productDetailId !== nextProductDetailId;
  });
  return [nextRecord, ...nextRecords];
}

function formatDateTime(value?: string): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

function readStructuredText(value?: string): string[] {
  if (!value) return [];
  try {
    const parsed = JSON.parse(value) as unknown;
    if (Array.isArray(parsed)) {
      return parsed.map((item) => (typeof item === 'string' ? item : JSON.stringify(item)));
    }
    if (parsed && typeof parsed === 'object') {
      return Object.entries(parsed as Record<string, unknown>).map(([key, item]) => `${key}: ${String(item)}`);
    }
  } catch {
    return [value];
  }
  return [value];
}

function filterAuditRecords(
  records: AuditRecord[],
  filters: typeof initialFilters,
): AuditRecord[] {
  return records.filter((record) => {
    if (filters.productDetailId && String(record.productDetailId ?? '') !== filters.productDetailId.trim()) {
      return false;
    }
    if (filters.status !== 'ALL' && String(normalizeAuditStatus(record.auditStatus)) !== filters.status) {
      return false;
    }
    if (filters.riskLevel !== 'ALL' && String(normalizeRiskLevel(record.riskLevel).code ?? '') !== filters.riskLevel) {
      return false;
    }
    if (filters.auditor && !(record.auditor ?? '').toLowerCase().includes(filters.auditor.trim().toLowerCase())) {
      return false;
    }
    return true;
  });
}

function StatusBadge({ status }: { status: AuditRecord['auditStatus'] }) {
  const meta = STATUS_META[normalizeAuditStatus(status)];
  return <span className={`audit-tag ${meta.className}`}>{meta.label}</span>;
}

function RiskBadge({ riskLevel }: { riskLevel: AuditRecord['riskLevel'] }) {
  const meta = normalizeRiskLevel(riskLevel);
  return <span className={`audit-tag ${meta.className}`}>{meta.label}</span>;
}

function DetailField({ label, value }: { label: string; value?: React.ReactNode }) {
  return (
    <div className="audit-detail-field">
      <dt>{label}</dt>
      <dd>{value || '-'}</dd>
    </div>
  );
}

export default function AuditCenterPage() {
  const [filters, setFilters] = useState(initialFilters);
  const [submitForm, setSubmitForm] = useState(initialSubmitForm);
  const [records, setRecords] = useState<AuditRecord[]>([]);
  const [selectedKey, setSelectedKey] = useState('');
  const [draftComment, setDraftComment] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [acting, setActing] = useState<'approve' | 'reject' | 'return' | 'withdraw' | 'reaudit' | ''>('');
  const [pagination, setPagination] = useState({ pageNum: 1, pageSize: 20, total: 0, pages: 0 });
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');

  const filteredRecords = useMemo(() => filterAuditRecords(records, filters), [filters, records]);
  const selectedRecord = useMemo(
    () => records.find((record) => getRecordKey(record) === selectedKey) ?? filteredRecords[0],
    [filteredRecords, records, selectedKey],
  );
  const selectedRiskItems = readStructuredText(selectedRecord?.riskItems);
  const selectedSuggestions = readStructuredText(selectedRecord?.modificationSuggestions);
  const selectedAuditStatus = selectedRecord ? normalizeAuditStatus(selectedRecord.auditStatus) : 0;
  const canApprove = selectedAuditStatus === 0;
  const canReject = selectedAuditStatus === 0;
  const canReturn = selectedAuditStatus === 0 || selectedAuditStatus === 1;
  const canWithdraw = selectedAuditStatus === 0;
  const canReaudit = Boolean(selectedRecord) && selectedAuditStatus !== 0;

  async function loadAuditList(nextFilters = filters) {
    setLoading(true);
    setError('');
    setNotice('');

    try {
      const page = await api.audit.list({
        pageNum: 1,
        pageSize: pagination.pageSize,
        status: nextFilters.status !== 'ALL' ? nextFilters.status : undefined,
        auditor: nextFilters.auditor.trim() || undefined,
      });

      setRecords(page.data ?? []);
      setPagination({
        pageNum: page.pageNum,
        pageSize: page.pageSize,
        total: page.total,
        pages: page.pages,
      });
      setSelectedKey((currentKey) => {
        if (currentKey && page.data?.some((record) => getRecordKey(record) === currentKey)) {
          return currentKey;
        }
        return page.data?.[0] ? getRecordKey(page.data[0]) : '';
      });
      setNotice(`已加载审核任务列表，共 ${page.total} 条。`);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : '加载审核任务列表失败。');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadAuditList(initialFilters);
    // 初次进入页面时加载列表；筛选由表单提交触发。
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleQueryByProductDetailId(event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault();

    if (!filters.productDetailId.trim()) {
      await loadAuditList();
      return;
    }

    const productDetailId = Number(filters.productDetailId);

    setLoading(true);
    setError('');
    setNotice('');

    try {
      if (!Number.isInteger(productDetailId) || productDetailId <= 0) {
        throw new Error('请输入有效的商品详情 ID。');
      }

      const record = await api.audit.getByProduct(productDetailId).catch(() => null);
      if (!record) {
        setNotice(`商品详情 ${productDetailId} 暂无审核记录。`);
        setRecords([]);
        setSelectedKey('');
        return;
      }

      setRecords([record]);
      setSelectedKey(getRecordKey(record));
      setDraftComment(record.auditComment ?? '');
      setNotice(`已加载商品详情 ${productDetailId} 的审核记录。`);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : '查询审核记录失败。');
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmitAudit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const productDetailId = Number(submitForm.productDetailId);
    const riskLevel = Number(submitForm.riskLevel) as RiskLevelCode;

    if (!Number.isInteger(productDetailId) || productDetailId <= 0) {
      setError('提交审核前请输入有效的商品详情 ID。');
      return;
    }
    if (!submitForm.auditor.trim()) {
      setError('提交审核前请输入审核人。');
      return;
    }
    if (!submitForm.auditComment.trim()) {
      setError('提交审核前请输入审核意见。');
      return;
    }

    const payload: AuditSubmitRequest = {
      productDetailId,
      auditStatus: 0,
      auditComment: submitForm.auditComment.trim(),
      auditor: submitForm.auditor.trim(),
      riskLevel,
      submitter: submitForm.submitter.trim() || undefined,
    };

    setSubmitting(true);
    setError('');
    setNotice('');

    try {
      const id = await api.audit.submit(payload);

      const record = await api.audit.getByProduct(productDetailId).catch(() => null);
      if (record) {
        setRecords((currentRecords) => mergeAuditRecord(currentRecords, record));
        setSelectedKey(getRecordKey(record));
        setDraftComment(record.auditComment ?? '');
      }

      setNotice(`审核提交成功，记录 ID：${id ?? record?.id ?? '待后端返回'}。已通过真实查询接口刷新记录。`);
      setFilters((currentFilters) => ({ ...currentFilters, productDetailId: String(productDetailId) }));
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : '提交审核失败。');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleAuditAction(action: 'approve' | 'reject' | 'return' | 'withdraw' | 'reaudit') {
    if (!selectedRecord?.id) {
      setError('请先选择审核记录。');
      return;
    }

    const comment = draftComment.trim();
    if ((action === 'reject' || action === 'return') && !comment) {
      setError('驳回或退回修改前请填写审核意见。');
      return;
    }

    setActing(action);
    setError('');
    setNotice('');

    try {
      if (action === 'approve') {
        await api.audit.approve(selectedRecord.id, { comment, auditComment: comment });
        setNotice('审核已通过。');
      } else if (action === 'reject') {
        await api.audit.reject(selectedRecord.id, { comment, auditComment: comment });
        setNotice('审核已驳回。');
      } else if (action === 'return') {
        await api.audit.returnForRevision(selectedRecord.id, { comment, auditComment: comment });
        setNotice('已退回修改。');
      } else if (action === 'withdraw') {
        await api.audit.withdraw(selectedRecord.id);
        setNotice('已撤回待审记录。');
      } else {
        await api.audit.reaudit(selectedRecord.id);
        setNotice('已重新送审。');
      }

      await loadAuditList();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : '审核操作失败。');
    } finally {
      setActing('');
    }
  }

  function selectRecord(record: AuditRecord) {
    setSelectedKey(getRecordKey(record));
    setDraftComment(record.auditComment ?? '');
  }

  function resetFilters() {
    setFilters(initialFilters);
    void loadAuditList(initialFilters);
  }

  return (
    <main className="audit-page">
      <style>{pageStyles}</style>

      <header className="audit-header">
        <div>
          <p className="audit-eyebrow">审核中心</p>
          <h1>审核任务处理</h1>
          <p>审核列表、通过、驳回和退回修改均通过 services/api 调用后端接口。</p>
        </div>
        <div className="audit-header-status">
          <span>后端接口：部分可用</span>
          <span>{aiPendingMessage}</span>
        </div>
      </header>

      <section className="audit-panel audit-filter-panel" aria-labelledby="audit-filter-title">
        <div className="audit-panel-title-row">
          <div>
            <h2 id="audit-filter-title">筛选区</h2>
            <p>{listReadyMessage}</p>
          </div>
          <button className="audit-secondary-button" type="button" onClick={resetFilters}>
            重置
          </button>
        </div>

        <form className="audit-filter-grid" onSubmit={handleQueryByProductDetailId}>
          <label>
            商品详情 ID
            <input
              inputMode="numeric"
              placeholder="输入 ID 后查询真实记录"
              value={filters.productDetailId}
              onChange={(event) => setFilters((current) => ({ ...current, productDetailId: event.target.value }))}
            />
          </label>
          <label>
            审核状态
            <select
              value={filters.status}
              onChange={(event) => setFilters((current) => ({ ...current, status: event.target.value }))}
            >
              <option value="ALL">全部状态</option>
              <option value="0">待审核</option>
              <option value="1">审核中</option>
              <option value="2">通过</option>
              <option value="3">驳回</option>
              <option value="4">需修改</option>
            </select>
          </label>
          <label>
            风险等级
            <select
              value={filters.riskLevel}
              onChange={(event) => setFilters((current) => ({ ...current, riskLevel: event.target.value }))}
            >
              <option value="ALL">全部风险</option>
              <option value="1">低风险</option>
              <option value="2">中风险</option>
              <option value="3">高风险</option>
              <option value="4">极高风险</option>
            </select>
          </label>
          <label>
            审核人
            <input
              placeholder="本地筛选已加载记录"
              value={filters.auditor}
              onChange={(event) => setFilters((current) => ({ ...current, auditor: event.target.value }))}
            />
          </label>
          <div className="audit-filter-actions">
            <button className="audit-primary-button" type="submit" disabled={loading}>
              {loading ? '查询中...' : filters.productDetailId.trim() ? '查询单条记录' : '查询任务列表'}
            </button>
            <button className="audit-secondary-button" type="button" disabled={loading} onClick={() => void loadAuditList()}>
              刷新列表
            </button>
          </div>
        </form>
      </section>

      {(notice || error) && (
        <section className={`audit-message ${error ? 'audit-message-error' : 'audit-message-info'}`} role="status">
          {error || notice}
        </section>
      )}

      <section className="audit-grid">
        <div className="audit-main-column">
          <section className="audit-panel" aria-labelledby="audit-submit-title">
            <div className="audit-panel-title-row">
              <div>
                <h2 id="audit-submit-title">提交审核</h2>
                <p>通过 services/api 提交审核，提交后状态进入待审核。</p>
              </div>
            </div>

            <form className="audit-submit-grid" onSubmit={handleSubmitAudit}>
              <label>
                商品详情 ID
                <input
                  inputMode="numeric"
                  value={submitForm.productDetailId}
                  onChange={(event) =>
                    setSubmitForm((current) => ({ ...current, productDetailId: event.target.value }))
                  }
                  placeholder="例如 1001"
                />
              </label>
              <label>
                审核人
                <input
                  value={submitForm.auditor}
                  onChange={(event) => setSubmitForm((current) => ({ ...current, auditor: event.target.value }))}
                  placeholder="输入审核人"
                />
              </label>
              <label>
                提交人
                <input
                  value={submitForm.submitter}
                  onChange={(event) => setSubmitForm((current) => ({ ...current, submitter: event.target.value }))}
                  placeholder="可选"
                />
              </label>
              <label>
                风险等级
                <select
                  value={submitForm.riskLevel}
                  onChange={(event) => setSubmitForm((current) => ({ ...current, riskLevel: event.target.value }))}
                >
                  <option value="1">低风险</option>
                  <option value="2">中风险</option>
                  <option value="3">高风险</option>
                  <option value="4">极高风险</option>
                </select>
              </label>
              <label className="audit-submit-comment">
                初始审核意见
                <textarea
                  value={submitForm.auditComment}
                  onChange={(event) => setSubmitForm((current) => ({ ...current, auditComment: event.target.value }))}
                  placeholder="请输入提交审核时的说明或初步意见"
                />
              </label>
              <div className="audit-submit-actions">
                <button className="audit-primary-button" type="submit" disabled={submitting}>
                  {submitting ? '提交中...' : '提交审核'}
                </button>
              </div>
            </form>
          </section>

          <section className="audit-panel" aria-labelledby="audit-task-list-title">
            <div className="audit-panel-title-row">
              <div>
                <h2 id="audit-task-list-title">审核任务列表</h2>
                <p>
                  已加载 {filteredRecords.length} 条记录。总计 {pagination.total} 条，移动端自动切换为卡片视图。
                </p>
              </div>
              <button className="audit-secondary-button" type="button" disabled={loading} onClick={() => void loadAuditList()}>
                {loading ? '刷新中...' : '刷新列表'}
              </button>
            </div>

            {filteredRecords.length === 0 ? (
              <div className="audit-empty-state">
                <strong>暂无审核任务</strong>
                <span>当前筛选条件下没有后端返回的审核记录。</span>
                <small>可调整筛选条件或提交一条审核任务。</small>
              </div>
            ) : (
              <>
                <div className="audit-table-wrap">
                  <table className="audit-task-table">
                    <thead>
                      <tr>
                        <th>记录 ID</th>
                        <th>详情 ID</th>
                        <th>状态</th>
                        <th>风险</th>
                        <th>提交/审核</th>
                        <th>时间</th>
                        <th>操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredRecords.map((record) => (
                        <tr
                          key={getRecordKey(record)}
                          className={getRecordKey(record) === getRecordKey(selectedRecord ?? {}) ? 'is-selected' : ''}
                        >
                          <td>{record.id ?? '-'}</td>
                          <td>{record.productDetailId ?? '-'}</td>
                          <td>
                            <StatusBadge status={record.auditStatus} />
                          </td>
                          <td>
                            <RiskBadge riskLevel={record.riskLevel} />
                          </td>
                          <td>
                            <div className="audit-person-cell">
                              <span>{record.submitter || '提交人未填'}</span>
                              <small>{record.auditor || '审核人未填'}</small>
                            </div>
                          </td>
                          <td>
                            <div className="audit-person-cell">
                              <span>{formatDateTime(record.submitTime || record.createTime)}</span>
                              <small>{formatDateTime(record.auditTime)}</small>
                            </div>
                          </td>
                          <td>
                            <button className="audit-link-button" type="button" onClick={() => selectRecord(record)}>
                              查看详情
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <div className="audit-card-list">
                  {filteredRecords.map((record) => (
                    <article
                      className={`audit-task-card ${
                        getRecordKey(record) === getRecordKey(selectedRecord ?? {}) ? 'is-selected' : ''
                      }`}
                      key={getRecordKey(record)}
                    >
                      <div className="audit-task-card-header">
                        <strong>记录 #{record.id ?? '-'}</strong>
                        <StatusBadge status={record.auditStatus} />
                      </div>
                      <dl>
                        <DetailField label="商品详情 ID" value={record.productDetailId} />
                        <DetailField label="风险等级" value={<RiskBadge riskLevel={record.riskLevel} />} />
                        <DetailField label="提交人" value={record.submitter} />
                        <DetailField label="审核人" value={record.auditor} />
                        <DetailField label="提交时间" value={formatDateTime(record.submitTime || record.createTime)} />
                      </dl>
                      <button className="audit-secondary-button" type="button" onClick={() => selectRecord(record)}>
                        查看详情
                      </button>
                    </article>
                  ))}
                </div>
              </>
            )}
          </section>
        </div>

        <aside className="audit-side-column" aria-labelledby="audit-detail-title">
          <section className="audit-panel audit-detail-panel">
            <div className="audit-panel-title-row">
              <div>
                <h2 id="audit-detail-title">审核详情</h2>
                <p>通过、驳回和退回修改均调用 services/api；不会生成本地假状态。</p>
              </div>
            </div>

            {selectedRecord ? (
              <>
                <div className="audit-detail-heading">
                  <div>
                    <span>记录 #{selectedRecord.id ?? '-'}</span>
                    <strong>商品详情 {selectedRecord.productDetailId ?? '-'}</strong>
                  </div>
                  <StatusBadge status={selectedRecord.auditStatus} />
                </div>

                <dl className="audit-detail-list">
                  <DetailField label="风险等级" value={<RiskBadge riskLevel={selectedRecord.riskLevel} />} />
                  <DetailField label="审核类型" value={selectedRecord.auditType} />
                  <DetailField label="提交人" value={selectedRecord.submitter} />
                  <DetailField label="审核人" value={selectedRecord.auditor} />
                  <DetailField label="提交时间" value={formatDateTime(selectedRecord.submitTime || selectedRecord.createTime)} />
                  <DetailField label="审核时间" value={formatDateTime(selectedRecord.auditTime)} />
                </dl>

                <section className="audit-comment-box" aria-labelledby="audit-comment-title">
                  <div className="audit-section-title-row">
                    <h3 id="audit-comment-title">审核意见区域</h3>
                    <span>真实审核操作</span>
                  </div>
                  <textarea
                    value={draftComment}
                    onChange={(event) => setDraftComment(event.target.value)}
                    placeholder="填写审核意见后可通过、驳回或退回修改。"
                  />
                  <div className="audit-action-row">
                    <button
                      className="audit-primary-button"
                      type="button"
                      disabled={Boolean(acting) || !canApprove}
                      onClick={() => void handleAuditAction('approve')}
                    >
                      {acting === 'approve' ? '处理中...' : '通过'}
                    </button>
                    <button
                      className="audit-danger-button"
                      type="button"
                      disabled={Boolean(acting) || !canReject}
                      onClick={() => void handleAuditAction('reject')}
                    >
                      {acting === 'reject' ? '处理中...' : '驳回'}
                    </button>
                    <button
                      className="audit-warning-button"
                      type="button"
                      disabled={Boolean(acting) || !canReturn}
                      onClick={() => void handleAuditAction('return')}
                    >
                      {acting === 'return' ? '处理中...' : '退回修改'}
                    </button>
                    <button
                      className="audit-secondary-button"
                      type="button"
                      disabled={Boolean(acting) || !canWithdraw}
                      onClick={() => void handleAuditAction('withdraw')}
                    >
                      {acting === 'withdraw' ? '处理中...' : '撤回'}
                    </button>
                    <button
                      className="audit-secondary-button"
                      type="button"
                      disabled={Boolean(acting) || !canReaudit}
                      onClick={() => void handleAuditAction('reaudit')}
                    >
                      {acting === 'reaudit' ? '处理中...' : '重新审核'}
                    </button>
                  </div>
                  <small>待审核记录可通过、驳回、退回或撤回；已处理记录可重新审核。操作成功后会刷新审核列表，最终状态以后端返回为准。</small>
                </section>

                <section className="audit-subsection" aria-labelledby="audit-risk-items-title">
                  <h3 id="audit-risk-items-title">风险项</h3>
                  {selectedRiskItems.length > 0 ? (
                    <ul>
                      {selectedRiskItems.map((item) => (
                        <li key={item}>{item}</li>
                      ))}
                    </ul>
                  ) : (
                    <p>暂无风险项数据。</p>
                  )}
                </section>

                <section className="audit-subsection" aria-labelledby="audit-suggestion-title">
                  <h3 id="audit-suggestion-title">修改建议</h3>
                  {selectedSuggestions.length > 0 ? (
                    <ul>
                      {selectedSuggestions.map((item) => (
                        <li key={item}>{item}</li>
                      ))}
                    </ul>
                  ) : (
                    <p>暂无修改建议。</p>
                  )}
                </section>
              </>
            ) : (
              <div className="audit-empty-state audit-detail-empty">
                <strong>未选择审核任务</strong>
                <span>查询或提交审核后，可在这里查看详情。</span>
              </div>
            )}
          </section>

          <section className="audit-panel audit-ai-panel" aria-labelledby="audit-ai-title">
            <div className="audit-section-title-row">
              <h2 id="audit-ai-title">AI 辅助审核</h2>
              <span>{aiPendingMessage}</span>
            </div>
            <p>{aiPendingMessage}</p>
            <div className="audit-ai-actions">
              <button className="audit-ai-button" type="button" disabled title={aiPendingMessage}>
                AI 风险复核
              </button>
              <button className="audit-ai-button" type="button" disabled title={aiPendingMessage}>
                AI 意见生成
              </button>
            </div>
          </section>
        </aside>
      </section>
    </main>
  );
}

const pageStyles = `
.audit-page {
  min-height: 100%;
  padding: 28px;
  color: #172033;
  background:
    radial-gradient(circle at top left, rgba(24, 144, 255, 0.08), transparent 32rem),
    linear-gradient(180deg, #f7f9fc 0%, #eef2f7 100%);
}

.audit-header,
.audit-panel {
  border: 1px solid rgba(129, 144, 168, 0.24);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 16px 48px rgba(42, 58, 86, 0.08);
  backdrop-filter: blur(14px);
}

.audit-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
  padding: 24px;
}

.audit-header h1,
.audit-panel h2,
.audit-subsection h3,
.audit-comment-box h3 {
  margin: 0;
  color: #111827;
  letter-spacing: 0;
}

.audit-header h1 {
  margin-top: 4px;
  font-size: 28px;
  line-height: 1.2;
}

.audit-header p,
.audit-panel-title-row p,
.audit-ai-panel p {
  margin: 8px 0 0;
  color: #5f6b7a;
  line-height: 1.6;
}

.audit-eyebrow {
  margin: 0;
  color: #2f6fed;
  font-size: 13px;
  font-weight: 700;
}

.audit-header-status {
  display: grid;
  gap: 8px;
  min-width: 210px;
}

.audit-header-status span,
.audit-section-title-row span {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  border: 1px solid #d1d5db;
  border-radius: 999px;
  padding: 6px 10px;
  color: #4b5563;
  background: #f9fafb;
  font-size: 12px;
  font-weight: 700;
}

.audit-panel {
  padding: 20px;
}

.audit-filter-panel {
  margin-bottom: 16px;
}

.audit-panel-title-row,
.audit-section-title-row,
.audit-detail-heading,
.audit-task-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.audit-panel h2 {
  font-size: 18px;
}

.audit-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 420px);
  gap: 16px;
  align-items: start;
}

.audit-main-column,
.audit-side-column {
  display: grid;
  gap: 16px;
}

.audit-filter-grid,
.audit-submit-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.audit-submit-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.audit-filter-grid label,
.audit-submit-grid label {
  display: grid;
  gap: 7px;
  color: #374151;
  font-size: 13px;
  font-weight: 700;
}

.audit-filter-grid input,
.audit-filter-grid select,
.audit-submit-grid input,
.audit-submit-grid select,
.audit-submit-grid textarea,
.audit-comment-box textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #d9e0ea;
  border-radius: 8px;
  padding: 10px 11px;
  color: #111827;
  background: #ffffff;
  font: inherit;
  outline: none;
}

.audit-filter-grid input:focus,
.audit-filter-grid select:focus,
.audit-submit-grid input:focus,
.audit-submit-grid select:focus,
.audit-submit-grid textarea:focus,
.audit-comment-box textarea:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.14);
}

.audit-submit-comment {
  grid-column: 1 / -1;
}

.audit-submit-grid textarea,
.audit-comment-box textarea {
  min-height: 96px;
  resize: vertical;
}

.audit-filter-actions,
.audit-submit-actions {
  display: flex;
  align-items: end;
  gap: 10px;
}

.audit-submit-actions {
  grid-column: 1 / -1;
  justify-content: flex-end;
}

.audit-primary-button,
.audit-secondary-button,
.audit-disabled-button,
.audit-danger-button,
.audit-warning-button,
.audit-link-button,
.audit-ai-button {
  min-height: 38px;
  border-radius: 8px;
  padding: 0 14px;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.audit-primary-button {
  border: 1px solid #1677d2;
  color: #ffffff;
  background: #1890ff;
}

.audit-primary-button:disabled {
  border-color: #b8c2cf;
  background: #b8c2cf;
  cursor: not-allowed;
}

.audit-secondary-button {
  border: 1px solid #b8cce5;
  color: #1769aa;
  background: #ffffff;
}

.audit-danger-button {
  border: 1px solid #d92d20;
  color: #ffffff;
  background: #f5222d;
}

.audit-warning-button {
  border: 1px solid #d48806;
  color: #5f3b00;
  background: #fff7e6;
}

.audit-danger-button:disabled,
.audit-warning-button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.audit-disabled-button {
  border: 1px solid #d1d5db;
  color: #8c8c8c;
  background: #f3f4f6;
  cursor: not-allowed;
}

.audit-link-button {
  min-height: auto;
  border: 0;
  padding: 0;
  color: #1769aa;
  background: transparent;
}

.audit-message {
  margin-bottom: 16px;
  border-radius: 8px;
  padding: 12px 14px;
  font-weight: 700;
}

.audit-message-info {
  border: 1px solid #b8d6ff;
  color: #164f8f;
  background: #eef6ff;
}

.audit-message-error {
  border: 1px solid #ffc9c9;
  color: #a8071a;
  background: #fff1f0;
}

.audit-table-wrap {
  overflow-x: auto;
  margin-top: 16px;
}

.audit-task-table {
  width: 100%;
  min-width: 760px;
  border-collapse: collapse;
}

.audit-task-table th {
  border-bottom: 1px solid #e5e7eb;
  padding: 12px 10px;
  color: #667085;
  background: #f8fafc;
  font-size: 12px;
  text-align: left;
}

.audit-task-table td {
  border-bottom: 1px solid #edf1f5;
  padding: 14px 10px;
  vertical-align: middle;
}

.audit-task-table tr.is-selected td {
  background: #f0f7ff;
}

.audit-person-cell {
  display: grid;
  gap: 4px;
}

.audit-person-cell small,
.audit-comment-box small,
.audit-empty-state small {
  color: #778193;
}

.audit-tag {
  display: inline-flex;
  align-items: center;
  border: 1px solid transparent;
  border-radius: 999px;
  min-height: 24px;
  padding: 0 9px;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

.audit-status-pending {
  border-color: #d1d5db;
  color: #4b5563;
  background: #f3f4f6;
}

.audit-status-reviewing {
  border-color: #9dccff;
  color: #0958a5;
  background: #eaf4ff;
}

.audit-status-approved,
.audit-risk-low {
  border-color: #b7eb8f;
  color: #237804;
  background: #f6ffed;
}

.audit-status-rejected,
.audit-risk-high {
  border-color: #ffa39e;
  color: #a8071a;
  background: #fff1f0;
}

.audit-status-returned,
.audit-risk-medium {
  border-color: #ffd591;
  color: #ad6800;
  background: #fff7e6;
}

.audit-risk-critical {
  border-color: #d3adf7;
  color: #531dab;
  background: #f9f0ff;
}

.audit-risk-empty {
  border-color: #d1d5db;
  color: #667085;
  background: #f8fafc;
}

.audit-card-list {
  display: none;
  margin-top: 16px;
}

.audit-task-card {
  border: 1px solid #e1e7ef;
  border-radius: 8px;
  padding: 14px;
  background: #ffffff;
}

.audit-task-card.is-selected {
  border-color: #8dc5ff;
  background: #f0f7ff;
}

.audit-task-card dl,
.audit-detail-list {
  display: grid;
  gap: 10px;
  margin: 14px 0;
}

.audit-detail-field {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
}

.audit-detail-field dt {
  color: #667085;
  font-size: 12px;
  font-weight: 800;
}

.audit-detail-field dd {
  margin: 0;
  min-width: 0;
  color: #1f2937;
  overflow-wrap: anywhere;
}

.audit-detail-panel {
  position: sticky;
  top: 18px;
}

.audit-detail-heading {
  margin-top: 16px;
  border: 1px solid #e1e7ef;
  border-radius: 8px;
  padding: 14px;
  background: #f8fafc;
}

.audit-detail-heading div {
  display: grid;
  gap: 4px;
}

.audit-detail-heading span {
  color: #667085;
  font-size: 12px;
  font-weight: 700;
}

.audit-detail-heading strong {
  font-size: 18px;
}

.audit-comment-box,
.audit-subsection {
  margin-top: 18px;
  border-top: 1px solid #edf1f5;
  padding-top: 16px;
}

.audit-comment-box textarea {
  margin-top: 12px;
}

.audit-action-row,
.audit-ai-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.audit-subsection ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #374151;
}

.audit-subsection li + li {
  margin-top: 8px;
}

.audit-subsection p {
  margin: 10px 0 0;
  color: #778193;
}

.audit-empty-state {
  display: grid;
  place-items: center;
  gap: 8px;
  min-height: 180px;
  border: 1px dashed #c9d3df;
  border-radius: 8px;
  margin-top: 16px;
  padding: 24px;
  color: #667085;
  text-align: center;
  background: #fbfcfe;
}

.audit-empty-state strong {
  color: #374151;
}

.audit-detail-empty {
  min-height: 240px;
}

.audit-ai-panel {
  border-style: dashed;
}

.audit-ai-panel p {
  color: #4b5563;
  font-weight: 700;
}

.audit-ai-button {
  border: 1px dashed #aeb8c5;
  color: #8c8c8c;
  background: #f8fafc;
  cursor: not-allowed;
}

@media (max-width: 1180px) {
  .audit-grid {
    grid-template-columns: 1fr;
  }

  .audit-detail-panel {
    position: static;
  }
}

@media (max-width: 767px) {
  .audit-page {
    padding: 16px;
  }

  .audit-header,
  .audit-panel-title-row,
  .audit-section-title-row {
    display: grid;
  }

  .audit-header-status {
    min-width: 0;
  }

  .audit-filter-grid,
  .audit-submit-grid {
    grid-template-columns: 1fr;
  }

  .audit-filter-actions,
  .audit-submit-actions {
    align-items: stretch;
    justify-content: stretch;
  }

  .audit-filter-actions button,
  .audit-submit-actions button {
    width: 100%;
  }

  .audit-table-wrap {
    display: none;
  }

  .audit-card-list {
    display: grid;
    gap: 12px;
  }

  .audit-detail-field {
    grid-template-columns: 88px minmax(0, 1fr);
  }
}
`;
