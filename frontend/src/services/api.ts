import { getToken, clearAuth } from "./auth";
import type {
  ApiResult,
  ApplyGenerationResultsRequest,
  AssetOcrTask,
  AssetOcrTaskListQuery,
  AssetOcrTaskResultRequest,
  AssetOcrTaskStatusRequest,
  AuditActionRequest,
  AuditListQuery,
  AuditRecord,
  AuditSubmitRequest,
  DetailGenerateRequest,
  DetailRiskResult,
  GenerationResult,
  GenerationResultListQuery,
  GenerationResultSelectionRequest,
  DesignDraft,
  DetailComposition,
  DetailCompositionCreateRequest,
  DetailCompositionListQuery,
  DetailCompositionQualityCheck,
  DetailDeliveryManifest,
  DesignDraftListQuery,
  DesignDraftUpdateRequest,
  ExportCreateRequest,
  ExportListQuery,
  ExportRecord,
  ImageJob,
  ImageJobCreateRequest,
  ImageJobListQuery,
  ImageJobRetryRequest,
  PageQuery,
  PageResult,
  ResearchTask,
  ResearchTaskCharts,
  ResearchTaskListQuery,
  ResearchTaskResultRequest,
  ResearchTaskStatusRequest,
  ProductContentTask,
  ProductContentTaskApplyRequest,
  ProductContentTaskCreateRequest,
  ProductContentTaskListQuery,
  ProductDetail,
  ProductDetailListQuery,
  ProductDetailUpdateRequest,
  ProductMaterial,
  ProductMaterialListQuery,
  ProductMaterialUpdateRequest,
  ProductMaterialUploadRequest,
  CategoryVisualPolicy,
  CategoryVisualPolicyQuery,
  ToolAdapterInfo,
  ToolInvokeRequest,
  ToolInvokeResponse,
  ModelProfile,
  ModelProfileQuery,
  PromptWorkbenchExpandRequest,
  PromptWorkbenchGuidedRequest,
  PromptWorkbenchImageToPromptRequest,
  PromptWorkbenchRequest,
  PromptWorkbenchResult,
  SkcPolicy,
  SkcPolicyQuery,
  VisualPlan,
  VisualPlanBatchResults,
  VisualPlanBatchStatus,
  VisualPlanCreateRequest,
  VisualPlanQuery,
  PostProcessTask,
  PostProcessTaskListQuery,
  PostProcessTaskCreateRequest,
  CostConfig,
  CostStats,
  TaskCostRecord,
  EnvironmentDiagnostic,
  PublishCheckSummary,
  OperationAuditLog,
  TeamUser,
  TeamRole,
  PromptTemplate,
  PromptTemplateCreateRequest,
  PromptTemplateQuery
} from "./types";

const API_BASE = "/api/v1";

export class ApiRequestError extends Error {
  status?: number;
  path: string;

  constructor(message: string, path: string, status?: number) {
    super(message);
    this.name = "ApiRequestError";
    this.path = path;
    this.status = status;
  }
}

type RequestOptions = Omit<RequestInit, "body"> & {
  body?: BodyInit | object;
};

type RequestQueryValue = string | number | boolean | null | undefined;
type RequestQuery = Record<string, RequestQueryValue>;
export type FileDownload = {
  blob: Blob;
  fileName: string;
};

const buildBody = (body: RequestOptions["body"]) => {
  if (!body || body instanceof FormData || body instanceof Blob) {
    return body as BodyInit | undefined;
  }
  return JSON.stringify(body);
};

const normalizePageQuery = <T extends PageQuery>(query: T = {} as T) => {
  const { page, size, pageNum, pageSize, ...rest } = query;
  return {
    pageNum: pageNum ?? page,
    pageSize: pageSize ?? size,
    ...rest
  } as Omit<T, "page" | "size" | "pageNum" | "pageSize"> & {
    pageNum?: number;
    pageSize?: number;
  };
};

const buildPath = (path: string, query?: RequestQuery) => {
  const params = new URLSearchParams();
  Object.entries(query ?? {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      params.set(key, String(value));
    }
  });
  const queryString = params.toString();
  return queryString ? `${path}?${queryString}` : path;
};

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const isPageResult = <T>(value: unknown): value is PageResult<T> =>
  isRecord(value) &&
  typeof value.code === "number" &&
  typeof value.message === "string" &&
  Array.isArray(value.data) &&
  typeof value.pageNum === "number" &&
  typeof value.pageSize === "number" &&
  typeof value.total === "number" &&
  typeof value.pages === "number";

