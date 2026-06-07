import { api, buildVisualPlanCreatePayload, extractVisualPlanPromptContext } from "./api";
import type {
  ApplyGenerationResultsRequest,
  DetailComposition,
  DetailCompositionQualityCheck,
  DetailDeliveryManifest,
  AgentRun,
  AgentRunEvent,
  AgentRunListQuery,
  PageResult,
  ProductContentTask,
  ProductContentTaskApplyRequest,
  ProductContentTaskCreateRequest,
  ImageJobCreateRequest,
  ImageJobRetryRequest,
  MaterialParseTask,
  MaterialParseTaskListQuery,
  MaterialParseTaskStatusRequest,
  DetailGenerateRequest,
  ProductDetailGenerateResponse,
  ProductLinkPreview,
  ProductLinkPreviewRequest,
  ProductMaterialFileUploadResponse,
  ExportCreateRequest,
  ExportFormat,
  ExportRecord,
  PublishCheckSummary,
  ResearchTask,
  VisualPlanBatchResults,
  VisualPlanBatchStatus,
  VisualPlanCreateRequest,
  VisualPlan,
  ResearchTaskCharts,
  ResearchTaskListQuery,
  ResearchTaskResultRequest,
  ResearchTaskStatusRequest,
  ResearchTaskSummary,
  BrandTemplate,
  BrandTemplateCreateRequest,
  BrandTemplateQuery,
  BrandTemplateSummary,
  EnvironmentDiagnostic
} from "./types";

// @ts-expect-error generationResultIds must be provided when applying generation results
const _applyGenerationResultsMissingIds: ApplyGenerationResultsRequest = {
  selectedOnly: true
};

const _applyGenerationResultsClient: (
  id: string | number,
  payload: ApplyGenerationResultsRequest
) => Promise<number | string> = api.detail.applyGenerationResults;

const _createQualityCheckClient: (id: string | number) => Promise<number | string> =
  api.detailCompositions.createQualityCheck;

const _listQualityChecksClient: (
  id: string | number,
  query?: { pageNum?: number; pageSize?: number }
) => Promise<PageResult<DetailCompositionQualityCheck>> = api.detailCompositions.listQualityChecks;

const _getDeliveryManifestClient: (id: string | number) => Promise<DetailDeliveryManifest> =
  api.detailCompositions.getDeliveryManifest;

const _detailShape: DetailComposition = {
  id: 1,
  latestQualityCheckStatus: "PASS",
  latestQualityCheckIssueCount: 0,
  latestQualityCheckScreenshotPath: "/tmp/qa.png",
  latestQualityCheckTime: "2026-05-27T00:00:00",
  deliverable: true
};

const _qualityCheckShape: DetailCompositionQualityCheck = {
  id: 1,
  detailCompositionId: 1,
  toolCode: "imagick",
  status: "PASS",
  issueCount: 0,
  issues: [],
  screenshotPath: "/tmp/qa.png",
  errorMessage: "",
  checkTime: "2026-05-27T00:00:00",
  createTime: "2026-05-27T00:00:00",
  updateTime: "2026-05-27T00:00:00"
};

const _deliveryManifestShape: DetailDeliveryManifest = {
  detailCompositionId: 1,
  productDetailId: 2,
  deliverable: true,
  compositionStatus: "SUCCEEDED",
  outputPath: "/tmp/detail.png",
  outputFileName: "detail.png",
  outputFileSize: 1,
  outputWidth: 1,
  outputHeight: 1,
  latestQualityCheckStatus: "PASS",
  latestQualityCheckIssueCount: 0,
  latestQualityCheckScreenshotPath: "/tmp/qa.png",
  latestQualityCheckTime: "2026-05-27T00:00:00",
  generationResults: [],
  toolchain: [],
  generatedAt: "2026-05-27T00:00:00"
};

void _applyGenerationResultsClient;
void _createQualityCheckClient;
void _listQualityChecksClient;
void _getDeliveryManifestClient;
void _detailShape;
void _qualityCheckShape;
void _deliveryManifestShape;

