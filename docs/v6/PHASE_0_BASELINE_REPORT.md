# v6.0 Phase 0 Baseline Report

> Generated: 2026-05-29 | Branch: master | HEAD: 4852004
> Phase 0 scope: READ-ONLY audit. No business code modified.

---

## 1. Git Status

- Current branch: `master`
- Uncommitted changes: 2 files (HANDOFF.md, HANDOFF_LOG.md - minor updates, non-blocking)
- Untracked files: None
- Recent commits:
  - `4852004` docs: add v5.0->v6.0 improvement plan based on full codebase audit
  - `fb66e16` feat: Prompt Template Library (backend + frontend + tests)
  - `ce65f19` feat: add homepage style mockup
  - `fee9963` docs: update handoff log and project metadata
  - `ffea714` docs: add API contracts, requirements, tool library docs, and handoff tools
- Recommendation: Create `feature/v6-hardening-and-ui` branch before Phase 1

---

## 2. Baseline Test Results

| Check | Result | Details |
|-------|--------|---------|
| `mvn test` | PASS | 152 tests, 0 failures, 0 errors |
| `npm test` (tsc) | PASS | TypeScript compiles clean, visual-plan contract check passed |
| `npm run build` | PASS (with warnings) | Built in 19.72s |

Build warnings (non-blocking):
- `excalidraw` chunk: 6,715 kB
- `echarts` chunk: 1,128 kB
- `index` chunk: 668 kB
- `graph-vendor` chunk: 503 kB

---

## 3. Project Scale

| Dimension | Count |
|-----------|-------|
| Backend Java files | 230 |
| Frontend TSX files | 58 |
| Controllers | 27 |
| Service Implementations | 27 |
| DB Tables (schema.sql) | 31 |
| Frontend Routes | 31 |
| Navigation Items | 23 |
| CSS Files | 2 (theme.css, glass.css) |

---

## 4. Issue Inventory

### 4.1 Mojibake (Chinese Encoding Corruption) [P0]

**Status: WIDESPREAD - affects 42+ files**

Frontend files with mojibake (12 pages + 2 layout files):
- `HomeWorkbenchPage.tsx` - quick entry titles, descriptions
- `ToolCenterPage.tsx` - tool state messages
- `AuditCenterPage.tsx` - status labels, risk labels, filter messages
- `PromptWorkbenchPage.tsx` - form labels, result panel messages
- `GenerateWorkbenchPage.tsx` - status labels, progress messages
- `GenerateTaskDetailPage.tsx` - status labels, detail fields
- `MaterialListPage.tsx` - uses Unicode escape sequences (\\uXXXX)
- `MaterialDetailPage.tsx` - material field labels
- `MaterialCreatePage.tsx` - form labels
- `ResultsPreviewPage.tsx` - status labels, empty states
- `AssetLibraryPage.tsx` - OCR messages
- `DetailEditorPage.tsx` - module names, field labels
- `ExportRecordsPage.tsx` - export status labels
- `PostProcessTasksPage.tsx` - task status labels
- `AppLayout.tsx` - footer text
- `TopBar.tsx` - status messages

Backend Java files with mojibake (30+ files):
- `Result.java` - Javadoc comments
- `PageResult.java` - Javadoc comments
- `WebConfig.java` - Javadoc comments
- `GlobalExceptionHandler.java` - log messages
- `ResourceNotFoundException.java` - Javadoc
- `FileOperationException.java` - Javadoc
- `PermissionDeniedException.java` - Javadoc
- `AuditServiceImpl.java` - log messages
- `ExportUtil.java` - Javadoc, log messages
- `FileUtil.java` - log messages
- `SecurityUtil.java` - separator comments
- Plus 20+ more entity/service/controller files with corrupted Javadoc

### 4.2 Security [P0]

- Spring Security: NOT present
- JWT: NOT implemented
- Login/Auth endpoint: NONE
- User account table: NONE (team_user exists but no auth credentials)
- CORS: `WebConfig.java` exists but only sets permissive CORS
- DB credentials: `root/root` hardcoded in `application.yml` (line 14-15)
- API keys: All use env vars via `${}` syntax (GOOD)

### 4.3 Database Credentials [P0]

- `username: root` hardcoded (line 14)
- `password: root` hardcoded (line 15)
- No `application-dev.yml` or `application-prod.yml` exists
- Only one `application.yml` for all environments

### 4.4 UI/Theme Issues [P1]

- Current theme: LIGHT (white background) - user requested DARK theme
- `theme.css`: `color-scheme: light`, `--color-primary: #1890ff` (Ant Design default blue)
- No three-column layout (left nav + center + right AI panel)
- Current layout: SideNav (left) + Content (center) only
- Footer exposes internal system status to users

### 4.5 P0Scaffold Placeholder Pages [P1]

13 pages import and use P0Scaffold, showing development status to users:
- AssetLibraryPage, GenerateWorkbenchPage, GenerateTaskDetailPage
- ResultsPreviewPage, ResearchCenterPage, NewResearchTaskPage
- ResearchReportPage (completely empty), CompetitorLibraryPage
- ResearchTaskDetailPage, DataImportPage, DesignDraftPage
- ToolDetailPage

### 4.6 Hardcoded Routes [P1]

- Navigation: `{ key: "/details/1", label: "Detail Editor" }` - hardcoded ID=1
- No `/details` index route to select a product first

### 4.7 Code Duplication [P1]

- `formatDateTime()`: duplicated in 7 files
- `normalizeStatus()`: duplicated in 3 files
- `safeJsonStringify()`: duplicated in 2 files
- `type Notice`: duplicated in 3 files
- `getStatusMeta()`: duplicated in 3 files

---

## 5. Phase 1 Readiness

### Conclusion: APPROVED to proceed to Phase 1

### Prerequisites met:
- [x] Baseline tests pass (mvn + tsc + build)
- [x] Git working tree clean (except HANDOFF files)
- [x] All files located and mapped
- [x] Risk list documented

### High-risk reminders for Phase 1:
- Mojibake repair touches 42+ files - must run tests after each file
- Security auth must not break existing 27 controllers
- DB credential change must not break test H2 database

### Recommended Phase 1 order:
1. Phase 1.1: Fix mojibake (frontend first, then backend)
2. Phase 1.3: DB credentials to env vars (smaller blast radius)
3. Phase 1.2: Spring Security + JWT (largest change, do last)
