# P2.5 Cleanup + P3 Generation/Results Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Clean the current P2 baseline, then add real image-job and generation-result persistence/API wiring without fabricating outputs.

**Architecture:** P2.5 is a cleanup pass that keeps the current verified baseline stable: fix visible mojibake, keep heavy frontend libraries route-loaded, and keep build artifacts out of functional commits. P3 introduces two separate backend aggregates, `image_job` for task state and `generation_result` for real output state, with the frontend reading only persisted state and using React Query for polling.

**Tech Stack:** Spring Boot, MyBatis-Plus, Jackson, ToolAdapterService, React, React Query, TypeScript, Vite, ComfyUI (external service through ToolAdapter only)

---

### Task 1: P2.5 baseline cleanup

**Files:**
- Modify: `frontend/src/pages/generate/GenerateWorkbenchPage.tsx`
- Modify: `frontend/src/pages/generate/GenerateTaskDetailPage.tsx`
- Modify: `frontend/src/pages/results/ResultsPreviewPage.tsx`
- Modify: `frontend/src/pages/workbench/HomeWorkbenchPage.tsx`
- Modify: `frontend/src/pages/tools/ToolCenterPage.tsx`
- Modify: `frontend/src/pages/tools/ToolDetailPage.tsx`
- Modify: `frontend/src/routes/index.tsx`
- Modify: `frontend/vite.config.ts`
- Modify: `frontend/src/components/common/StatusTag.tsx`
- Modify: `frontend/src/components/common/RiskTag.tsx`
- Modify: `docs/FRONTEND_UI_REQUIREMENTS.md`
- Modify: `docs/TOOL_LIBRARY.md`
- Modify: `.gitignore` if needed for generated outputs

- [ ] **Step 1: Confirm the baseline is still green**

```bash
cd frontend
npm test
npm run build
```

- [ ] **Step 2: Clean visible mojibake without changing behavior**

Keep current honest empty/pending states. Rewrite only user-facing copy that still renders as garbled text. Do not introduce fake jobs, fake generation results, or fake compliance text.

- [ ] **Step 3: Keep heavy client libraries route-loaded**

Keep `DesignDraftPage` lazy-loaded and keep Vite `manualChunks` entries for `@excalidraw/excalidraw` and `pdfjs-dist`. If the build still warns, stop after confirming the warning is non-blocking and does not affect route behavior.

- [ ] **Step 4: Keep generated output out of the functional commit surface**

Make sure `exports/`, `logs/`, and build outputs stay untracked or ignored. Do not delete user data unexpectedly.

- [ ] **Step 5: Re-run frontend verification**

```bash
cd frontend
npm test
npm run build
```

Expected: both pass; build may still warn about large chunks.

### Task 2: Backend `image_job` persistence and controller

**Files:**
- Create: `src/main/java/com/ecommerce/detail/ai/entity/ImageJob.java`
- Create: `src/main/java/com/ecommerce/detail/ai/dto/ImageJobDTO.java`
- Create: `src/main/java/com/ecommerce/detail/ai/dto/ImageJobCreateDTO.java`
- Create: `src/main/java/com/ecommerce/detail/ai/dto/ImageJobStatusDTO.java`
- Create: `src/main/java/com/ecommerce/detail/ai/dto/ImageJobRetryDTO.java`
- Create: `src/main/java/com/ecommerce/detail/ai/mapper/ImageJobMapper.java`
- Create: `src/main/java/com/ecommerce/detail/ai/service/ImageJobService.java`
- Create: `src/main/java/com/ecommerce/detail/ai/service/impl/ImageJobServiceImpl.java`
- Create: `src/main/java/com/ecommerce/detail/ai/controller/ImageJobController.java`
- Modify: `src/main/resources/db/schema.sql`
- Modify: `src/main/java/com/ecommerce/detail/ai/service/impl/ToolAdapterServiceImpl.java` only if a helper is needed for ComfyUI response parsing
- Create: `src/test/java/com/ecommerce/detail/ai/service/impl/ImageJobServiceImplTest.java`
- Create: `src/test/java/com/ecommerce/detail/ai/controller/ImageJobControllerContractTest.java`
- Modify: `src/test/java/com/ecommerce/detail/ai/database/SchemaAlignmentTest.java`

- [ ] **Step 1: Write the failing contract and schema tests**

```java
assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS image_job"));
assertTrue(schema.contains("external_job_id"));
assertTrue(schema.contains("status VARCHAR"));
assertTrue(schema.contains("progress INT"));
assertControllerBasePath(ImageJobController.class, "/image-jobs");
```

- [ ] **Step 2: Verify tests fail before implementation**

Run:

```bash
& 'C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd' test -Dtest=ImageJobServiceImplTest,ImageJobControllerContractTest,SchemaAlignmentTest
```

Expected: fail because classes/table are missing.

- [ ] **Step 3: Implement `image_job` persistence**