const _detailGeneratePayload: DetailGenerateRequest = {
  materialId: 1,
  title: "Demo detail",
  brandId: 1001,
  brandName: "示例品牌",
  imageTemplateId: 77
};

const _detailGenerateWithResponseClient: (payload: DetailGenerateRequest) => Promise<ProductDetailGenerateResponse> =
  api.detail.generateWithResponse;

const _detailGenerateResponseShape: ProductDetailGenerateResponse = {
  materialId: 1,
  detailId: 2,
  agentRunId: 3,
  status: "SUCCEEDED",
  nextRoute: "/generate/3?materialId=1&detailId=2"
};

void _detailGeneratePayload;
void _detailGenerateWithResponseClient;
void _detailGenerateResponseShape;

const _productLinkPreviewPayload: ProductLinkPreviewRequest = {
  url: "https://item.jd.com/100094034295.html"
};
const _productLinkPreviewClient: (payload: ProductLinkPreviewRequest) => Promise<ProductLinkPreview> =
  api.material.previewLink;
const _productLinkPreviewShape: ProductLinkPreview = {
  originalUrl: "https://item.jd.com/100094034295.html",
  resolvedUrl: "https://item.jd.com/100094034295.html?id=100094034295",
  host: "item.jd.com",
  platform: "京东",
  productName: "便携护眼台灯",
  category: "家居照明",
  rawCategoryPath: "照明灯具 / 家居照明",
  brandName: "示例品牌",
  fetched: true,
  httpStatus: 200,
  loginRequired: false,
  source: "website-metadata",
  message: "已识别平台、商品名称、商品类目。若结果不完整，你仍可继续手动补充。"
};

void _productLinkPreviewPayload;
void _productLinkPreviewClient;
void _productLinkPreviewShape;

const _v1ExportFormats: ExportFormat[] = ["PNG", "JPG", "HTML", "JSON", "ZIP"];
// @ts-expect-error PSD is not a V1/export API format until backend support exists.
const _futurePsdExportFormat: ExportFormat = "PSD";
// @ts-expect-error Figma is not a V1/export API format until backend support exists.
const _futureFigmaExportFormat: ExportFormat = "FIGMA";
// @ts-expect-error Platform packages are not a V1/export API format until backend support exists.
const _futurePlatformExportFormat: ExportFormat = "PLATFORM_PACKAGE";
const _exportCreatePayload: ExportCreateRequest = {
  productDetailId: 42,
  exportFormat: "PNG",
  exporter: "ShopPage",
  detailCompositionId: 88,
  visualPlanId: 99
};
const _exportCreateClient: (payload: ExportCreateRequest) => Promise<number | string> = api.export.create;
const _exportGetClient: (id: string | number) => Promise<ExportRecord> = api.export.get;
const _exportListClient: (query?: { productDetailId?: number | string; status?: number | string }) => Promise<PageResult<ExportRecord>> =
  api.export.list;
const _exportDownloadClient: (id: string | number) => Promise<{ blob: Blob; fileName?: string }> = api.export.download;
const _exportRemoveClient: (id: string | number) => Promise<boolean> = api.export.remove;
const _exportReexportClient: (id: string | number) => Promise<boolean> = api.export.reexport;
const _publishCheckSummaryClient: (id: number) => Promise<PublishCheckSummary> = api.publishChecks.summary;
const _exportRecordShape: ExportRecord = {
  id: 1,
  productDetailId: 42,
  exportFormat: "PNG",
  fileName: "detail.png",
  fileSize: 1024,
  exportStatus: 1,
  detailCompositionId: 88,
  manifestConsistent: true,
  qaStatus: "PASS",
  exportTime: "2026-06-04T00:00:00"
};
const _publishCheckSummaryShape: PublishCheckSummary = {
  productDetailId: 42,
  publishable: true,
  totalChecks: 4,
  passedChecks: 2,
  failedChecks: 2,
  hardFailedChecks: 0,
  softFailedChecks: 2,
  items: []
};

void _v1ExportFormats;
void _futurePsdExportFormat;
void _futureFigmaExportFormat;
void _futurePlatformExportFormat;
void _exportCreatePayload;
void _exportCreateClient;
void _exportGetClient;
void _exportListClient;
void _exportDownloadClient;
void _exportRemoveClient;
void _exportReexportClient;
void _publishCheckSummaryClient;
void _exportRecordShape;
void _publishCheckSummaryShape;

