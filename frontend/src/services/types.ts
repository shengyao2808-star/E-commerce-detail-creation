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

export type AgentRun = {
  id?: number | string;
  taskType?: string;
  agentCode?: string;
  callType?: string;
  status?: string;
  progress?: number;
  projectId?: number | string;
  productMaterialId?: number | string;
  productDetailId?: number | string;
  visualPlanId?: number | string;
  sourceType?: string;
  sourceId?: number | string;
  toolCode?: string;
  operation?: string;
  requestSummary?: Record<string, unknown>;
  responseSummary?: Record<string, unknown>;
  errorMessage?: string;
  durationMs?: number;
  startedTime?: string;
  finishedTime?: string;
  createTime?: string;
  updateTime?: string;
};

export type AgentRunEvent = {
  id?: number | string;
  agentRunId?: number | string;
  eventType?: string;
  status?: string;
  progress?: number;
  message?: string;
  data?: Record<string, unknown>;
  createTime?: string;
};

export type AgentRunListQuery = PageQuery & {
  taskType?: string;
  agentCode?: string;
  status?: string;
  callType?: string;
  projectId?: number | string;
  productMaterialId?: number | string;
  productDetailId?: number | string;
  visualPlanId?: number | string;
  sourceType?: string;
  sourceId?: number | string;
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

export type ProductLinkPreviewRequest = {
  url: string;
};

export type ProductLinkPreview = {
  originalUrl?: string;
  resolvedUrl?: string;
  host?: string;
  platform?: string;
  productName?: string;
  category?: string;
  rawCategoryPath?: string;
  brandName?: string;
  pageTitle?: string;
  httpStatus?: number;
  fetched?: boolean;
  loginRequired?: boolean;
  source?: string;
  message?: string;
};

export type ProductMaterialUploadedFile = {
  originalName?: string;
  storedPath?: string;
  fileType?: string;
  extension?: string;
  fileSize?: number;
  status?: string;
  message?: string;
};

export type MaterialParseTask = {
  id?: number | string;
  materialId?: number | string;
  originalName?: string;
  storedPath?: string;
  fileType?: string;
  extension?: string;
  fileSize?: number;
  ownerUsername?: string;
  parseType?: string;
  requiredPluginCode?: string;
  agentRunId?: number | string;
  status?: string;
  progress?: number;
  userMessage?: string;
  resultJson?: string;
  errorMessage?: string;
  startedTime?: string;
  finishedTime?: string;
  createTime?: string;
  updateTime?: string;
};

export type MaterialParseTaskListQuery = PageQuery & {
  materialId?: number | string;
  status?: string;
  parseType?: string;
};

export type MaterialParseTaskStatusRequest = {
  status?: string;
  progress?: number;
  userMessage?: string;
  resultJson?: string;
  errorMessage?: string;
};

export type ProductMaterialFileUploadResponse = {
  materialId?: number | string;
  status?: string;
  message?: string;
  uploadedCount?: number;
  failedCount?: number;
  totalSize?: number;
  images?: string[];
  documents?: string[];
  videos?: string[];
  uploadedFiles?: ProductMaterialUploadedFile[];
  failedFiles?: ProductMaterialUploadedFile[];
  parseTasks?: MaterialParseTask[];
};

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
  moduleOrder?: string[] | string;
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

export type ProductDetailGenerateResponse = {
  materialId?: number | string;
  detailId?: number | string;
  agentRunId: number | string;
  status?: string;
  nextRoute: string;
};

export type ProductDetailListQuery = PageQuery & {
  status?: number | string;
};

export type ProductDetailUpdateRequest = Partial<Omit<ProductDetail, "id" | "createTime" | "updateTime">>;

export type ProductDetailAuditRequest = {
  approved: boolean;
  comment?: string;
};

export type ProductContentTask = {
  id?: number | string;
  productDetailId?: number | string;
  taskName?: string;
  toolCode?: string;
  status?: string;
  version?: number;
  inputData?: Record<string, unknown>;
  outputData?: Record<string, unknown>;
  outputText?: string;
  title?: string;
  subtitle?: string;
  sellingPoints?: string[];
  detailModules?: Array<Record<string, unknown>>;
  faq?: Array<Record<string, unknown>>;
  seoKeywords?: string[];
  riskWarnings?: string[];
  sourceData?: Record<string, unknown>;
  appliedFields?: string[];
  appliedTime?: string;
  errorMessage?: string;
  createTime?: string;
  updateTime?: string;
};

export type ProductContentTaskListQuery = PageQuery & {
  productDetailId?: number | string;
  status?: string;
};

export type ProductContentTaskCreateRequest = {
  productDetailId: number | string;
  materialId?: number | string;
  brandTemplateId?: number | string;
  visualPlanId?: number | string;
  promptWorkbenchEntryId?: number | string;
  taskName?: string;
  toolCode?: string;
  inputData?: Record<string, unknown>;
};

export type ProductContentTaskApplyRequest = {
  fields: Array<"title" | "subtitle" | "sellingPoints" | "seoKeywords" | "description" | "aiGeneratedContent">;
};

export type ResearchChartDatum = {
  label?: string;
  value?: number;
  name?: string;
  x?: number;
  y?: number;
  [key: string]: unknown;
};

export type ResearchTask = {
  id?: number;
  taskName?: string;
  category?: string;
  owner?: string;
  status?: string;
  inputData?: Record<string, unknown>;
  resultData?: Record<string, unknown>;
  createTime?: string;
  updateTime?: string;
};

export type ResearchTaskListQuery = PageQuery & {
  status?: string;
  reportedOnly?: boolean;
};

export type ResearchTaskSummary = {
  total?: number;
  completed?: number;
  running?: number;
  pending?: number;
};

export type ResearchTaskStatusRequest = {
  status?: string;
};

export type ResearchTaskResultRequest = {
  resultData?: Record<string, unknown> | null;
};

export type ResearchTaskCharts = {
  priceBands: ResearchChartDatum[];
  keywordRanking: ResearchChartDatum[];
  painPointRanking: ResearchChartDatum[];
  competitorMatrix: ResearchChartDatum[];
};

export type ImageJobInputData = unknown;

export type ImageJob = {
  id?: number | string;
  taskName?: string;
  toolCode?: string;
  inputData?: ImageJobInputData;
  inputJson?: string;
  status?: string;
  progress?: number;
  externalJobId?: string;
  errorMessage?: string;
  visualPlanId?: number | string;
  slot?: string;
  ratio?: string;
  promptVersion?: number;
  modelProfileId?: number | string;
  sourceSnapshotJson?: string;
  createTime?: string;
  updateTime?: string;
};

export type ImageJobListQuery = PageQuery & {
  status?: string;
  toolCode?: string;
  visualPlanId?: number | string;
};

export type ImageJobCreateRequest = {
  taskName: string;
  toolCode: string;
  inputData?: ImageJobInputData;
  inputJson?: string;
  visualPlanId?: number | string;
  slot?: string;
  ratio?: string;
  promptVersion?: number;
  modelProfileId?: number | string;
  sourceSnapshotJson?: string;
};

export type ImageJobRetryRequest = {
  retryReason?: string;
};

export type GenerationResultParams = unknown;

export type GenerationResult = {
  id?: number | string;
  imageJobId?: number | string;
  resultUrl?: string;
  thumbnailUrl?: string;
  prompt?: string;
  params?: GenerationResultParams;
  paramsJson?: string;
  complianceStatus?: string;
  selected?: boolean;
  createTime?: string;
  updateTime?: string;
};

export type GenerationResultListQuery = PageQuery & {
  imageJobId?: number | string;
  selected?: boolean;
  complianceStatus?: string;
};

export type GenerationResultSelectionRequest = {
  selected: boolean;
};

export type ApplyGenerationResultsRequest = {
  generationResultIds: number[];
  selectedOnly?: boolean;
};

export type DetailCompositionQualityCheck = {
  id?: number;
  detailCompositionId?: number;
  toolCode?: string;
  status?: string;
  issueCount?: number;
  issues: string[];
  screenshotPath?: string;
  errorMessage?: string;
  checkTime?: string;
  createTime?: string;
  updateTime?: string;
};

export type DetailDeliveryManifest = {
  detailCompositionId?: number;
  productDetailId?: number;
  deliverable?: boolean;
  compositionStatus?: string;
  outputPath?: string;
  outputFileName?: string;
  outputFileSize?: number;
  outputWidth?: number;
  outputHeight?: number;
  latestQualityCheckStatus?: string;
  latestQualityCheckIssueCount?: number;
  latestQualityCheckScreenshotPath?: string;
  latestQualityCheckTime?: string;
  generationResults: Array<Record<string, unknown>>;
  toolchain: string[];
  generatedAt?: string;
};

export type AssetOcrTask = {
  id?: number;
  materialId?: number;
  assetName?: string;
  assetType?: string;
  language?: string;
  status?: string;
  progress?: number;
  ocrText?: string;
  confidence?: number;
  errorMessage?: string;
  createTime?: string;
  updateTime?: string;
};

export type AssetOcrTaskListQuery = PageQuery & {
  materialId?: number;
  status?: string;
  language?: string;
};

export type AssetOcrTaskStatusRequest = {
  status?: string;
  progress?: number;
  errorMessage?: string;
};

export type AssetOcrTaskResultRequest = {
  ocrText?: string;
  confidence?: number;
  progress?: number;
  errorMessage?: string;
};

export type DesignDraft = {
  id?: number;
  productDetailId?: number;
  productMaterialId?: number;
  draftName?: string;
  sceneJson?: string;
  selectedAssets?: Array<Record<string, unknown>>;
  status?: string;
  createTime?: string;
  updateTime?: string;
};

export type DesignDraftListQuery = PageQuery & {
  productDetailId?: number;
  productMaterialId?: number;
  status?: string;
};

export type DesignDraftUpdateRequest = Partial<Omit<DesignDraft, "id" | "createTime" | "updateTime">>;

export type DetailComposition = {
  id?: number;
  productDetailId?: number;
  taskName?: string;
  toolCode?: string;
  inputData?: Record<string, unknown>;
  moduleOrder?: string[];
  status?: string;
  progress?: number;
  externalJobId?: string;
  outputPath?: string;
  outputFileName?: string;
  outputFileSize?: number;
  outputWidth?: number;
  outputHeight?: number;
  mimeType?: string;
  latestQualityCheckStatus?: string;
  latestQualityCheckIssueCount?: number;
  latestQualityCheckScreenshotPath?: string;
  latestQualityCheckTime?: string;
  deliverable?: boolean;
  errorMessage?: string;
  createTime?: string;
  updateTime?: string;
};

export type DetailCompositionListQuery = PageQuery & {
  productDetailId?: number;
  status?: string;
  toolCode?: string;
  keyword?: string;
};

export type DetailCompositionCreateRequest = {
  productDetailId: number;
  taskName?: string;
  toolCode?: string;
  detailData?: Partial<ProductDetail> & { productId?: number };
  moduleOrder?: string[];
};

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

export type ExportFormat = "PNG" | "JPG" | "JSON" | "HTML" | "ZIP" | "WORD" | "MARKDOWN" | "TXT" | "PDF";

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
  detailCompositionId?: number;
  manifestJson?: string;
  manifestConsistent?: boolean;
  qaCheckId?: number;
  qaStatus?: string;
  createTime?: string;
  updateTime?: string;
};

