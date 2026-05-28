# P3 API Contract Addendum

Updated: 2026-05-27

This addendum documents the P3 generation-task and generation-result contracts that extend the existing `/api/v1` API surface.

## Visual planning persistence base

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/category-visual-policies` | List persisted category visual policies |
| `POST` | `/api/v1/category-visual-policies` | Create a category visual policy |
| `GET` | `/api/v1/model-profiles` | List persisted model profiles |
| `POST` | `/api/v1/model-profiles` | Create a model profile |
| `POST` | `/api/v1/visual-plans` | Create a visual plan draft |
| `GET` | `/api/v1/visual-plans/{id}` | Read one persisted visual plan |
| `POST` | `/api/v1/visual-plans/{id}/confirm` | Freeze and confirm one visual plan |

Notes:
- `GET /api/v1/category-visual-policies/list`, `GET /api/v1/model-profiles/list`, and `GET /api/v1/visual-plans/list` remain supported as list aliases.
- `visual_plan.confirmed_snapshot_json` is the persisted confirmed snapshot; there is no separate `visual_plan_snapshot` table in this slice.

Rules:
- A newly created `visual_plan` always persists as `DRAFT`.
- `visual_plan` create ignores caller-supplied terminal status or version overrides; persisted create-time version starts at `1`.
- Confirm changes a plan from `DRAFT` to `CONFIRMED`, writes `confirmed_time`, and freezes the current category policy, model profile, SKC policy, and referenced prompt-workbench entries into `confirmed_snapshot_json`.
- Repeating confirm on an already confirmed plan is idempotent and returns the stored confirmed state without reopening it.
- The frozen prompt snapshot includes the persisted prompt request snapshot, response snapshot, status, and `version` for downstream `/image-jobs` usage.

## Prompt workbench persistence base

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/prompt-workbench/guided` | Persist and execute one guided prompt request |
| `POST` | `/api/v1/prompt-workbench/expand` | Persist and execute one expand prompt request |
| `POST` | `/api/v1/prompt-workbench/image-to-prompt` | Persist and execute one image-to-prompt request |

Rules:
- Every prompt-workbench execution persists one real `prompt_workbench_entry` row before execution.
- Persisted fields include normalized `entryType`, effective `toolCode`, request snapshot (`input_json`), response snapshot (`output_json`/`output_text`), status, and `version`.
- Prompt-workbench entries now persist `version` starting at `1`.
- `output_text` stores the normalized primary text for downstream use; raw relay/tool payloads remain in `output_json.rawBody`.
- `output_json` now carries normalized `source` metadata and `riskWarnings` when the relay or tool response provides them.
- `CANCELED` means the relay/tool is unavailable or not configured; `FAILED` means execution started and then failed.