const _environmentDiagnosticShape: EnvironmentDiagnostic = {
  overallStatus: "READY",
  message: "AI relay is configured.",
  aiRelay: {
    status: "AVAILABLE",
    message: "AI relay model endpoint is reachable.",
    baseUrl: "http://127.0.0.1:8000",
    model: "chatgpt-images",
    enabled: true,
    hasApiKey: true,
    availableModels: ["chatgpt-images", "other-company-model"],
    configuredModelAvailable: true,
    missingFields: []
  },
  tools: [],
  export: {
    supportedFormats: ["PNG", "JPG"]
  },
  generatedAt: 1710000000000
};

void _environmentDiagnosticShape;

const _researchTaskListQuery: ResearchTaskListQuery = {
  pageNum: 1,
  pageSize: 10,
  keyword: "护眼",
  status: "SUCCEEDED",
  reportedOnly: true
};
const _researchTaskListClient: (query?: ResearchTaskListQuery) => Promise<PageResult<ResearchTask>> = api.research.list;
const _researchTaskSummaryClient: (query?: ResearchTaskListQuery) => Promise<ResearchTaskSummary> = api.research.summary;
const _researchTaskChartsClient: (id: string | number) => Promise<ResearchTaskCharts> = api.research.charts;
const _researchTaskStatusClient: (id: string | number, payload: ResearchTaskStatusRequest) => Promise<boolean> =
  api.research.updateStatus;
const _researchTaskResultClient: (id: string | number, payload: ResearchTaskResultRequest) => Promise<boolean> =
  api.research.updateResult;
const _researchTaskShape: ResearchTask = {
  id: 7001,
  taskName: "调研任务",
  category: "灯具",
  owner: "alice",
  status: "SUCCEEDED",
  inputData: { keyword: "护眼" },
  resultData: { summary: "已完成调研" },
  createTime: "2026-06-05T00:00:00",
  updateTime: "2026-06-05T00:00:00"
};
const _researchTaskSummaryShape: ResearchTaskSummary = {
  total: 3,
  completed: 1,
  running: 1,
  pending: 1
};
const _researchTaskChartsShape: ResearchTaskCharts = {
  priceBands: [],
  keywordRanking: [],
  painPointRanking: [],
  competitorMatrix: []
};

void _researchTaskListQuery;
void _researchTaskListClient;
void _researchTaskSummaryClient;
void _researchTaskChartsClient;
void _researchTaskStatusClient;
void _researchTaskResultClient;
void _researchTaskShape;
void _researchTaskSummaryShape;
void _researchTaskChartsShape;

const _materialUploadFilesClient: (
  id: string | number,
  files: File[]
) => Promise<ProductMaterialFileUploadResponse> = api.material.uploadFiles;

const _materialFileUploadResponseShape: ProductMaterialFileUploadResponse = {
  materialId: 1,
  status: "UPLOADED",
  message: "uploaded",
  uploadedCount: 1,
  failedCount: 0,
  totalSize: 12,
  images: ["uploads/materials/1/main.png"],
  documents: [],
  videos: [],
  uploadedFiles: [
    {
      originalName: "main.png",
      storedPath: "uploads/materials/1/main.png",
      fileType: "IMAGE",
      extension: "png",
      fileSize: 12,
      status: "UPLOADED",
      message: "文件已上传"
    }
  ],
  failedFiles: [],
  parseTasks: [
    {
      id: 9,
      materialId: 1,
      originalName: "main.png",
      storedPath: "uploads/materials/1/main.png",
      fileType: "IMAGE",
      extension: "png",
      fileSize: 12,
      ownerUsername: "alice",
      parseType: "IMAGE_OCR",
      requiredPluginCode: "ocr-parser",
      agentRunId: 8,
      status: "PENDING",
      progress: 0,
      userMessage: "图片已上传，等待 OCR / 图片识别插件接入后提取内容。"
    }
  ]
};

