import { CheckCircleOutlined, ExclamationCircleOutlined, LoadingOutlined, WarningOutlined } from "@ant-design/icons";
import { Button, Descriptions, Form, Input, Space, Spin, Tag, Typography, notification } from "antd";
import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import { EmptyState, GlassPanel, RiskTag, StatusTag } from "../../components/common";
import { api } from "../../services/api";
import type { AuditRecord, AuditSubmitRequest, DetailRiskResult, ProductDetail } from "../../services/types";

type RiskLevelKey = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL" | "EXTREME";

type ReviewState = {
  detail?: ProductDetail | null;
  audit?: AuditRecord | null;
  risk?: DetailRiskResult | null;
  loading: boolean;
  error?: string;
  submitBusy: boolean;
  riskBusy: boolean;
  riskMessage?: string;
};

type ReviewFormValues = Pick<AuditSubmitRequest, "auditComment" | "auditor" | "submitter">;

const riskPalette: Record<RiskLevelKey, { color: string; label: string; description: string }> = {
  LOW: { color: "#52C41A", label: "LOW", description: "低风险，可继续流转" },
  MEDIUM: { color: "#FAAD14", label: "MEDIUM", description: "中风险，建议人工确认" },
  HIGH: { color: "#F5222D", label: "HIGH", description: "高风险，必须修改后再提交" },
  CRITICAL: { color: "#722ED1", label: "CRITICAL", description: "极高风险，禁止直接发布" },
  EXTREME: { color: "#722ED1", label: "EXTREME", description: "极高风险，禁止直接发布" }
};

const aiPendingText = "待接入本地AI服务";
const riskEmptyText = "暂无风险结果";
const reviewSubmitNote =
  "提交审核可用，提交内容依赖人工整理的审核意见与当前详情数据；风险检测结果仅展示后端真实返回。";
const chainNote =
  "逻辑链路：详情基础信息 /api/v1/detail/{id} → 风险检测 /api/v1/detail/{id}/risk-check → 风险结果 /api/v1/detail/{id}/risk → 人工审核提交 /api/v1/audit/submit。";

function parseRiskLevel(input: ProductDetail["riskLevel"] | AuditRecord["riskLevel"] | DetailRiskResult["riskLevel"] | undefined): RiskLevelKey | undefined {
  const value = String(input ?? "").trim().toUpperCase();
  if (value === "LOW" || value === "MEDIUM" || value === "HIGH" || value === "CRITICAL" || value === "EXTREME") {
    return value;
  }
  if (value === "1") return "LOW";
  if (value === "2") return "MEDIUM";
  if (value === "3") return "HIGH";
  if (value === "4") return "CRITICAL";
  return undefined;
}

function riskLevelToCode(level: RiskLevelKey | undefined): number | undefined {
  if (!level) return undefined;
  if (level === "LOW") return 1;
  if (level === "MEDIUM") return 2;
  if (level === "HIGH") return 3;
  return 4;
}

function formatHasRisk(value: DetailRiskResult["hasRisk"] | undefined) {
  if (value === true) return "有风险";
  if (value === false) return "无风险";
  return "未返回";
}

function hasRiskTagColor(value: DetailRiskResult["hasRisk"] | undefined) {
  if (value === true) return "error";
  if (value === false) return "success";
  return "default";
}

function hasRiskItemLabel(value: DetailRiskResult["hasRisk"] | undefined) {
  if (value === true) return "风险";
  if (value === false) return "提示";
  return "未标记";
}

function normalizeStatus(status: ProductDetail["auditStatus"] | AuditRecord["auditStatus"] | DetailRiskResult["auditStatus"] | undefined) {
  const value = String(status ?? "").trim().toUpperCase();
  if (value === "0" || value === "PENDING") return "PENDING";
  if (value === "1" || value === "REVIEWING" || value === "PROCESSING") return "REVIEWING";
  if (value === "2" || value === "APPROVED" || value === "PASS") return "APPROVED";
  if (value === "3" || value === "REJECTED" || value === "REJECT") return "REJECTED";
  if (value === "4" || value === "RETURNED" || value === "NEED_MODIFICATION") return "RETURNED";
  return "DISABLED";
}