## Product content tasks (P3.8)

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/product-content-tasks` | List persisted product content tasks |
| `GET` | `/api/v1/product-content-tasks/list` | List alias for compatibility |
| `POST` | `/api/v1/product-content-tasks` | Create and execute one persisted product content task |
| `GET` | `/api/v1/product-content-tasks/{id}` | Read one persisted product content task |
| `POST` | `/api/v1/product-content-tasks/{id}/apply` | Apply selected task output fields to `product_detail` |

Create request shape:
- `productDetailId` (required)
- `materialId`, `brandTemplateId`, `visualPlanId`, `promptWorkbenchEntryId` (optional trace links)
- `taskName` (optional; defaults to `Product content task`)
- `toolCode` (optional; defaults to `ai-relay`)
- `inputData` (optional object; defaults to `{}`)

Status semantics:
- Create always persists a real `product_content_task` row first with `PENDING` and `version=1`.
- `SUCCEEDED`: relay returned valid structured JSON and the backend persisted real output snapshots.
- `CANCELED`: relay is unavailable or not configured (`UnsupportedOperationException` path).
- `FAILED`: relay execution started but failed, response is invalid JSON, or response body is empty.

Persisted output/DTO structure:
- `inputData`: decoded `input_json` snapshot.
- `outputText`: raw relay response text.
- `outputData`: normalized output snapshot with:
  - `toolCode`
  - `body` (parsed structured JSON payload)
  - `rawBody` (original response string)
  - `text` (same as response string)
  - `source` (includes `sourceType=AI_RELAY` and `toolCode`)
  - `riskWarnings` (normalized list)
- Flattened convenience fields returned from `outputData.body` where present: `title`, `subtitle`, `sellingPoints`, `detailModules`, `faq`, `seoKeywords`.
- Additional trace fields: `sourceData`, `appliedFields`, `appliedTime`, `errorMessage`, `createTime`, `updateTime`.

Apply field rules (`POST /product-content-tasks/{id}/apply`):
- Only `SUCCEEDED` tasks can be applied.
- `fields` is required and must not be empty.
- Supported values only: `title`, `subtitle`, `sellingPoints`, `seoKeywords`, `description`, `aiGeneratedContent`.
- Unknown fields are rejected with `unsupported apply field`.
- `title`/`subtitle`/`description` update only when the output value is non-blank.
- `sellingPoints` and `seoKeywords` are normalized to string arrays and persisted as JSON array strings in `product_detail`.
- `aiGeneratedContent` stores the full normalized output body JSON string in `product_detail.ai_generated_content`.
- Applied fields are deduplicated in request order, persisted to `applied_fields_json`, and `applied_time` is written.

No fake content rules:
- The backend never fabricates generated task output: if relay output is unavailable/invalid/empty, task ends in `CANCELED` or `FAILED` and output stays empty.
- The frontend (`frontend/src/pages/details/DetailEditorPage.tsx`) shows persisted task states and empty/error states directly; apply action is disabled unless selected task status is `SUCCEEDED`.

## Image jobs

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/image-jobs/list` | List persisted generation jobs |
| `POST` | `/api/v1/image-jobs` | Create a real generation job record |
| `GET` | `/api/v1/image-jobs/{id}` | Read one persisted generation job |
| `PUT` | `/api/v1/image-jobs/{id}/status` | Update persisted job status/progress |
| `POST` | `/api/v1/image-jobs/{id}/retry` | Retry a failed or canceled job |
| `POST` | `/api/v1/image-jobs/{id}/cancel` | Cancel a persisted job |

Rules:
- A create request always persists a real `image_job` row.
- If the configured tool is unavailable, the job is persisted as `CANCELED` with a clear reason.
- The backend does not fabricate output images, progress, or success state.
- The backend now polls running jobs on a fixed delay via `business.image-job.poll-fixed-delay-ms` (default `30000` ms).
- Polling reads real external history through the existing `ToolAdapter` boundary using the `history` operation.
- Tool unavailable during polling ends the job as `CANCELED`.
- Polling failure, malformed history payloads, or a terminal external response without real output ends the job as `FAILED`.
- A job is marked `SUCCEEDED` only after the backend has persisted at least one real `generation_result`.
- Terminal jobs (`SUCCEEDED`, `FAILED`, `CANCELED`) are not reopened by direct status update or cancel requests; only explicit retry can move `FAILED`/`CANCELED` back into execution.