export type ExportCreateRequest = {
  productDetailId: number;
  exportFormat: ExportFormat | string;
  exporter: string;
  detailCompositionId?: number;
  visualPlanId?: number | string;
};

export type ExportListQuery = PageQuery & {
  status?: number | string;
  exportStatus?: number | string;
  exporter?: string;
  productDetailId?: number | string;
};

export type ToolAdapterInfo = {
  code: string;
  name: string;
  category?: string;
  repository?: string;
  stars?: number;
  license?: string;
  integrationMode?: string;
  commercialPolicy?: string;
  defaultOperation?: string;
  defaultPath?: string;
  operations?: string[];
  configured: boolean;
  status?: string;
};

export type CategoryVisualPolicy = {
  id?: number;
  categoryCode?: string;
  categoryName?: string;
  modelPolicy?: "REQUIRED" | "OPTIONAL" | "FORBIDDEN" | string;
  modelConsistencyLevel?: "STRICT" | "LOOSE" | "NONE" | string;
  allowedShotTypes?: string[];
  requiredMainImages?: Record<string, unknown>;
  detailScreenCountRange?: Record<string, unknown>;
  riskRules?: string[];
  status?: string;
  version?: number;
  createTime?: string;
  updateTime?: string;
};

export type CategoryVisualPolicyQuery = PageQuery & {
  categoryCode?: string;
  categoryName?: string;
  keyword?: string;
  status?: string;
};

