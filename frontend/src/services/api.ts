import type {
  ApiResult,
  AuditActionRequest,
  AuditListQuery,
  AuditRecord,
  AuditSubmitRequest,
  DetailGenerateRequest,
  DetailRiskResult,
  ExportCreateRequest,
  ExportListQuery,
  ExportRecord,
  PageQuery,
  PageResult,
  ProductDetail,
  ProductDetailListQuery,
  ProductDetailUpdateRequest,
  ProductMaterial,
  ProductMaterialListQuery,
  ProductMaterialUpdateRequest,
  ProductMaterialUploadRequest
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
  if (!response.ok || !payload || payload.code < 200 || payload.code >= 300) {
    throw new ApiRequestError(payload?.message ?? `Request failed: ${API_BASE}${path}`, path, response.status);
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
    throw new ApiRequestError(message ?? `Request failed: ${API_BASE}${path}`, path, response.status);
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
    throw new ApiRequestError(payload?.message ?? `Request failed: ${API_BASE}${path}`, path, response.status);
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

export const api = {
  basePath: API_BASE,
  material: materialApi,
  detail: detailApi,
  audit: auditApi,
  export: exportApi
};
