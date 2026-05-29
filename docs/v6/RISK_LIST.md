# v6.0 Risk List

> Generated: 2026-05-29 | Based on actual source code audit.

---

## HIGH RISK

### H1. Mojibake in 42+ files
- Description: Chinese text corrupted across frontend pages, backend Javadoc, config comments, layout footers
- Impact: Product unusable for Chinese users; all UI text garbled
- Scope: 16 frontend TSX files, 30+ backend Java files, application.yml comments, schema.sql comments
- Fix approach: Read each file with correct encoding (windows-1252 or GBK), replace garbled text with proper UTF-8, write back with UTF-8 no-BOM
- Risk: Each file edit could break compilation if encoding handled wrong
- Mitigation: Run `mvn test` and `npm test` after each file

### H2. No authentication/authorization
- Description: All 27 controllers have zero auth. Any HTTP client can read/write/delete all data.
- Impact: Complete data exposure. Cannot deploy to any non-local environment.
- Scope: All `/api/v1/*` endpoints
- Fix approach: Add Spring Security + JWT filter. Whitelist login endpoint, protect everything else.
- Risk: Adding security filter may break existing frontend API calls if token header not added
- Mitigation: Implement auth in stages: (1) add filter with permissive mode, (2) add frontend token, (3) enable enforcement

### H3. Hardcoded database credentials
- Description: `application.yml` line 14-15: `username: root`, `password: root`
- Impact: Credentials committed to Git. Same password for dev/prod.
- Scope: `src/main/resources/application.yml`
- Fix approach: Create `application-dev.yml` and `application-prod.yml`. Move credentials to env vars. Keep `application.yml` for shared config only.
- Risk: Must ensure test H2 database config is not affected
- Mitigation: Tests use separate H2 datasource; verify after change

---

## MEDIUM RISK

### M1. Light theme instead of requested dark theme
- Description: User explicitly requested dark theme with blue-purple tech aesthetic. Current theme is light (white bg, default blue).
- Impact: Visual style does not match product vision. Cannot demo to stakeholders.
- Scope: `frontend/src/styles/theme.css`, `frontend/src/styles/glass.css`, Ant Design ConfigProvider
- Fix approach: Rewrite CSS variables for dark scheme. Configure Ant Design `theme.algorithm = darkAlgorithm`.
- Risk: Dark theme may look broken on some pages with hardcoded colors
- Mitigation: Use Ant Design token system; avoid hardcoded hex colors

### M2. 13 pages use P0Scaffold placeholder component
- Description: P0Scaffold exposes internal development status (API availability, tool status) to end users
- Impact: Unprofessional appearance. Users see "pending" and "disabled" capability lists.
- Scope: 12 page files + P0Scaffold.tsx itself
- Fix approach: Replace with standard PageHeader component. Keep P0Scaffold for internal dev only.
- Risk: Each page replacement needs layout verification
- Mitigation: Replace one page at a time, verify visually

### M3. Code duplication (5 utility functions x 7+ locations)
- Description: `formatDateTime`, `normalizeStatus`, `safeJsonStringify`, `Notice` type, `getStatusMeta` duplicated across multiple pages
- Impact: Bug fixes require changing 7 files. Inconsistent behavior possible.
- Scope: GenerateWorkbenchPage, GenerateTaskDetailPage, DetailReview, ResultsPreviewPage, MaterialListPage, MaterialDetailPage, AuditCenterPage
- Fix approach: Extract to `frontend/src/utils/format.ts`, `frontend/src/utils/statusMeta.ts`
- Risk: Import path changes may cause merge conflicts if done in parallel with other changes
- Mitigation: Do extraction early in Phase 2, before other frontend changes

### M4. Hardcoded route `/details/1` in navigation
- Description: `navigation.tsx` line has `{ key: "/details/1", label: "Detail Editor" }`
- Impact: Navigation always goes to product ID 1 regardless of context
- Scope: `frontend/src/layouts/navigation.tsx`
- Fix approach: Change to `/details` with empty-state page, or remove ID from nav
- Risk: Low. Simple fix.
- Mitigation: Add `/details` index route that shows "select a product" message