export type ModelProfile = {
  id?: number;
  displayName?: string;
  frontImage?: string;
  sideImage?: string;
  backImage?: string;
  height?: number;
  weight?: number;
  bust?: number;
  waist?: number;
  hip?: number;
  styleTags?: string[];
  categoryScopes?: string[];
  authorizationStatus?: string;
  version?: number;
  status?: string;
  createTime?: string;
  updateTime?: string;
};

export type ModelProfileQuery = PageQuery & {
  status?: string;
  displayName?: string;
  keyword?: string;
};

export type SkcPolicy = {
  id?: number;
  name?: string;
  policyName?: string;
  categoryCode?: string;
  colorCount?: number;
  specCount?: number;
  colors?: Array<Record<string, unknown>>;
  specs?: Array<Record<string, unknown>>;
  renderMode?: "MODEL" | "FLAT_LAY" | "REAL_PRODUCT" | "MIXED" | string;
  variantDisplayMode?: string;
  status?: string;
  version?: number;
  generationRules?: string[];
  createTime?: string;
  updateTime?: string;
};

export type SkcPolicyQuery = PageQuery & {
  categoryCode?: string;
  keyword?: string;
  status?: string;
};

export type PromptWorkbenchRequest = {
  taskName?: string;
  entryType?: string;
  toolCode?: string;
  productDetailId?: number | string;
  productMaterialId?: number | string;
  categoryCode?: string;
  promptText?: string;
  imageUrl?: string;
  inputData?: Record<string, unknown>;
  materialId?: number | string;
  brandGuideline?: string;
  platformRequirement?: string;
  ratio?: string;
  skcPolicyId?: number | string;
  modelProfileId?: number | string;
  referenceNotes?: string;
  positivePrompt?: string;
  negativePrompt?: string;
  styleTags?: string[];
  constraints?: string[];
  language?: string;
  output?: string[];
  outputText?: string;
};

