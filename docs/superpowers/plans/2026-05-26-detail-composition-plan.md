# Detail Composition Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a real detail composition job/result flow that stitches a long PNG from the current detail data, exposes it through the editor, and hardens export to reject fake outputs.

**Architecture:** Keep composition separate from export records. Add a dedicated `detail_composition` job table and a result table, then drive the stitching through the existing `imagemagick` ToolAdapter boundary with fail-closed terminal states. The editor page creates and polls jobs, shows real error/output state, and downloads only files that exist. Export remains a real-file-only path and continues to reject PDF.

**Tech Stack:** Spring Boot, MyBatis-Plus, Jackson, React, TanStack Query, existing ToolAdapter, ImageMagick CLI/service boundary.

---

### Task 1: Define detail composition persistence and API

**Files:**
- Create: `src/main/java/com/ecommerce/detail/ai/entity/DetailComposition.java`
- Create: `src/main/java/com/ecommerce/detail/ai/entity/DetailCompositionResult.java`
- Create: `src/main/java/com/ecommerce/detail/ai/mapper/DetailCompositionMapper.java`
- Create: `src/main/java/com/ecommerce/detail/ai/mapper/DetailCompositionResultMapper.java`
- Create: `src/main/java/com/ecommerce/detail/ai/dto/DetailCompositionDTO.java`
- Create: `src/main/java/com/ecommerce/detail/ai/dto/DetailCompositionCreateDTO.java`
- Create: `src/main/java/com/ecommerce/detail/ai/dto/DetailCompositionStatusDTO.java`
- Create: `src/main/java/com/ecommerce/detail/ai/dto/DetailCompositionListQuery.java`
- Create: `src/main/java/com/ecommerce/detail/ai/service/DetailCompositionService.java`
- Create: `src/main/java/com/ecommerce/detail/ai/service/impl/DetailCompositionServiceImpl.java`
- Create: `src/main/java/com/ecommerce/detail/ai/controller/DetailCompositionController.java`
- Modify: `src/main/resources/db/schema.sql`
- Modify: `src/test/java/com/ecommerce/detail/ai/database/SchemaAlignmentTest.java`
- Modify: `src/test/java/com/ecommerce/detail/ai/controller/ControllerContractExposureTest.java`
- Modify: `src/test/java/com/ecommerce/detail/ai/controller/ControllerMappingTest.java`
- Create: `src/test/java/com/ecommerce/detail/ai/service/impl/DetailCompositionServiceImplTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void assertDetailCompositionRoutesAreExposedWithoutApiPrefix() {
    assertControllerBasePath(DetailCompositionController.class, "/detail-compositions");
    assertRoute(DetailCompositionController.class, "listDetailCompositions", RequestMethod.GET, "/list");
    assertRoute(DetailCompositionController.class, "createDetailComposition", RequestMethod.POST, "");
    assertRoute(DetailCompositionController.class, "getDetailCompositionById", RequestMethod.GET, "/{id}");
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `& "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd" test -Dtest=ControllerContractExposureTest,ControllerMappingTest,SchemaAlignmentTest,DetailCompositionServiceImplTest`
Expected: FAIL because the detail-composition classes and schema entries do not exist yet.

- [ ] **Step 3: Write minimal implementation**

```java
// Create the entity, mapper, DTOs, service, and controller.
// Persist JSON fields as strings, normalize status to PENDING/RUNNING/SUCCEEDED/FAILED/CANCELED,
// and keep list/get/create with no fake output fields.
```

- [ ] **Step 4: Run test to verify it passes**

Run: `& "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd" test -Dtest=ControllerContractExposureTest,ControllerMappingTest,SchemaAlignmentTest,DetailCompositionServiceImplTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/db/schema.sql src/main/java/com/ecommerce/detail/ai/{controller,dto,entity,mapper,service,service/impl}/DetailComposition* src/test/java/com/ecommerce/detail/ai/{controller,database,service/impl}/DetailComposition*
git commit -m "feat: add detail composition persistence"
```

### Task 2: Implement composition stitching and fail-closed job resolution

**Files:**
- Modify: `src/main/java/com/ecommerce/detail/ai/service/ToolAdapterService.java`
- Modify: `src/main/java/com/ecommerce/detail/ai/service/impl/ToolAdapterServiceImpl.java`
- Create: `src/main/java/com/ecommerce/detail/ai/service/impl/DetailCompositionJobCoordinator.java`
- Modify: `src/main/java/com/ecommerce/detail/ai/EcommerceDetailAiApplication.java`
- Modify: `src/main/resources/application.yml`
- Create: `src/test/java/com/ecommerce/detail/ai/service/impl/DetailCompositionJobCoordinatorTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void marksCompositionFailedWhenImageMagickIsUnavailable() {
    // set up a queued detail composition with imagemagick disabled
    // expect FAILED and a concrete error message
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `& "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd" test -Dtest=DetailCompositionJobCoordinatorTest`
Expected: FAIL because the coordinator and adapter flow do not exist yet.

- [ ] **Step 3: Write minimal implementation**

```java
// Add a scheduled coordinator that:
// - loads PENDING/RUNNING composition jobs,
// - invokes ToolAdapterService with imagemagick compose/stitch,
// - verifies the output file exists,
// - writes only real terminal states,
// - never reopens terminal jobs.
```

- [ ] **Step 4: Run test to verify it passes**

Run: `& "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd" test -Dtest=DetailCompositionJobCoordinatorTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/ecommerce/detail/ai/{EcommerceDetailAiApplication.java,service/ToolAdapterService.java,service/impl/ToolAdapterServiceImpl.java,service/impl/DetailCompositionJobCoordinator.java} src/main/resources/application.yml src/test/java/com/ecommerce/detail/ai/service/impl/DetailCompositionJobCoordinatorTest.java
git commit -m "feat: add detail composition stitching"
```

### Task 3: Wire the detail editor to real composition state

**Files:**
- Modify: `frontend/src/services/types.ts`
- Modify: `frontend/src/services/api.ts`
- Modify: `frontend/src/pages/details/DetailEditorPage.tsx`
- Modify: `frontend/src/routes/index.tsx` only if route imports need lazy loading for the editor shell
- Create: `frontend/src/pages/details/DetailCompositionPreview.tsx` if the editor needs a focused preview component
- Create: `frontend/src/pages/details/DetailCompositionDownload.tsx` if download handling needs isolation

- [ ] **Step 1: Write the failing test**

```ts
// Add a route-splitting or API-contract regression check that expects the new composition API
// and the editor page to call it, while still rendering honest empty/error states.
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd frontend; npm test`
Expected: FAIL because the new composition API/types and editor wiring do not exist yet.

- [ ] **Step 3: Write minimal implementation**

```tsx
// Add composition create/get/list client calls, editor submission from current detail/module order,
// status polling, error display, output preview, and download link.
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd frontend; npm test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/services/{api.ts,types.ts} frontend/src/pages/details/DetailEditorPage.tsx frontend/src/pages/details/DetailCompositionPreview.tsx frontend/src/pages/details/DetailCompositionDownload.tsx
git commit -m "feat: wire detail editor composition flow"
```

### Task 4: Harden export and update docs

**Files:**
- Modify: `src/main/java/com/ecommerce/detail/ai/service/impl/ExportServiceImpl.java`
- Modify: `src/main/java/com/ecommerce/detail/ai/util/ExportUtil.java`
- Modify: `src/test/java/com/ecommerce/detail/ai/service/impl/ExportServiceImplTest.java`
- Modify: `src/test/java/com/ecommerce/detail/ai/util/ExportUtilTest.java`
- Modify: `docs/FRONTEND_UI_REQUIREMENTS.md`
- Modify: `docs/FRONTEND_UI_REQUIREMENTS_NEW.md`
- Modify: `docs/P3_API_CONTRACT_ADDENDUM.md` or create a new composition addendum if needed
- Modify: `HANDOFF.md`
- Modify: `HANDOFF_LOG.md`

- [ ] **Step 1: Write the failing test**

```java
@Test
void exportRejectsPdfAndMissingFiles() {
    // assert PDF still throws UnsupportedOperationException
    // assert download fails when file path is absent or file missing
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `& "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd" test -Dtest=ExportServiceImplTest,ExportUtilTest`
Expected: FAIL until hardening is in place.

- [ ] **Step 3: Write minimal implementation**

```java
// Keep export real-file only, reject PDF clearly, and avoid adding a fake composition export type.
```

- [ ] **Step 4: Run test to verify it passes**

Run: `& "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd" test -Dtest=ExportServiceImplTest,ExportUtilTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/ecommerce/detail/ai/{service/impl/ExportServiceImpl.java,util/ExportUtil.java} src/test/java/com/ecommerce/detail/ai/{service/impl/ExportServiceImplTest.java,util/ExportUtilTest.java} docs/*.md HANDOFF.md HANDOFF_LOG.md
git commit -m "feat: harden detail composition and export"
```

### Task 5: Full verification

**Files:**
- No new files expected

- [ ] **Step 1: Run backend tests**

Run: `& "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd" test`
Expected: PASS.

- [ ] **Step 2: Run frontend tests**

Run: `cd frontend; npm test`
Expected: PASS.

- [ ] **Step 3: Run frontend build**

Run: `cd frontend; npm run build`
Expected: PASS.

- [ ] **Step 4: Update handoff**

```text
Append the exact verification result and remaining non-blockers to HANDOFF.md and HANDOFF_LOG.md.
```