## Generation results

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/generation-results/list` | List persisted generation results |
| `GET` | `/api/v1/generation-results/{id}` | Read one persisted generation result |
| `PUT` | `/api/v1/generation-results/{id}/selection` | Persist the selected/unselected state |

Rules:
- `generation_result` rows only represent real outputs.
- Empty result sets remain empty in responses and in the frontend.
- Compliance state is persisted only when the backend has a real value.
- Result backfill is idempotent on `(imageJobId, resultUrl)` and updates existing rows instead of duplicating them.
- Duplicate payload rows for the same `resultUrl` are merged without reopening terminal jobs or clearing existing selection state.
- ComfyUI-style image history (`filename`, `subfolder`, `type`) is converted into a real asset URL through the configured tool adapter base URL and `/view`.

## Frontend wiring

The frontend reads these contracts through:
- `frontend/src/services/api.ts`
- `frontend/src/services/types.ts`
- `frontend/src/pages/generate/GenerateWorkbenchPage.tsx`
- `frontend/src/pages/generate/GenerateTaskDetailPage.tsx`
- `frontend/src/pages/results/ResultsPreviewPage.tsx`

Frontend refresh rules:
- `/generate` refetches the job list while any persisted job is still non-terminal.
- `/generate/:taskId` polls the persisted job detail only while the job is non-terminal.
- `/results` refreshes persisted result rows on a fixed interval and still shows a real empty state when the backend has no rows.

## Visual plan batch dispatch (P3.9)

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/visual-plans/{id}/dispatch` | Create image jobs from one confirmed visual plan |
| `GET` | `/api/v1/visual-plans/{id}/batch-status` | Read persisted image-job aggregate status for a visual plan |
| `POST` | `/api/v1/visual-plans/{id}/batch-retry` | Retry failed/canceled image jobs for the visual plan |
| `POST` | `/api/v1/visual-plans/{id}/batch-cancel` | Cancel pending/running image jobs for the visual plan |
| `GET` | `/api/v1/visual-plans/{id}/batch-results` | Read image jobs grouped by slot; optional `slot` filter |

Dispatch rules:
- Only `CONFIRMED` visual plans can dispatch image jobs; `DRAFT` plans are rejected.
- Dispatch input is a non-empty array of real `ImageJobCreateDTO` entries supplied by the caller.
- Each created job is linked back to the plan through `visualPlanId`.
- The backend fills missing `modelProfileId` from the confirmed plan and freezes `sourceSnapshotJson` from `visual_plan.confirmed_snapshot_json`.
- Job-level traceability fields include `slot`, `ratio`, `promptVersion`, `modelProfileId`, and `sourceSnapshotJson`.
- No fake image result, fake progress, or synthetic success is written during dispatch.

Batch status rules:
- Aggregate statuses are `RUNNING`, `SUCCEEDED`, `PARTIAL_SUCCEEDED`, `FAILED`, and `CANCELED`.
- Presence of pending/running jobs keeps the aggregate status at `RUNNING`.
- `PARTIAL_SUCCEEDED` is reserved for terminal mixed outcomes where at least one job succeeded and at least one failed/canceled.
- Empty job sets return `CANCELED`, not fabricated progress.
- Retry and cancel delegate to the persisted `image_job` lifecycle rules; terminal rows are not reopened except through explicit retry of failed/canceled jobs.
- Visual-plan-linked running jobs are polled through the same fixed-delay backend coordinator, but through the batch path so affected plan IDs can be returned for aggregate status recomputation.
- Standalone image-job polling excludes visual-plan-linked rows to avoid double polling; batch polling remains the source for plan-scoped convergence.

Frontend wiring:
- `frontend/src/services/api.ts` exposes `visualPlans.dispatch`, `batchStatus`, `batchRetry`, `batchCancel`, and `batchResults`.
- `frontend/src/services/types.ts` defines `VisualPlanBatchStatus`, `VisualPlanBatchJobSummary`, and `VisualPlanBatchResults`.
- `frontend/src/pages/visual/VisualPlansPage.tsx` shows the batch panel only for confirmed plans, polls batch status while non-terminal, and requires caller-provided jobs JSON before dispatch.
- `VisualPlansPage` now shows persisted batch completion counts, failed/canceled counts, error messages, and per-job retry/cancel controls that call the real `image-jobs` lifecycle APIs.
- `VisualPlansPage` reads `/visual-plans/{id}/batch-results` with an optional slot filter and displays only persisted `generation_result` rows returned by the backend.
- `frontend/src/pages/generate/GenerateWorkbenchPage.tsx` can filter persisted image jobs by `visualPlanId` through `GET /api/v1/image-jobs/list`.
- `GET /api/v1/visual-plans/{id}/batch-results` returns job entries grouped by slot and includes a `results` array containing real persisted `generation_result` DTOs for each job.
- `frontend/src/pages/results/ResultsPreviewPage.tsx` filters by `visualPlanId` and optional `slot` through `/visual-plans/{id}/batch-results`, then flattens only the returned persisted `results` arrays.
- `sourceSnapshotJson` is typed as the backend-owned JSON string on the frontend; the UI does not parse or mutate the frozen snapshot during dispatch.
- `ImageJobRetryRequest` is typed to the backend retry contract (`retryReason`) instead of reusing create-job payloads.

