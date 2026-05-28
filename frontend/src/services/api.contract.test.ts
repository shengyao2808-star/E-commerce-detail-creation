import { api, buildVisualPlanCreatePayload } from "./api";
import type {
  ApplyGenerationResultsRequest,
  DetailComposition,
  DetailCompositionQualityCheck,
  DetailDeliveryManifest,
  PageResult,
  ProductContentTask,
  ProductContentTaskApplyRequest,
  ProductContentTaskCreateRequest,
  ImageJobCreateRequest,
  ImageJobRetryRequest,
  VisualPlanBatchResults,
  VisualPlanBatchStatus,
  VisualPlanCreateRequest
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

const _visualPlanCreatePayload: VisualPlanCreateRequest = {
  productDetailId: 1,
  taskName: "Visual plan draft",
  promptContext: {
    heroImages: 5,
    detailScreens: 8
  }
};

const _visualPlanCreateClient: (payload: VisualPlanCreateRequest) => Promise<number | string> =
  api.visualPlans.create;
const _mappedVisualPlanCreatePayload = buildVisualPlanCreatePayload(_visualPlanCreatePayload);
const _mappedVisualPlanInputData: Record<string, unknown> = _mappedVisualPlanCreatePayload.inputData;
const _mappedVisualPlanPlanData: Record<string, unknown> = _mappedVisualPlanCreatePayload.planData;


void _visualPlanCreatePayload;
void _visualPlanCreateClient;
void _mappedVisualPlanCreatePayload;
void _mappedVisualPlanInputData;
void _mappedVisualPlanPlanData;

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