const request = async <T>(
  path: string,
  { headers, body, ...options }: RequestOptions = {}
): Promise<T> => {
  const isJsonBody = body && !(body instanceof FormData) && !(body instanceof Blob);
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      ...(isJsonBody ? { "Content-Type": "application/json" } : {}),
      ...headers
    },
    body: buildBody(body)
  });

  const payload = (await response.json().catch(() => null)) as ApiResult<T> | null;
  if (response.status === 401) {
    clearAuth();
    window.location.href = "/login";
    throw new Error("Session expired");
  }
  if (!response.ok || !payload || payload.code < 200 || payload.code >= 300) {
    throw new ApiRequestError(payload?.message ?? `请求失败: ${API_BASE}${path}`, path, response.status);
  }
  return payload.data;
};

const requestPage = async <T>(
  path: string,
  { headers, body, ...options }: RequestOptions = {}
): Promise<PageResult<T>> => {
  const isJsonBody = body && !(body instanceof FormData) && !(body instanceof Blob);
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      ...(isJsonBody ? { "Content-Type": "application/json" } : {}),
      ...headers
    },
    body: buildBody(body)
  });

  const payload = (await response.json().catch(() => null)) as unknown;
  const page = isPageResult<T>(payload)
    ? payload
    : isRecord(payload) && isPageResult<T>(payload.data)
      ? payload.data
      : null;
  const responseCode = isRecord(payload) && typeof payload.code === "number" ? payload.code : null;
  const nestedCode = isRecord(payload) && isPageResult<T>(payload.data) ? payload.data.code : null;
  const message = isRecord(payload) && typeof payload.message === "string" ? payload.message : null;

  if (
    !response.ok ||
    !page ||
    (responseCode !== null && (responseCode < 200 || responseCode >= 300)) ||
    (nestedCode !== null && (nestedCode < 200 || nestedCode >= 300))
  ) {
    throw new ApiRequestError(message ?? `请求失败: ${API_BASE}${path}`, path, response.status);
  }
  return page;
};

const parseDownloadFileName = (contentDisposition: string | null, fallback: string) => {
  if (!contentDisposition) {
    return fallback;
  }

  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1]);
  }

  const plainMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
  return plainMatch?.[1] ? plainMatch[1] : fallback;
};

const downloadRequest = async (path: string, fallbackFileName: string): Promise<FileDownload> => {
  const response = await fetch(`${API_BASE}${path}`, {
    method: "GET"
  });

  if (!response.ok) {
    const payload = (await response.json().catch(() => null)) as ApiResult<unknown> | null;
    throw new ApiRequestError(payload?.message ?? `请求失败: ${API_BASE}${path}`, path, response.status);
  }

  return {
    blob: await response.blob(),
    fileName: parseDownloadFileName(response.headers.get("Content-Disposition"), fallbackFileName)
  };
};

const actionCommentBody = (payload?: string | AuditActionRequest) => {
  if (typeof payload === "string") {
    return { comment: payload, auditComment: payload };
  }
  return payload ?? {};
};

const normalizeVisualLifecycleStatus = (status?: string, fallback = "DRAFT") => {
  const normalized = (status ?? "").trim().toUpperCase();
  if (!normalized) {
    return fallback;
  }
  if (normalized === "ACTIVE" || normalized === "CONFIRMED" || normalized === "DRAFT" || normalized === "ARCHIVED") {
    return normalized === "ACTIVE" ? "CONFIRMED" : normalized;
  }
  if (normalized === "INACTIVE") {
    return "ARCHIVED";
  }
  return fallback;
};

const mergePromptOutput = (entry: PromptWorkbenchResult): PromptWorkbenchResult => ({
  ...entry,
  positivePrompt: entry.positivePrompt ?? entry.outputText,
  shotScript: entry.shotScript ?? entry.outputText,
  styleTags: entry.styleTags ?? [],
  riskWarnings: entry.riskWarnings ?? []
});

const mergeVisualPlan = (plan: VisualPlan): VisualPlan => ({
  ...plan,
  taskName: plan.taskName ?? plan.planName,
  promptContext: plan.promptContext ?? plan.inputData ?? undefined
});