function formatList(input: unknown): string[] {
  if (!input) return [];
  if (Array.isArray(input)) {
    return input.map((item) => String(item).trim()).filter(Boolean);
  }

  const text = String(input).trim();
  return text ? [text] : [];
}

function formatIssueDetails(details?: DetailRiskResult["issueDetails"]) {
  if (!details || Object.keys(details).length === 0) {
    return [];
  }

  return Object.entries(details).map(([category, values]) => ({
    category,
    values: formatList(values)
  }));
}

function formatDateTime(value?: string) {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString("zh-CN", { hour12: false });
}

export default function DetailReviewPage() {
  const { id } = useParams();
  const detailId = id?.trim() || "";
  const [state, setState] = useState<ReviewState>({
    detail: null,
    audit: null,
    risk: null,
    loading: Boolean(detailId),
    submitBusy: false,
    riskBusy: false
  });
  const [form] = Form.useForm<ReviewFormValues>();
  const [apiMessage, setApiMessage] = useState<string>("");

  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      if (!detailId) {
        setState({
          detail: null,
          audit: null,
          risk: null,
          loading: false,
          error: "缺少审查对象 ID。",
          submitBusy: false,
          riskBusy: false
        });
        return;
      }

      setState((current) => ({ ...current, loading: true, error: undefined }));
      setApiMessage("");

      try {
        const [detail, audit, riskOutcome] = await Promise.all([
          api.detail.get(detailId),
          api.audit.getByProduct(detailId).catch(() => null),
          api.detail
            .getRisk(detailId)
            .then((risk) => ({ risk, error: undefined as string | undefined }))
            .catch((error) => ({
              risk: null,
              error: error instanceof Error ? error.message : "风险结果读取失败。"
            }))
        ]);

        if (!cancelled) {
          setState({
            detail,
            audit,
            risk: riskOutcome.risk,
            loading: false,
            submitBusy: false,
            riskBusy: false,
            riskMessage: riskOutcome.error
          });
          form.setFieldsValue({
            auditComment: audit?.auditComment ?? "",
            auditor: audit?.auditor ?? "",
            submitter: audit?.submitter ?? ""
          });
        }
      } catch (error) {
        if (!cancelled) {
          setState({
            detail: null,
            audit: null,
            risk: null,
            loading: false,
            error: error instanceof Error ? error.message : "详情读取失败。",
            submitBusy: false,
            riskBusy: false
          });
        }
      }
    }

    void loadData();
    return () => {
      cancelled = true;
    };
  }, [detailId, form]);

  const riskResultLevel = parseRiskLevel(state.risk?.riskLevel);
  const fallbackRiskLevel = parseRiskLevel(state.detail?.riskLevel ?? state.audit?.riskLevel);
  const riskLevel = riskResultLevel ?? fallbackRiskLevel;
  const statusKey = normalizeStatus(state.risk?.auditStatus ?? state.detail?.auditStatus ?? state.audit?.auditStatus);
  const primaryRisk = riskResultLevel ? riskPalette[riskResultLevel] : undefined;
  const issueList = formatList(state.risk?.issues);
  const suggestionList = formatList(state.risk?.suggestions);
  const issueDetails = formatIssueDetails(state.risk?.issueDetails);
  const riskResultMessage = state.risk
    ? `风险结果已更新：${formatHasRisk(state.risk.hasRisk)}。`
    : riskEmptyText;

  async function handleSubmitAudit() {
    if (!detailId) {
      notification.error({ message: "缺少审查对象 ID" });
      return;
    }

    try {
      const values = await form.validateFields();
      setState((current) => ({ ...current, submitBusy: true }));
      setApiMessage("");

      const riskCode = riskLevelToCode(riskResultLevel);
      const payload: AuditSubmitRequest = {
        productDetailId: Number(detailId),
        auditStatus: 0,
        auditComment: values.auditComment,
        auditor: values.auditor,
        submitter: values.submitter
      };

      if (riskCode !== undefined) {
        payload.riskLevel = riskCode;
      }

      const submitted = await api.audit.submit(payload);

      notification.success({ message: "已提交人工审核" });
      setApiMessage(`人工审核已提交，记录 ID：${submitted ?? "待返回"}。风险等级仅在后端已返回风险结果时随审核提交。`);
    } catch (error) {
      notification.error({
        message: error instanceof Error ? error.message : "提交审核失败"
      });
    } finally {
      setState((current) => ({ ...current, submitBusy: false }));
    }
  }

  async function handleRiskCheck() {
    if (!detailId) {
      notification.error({ message: "缺少审查对象 ID" });
      return;
    }

    setState((current) => ({ ...current, riskBusy: true, riskMessage: undefined }));

    try {
      await api.detail.riskCheck(detailId);
      const riskResult = await api.detail.getRisk(detailId);
      notification.success({ message: "风险检测完成" });
      setState((current) => ({
        ...current,
        risk: riskResult,
        riskBusy: false,
        riskMessage: riskResult ? "风险检测完成，已刷新真实风险结果。" : "风险检测完成，但未返回可展示结果。"
      }));
    } catch (error) {
      const message = error instanceof Error ? error.message : "风险检测失败。";
      notification.error({ message: "风险检测失败", description: message });
      setState((current) => ({ ...current, riskBusy: false, riskMessage: message }));
    }
  }

  async function handleRiskResultRefresh() {
    if (!detailId) {
      notification.error({ message: "缺少审查对象 ID" });
      return;
    }

    setState((current) => ({ ...current, riskBusy: true, riskMessage: undefined }));

    try {
      const riskResult = await api.detail.getRisk(detailId);
      notification.success({ message: "风险结果已刷新" });
      setState((current) => ({
        ...current,
        risk: riskResult,
        riskBusy: false,
        riskMessage: riskResult ? "已刷新真实风险结果。" : "暂无可展示的风险结果。"
      }));
    } catch (error) {
      const message = error instanceof Error ? error.message : "风险结果读取失败。";
      notification.error({ message: "风险结果读取失败", description: message });
      setState((current) => ({ ...current, riskBusy: false, riskMessage: message }));
    }
  }

  const titleText = state.detail?.title?.trim() || `审查对象 ${detailId || "-"}`;
  const subtitleText = state.detail?.subtitle?.trim() || "展示后端真实风险检测结果与人工审核路径。";

  return (
    <main className="page-stack">
      <style>{`
        .detail-review-shell {
          display: grid;
          gap: 16px;
        }

        .detail-review-top {
          display: flex;
          align-items: flex-start;
          justify-content: space-between;
          gap: 16px;
        }

        .detail-review-title {
          min-width: 0;
        }

        .detail-review-title h1 {
          margin: 0;
          color: var(--text-strong);
          font-size: 24px;
          font-weight: 700;
          line-height: 1.2;
        }

        .detail-review-title p {
          margin: 8px 0 0;
          color: var(--text-muted);
          line-height: 1.65;
        }

        .detail-review-grid {
          display: grid;
          gap: 16px;
          grid-template-columns: minmax(0, 1.15fr) minmax(360px, 0.85fr);
        }

        .detail-review-risk-header {
          display: grid;
          gap: 12px;
        }

        .detail-review-risk-overview {
          display: grid;
          gap: 12px;
          grid-template-columns: repeat(4, minmax(0, 1fr));
        }

        .detail-review-stat {
          padding: 14px;
          border-radius: 8px;
          border: 1px solid rgba(148, 163, 184, 0.22);
          background: rgba(255,255,255,0.42);
        }

        .detail-review-stat strong {
          display: block;
          color: var(--text-strong);
          font-size: 20px;
          margin-top: 6px;
        }

        .detail-review-stat span {
          color: var(--text-muted);
          font-size: 13px;
        }

        .detail-review-note {
          padding: 12px 14px;
          border-radius: 8px;
          border: 1px dashed rgba(140, 140, 140, 0.45);
          background: rgba(255,255,255,0.36);
          color: var(--text-default);
          line-height: 1.6;
        }

        .detail-review-disabled {
          display: grid;
          gap: 12px;
          padding: 14px;
          border-radius: 8px;
          border: 1px dashed #8c8c8c;
          background: rgba(255,255,255,0.48);
        }

        .detail-review-disabled h4 {
          margin: 0;
          color: var(--text-strong);
        }

        .detail-review-disabled p {
          margin: 0;
          color: var(--text-muted);
          line-height: 1.6;
        }

        .detail-review-chain {
          color: var(--text-muted);
          line-height: 1.7;
        }

        .detail-review-risk-card {
          display: grid;
          gap: 12px;
        }

        .detail-review-risk-item {
          display: grid;
          gap: 8px;
          padding: 14px;
          border-radius: 8px;
          border: 1px solid rgba(148, 163, 184, 0.22);
          background: rgba(255,255,255,0.45);
        }

        .detail-review-risk-item-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 12px;
        }

        .detail-review-risk-item h4 {
          margin: 0;
          color: var(--text-strong);
          font-size: 15px;
        }

        .detail-review-risk-item small {
          color: var(--text-muted);
        }

        .detail-review-risk-item p {
          margin: 0;
          color: var(--text-default);
          line-height: 1.6;
        }

        .detail-review-risk-item .suggestion {
          color: var(--text-muted);
        }

        .detail-review-risk-item .risk-chip {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          font-size: 12px;
          font-weight: 600;
        }

        .detail-review-form {
          display: grid;
          gap: 12px;
        }

        .detail-review-form .ant-form-item {
          margin-bottom: 0;
        }

        .detail-review-form .ant-input,
        .detail-review-form .ant-input-affix-wrapper {
          border-radius: 8px;
        }

        .detail-review-form-actions {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
          align-items: center;
          justify-content: space-between;
          padding-top: 2px;
        }

        .detail-review-footer-note {
          padding: 12px 14px;
          border-radius: 8px;
          border: 1px solid rgba(250, 173, 20, 0.28);
          background: rgba(250, 173, 20, 0.08);
          color: var(--text-default);
          line-height: 1.65;
        }

        .detail-review-status-stack {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
        }

        .detail-review-risk-badge {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          padding: 8px 12px;
          border-radius: 999px;
          border: 1px solid rgba(148, 163, 184, 0.24);
          background: rgba(255,255,255,0.52);
          color: var(--text-strong);
        }

        .detail-review-risk-dot {
          width: 10px;
          height: 10px;
          border-radius: 999px;
        }

        .detail-review-summary {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
        }

        @media (max-width: 960px) {
          .detail-review-grid,
          .detail-review-risk-overview {
            grid-template-columns: 1fr;
          }

          .detail-review-top {
            flex-direction: column;
          }

          .detail-review-form-actions {
            flex-direction: column;
            align-items: stretch;
          }
        }
      `}</style>

      <GlassPanel
        title="合规审查"
        subtitle="展示真实详情、后端风险结果、人工审核入口和审查链路。"
        extra={
          <Button type="link" disabled={!detailId}>
            <Link to={`/details/${detailId || "1"}`}>返回编辑</Link>
          </Button>
        }
      >
        <div className="detail-review-top">
          <div className="detail-review-title">
            <h1>{titleText}</h1>
            <p>{subtitleText}</p>
          </div>
          <div className="detail-review-status-stack">
            <StatusTag value={statusKey} />
            <RiskTag value={riskLevel ?? "UNKNOWN"} />
            <Button className="ai-pending-button" disabled>
              {aiPendingText}
            </Button>
          </div>
        </div>
      </GlassPanel>

      {state.loading ? (
        <GlassPanel title="加载审查对象" subtitle="正在读取详情基础信息与审核记录">
          <div style={{ display: "grid", placeItems: "center", minHeight: 180 }}>
            <Spin indicator={<LoadingOutlined style={{ fontSize: 28 }} spin />} />
          </div>
        </GlassPanel>
      ) : state.error ? (
        <GlassPanel title="读取失败" subtitle={`审查对象 ID：${detailId || "-"}`}>
          <EmptyState title="详情读取失败" description={state.error} />
        </GlassPanel>
      ) : (
        <div className="detail-review-shell">
          <div className="detail-review-grid">
            <div className="detail-review-risk-card">
              <GlassPanel title="风险总览" subtitle="读取并展示后端真实风险检测结果">
                <div className="detail-review-risk-header">
                  <div className="detail-review-risk-overview">
                    <div className="detail-review-stat">
                      <span>审查对象 ID</span>
                      <strong>{detailId || "-"}</strong>
                    </div>
                    <div className="detail-review-stat">
                      <span>风险等级</span>
                      <strong>{primaryRisk?.label ?? (state.risk ? "未返回" : "未检测")}</strong>
                    </div>
                    <div className="detail-review-stat">
                      <span>风险项数量</span>
                      <strong>{issueList.length + issueDetails.reduce((total, item) => total + item.values.length, 0)}</strong>
                    </div>
                    <div className="detail-review-stat">
                      <span>是否有风险</span>
                      <strong>{state.risk ? formatHasRisk(state.risk.hasRisk) : "无结果"}</strong>
                    </div>
                  </div>

                  <div className="detail-review-note">
                    <Space direction="vertical" size={4}>
                      <span>
                        <ExclamationCircleOutlined style={{ color: "#FAAD14" }} /> 风险检测结果
                      </span>
                      <span>{riskResultMessage}</span>
                      <span>接口失败或暂无结果时只显示错误/空态，不生成本地风险项。</span>
                    </Space>
                  </div>

                  <div className="detail-review-disabled">
                    <h4>风险检测入口</h4>
                    <p>点击后调用真实 riskCheck，再刷新 getRisk 结果；失败时展示错误信息。</p>
                    <Space wrap>
                      <Button
                        disabled={!detailId || state.riskBusy}
                        icon={<WarningOutlined />}
                        loading={state.riskBusy}
                        onClick={() => void handleRiskCheck()}
                      >
                        风险检测
                      </Button>
                      <Button
                        disabled={!detailId || state.riskBusy}
                        loading={state.riskBusy}
                        onClick={() => void handleRiskResultRefresh()}
                      >
                        刷新风险结果
                      </Button>
                      <Tag color={state.risk ? "success" : "default"}>{state.risk ? "已返回风险结果" : riskEmptyText}</Tag>
                    </Space>
                    {state.riskMessage && <p>{state.riskMessage}</p>}
                  </div>

                  <div className="detail-review-footer-note">
                    <strong>提交审核可用</strong>
                    <div style={{ marginTop: 6 }}>{reviewSubmitNote}</div>
                  </div>
                </div>
              </GlassPanel>

              <GlassPanel title="风险项列表" subtitle="仅展示后端 DetailRiskResultDTO 返回的 issues 与 issueDetails">
                {state.risk ? (
                  <Space direction="vertical" size={12} style={{ width: "100%" }}>
                    {issueList.length > 0 ? (
                      issueList.map((issue, index) => (
                        <div key={`${issue}-${index}`} className="detail-review-risk-item">
                          <div className="detail-review-risk-item-header">
                            <h4>风险项 {index + 1}</h4>
                            <Tag color={hasRiskTagColor(state.risk?.hasRisk)}>{hasRiskItemLabel(state.risk?.hasRisk)}</Tag>
                          </div>
                          <p>{issue}</p>
                        </div>
                      ))
                    ) : (
                      <EmptyState title="暂无 issues" description="后端风险结果未返回 issues 列表。" />
                    )}
                    {issueDetails.map((detail) => (
                      <div key={detail.category} className="detail-review-risk-item">
                        <div className="detail-review-risk-item-header">
                          <h4>{detail.category}</h4>
                          <Tag color="warning">issueDetails</Tag>
                        </div>
                        {detail.values.length > 0 ? (
                          <Space direction="vertical" size={6}>
                            {detail.values.map((value, index) => (
                              <p key={`${detail.category}-${index}`}>{value}</p>
                            ))}
                          </Space>
                        ) : (
                          <p>该分类暂无明细。</p>
                        )}
                      </div>
                    ))}
                  </Space>
                ) : (
                  <EmptyState
                    title={riskEmptyText}
                    description={state.riskMessage || "尚未读取到后端风险结果。可点击风险检测或刷新风险结果。"}
                  />
                )}
              </GlassPanel>

              <GlassPanel title="修改建议" subtitle="仅展示后端 DetailRiskResultDTO 返回的 suggestions">
                {state.risk ? (
                  suggestionList.length > 0 ? (
                    <Space direction="vertical" size={10} style={{ width: "100%" }}>
                      {suggestionList.map((suggestion, index) => (
                        <div key={`${suggestion}-${index}`} className="detail-review-risk-item">
                          <div className="detail-review-risk-item-header">
                            <h4>建议 {index + 1}</h4>
                            <Tag color="processing">suggestions</Tag>
                          </div>
                          <p className="suggestion">{suggestion}</p>
                        </div>
                      ))}
                    </Space>
                  ) : (
                    <EmptyState title="暂无 suggestions" description="后端风险结果未返回修改建议。" />
                  )
                ) : (
                  <EmptyState title={riskEmptyText} description="没有风险结果时不展示本地生成建议。" />
                )}
              </GlassPanel>
            </div>

            <div className="detail-review-risk-card">
              <GlassPanel
                title="人工审核说明"
                subtitle="当前通过真实风险接口展示检测结果，通过审核提交接口完成人工流转。"
              >
                <Descriptions column={1} size="small" bordered>
                  <Descriptions.Item label="当前状态">
                    <StatusTag value={statusKey} />
                  </Descriptions.Item>
                  <Descriptions.Item label="风险等级色">
                    <Space wrap>
                      {(["LOW", "MEDIUM", "HIGH", "CRITICAL"] as RiskLevelKey[]).map((key) => (
                        <span key={key} className="detail-review-risk-badge">
                          <span
                            className="detail-review-risk-dot"
                            style={{ background: riskPalette[key].color }}
                          />
                          {key} {riskPalette[key].description}
                        </span>
                      ))}
                    </Space>
                  </Descriptions.Item>
                  <Descriptions.Item label="状态色">
                    <Space wrap>
                      <Tag color="#8C8C8C">无结果</Tag>
                      <Tag color="#FAAD14">待审核</Tag>
                      <Tag color="#52C41A">已通过</Tag>
                      <Tag color="#F5222D">已驳回</Tag>
                    </Space>
                  </Descriptions.Item>
                  <Descriptions.Item label="逻辑链路">
                    <div className="detail-review-chain">{chainNote}</div>
                  </Descriptions.Item>
                </Descriptions>
              </GlassPanel>

              <GlassPanel title="提交审核" subtitle="可用，但依赖人工填写审核意见与审核人">
                <Form
                  form={form}
                  layout="vertical"
                  className="detail-review-form"
                  initialValues={{
                    auditComment: "",
                    auditor: "",
                    submitter: ""
                  }}
                >
                  <Form.Item
                    label="审核意见"
                    name="auditComment"
                    rules={[{ required: true, message: "请输入审核意见" }]}
                  >
                    <Input.TextArea rows={5} placeholder="填写人工审核意见" />
                  </Form.Item>
                  <Form.Item
                    label="审核人"
                    name="auditor"
                    rules={[{ required: true, message: "请输入审核人" }]}
                  >
                    <Input placeholder="审核人姓名" />
                  </Form.Item>
                  <Form.Item label="提交人" name="submitter">
                    <Input placeholder="可选，填写提交人" />
                  </Form.Item>
                  <div className="detail-review-form-actions">
                    <Typography.Text type="secondary">
                      提交后会调用 `/api/v1/audit/submit`，不会伪造风险检测结果。
                    </Typography.Text>
                    <Button
                      type="primary"
                      icon={<CheckCircleOutlined />}
                      loading={state.submitBusy}
                      onClick={() => void handleSubmitAudit()}
                    >
                      提交审核
                    </Button>
                  </div>
                </Form>
                {apiMessage && <div className="detail-review-footer-note" style={{ marginTop: 12 }}>{apiMessage}</div>}
              </GlassPanel>

              <GlassPanel title="风险结果详情" subtitle="DetailRiskResultDTO 原始字段预览">
                {state.risk ? (
                  <Descriptions column={1} size="small" bordered>
                    <Descriptions.Item label="riskLevel">
                      <RiskTag value={state.risk.riskLevel ?? "UNKNOWN"} />
                    </Descriptions.Item>
                    <Descriptions.Item label="hasRisk">
                      <Tag color={hasRiskTagColor(state.risk.hasRisk)}>
                        {formatHasRisk(state.risk.hasRisk)}
                      </Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="issues">
                      {issueList.length ? issueList.join("；") : "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="issueDetails">
                      {issueDetails.length ? (
                        <Space direction="vertical" size={4}>
                          {issueDetails.map((detail) => (
                            <span key={detail.category}>
                              {detail.category}: {detail.values.length ? detail.values.join("；") : "-"}
                            </span>
                          ))}
                        </Space>
                      ) : (
                        "-"
                      )}
                    </Descriptions.Item>
                    <Descriptions.Item label="suggestions">
                      {suggestionList.length ? suggestionList.join("；") : "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="content">
                      <Typography.Paragraph style={{ marginBottom: 0 }}>
                        {state.risk.content || "-"}
                      </Typography.Paragraph>
                    </Descriptions.Item>
                    <Descriptions.Item label="auditStatus">
                      <StatusTag value={normalizeStatus(state.risk.auditStatus)} />
                    </Descriptions.Item>
                    <Descriptions.Item label="auditComment">
                      {state.risk.auditComment || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="updateTime">
                      {formatDateTime(state.risk.updateTime)}
                    </Descriptions.Item>
                  </Descriptions>
                ) : (
                  <EmptyState
                    title={riskEmptyText}
                    description={
                      <Space direction="vertical" size={4}>
                        <span>{state.riskMessage || "尚未读取到后端风险结果。"}</span>
                        <span>{aiPendingText}</span>
                      </Space>
                    }
                  />
                )}
              </GlassPanel>

              <GlassPanel title="基础信息" subtitle="来自 GET /api/v1/detail/{id}">
                <Descriptions column={1} size="small" bordered>
                  <Descriptions.Item label="标题">{state.detail?.title || "-"}</Descriptions.Item>
                  <Descriptions.Item label="副标题">{state.detail?.subtitle || "-"}</Descriptions.Item>
                  <Descriptions.Item label="SKU">{state.detail?.sku || "-"}</Descriptions.Item>
                  <Descriptions.Item label="分类">{state.detail?.category || "-"}</Descriptions.Item>
                  <Descriptions.Item label="价格">{state.detail?.price ?? "-"}</Descriptions.Item>
                  <Descriptions.Item label="审核状态">
                    <StatusTag value={statusKey} />
                  </Descriptions.Item>
                  <Descriptions.Item label="风险等级">
                    <RiskTag value={riskLevel ?? "UNKNOWN"} />
                  </Descriptions.Item>
                </Descriptions>
              </GlassPanel>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