export type PromptWorkbenchGuidedRequest = PromptWorkbenchRequest;

export type PromptWorkbenchExpandRequest = PromptWorkbenchRequest;

export type PromptWorkbenchImageToPromptRequest = PromptWorkbenchRequest & {
  imageUrl: string;
};

export type PromptWorkbenchResult = {
  id?: number;
  entryType?: string;
  taskName?: string;
  toolCode?: string;
  status?: string;
  version?: number;
  inputData?: Record<string, unknown>;
  outputData?: Record<string, unknown>;
  outputText?: string;
  errorMessage?: string;
  positivePrompt?: string;
  negativePrompt?: string;
  shotScript?: string;
  composition?: string;
  lighting?: string;
  camera?: string;
  styleTags?: string[];
  sourceData?: Record<string, unknown>;
  riskWarnings?: string[];
  createTime?: string;
  updateTime?: string;
};

export type VisualPlan = {
  id?: number | string;
  productDetailId?: number | string;
  planName?: string;
  taskName?: string;
  categoryCode?: string;
  categoryVisualPolicyId?: number | string;
  modelProfileId?: number | string;
  skcPolicyId?: number | string;
  promptWorkbenchEntryIds?: Array<number | string>;
  inputData?: Record<string, unknown>;
  planData?: Record<string, unknown>;
  promptContext?: Record<string, unknown>;
  snapshotData?: Record<string, unknown>;
  status?: string;
  version?: number;
  confirmedTime?: string;
  createTime?: string;
  updateTime?: string;
};