const mergeCategoryVisualPolicy = (policy: CategoryVisualPolicy): CategoryVisualPolicy => policy;

const mergeModelProfile = (profile: ModelProfile): ModelProfile => profile;

const mergeSkcPolicy = (policy: SkcPolicy): SkcPolicy => ({
  ...policy,
  name: policy.name ?? policy.policyName,
  policyName: policy.policyName ?? policy.name
});

const splitListText = (raw?: string) =>
  (raw ?? "")
    .split(/[\n,]+/)
    .map((item) => item.trim())
    .filter(Boolean);

const buildPromptWorkbenchPayload = (payload: PromptWorkbenchRequest, entryType: string, toolCode: string) => {
  const inputData = {
    ...(payload.inputData ?? {}),
    materialId: payload.materialId,
    brandGuideline: payload.brandGuideline,
    platformRequirement: payload.platformRequirement,
    ratio: payload.ratio,
    skcPolicyId: payload.skcPolicyId,
    modelProfileId: payload.modelProfileId,
    referenceNotes: payload.referenceNotes,
    positivePrompt: payload.positivePrompt,
    negativePrompt: payload.negativePrompt,
    styleTags: payload.styleTags,
    constraints: payload.constraints,
    language: payload.language,
    output: payload.output,
    outputText: payload.outputText
  };

  return {
    taskName: payload.taskName ?? `${entryType} prompt`,
    entryType,
    toolCode,
    productDetailId: payload.productDetailId,
    productMaterialId: payload.productMaterialId ?? payload.materialId,
    categoryCode: payload.categoryCode,
    promptText:
      payload.promptText ??
      [payload.brandGuideline, payload.platformRequirement, payload.referenceNotes, payload.positivePrompt, payload.negativePrompt]
        .filter(Boolean)
        .join("\n"),
    imageUrl: payload.imageUrl,
    inputData
  };
};

export const buildVisualPlanCreatePayload = (payload: VisualPlanCreateRequest) => {
  const promptWorkbenchEntryIds = Array.isArray(payload.promptWorkbenchEntryIds) ? payload.promptWorkbenchEntryIds : [];
  const planData = payload.planData ?? payload.promptContext ?? {};
  const inputData = payload.inputData ?? payload.promptContext ?? {};
  return {
    productDetailId: payload.productDetailId,
    planName: payload.planName ?? payload.taskName ?? "Visual Plan",
    categoryCode: payload.categoryCode,
    categoryVisualPolicyId: payload.categoryVisualPolicyId,
    modelProfileId: payload.modelProfileId,
    skcPolicyId: payload.skcPolicyId,
    promptWorkbenchEntryIds,
    inputData,
    planData,
    status: normalizeVisualLifecycleStatus(payload.status)
  };
};

const mapPageResult = <T, U>(page: PageResult<T>, mapper: (item: T) => U): PageResult<U> => ({
  ...page,
  data: page.data.map((item) => mapper(item))
});

export const researchTaskApi = {
  list: (query: ResearchTaskListQuery = {}) =>
    requestPage<ResearchTask>(buildPath("/research/tasks/list", normalizePageQuery(query))),
  create: (payload: ResearchTask) =>
    request<number | string>("/research/tasks", {
      method: "POST",
      body: payload
    }),
  get: (id: string | number) => request<ResearchTask>(`/research/tasks/${id}`),
  updateStatus: (id: string | number, payload: ResearchTaskStatusRequest) =>
    request<boolean>(`/research/tasks/${id}/status`, {
      method: "PUT",
      body: payload
    }),
  updateResult: (id: string | number, payload: ResearchTaskResultRequest) =>
    request<boolean>(`/research/tasks/${id}/result`, {
      method: "PUT",
      body: payload
    }),
  charts: (id: string | number) => request<ResearchTaskCharts>(`/research/tasks/${id}/charts`)
};

export const productContentTaskApi = {
  list: (query: ProductContentTaskListQuery = {}) =>
    requestPage<ProductContentTask>(buildPath("/product-content-tasks", normalizePageQuery(query))),
  create: (payload: ProductContentTaskCreateRequest) =>
    request<ProductContentTask>("/product-content-tasks", {
      method: "POST",
      body: payload
    }),
  get: (id: string | number) => request<ProductContentTask>(`/product-content-tasks/${id}`),
  apply: (id: string | number, payload: ProductContentTaskApplyRequest) =>
    request<ProductContentTask>(`/product-content-tasks/${id}/apply`, {
      method: "POST",
      body: payload
    })
};

