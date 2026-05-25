export type ApiResult<T> = {
  code: number;
  message: string;
  data: T;
  timestamp?: number;
};

export type PageResult<T> = ApiResult<T[]> & {
  pageNum: number;
  pageSize: number;
  total: number;
  pages: number;
};

export type PageQuery = {
  pageNum?: number;
  pageSize?: number;
  page?: number;
  size?: number;
  keyword?: string;
};

export type ProductMaterial = {
  id: number;
  brandId?: number;
  brand?: string;
  productName?: string;
  sku?: string;
  productSku?: string;
  category?: string;
  brandName?: string;
  price?: number;
  description?: string;
  images?: string[];
  videos?: string[];
  documents?: string[];
  status?: string | number;
  originalFilePath?: string;
  fileType?: string;
  fileSize?: number;
  uploader?: string;
  uploadTime?: string;
  parseStatus?: number;
  parseError?: string;
  createTime?: string;
  updateTime?: string;
};

export type ProductMaterialUploadRequest = FormData | Record<string, unknown>;

export type ProductMaterialListQuery = PageQuery;

export type ProductMaterialUpdateRequest = Partial<Omit<ProductMaterial, "id" | "createTime" | "updateTime">>;

export type ProductDetail = {
  id: number;
  materialId?: number;
  brandId?: number;
  productId?: number;
  productName?: string;
  brandName?: string;
  title?: string;
  subtitle?: string;
  sellingPoints?: string[] | string;
  description?: string;
  seoKeywords?: string[] | string;
  imageTemplateId?: number;
  sku?: string;
  category?: string;
  price?: number;
  images?: string[] | string;
  videos?: string[] | string;
  documents?: string[] | string;
  riskLevel?: string;
  riskDescription?: string;
  auditStatus?: number | string;
  auditor?: string;
  auditTime?: string;
  auditComment?: string;
  version?: number;
  isCurrentVersion?: boolean;
  aiGeneratedContent?: string;
  creator?: string;
  updater?: string;
  createTime?: string;
  updateTime?: string;
};

export type DetailGenerateRequest = Partial<ProductDetail> & {
  materialId: number;
  brandId?: number;
  title?: string;
};

export type ProductDetailListQuery = PageQuery & {
  status?: number | string;
};

export type ProductDetailUpdateRequest = Partial<Omit<ProductDetail, "id" | "createTime" | "updateTime">>;

export type DetailRiskResult = {
  id?: number;
  productDetailId?: number;
  riskLevel?: string;
  riskDescription?: string;
  hasRisk?: boolean;
  issues?: string[];
  issueDetails?: Record<string, string[]>;
  suggestions?: string[];
  content?: string;
  auditStatus?: number | string;
  auditComment?: string;
  updateTime?: string;
};

export type AuditRecord = {
  id: number;
  productDetailId: number;
  auditType?: string;
  auditStatus?: number | string;
  auditComment?: string;
  riskLevel?: string | number;
  riskItems?: string;
  modificationSuggestions?: string;
  submitter?: string;
  auditor?: string;
  submitTime?: string;
  auditTime?: string;
  auditDuration?: number;
  createTime?: string;
};

export type AuditSubmitRequest = {
  productDetailId: number;
  auditStatus: number;
  auditComment: string;
  auditor: string;
  riskLevel?: number;
  submitter?: string;
};

export type AuditListQuery = PageQuery & {
  status?: number | string;
  auditStatus?: number | string;
  auditor?: string;
};

export type AuditActionRequest = {
  comment?: string;
  auditComment?: string;
  auditor?: string;
};

export type ExportFormat = "WORD" | "MARKDOWN" | "JSON" | "HTML" | "TXT" | "PDF";

export type ExportRecord = {
  id: number;
  productDetailId: number;
  title?: string;
  format?: ExportFormat | string;
  exportFormat?: ExportFormat | string;
  filePath?: string;
  fileName?: string;
  fileSize?: number;
  status?: number;
  exportStatus?: number;
  errorMessage?: string;
  exporter?: string;
  exportTime?: string;
  createTime?: string;
};

export type ExportCreateRequest = {
  productDetailId: number;
  exportFormat: ExportFormat | string;
  exporter: string;
};

export type ExportListQuery = PageQuery & {
  status?: number | string;
  exportStatus?: number | string;
  exporter?: string;
};