### M5. Two-column layout instead of three-column
- Description: User requested three-column (left nav + center workspace + right AI assistant panel). Current is two-column (nav + content).
- Impact: No AI assistant panel visible. Layout does not match product vision.
- Scope: `frontend/src/layouts/AppLayout.tsx`, `frontend/src/layouts/SideNav.tsx`, `frontend/src/layouts/TopBar.tsx`
- Fix approach: Add collapsible right panel (320px) in AppLayout. Use Ant Design Layout.Sider for right panel.
- Risk: Right panel may squeeze content area on smaller screens
- Mitigation: Make right panel collapsible with toggle button. Use `minmax(0, 1fr)` for content area.

### M6. Footer exposes internal system status
- Description: AppLayout footer shows "系统状态：按真实接口返回展示", "AI 服务：待接入本地 AI 服务", "工具适配器：默认待配置/不可用"
- Impact: Users see internal system status that should be hidden
- Scope: `frontend/src/layouts/AppLayout.tsx` footer section
- Fix approach: Remove footer or replace with minimal version info only
- Risk: Low

---

## LOW RISK

### L1. Large bundle chunks
- Description: `excalidraw` (6.7MB), `echarts` (1.1MB), `index` (668KB) exceed Vite 500KB warning threshold
- Impact: Slow initial page load
- Scope: `frontend/src/routes/index.tsx` (lazy imports already used)
- Fix approach: Configure `manualChunks` in vite.config.ts to split vendor libraries
- Risk: Low. Build config change only.

### L2. Orphan page files not in routes
- Description: `ResultPreviewPage.tsx`, `ResearchReportsPage.tsx`, `ToolsCenterPage.tsx` exist but are not in routes
- Impact: Dead code. May confuse developers.
- Scope: 3 files in frontend/src/pages
- Fix approach: Either add routes or delete orphan files
- Risk: Low. No user-facing impact.

### L3. Mixed Chinese/English in navigation labels
- Description: Navigation uses English labels ("Research Center", "Material List") while page content is Chinese
- Impact: Inconsistent language experience
- Scope: `frontend/src/layouts/navigation.tsx`
- Fix approach: Unify to Chinese in Phase 3 after i18n decision
- Risk: Low

### L4. schema.sql comments have mojibake
- Description: Table/column COMMENT fields in schema.sql have garbled Chinese
- Impact: Database admin tools show garbled comments. Does not affect runtime.
- Scope: `src/main/resources/db/schema.sql`
- Fix approach: Rewrite comments in proper UTF-8
- Risk: Low. Comments only, no functional impact.

### L5. PDF export not implemented
- Description: `ExportRecordsPage.tsx` shows PDF as `available: false`
- Impact: Cannot export detail pages as PDF
- Scope: Frontend export page + backend ExportService
- Fix approach: Implement in Phase 5
- Risk: Low for Phase 1. Will need new backend dependency (itext/openhtmltopdf).

### L6. All 12 tool adapters disabled by default
- Description: Every tool in `application.yml` has `enabled: false`
- Impact: No external tool works without manual config
- Scope: `src/main/resources/application.yml` tools section
- Fix approach: Enable at least Real-ESRGAN in Phase 4 with docker-compose setup
- Risk: Low for Phase 1. Requires external service deployment.

---

## DO NOT MODIFY (until verified)

The following must NOT be changed until their dependencies are confirmed:

1. **`frontend/src/components/common/`** - Shared components used by 30+ pages. Changes may cascade.
2. **`frontend/src/stores/workbenchStore.ts`** - Zustand store used by multiple pages.
3. **`frontend/src/services/api.ts`** - Central API layer. Any change affects all pages.
4. **`frontend/src/services/types.ts`** - Shared TypeScript types. Type changes cascade.
5. **`src/.../service/impl/ToolAdapterServiceImpl.java`** - Central tool adapter. 12 tool definitions.
6. **`src/.../config/GlobalExceptionHandler.java`** - Exception handling. Wrong change breaks all error responses.
7. **`src/main/resources/db/schema.sql`** - DB schema. Wrong change may need data migration.
8. **`pom.xml`** - Dependency changes may break build or introduce conflicts.

---

## Phase 1 Execution Order (Recommended)

1. **Phase 1.1** - Fix mojibake (42+ files). Safest change: text-only, no logic.
2. **Phase 1.3** - DB credentials to env vars. Small blast radius.
3. **Phase 1.2** - Spring Security + JWT. Largest change. Do last.