export const imageJobApi = {
  list: (query: ImageJobListQuery = {}) =>
    requestPage<ImageJob>(buildPath("/image-jobs/list", normalizePageQuery(query))),
  create: (payload: ImageJobCreateRequest) =>
    request<number | string>("/image-jobs", {
      method: "POST",
      body: payload
    }),
  get: (id: string | number) => request<ImageJob>(`/image-jobs/${id}`),
  retry: (id: string | number, payload?: ImageJobRetryRequest) =>
    request<boolean>(`/image-jobs/${id}/retry`, {
      method: "POST",
      body: payload
    }),
  cancel: (id: string | number) =>
    request<boolean>(`/image-jobs/${id}/cancel`, {
      method: "POST"
    })
};

export const generationResultApi = {
  list: (query: GenerationResultListQuery = {}) =>
    requestPage<GenerationResult>(buildPath("/generation-results/list", normalizePageQuery(query))),
  get: (id: string | number) => request<GenerationResult>(`/generation-results/${id}`),
  updateSelection: (id: string | number, payload: GenerationResultSelectionRequest) =>
    request<boolean>(`/generation-results/${id}/selection`, {
      method: "PUT",
      body: payload
    })
};

export const assetOcrTaskApi = {
  list: (query: AssetOcrTaskListQuery = {}) =>
    requestPage<AssetOcrTask>(buildPath("/assets/ocr-tasks/list", normalizePageQuery(query))),
  create: (payload: AssetOcrTask) =>
    request<number | string>("/assets/ocr-tasks", {
      method: "POST",
      body: payload
    }),
  get: (id: string | number) => request<AssetOcrTask>(`/assets/ocr-tasks/${id}`),
  updateStatus: (id: string | number, payload: AssetOcrTaskStatusRequest) =>
    request<boolean>(`/assets/ocr-tasks/${id}/status`, {
      method: "PUT",
      body: payload
    }),
  updateResult: (id: string | number, payload: AssetOcrTaskResultRequest) =>
    request<boolean>(`/assets/ocr-tasks/${id}/result`, {
      method: "PUT",
      body: payload
    })
};

export const designDraftApi = {
  list: (query: DesignDraftListQuery = {}) =>
    requestPage<DesignDraft>(buildPath("/design-drafts/list", normalizePageQuery(query))),
  create: (payload: DesignDraft) =>
    request<number | string>("/design-drafts", {
      method: "POST",
      body: payload
    }),
  get: (id: string | number) => request<DesignDraft>(`/design-drafts/${id}`),
  update: (id: string | number, payload: DesignDraftUpdateRequest) =>
    request<boolean>(`/design-drafts/${id}`, {
      method: "PUT",
      body: payload
    })
};

export const detailCompositionApi = {
  list: (query: DetailCompositionListQuery = {}) =>
    requestPage<DetailComposition>(buildPath("/detail-compositions/list", normalizePageQuery(query))),
  create: (payload: DetailCompositionCreateRequest) =>
    request<number | string>("/detail-compositions", {
      method: "POST",
      body: payload
    }),
  get: (id: string | number) => request<DetailComposition>(`/detail-compositions/${id}`),
  createQualityCheck: (id: string | number) =>
    request<number | string>(`/detail-compositions/${id}/quality-checks`, {
      method: "POST"
    }),
  listQualityChecks: (id: string | number, query: { pageNum?: number; pageSize?: number } = {}) =>
    requestPage<DetailCompositionQualityCheck>(buildPath(`/detail-compositions/${id}/quality-checks/list`, query)),
  getDeliveryManifest: (id: string | number) => request<DetailDeliveryManifest>(`/detail-compositions/${id}/delivery-manifest`),
  download: (id: string | number) =>
    downloadRequest(`/detail-compositions/${id}/download`, `detail-composition-${id}.png`)
};