Store JSON payloads as strings through Jackson. Required fields: `taskName`, `toolCode`, `inputJson`, `status`, `progress`, `externalJobId`, `errorMessage`, `createTime`, `updateTime`.

- [ ] **Step 4: Implement create/list/get/retry/cancel/status update**

Required routes:

```text
POST /api/v1/image-jobs
GET /api/v1/image-jobs/{id}
GET /api/v1/image-jobs/list
POST /api/v1/image-jobs/{id}/retry
POST /api/v1/image-jobs/{id}/cancel
```

Behavior:
- If `comfyui` is not configured, create the record and mark it `CANCELED` with a clear reason.
- If configured, call `ToolAdapter` and persist the returned external job id.
- Never fabricate a finished image or a fake progress bar.

- [ ] **Step 5: Re-run backend tests**

```bash
& 'C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd' test
```

Expected: new image-job tests pass and existing controller/schema tests remain green.

### Task 3: Backend `generation_result` persistence and controller

**Files:**
- Create: `src/main/java/com/ecommerce/detail/ai/entity/GenerationResult.java`
- Create: `src/main/java/com/ecommerce/detail/ai/dto/GenerationResultDTO.java`
- Create: `src/main/java/com/ecommerce/detail/ai/dto/GenerationResultListQuery.java`
- Create: `src/main/java/com/ecommerce/detail/ai/dto/GenerationResultSelectionDTO.java`
- Create: `src/main/java/com/ecommerce/detail/ai/mapper/GenerationResultMapper.java`
- Create: `src/main/java/com/ecommerce/detail/ai/service/GenerationResultService.java`
- Create: `src/main/java/com/ecommerce/detail/ai/service/impl/GenerationResultServiceImpl.java`
- Create: `src/main/java/com/ecommerce/detail/ai/controller/GenerationResultController.java`
- Modify: `src/main/resources/db/schema.sql`
- Create: `src/test/java/com/ecommerce/detail/ai/service/impl/GenerationResultServiceImplTest.java`
- Create: `src/test/java/com/ecommerce/detail/ai/controller/GenerationResultControllerContractTest.java`
- Modify: `src/test/java/com/ecommerce/detail/ai/database/SchemaAlignmentTest.java`

- [ ] **Step 1: Add failing tests for storage and selection**

```java
assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS generation_result"));
assertTrue(schema.contains("image_job_id"));
assertTrue(schema.contains("result_url"));
assertTrue(schema.contains("compliance_status"));
```

- [ ] **Step 2: Implement the result model**

Persist only real output data:
- `imageJobId`
- `resultUrl`
- `thumbnailUrl`
- `prompt`
- `paramsJson`
- `complianceStatus`
- `selected`
- timestamps

- [ ] **Step 3: Implement list/get/selection endpoints**

Required routes:

```text
GET /api/v1/generation-results/list
GET /api/v1/generation-results/{id}
PUT /api/v1/generation-results/{id}/selection
```

No fake output, no fake compliance results.

- [ ] **Step 4: Re-run backend tests**

```bash
& 'C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd' test
```

Expected: all backend tests green.

### Task 4: Frontend generation/results wiring

**Files:**
- Modify: `frontend/src/services/types.ts`
- Modify: `frontend/src/services/api.ts`
- Modify: `frontend/src/pages/generate/GenerateWorkbenchPage.tsx`
- Modify: `frontend/src/pages/generate/GenerateTaskDetailPage.tsx`
- Modify: `frontend/src/pages/results/ResultsPreviewPage.tsx`
- Modify: `frontend/src/lib/queryClient.ts` only if polling defaults need a tiny helper

- [ ] **Step 1: Extend the typed clients**

Add typed clients for:
- `imageJobs.list/create/get/retry/cancel`
- `generationResults.list/get/updateSelection`

- [ ] **Step 2: Wire `/generate`**

The page should:
- submit real image-job creation requests when the form is enabled
- show honest empty/error states when no backend data exists
- read `image_jobs` status from the API, not local placeholders

- [ ] **Step 3: Wire `/generate/:taskId`**

The detail page should:
- read one image-job record
- poll status with React Query
- render real persisted progress/error/job id
- avoid fake progress and avoid fake final images

- [ ] **Step 4: Wire `/results`**

The results page should:
- read real generation results
- show empty state when the list is empty
- show selected/unselected state only from persisted data

- [ ] **Step 5: Re-run frontend tests**

```bash
cd frontend
npm test
npm run build
```

Expected: pass, with any remaining chunk warning treated as non-blocking.

### Task 5: Final verification and handoff

**Files:**
- Modify: `HANDOFF.md`
- Modify: `HANDOFF_LOG.md`

- [ ] **Step 1: Run the full verification set**

```bash
& 'C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd' test
cd frontend
npm test
npm run build
```

- [ ] **Step 2: Update handoff**

Record:
- what shipped
- exact next step
- blockers, if any
- files touched
- verification results

- [ ] **Step 3: Append handoff log**

Append a new entry only; do not rewrite history.