## Detail compositions

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/detail-compositions/list` | List persisted detail-composition jobs |
| `POST` | `/api/v1/detail-compositions` | Create a real detail-composition job record |
| `GET` | `/api/v1/detail-compositions/{id}` | Read one persisted detail-composition job |
| `GET` | `/api/v1/detail-compositions/{id}/download` | Download the real stitched PNG output for a succeeded job |

Create request shape:
- `productDetailId` (required)
- `taskName` (optional)
- `toolCode` (optional, defaults to `imagemagick`)
- `detailData` (optional detail snapshot override)
- `moduleOrder` (optional explicit module order override)

Persisted response fields:
- Job state: `id`, `productDetailId`, `taskName`, `toolCode`, `status`, `progress`, `externalJobId`, `errorMessage`, `createTime`, `updateTime`
- Captured input: `inputData`, `moduleOrder`
- Real output metadata: `outputPath`, `outputFileName`, `outputFileSize`, `mimeType`

Rules:
- A create request always persists a real `detail_composition` row before any tool execution starts.
- The backend keeps detail composition separate from `export_record`; stitched long images are not backfilled into export formats.
- The backend invokes the existing `ToolAdapter` with tool code `imagemagick` and operation `compose` (and accepts the same contract shape for `stitch`).
- The ImageMagick payload is contract-checked before execution: `inputImages` must contain real local files only, `outputRatio` must match the supported ratio pattern, and `outputPath` must stay within the allowed local output roots.
- If the tool adapter is unavailable or not configured, the job converges to `CANCELED` with a real reason.
- If the adapter response is malformed, the output path is missing, the output is not a `.png`, or the output file does not exist on disk, the job converges to `FAILED`.
- A job converges to `SUCCEEDED` only after at least one real `detail_composition_result` row has been written.
- Result persistence is idempotent on `(detailCompositionId, outputPath)` and updates real file metadata instead of duplicating rows.
- Terminal jobs do not reopen during in-process execution retries.
- Download is allowed only for `SUCCEEDED` jobs with an existing real file on disk.
- Persisted result metadata includes `outputWidth`, `outputHeight`, and `outputFileSize` derived from the real image file.

Frontend wiring for this slice:
- `frontend/src/services/api.ts`
- `frontend/src/services/types.ts`
- `frontend/src/pages/details/DetailEditorPage.tsx`

Frontend behavior:
- The detail editor lists real composition jobs for the current detail record and polls only while any job remains non-terminal.
- Preview and download use the real `/detail-compositions/{id}/download` response and do not render fabricated output.
- Empty/error states remain empty/error states when the backend has no rows or returns a failure.

## Result application and delivery QA

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/detail/{id}/generation-results/apply` | Apply selected persisted generation results to the real detail record |
| `POST` | `/api/v1/detail-compositions/{id}/quality-checks` | Create a real visual QA job for a persisted detail-composition job |
| `GET` | `/api/v1/detail-compositions/{id}/quality-checks/list` | List persisted QA history for a detail-composition job |
| `GET` | `/api/v1/detail-compositions/{id}/delivery-manifest` | Read the persisted delivery manifest JSON for a composition |

Rules:
- Applying generation results only writes selected persisted `generation_result` rows into `product_detail.images`.
- Existing detail images are preserved and URLs are deduplicated before writeback.
- Blank selection, blank `resultUrl`, or non-persisted result IDs are rejected.
- QA jobs use the existing `playwright` tool adapter and still follow fail-closed terminal rules: unavailable -> `CANCELED`, malformed/failed execution -> `FAILED`, real QA success -> `SUCCEEDED`.
- Delivery manifests are persisted-only JSON snapshots; they are downloadable as JSON from the frontend and do not include fabricated rows or AI judgments.