export const materialApi = {
  upload: (payload: ProductMaterialUploadRequest) =>
    request<number | string>("/material/upload", {
      method: "POST",
      body: payload
    }),
  get: (id: string | number) => request<ProductMaterial>(`/material/${id}`),
  list: (query: ProductMaterialListQuery = {}) =>
    request<PageResult<ProductMaterial>>(buildPath("/material/list", normalizePageQuery(query))),
  update: (id: string | number, payload: ProductMaterialUpdateRequest) =>
    request<boolean>(`/material/${id}`, {
      method: "PUT",
      body: payload
    }),
  remove: (id: string | number) =>
    request<boolean>(`/material/${id}`, {
      method: "DELETE"
    })
};

export const detailApi = {
  generate: (payload: DetailGenerateRequest) =>
    request<number | string>("/detail/generate", {
      method: "POST",
      body: payload
    }),
  get: (id: string | number) => request<ProductDetail>(`/detail/${id}`),
  list: (query: ProductDetailListQuery = {}) =>
    request<PageResult<ProductDetail>>(buildPath("/detail/list", normalizePageQuery(query))),
  update: (id: string | number, payload: ProductDetailUpdateRequest) =>
    request<boolean>(`/detail/${id}`, {
      method: "PUT",
      body: payload
    }),
  getModuleOrder: (id: string | number) => request<string[]>(`/detail/${id}/module-order`),
  updateModuleOrder: (id: string | number, payload: string[]) =>
    request<boolean>(`/detail/${id}/module-order`, {
      method: "PUT",
      body: payload
    }),
  applyGenerationResults: (id: string | number, payload: ApplyGenerationResultsRequest) =>
    request<number | string>(`/detail/${id}/generation-results/apply`, {
      method: "POST",
      body: payload
    }),
  remove: (id: string | number) =>
    request<boolean>(`/detail/${id}`, {
      method: "DELETE"
    }),
  riskCheck: (id: string | number) =>
    request<DetailRiskResult>(`/detail/${id}/risk-check`, {
      method: "POST"
    }),
  getRisk: (id: string | number) => request<DetailRiskResult>(`/detail/${id}/risk`),
  regenerate: (id: string | number) =>
    request<boolean>(`/detail/${id}/regenerate`, {
      method: "POST"
    })
};

export const auditApi = {
  submit: (payload: AuditSubmitRequest) =>
    request<number | string>("/audit/submit", {
      method: "POST",
      body: payload
    }),
  getByProduct: (productDetailId: string | number) => request<AuditRecord>(`/audit/product/${productDetailId}`),
  list: (query: AuditListQuery = {}) => {
    const { auditStatus, status, ...rest } = normalizePageQuery(query);
    return requestPage<AuditRecord>(
      buildPath("/audit/list", {
        ...rest,
        status: status ?? auditStatus
      })
    );
  },
  approve: (id: string | number, payload?: string | AuditActionRequest) =>
    request<boolean>(`/audit/${id}/approve`, {
      method: "PUT",
      body: actionCommentBody(payload)
    }),
  reject: (id: string | number, payload?: string | AuditActionRequest) =>
    request<boolean>(`/audit/${id}/reject`, {
      method: "PUT",
      body: actionCommentBody(payload)
    }),
  returnForRevision: (id: string | number, payload?: string | AuditActionRequest) =>
    request<boolean>(`/audit/${id}/return`, {
      method: "PUT",
      body: actionCommentBody(payload)
    }),
  withdraw: (id: string | number) =>
    request<boolean>(`/audit/${id}/withdraw`, {
      method: "PUT"
    }),
  reaudit: (id: string | number) =>
    request<boolean>(`/audit/${id}/reaudit`, {
      method: "PUT"
    })
};

export const exportApi = {
  create: (payload: ExportCreateRequest) =>
    request<number | string>("/export/export", {
      method: "POST",
      body: payload
    }),
  get: (id: string | number) => request<ExportRecord>(`/export/${id}`),
  list: (query: ExportListQuery = {}) => {
    const { exportStatus, status, ...rest } = normalizePageQuery(query);
    return requestPage<ExportRecord>(
      buildPath("/export/list", {
        ...rest,
        status: status ?? exportStatus
      })
    );
  },
  download: (id: string | number) =>
    downloadRequest(`/export/${id}/download`, `export-${id}`),
  remove: (id: string | number) =>
    request<boolean>(`/export/${id}`, {
      method: "DELETE"
    }),
  reexport: (id: string | number) =>
    request<boolean>(`/export/${id}/reexport`, {
      method: "POST"
    })
};