void _materialUploadFilesClient;
void _materialFileUploadResponseShape;

const _materialParseTaskListClient: (
  query?: MaterialParseTaskListQuery
) => Promise<PageResult<MaterialParseTask>> = api.materialParseTasks.list;
const _materialParseTaskGetClient: (id: string | number) => Promise<MaterialParseTask> = api.materialParseTasks.get;
const _materialParseTaskStatusClient: (
  id: string | number,
  payload: MaterialParseTaskStatusRequest
) => Promise<boolean> = api.materialParseTasks.updateStatus;
const _materialParseTaskExecuteClient: (id: string | number) => Promise<MaterialParseTask> =
  api.materialParseTasks.execute;
const _materialParseTaskRetryClient: (id: string | number) => Promise<MaterialParseTask> = api.materialParseTasks.retry;
const _materialParseTasksByMaterialClient: (id: string | number) => Promise<MaterialParseTask[]> =
  api.material.listParseTasks;

void _materialParseTaskListClient;
void _materialParseTaskGetClient;
void _materialParseTaskStatusClient;
void _materialParseTaskExecuteClient;
void _materialParseTaskRetryClient;
void _materialParseTasksByMaterialClient;

const _agentRunListClient: (query?: AgentRunListQuery) => Promise<PageResult<AgentRun>> = api.agentRuns.list;
const _agentRunGetClient: (id: string | number) => Promise<AgentRun> = api.agentRuns.get;
const _agentRunEventsClient: (id: string | number) => Promise<AgentRunEvent[]> = api.agentRuns.listEvents;

const _agentRunShape: AgentRun = {
  id: 1,
  taskType: "DETAIL_PAGE_GENERATION",
  agentCode: "detail-page-agent",
  callType: "TEXT_RELAY",
  status: "RUNNING",
  progress: 50,
  productMaterialId: 10,
  requestSummary: {
    payloadKeys: ["productName"]
  },
  createTime: "2026-06-02T00:00:00"
};

const _agentRunEventShape: AgentRunEvent = {
  id: 1,
  agentRunId: 1,
  eventType: "STARTED",
  status: "RUNNING",
  progress: 10,
  message: "Agent call started",
  data: {
    sourceType: "PRODUCT_MATERIAL"
  },
  createTime: "2026-06-02T00:00:00"
};

void _agentRunListClient;
void _agentRunGetClient;
void _agentRunEventsClient;
void _agentRunShape;
void _agentRunEventShape;

const _visualPlanCreatePayload: VisualPlanCreateRequest = {
  productDetailId: 1,
  taskName: "Visual plan draft",
  promptContext: {
    heroImages: 5,
    detailScreens: 8
  }
};
const _visualPlanCreatePayloadWithVersions: VisualPlanCreateRequest = {
  productDetailId: 1,
  taskName: "Visual plan draft with versions",
  promptContext: {
    heroImages: 5,
    detailScreens: 8
  },
  planData: {
    selectedDirectionId: "balanced",
    pageReviewVersions: [
      {
        index: 1,
        slot: "main",
        copy: "A",
        visual: "B",
        confirmedAt: "2026-06-05T00:00:00Z",
        planVersion: 2
      }
    ],
    adoptedPartialEditVersions: {
      "100::main": {
        sourceGenerationResultId: "100",
        outputImagePath: "file.png"
      }
    }
  }
};

const _visualPlanCreateClient: (payload: VisualPlanCreateRequest) => Promise<number | string> =
  api.visualPlans.create;
const _mappedVisualPlanCreatePayload = buildVisualPlanCreatePayload(_visualPlanCreatePayload);
const _mappedVisualPlanInputData: Record<string, unknown> = _mappedVisualPlanCreatePayload.inputData;
const _mappedVisualPlanPlanData: Record<string, unknown> = _mappedVisualPlanCreatePayload.planData;
const _mappedVisualPlanCreatePayloadWithVersions = buildVisualPlanCreatePayload(_visualPlanCreatePayloadWithVersions);
const _mappedVisualPlanCreatePayloadWithVersionsPlanData: Record<string, unknown> =
  _mappedVisualPlanCreatePayloadWithVersions.planData;