export type VisualPlanQuery = PageQuery & {
  productDetailId?: number | string;
  status?: string;
};

export type VisualPlanCreateRequest = {
  productDetailId: number | string;
  planName?: string;
  taskName?: string;
  categoryCode?: string;
  categoryVisualPolicyId?: number | string;
  modelProfileId?: number | string;
  skcPolicyId?: number | string;
  promptWorkbenchEntryIds?: Array<number | string>;
  inputData?: Record<string, unknown>;
  planData?: Record<string, unknown>;
  promptContext?: Record<string, unknown>;
  status?: string;
};

export type VisualPlanBatchJobSummary = {
  id?: number | string;
  taskName?: string;
  status?: string;
  slot?: string;
  ratio?: string;
  progress?: number;
  promptVersion?: number;
  modelProfileId?: number | string;
  errorMessage?: string;
};

export type VisualPlanBatchResultJob = VisualPlanBatchJobSummary & {
  results?: GenerationResult[];
};

export type VisualPlanBatchStatus = {
  visualPlanId?: number | string;
  totalJobs?: number;
  succeededJobs?: number;
  failedJobs?: number;
  pendingJobs?: number;
  runningJobs?: number;
  canceledJobs?: number;
  aggregatedStatus?: string;
  jobSummaries?: VisualPlanBatchJobSummary[];
};

export type VisualPlanBatchResults = {
  visualPlanId?: number | string;
  slotFilter?: string;
  totalSlots?: number;
  slotGroups?: Record<string, VisualPlanBatchResultJob[]>;
};

export type PostProcessTask = {
  id?: number | string;
  sourceGenerationResultId?: number | string;
  sourceImagePath?: string;
  outputImagePath?: string;
  toolCode?: string;
  operation?: string;
  params?: Record<string, unknown>;
  inputWidth?: number;
  inputHeight?: number;
  inputFileSize?: number;
  inputMimeType?: string;
  outputWidth?: number;
  outputHeight?: number;
  outputFileSize?: number;
  outputMimeType?: string;
  sourceChain?: Record<string, unknown>[];
  status?: string;
  progress?: number;
  errorMessage?: string;
  createTime?: string;
  updateTime?: string;
};

export type PostProcessTaskListQuery = PageQuery & {
  sourceGenerationResultId?: number | string;
  toolCode?: string;
  operation?: string;
  status?: string;
};

export type PostProcessTaskCreateRequest = {
  sourceGenerationResultId?: number | string;
  sourceImagePath?: string;
  toolCode: string;
  operation: string;
  params?: Record<string, unknown>;
  maskImagePath?: string;
  targetWidth?: number;
  targetHeight?: number;
  outputRatio?: string;
};

export type CostConfig = {
  id?: number;
  providerType?: string;
  providerCode?: string;
  unitPrice?: number;
  unitType?: string;
  currency?: string;
  description?: string;
};

export type CostStats = {
  scope?: string;
  scopeId?: number;
  totalJobs?: number;
  succeededJobs?: number;
  failedJobs?: number;
  canceledJobs?: number;
  totalCost?: number;
  avgCostPerJob?: number;
  successCost?: number;
  failCost?: number;
  costByTool?: Record<string, number>;
  costByModel?: Record<string, number>;
  countByStatus?: Record<string, number>;
  costBySource?: Record<string, number>;
  costCurrency?: string;
};

export type TaskCostRecord = {
  id?: number;
  taskType?: string;
  taskId?: number;
  toolCode?: string;
  modelCode?: string;
  visualPlanId?: number;
  costAmount?: number;
  costCurrency?: string;
  status?: string;
  createTime?: string;
  updateTime?: string;
};