export const toolAdapterApi = {
  list: () => request<ToolAdapterInfo[]>("/tool-adapters"),
  get: (code: string) => request<ToolAdapterInfo>(`/tool-adapters/${encodeURIComponent(code)}`),
  invoke: (code: string, payload: ToolInvokeRequest) =>
    request<ToolInvokeResponse>(`/tool-adapters/${encodeURIComponent(code)}/invoke`, {
      method: "POST",
      body: payload
    })
};

export const categoryVisualPolicyApi = {
  list: (query: CategoryVisualPolicyQuery = {}) =>
    requestPage<CategoryVisualPolicy>(
      buildPath("/category-visual-policies/list", {
        ...normalizePageQuery(query),
        keyword: query.keyword ?? [query.categoryCode, query.categoryName].filter(Boolean).join(" ")
      })
    ).then((page) => mapPageResult(page, mergeCategoryVisualPolicy)),
  get: (id: string | number) => request<CategoryVisualPolicy>(`/category-visual-policies/${id}`).then(mergeCategoryVisualPolicy),
  create: (payload: CategoryVisualPolicy) =>
    request<number | string>("/category-visual-policies", {
      method: "POST",
      body: {
        ...payload,
        status: normalizeVisualLifecycleStatus(payload.status)
      }
    }),
  update: (id: string | number, payload: Partial<CategoryVisualPolicy>) =>
    request<boolean>(`/category-visual-policies/${id}`, {
      method: "PUT",
      body: {
        ...payload,
        status: payload.status ? normalizeVisualLifecycleStatus(payload.status) : payload.status
      }
    }),

  confirm: (id: string | number) =>
    request<CategoryVisualPolicy>(`/category-visual-policies/${id}/confirm`, {
      method: "POST"
    }).then(mergeCategoryVisualPolicy)
};

export const modelProfileApi = {
  list: (query: ModelProfileQuery = {}) =>
    requestPage<ModelProfile>(
      buildPath("/model-profiles/list", {
        ...normalizePageQuery(query),
        keyword: query.keyword ?? query.displayName
      })
    ).then((page) => mapPageResult(page, mergeModelProfile)),
  get: (id: string | number) => request<ModelProfile>(`/model-profiles/${id}`).then(mergeModelProfile),
  create: (payload: ModelProfile) =>
    request<number | string>("/model-profiles", {
      method: "POST",
      body: {
        ...payload,
        status: normalizeVisualLifecycleStatus(payload.status)
      }
    }),
  update: (id: string | number, payload: Partial<ModelProfile>) =>
    request<boolean>(`/model-profiles/${id}`, {
      method: "PUT",
      body: {
        ...payload,
        status: payload.status ? normalizeVisualLifecycleStatus(payload.status) : payload.status
      }
    }),

  confirm: (id: string | number) =>
    request<ModelProfile>(`/model-profiles/${id}/confirm`, {
      method: "POST"
    }).then(mergeModelProfile)
};

export const skcPolicyApi = {
  list: (query: SkcPolicyQuery = {}) =>
    requestPage<SkcPolicy>(
      buildPath("/skc-policies/list", {
        ...normalizePageQuery(query),
        keyword: query.keyword ?? query.categoryCode
      })
    ).then((page) => mapPageResult(page, mergeSkcPolicy)),
  get: (id: string | number) => request<SkcPolicy>(`/skc-policies/${id}`).then(mergeSkcPolicy),
  create: (payload: SkcPolicy) =>
    request<number | string>("/skc-policies", {
      method: "POST",
      body: {
        ...payload,
        policyName: payload.policyName ?? payload.name,
        status: normalizeVisualLifecycleStatus(payload.status)
      }
    }),

  confirm: (id: string | number) =>
    request<SkcPolicy>(`/skc-policies/${id}/confirm`, {
      method: "POST"
    }).then(mergeSkcPolicy)
};