const _visualPlanPromptContextSource: VisualPlan = {
  id: 1,
  promptContext: {
    tone: "legacy"
  },
  inputData: {
    tone: "input"
  },
  planData: {
    editablePromptContext: {
      tone: "updated",
      pages: [{ index: 1, copy: "A", visual: "B" }]
    },
    pageReviewVersions: [
      {
        index: 1,
        slot: "main",
        copy: "A",
        visual: "B",
        confirmedAt: "2026-06-05T00:00:00Z",
        planVersion: 2
      }
    ],
    adoptedPartialEditVersions: {
      "100::main": {
        sourceGenerationResultId: "100",
        outputImagePath: "file.png"
      }
    }
  }
};
const _visualPlanPromptContext = extractVisualPlanPromptContext(_visualPlanPromptContextSource);
if (_visualPlanPromptContext && typeof _visualPlanPromptContext === "object" && "pageReviewVersions" in _visualPlanPromptContext) {
  throw new Error("pageReviewVersions should not leak into prompt context");
}


void _visualPlanCreatePayload;
void _visualPlanCreateClient;
void _mappedVisualPlanCreatePayload;
void _mappedVisualPlanInputData;
void _mappedVisualPlanPlanData;
void _visualPlanCreatePayloadWithVersions;
void _mappedVisualPlanCreatePayloadWithVersions;
void _mappedVisualPlanCreatePayloadWithVersionsPlanData;
void _visualPlanPromptContextSource;
void _visualPlanPromptContext;

const _visualPlanDispatchClient: (
  id: string | number,
  jobs: ImageJobCreateRequest[]
) => Promise<Array<number | string>> = api.visualPlans.dispatch;
const _visualPlanBatchStatusClient: (id: string | number) => Promise<VisualPlanBatchStatus> =
  api.visualPlans.batchStatus;
const _visualPlanBatchRetryClient: (id: string | number) => Promise<number> = api.visualPlans.batchRetry;
const _visualPlanBatchCancelClient: (id: string | number) => Promise<number> = api.visualPlans.batchCancel;
const _visualPlanBatchResultsClient: (
  id: string | number,
  slot?: string
) => Promise<VisualPlanBatchResults> = api.visualPlans.batchResults;
const _imageJobRetryClient: (id: string | number, payload?: ImageJobRetryRequest) => Promise<boolean> =
  api.imageJobs.retry;

const _imageJobRetryPayload: ImageJobRetryRequest = {
  retryReason: "retry one failed visual-plan slot"
};

const _visualPlanBatchStatusShape: VisualPlanBatchStatus = {
  visualPlanId: 1,
  totalJobs: 2,
  succeededJobs: 1,
  failedJobs: 1,
  pendingJobs: 0,
  runningJobs: 0,
  canceledJobs: 0,
  aggregatedStatus: "PARTIAL_SUCCEEDED",
  jobSummaries: [{ id: 1, taskName: "main", status: "SUCCEEDED", slot: "main", ratio: "1:1" }]
};

const _visualPlanBatchResultsShape: VisualPlanBatchResults = {
  visualPlanId: 1,
  slotFilter: "main",
  totalSlots: 1,
  slotGroups: {
    main: [
      {
        id: 1,
        taskName: "main image",
        status: "SUCCEEDED",
        slot: "main",
        results: [
          {
            id: 10,
            imageJobId: 1,
            resultUrl: "http://127.0.0.1:8188/view?filename=result.png",
            selected: false
          }
        ]
      }
    ]
  }
};

void _visualPlanDispatchClient;
void _visualPlanBatchStatusClient;
void _visualPlanBatchRetryClient;
void _visualPlanBatchCancelClient;
void _visualPlanBatchResultsClient;
void _visualPlanBatchStatusShape;
void _visualPlanBatchResultsShape;
void _imageJobRetryClient;
void _imageJobRetryPayload;

const _brandTemplateListQuery: BrandTemplateQuery = {
  pageNum: 1,
  pageSize: 10,
  brandId: 1001,
  brandName: "示例品牌",
  templateType: "STANDARD",
  enabled: true,
  keyword: "详情页"
};
const _brandTemplateListClient: (query?: BrandTemplateQuery) => Promise<PageResult<BrandTemplate>> =
  api.brandTemplates.list;