export type EnvironmentDiagnostic = {
  overallStatus?: string;
  message?: string;
  aiRelay?: {
    status?: string;
    message?: string;
    baseUrl?: string;
    model?: string;
    enabled?: boolean;
    hasApiKey?: boolean;
    availableModels?: string[];
    configuredModelAvailable?: boolean;
    missingFields?: string[];
  };
  tools?: Array<{
    code?: string;
    name?: string;
    category?: string;
    status?: string;
    message?: string;
    enabled?: boolean;
    repository?: string;
    operations?: string[];
  }>;
  paths?: {
    status?: string;
    message?: string;
    imagemagickInputRoots?: Array<{ configuredValue?: string; exists?: boolean; writable?: boolean; status?: string }>;
    exportRoots?: Array<{ configuredValue?: string; exists?: boolean; writable?: boolean; status?: string }>;
  };
  export?: {
    supportedFormats?: string[];
    unimplementedFormats?: string[];
    status?: string;
  };
  generatedAt?: number;
};

export type PublishCheck = {
  id?: number;
  productDetailId?: number;
  checkType?: string;
  targetType?: string;
  targetId?: string;
  targetField?: string;
  severity?: string;
  status?: string;
  message?: string;
  details?: unknown;
  overridden?: boolean;
  overrideReason?: string;
  overrideOperator?: string;
  overrideTime?: string;
  createTime?: string;
  updateTime?: string;
};

export type PublishCheckSummary = {
  productDetailId?: number;
  publishable?: boolean;
  totalChecks?: number;
  passedChecks?: number;
  failedChecks?: number;
  hardFailedChecks?: number;
  softFailedChecks?: number;
  warningChecks?: number;
  errorChecks?: number;
  overriddenChecks?: number;
  items?: PublishCheck[];
  canPublish?: boolean;
  checks?: PublishCheck[];
};

export type OperationAuditLog = {
  id?: number;
  userId?: number;
  userName?: string;
  action?: string;
  resourceType?: string;
  resourceId?: string;
  detail?: string;
  ipAddress?: string;
  createTime?: string;
};

export type TeamUser = {
  id?: number;
  username?: string;
  displayName?: string;
  email?: string;
  status?: string;
  createTime?: string;
};

export type TeamRole = {
  id?: number;
  roleName?: string;
  description?: string;
  permissions?: string[];
  createTime?: string;
};

export type PromptTemplate = {
  id?: number;
  templateName?: string;
  category?: string;
  sceneType?: string;
  platform?: string;
  style?: string;
  positivePrompt?: string;
  negativePrompt?: string;
  styleTags?: string[];
  constraints?: string[];
  description?: string;
  previewImageUrl?: string;
  usageCount?: number;
  rating?: number;
  source?: string;
  sourceRef?: string;
  language?: string;
  author?: string;
  tags?: string[];
  status?: string;
  createTime?: string;
  updateTime?: string;
};

export type PromptTemplateCreateRequest = {
  templateName: string;
  category: string;
  sceneType?: string;
  platform?: string;
  style?: string;
  positivePrompt: string;
  negativePrompt?: string;
  styleTags?: string[];
  constraints?: string[];
  description?: string;
  previewImageUrl?: string;
  source?: string;
  sourceRef?: string;
  language?: string;
  author?: string;
  tags?: string[];
};

export type PromptTemplateQuery = {
  pageNum?: number;
  pageSize?: number;
  category?: string;
  platform?: string;
  style?: string;
  source?: string;
  keyword?: string;
};

export type BrandTemplate = {
  id?: number;
  brandId?: number;
  brandName?: string;
  templateName?: string;
  templateType?: string;
  templateContent?: string;
  styleTags?: string;
  styleDescription?: string;
  applicableCategories?: string;
  enabled?: boolean;
  usageCount?: number;
  creator?: string;
  updater?: string;
  createTime?: string;
  updateTime?: string;
};

export type BrandTemplateCreateRequest = {
  brandId?: number;
  brandName?: string;
  templateName: string;
  templateType?: string;
  templateContent?: string;
  styleTags?: string;
  styleDescription?: string;
  applicableCategories?: string;
  enabled?: boolean;
  creator?: string;
  updater?: string;
};

export type BrandTemplateQuery = PageQuery & {
  brandId?: number | string;
  brandName?: string;
  templateType?: string;
  enabled?: boolean;
  keyword?: string;
};

export type BrandTemplateSummary = {
  total: number;
  enabled: number;
  disabled: number;
  usageCount: number;
};