export const promptWorkbenchApi = {
  list: (query: { pageNum?: number; pageSize?: number; entryType?: string; status?: string } = {}) =>
    requestPage<PromptWorkbenchResult>(buildPath("/prompt-workbench/list", normalizePageQuery(query))).then((page) =>
      mapPageResult(page, mergePromptOutput)
    ),
  get: (id: string | number) => request<PromptWorkbenchResult>(`/prompt-workbench/${id}`).then(mergePromptOutput),
  guided: (payload: PromptWorkbenchGuidedRequest) =>
    request<PromptWorkbenchResult>("/prompt-workbench/guided", {
      method: "POST",
      body: buildPromptWorkbenchPayload(payload, "GUIDED", "ai-relay")
    }).then(mergePromptOutput),
  expand: (payload: PromptWorkbenchExpandRequest) =>
    request<PromptWorkbenchResult>("/prompt-workbench/expand", {
      method: "POST",
      body: buildPromptWorkbenchPayload(payload, "EXPAND", "ai-relay")
    }).then(mergePromptOutput),
  imageToPrompt: (payload: PromptWorkbenchImageToPromptRequest) =>
    request<PromptWorkbenchResult>("/prompt-workbench/image-to-prompt", {
      method: "POST",
      body: buildPromptWorkbenchPayload(
        {
          ...payload,
          output: payload.output ?? splitListText(payload.outputText)
        },
        "IMAGE_TO_PROMPT",
        "llava"
      )
    }).then(mergePromptOutput)
};

export const visualPlanApi = {
  list: (query: VisualPlanQuery = {}) =>
    requestPage<VisualPlan>(buildPath("/visual-plans/list", normalizePageQuery(query))).then((page) =>
      mapPageResult(page, mergeVisualPlan)
    ),
  create: (payload: VisualPlanCreateRequest) =>
    request<number | string>("/visual-plans", {
      method: "POST",
      body: buildVisualPlanCreatePayload(payload)
    }),
  get: (id: string | number) => request<VisualPlan>(`/visual-plans/${id}`).then(mergeVisualPlan),
  confirm: (id: string | number, payload?: { confirmData?: Record<string, unknown> }) =>
    request<VisualPlan>(`/visual-plans/${id}/confirm`, {
      method: "POST",
      body: payload
    }).then(mergeVisualPlan),
  update: (id: string | number, payload: Record<string, unknown>) =>
    request<boolean>(`/visual-plans/${id}`, {
      method: "PUT",
      body: payload
    }),
  dispatch: (id: string | number, jobs: ImageJobCreateRequest[]) =>
    request<(number | string)[]>(`/visual-plans/${id}/dispatch`, {
      method: "POST",
      body: jobs
    }),
  batchStatus: (id: string | number) => request<VisualPlanBatchStatus>(`/visual-plans/${id}/batch-status`),
  batchRetry: (id: string | number) =>
    request<number>(`/visual-plans/${id}/batch-retry`, {
      method: "POST"
    }),
  batchCancel: (id: string | number) =>
    request<number>(`/visual-plans/${id}/batch-cancel`, {
      method: "POST"
    }),
  batchResults: (id: string | number, slot?: string) =>
    request<VisualPlanBatchResults>(buildPath(`/visual-plans/${id}/batch-results`, slot ? { slot } : {}))
};


export const promptTemplateApi = {
  list: (query: PromptTemplateQuery = {}) =>
    requestPage<PromptTemplate>(buildPath("/prompt-templates/list", normalizePageQuery(query))),
  get: (id: number) => request<PromptTemplate>(`/prompt-templates/${id}`),
  create: (payload: PromptTemplateCreateRequest) =>
    request<PromptTemplate>("/prompt-templates", { method: "POST", body: payload }),
  update: (id: number, payload: Partial<PromptTemplateCreateRequest>) =>
    request<PromptTemplate>(`/prompt-templates/${id}`, { method: "PUT", body: payload }),
  delete: (id: number) => request<void>(`/prompt-templates/${id}`, { method: "DELETE" }),
  duplicate: (id: number) => request<PromptTemplate>(`/prompt-templates/${id}/duplicate`, { method: "POST" }),
  use: (id: number) => request<void>(`/prompt-templates/${id}/use`, { method: "POST" })
};
export const teamApi = {
  listUsers: (query: Record<string, unknown> = {}) =>
    requestPage<TeamUser>(buildPath("/team/users/list", normalizePageQuery(query as PageQuery))),
  createUser: (payload: Record<string, unknown>) =>
    request<number>("/team/users", { method: "POST", body: payload }),
  getUser: (id: number) => request<TeamUser>(`/team/users/${id}`),
  updateUser: (id: number, payload: Record<string, unknown>) =>
    request<boolean>(`/team/users/${id}`, { method: "PUT", body: payload }),
  deleteUser: (id: number) => request<boolean>(`/team/users/${id}`, { method: "DELETE" }),
  listRoles: () => request<TeamRole[]>("/team/roles/all"),
  createRole: (payload: Record<string, unknown>) =>
    request<number>("/team/roles", { method: "POST", body: payload })
};