const _brandTemplateSummaryClient: (query?: BrandTemplateQuery) => Promise<BrandTemplateSummary> =
  api.brandTemplates.summary;
const _brandTemplateGetClient: (id: string | number) => Promise<BrandTemplate> = api.brandTemplates.get;
const _brandTemplateCreatePayload: BrandTemplateCreateRequest = {
  brandId: 1001,
  brandName: "示例品牌",
  templateName: "标准详情页模板",
  templateType: "STANDARD",
  templateContent: "{\"modules\":[\"商品介绍\",\"核心卖点\"]}",
  styleTags: "专业,清晰",
  styleDescription: "专业克制",
  applicableCategories: "电子产品,家居用品",
  enabled: true,
  creator: "admin",
  updater: "admin"
};
const _brandTemplateCreateClient: (payload: BrandTemplateCreateRequest) => Promise<BrandTemplate> =
  api.brandTemplates.create;
const _brandTemplateUpdateClient: (
  id: string | number,
  payload: Partial<BrandTemplateCreateRequest>
) => Promise<BrandTemplate> = api.brandTemplates.update;
const _brandTemplateDeleteClient: (id: string | number) => Promise<void> = api.brandTemplates.delete;
const _brandTemplateDuplicateClient: (id: string | number) => Promise<BrandTemplate> = api.brandTemplates.duplicate;
const _brandTemplateUseClient: (id: string | number) => Promise<void> = api.brandTemplates.use;
const _brandTemplateShape: BrandTemplate = {
  id: 1,
  brandId: 1001,
  brandName: "示例品牌",
  templateName: "标准详情页模板",
  templateType: "STANDARD",
  templateContent: "{\"modules\":[\"商品介绍\",\"核心卖点\"]}",
  styleTags: "专业,清晰",
  styleDescription: "专业克制",
  applicableCategories: "电子产品,家居用品",
  enabled: true,
  usageCount: 3,
  creator: "admin",
  updater: "admin",
  createTime: "2026-06-05T00:00:00",
  updateTime: "2026-06-05T00:00:00"
};
const _brandTemplateSummaryShape: BrandTemplateSummary = {
  total: 2,
  enabled: 1,
  disabled: 1,
  usageCount: 7
};

void _brandTemplateListQuery;
void _brandTemplateListClient;
void _brandTemplateSummaryClient;
void _brandTemplateGetClient;
void _brandTemplateCreatePayload;
void _brandTemplateCreateClient;
void _brandTemplateUpdateClient;
void _brandTemplateDeleteClient;
void _brandTemplateDuplicateClient;
void _brandTemplateUseClient;
void _brandTemplateShape;
void _brandTemplateSummaryShape;

const _productContentCreatePayload: ProductContentTaskCreateRequest = {
  productDetailId: 1,
  taskName: "content draft",
  inputData: {
    productName: "Demo product"
  }
};

const _productContentTaskCreateClient: (payload: ProductContentTaskCreateRequest) => Promise<ProductContentTask> =
  api.productContentTasks.create;
const _productContentTaskApplyClient: (
  id: string | number,
  payload: ProductContentTaskApplyRequest
) => Promise<ProductContentTask> = api.productContentTasks.apply;

const _productContentApplyPayload: ProductContentTaskApplyRequest = {
  fields: ["title", "subtitle", "sellingPoints", "seoKeywords", "aiGeneratedContent"]
};

const _productContentTaskShape: ProductContentTask = {
  id: 1,
  productDetailId: 1,
  status: "SUCCEEDED",
  version: 1,
  title: "Generated title",
  sellingPoints: [],
  detailModules: [],
  faq: [],
  seoKeywords: [],
  riskWarnings: [],
  sourceData: {
    sourceType: "AI_RELAY"
  }
};

void _productContentCreatePayload;
void _productContentTaskCreateClient;
void _productContentTaskApplyClient;
void _productContentApplyPayload;
void _productContentTaskShape;