export const auditLogApi = {
  list: (query: Record<string, unknown> = {}) =>
    requestPage<OperationAuditLog>(buildPath("/audit-logs/list", normalizePageQuery(query as PageQuery)))
};
export const costConfigApi = {
  list: (query: Record<string, unknown> = {}) =>
    requestPage<CostConfig>(buildPath("/cost-configs/list", normalizePageQuery(query as PageQuery))),
  create: (payload: CostConfig) =>
    request<number>("/cost-configs", { method: "POST", body: payload }),
  get: (id: number) => request<CostConfig>(`/cost-configs/${id}`),
  update: (id: number, payload: CostConfig) =>
    request<boolean>(`/cost-configs/${id}`, { method: "PUT", body: payload }),
  remove: (id: number) =>
    request<boolean>(`/cost-configs/${id}`, { method: "DELETE" })
};

export const costStatsApi = {
  overall: () => request<CostStats>("/cost-stats"),
  byTask: (taskType: string, taskId?: number) =>
    request<CostStats>(buildPath("/cost-stats/task", { taskType, taskId })),
  byVisualPlan: (planId: number) => request<CostStats>(`/cost-stats/visual-plan/${planId}`),
  byTool: (toolCode: string) => request<CostStats>(buildPath("/cost-stats/tool", { toolCode })),
  byModel: (modelCode: string) => request<CostStats>(buildPath("/cost-stats/model", { modelCode })),
  records: (query: Record<string, unknown> = {}) =>
    requestPage<TaskCostRecord>(buildPath("/cost-stats/records", normalizePageQuery(query as PageQuery)))
};

export const systemApi = {
  diagnostics: () => request<EnvironmentDiagnostic>("/system/diagnostics")
};

export const publishCheckApi = {
  run: (productDetailId: number) =>
    request<PublishCheckSummary>(`/publish-checks/run/${productDetailId}`, { method: "POST" }),
  list: (productDetailId: number) =>
    request<unknown>(`/publish-checks/list/${productDetailId}`),
  summary: (productDetailId: number) =>
    request<PublishCheckSummary>(`/publish-checks/summary/${productDetailId}`),
  override: (checkId: number, reason: string) =>
    request<boolean>(`/publish-checks/${checkId}/override`, {
      method: "POST",
      body: { overrideReason: reason }
    })
};
export const postProcessTaskApi = {
  list: (query: PostProcessTaskListQuery = {}) =>
    requestPage<PostProcessTask>(buildPath("/post-process-tasks/list", normalizePageQuery(query))),
  create: (payload: PostProcessTaskCreateRequest) =>
    request<number | string>("/post-process-tasks", {
      method: "POST",
      body: payload
    }),
  get: (id: string | number) => request<PostProcessTask>(`/post-process-tasks/${id}`),
  retry: (id: string | number) =>
    request<boolean>(`/post-process-tasks/${id}/retry`, {
      method: "POST"
    }),
  cancel: (id: string | number) =>
    request<boolean>(`/post-process-tasks/${id}/cancel`, {
      method: "POST"
    })
};
export const api = {
  basePath: API_BASE,
  material: materialApi,
  detail: detailApi,
  research: researchTaskApi,
  productContentTasks: productContentTaskApi,
  imageJobs: imageJobApi,
  generationResults: generationResultApi,
  ocrTasks: assetOcrTaskApi,
  designDrafts: designDraftApi,
  detailCompositions: detailCompositionApi,
  audit: auditApi,
  export: exportApi,
  tools: toolAdapterApi,
  visualCategoryPolicies: categoryVisualPolicyApi,
  visualModelProfiles: modelProfileApi,
  visualSkcPolicies: skcPolicyApi,
  visualPromptWorkbench: promptWorkbenchApi,
  visualPlans: visualPlanApi,
  postProcessTasks: postProcessTaskApi,
  costConfigs: costConfigApi,
  costStats: costStatsApi,
  system: systemApi,
  publishChecks: publishCheckApi,
  team: teamApi,
  auditLogs: auditLogApi,
  promptTemplates: promptTemplateApi
};



