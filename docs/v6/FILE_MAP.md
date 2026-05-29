# v6.0 File Map

> Generated: 2026-05-29 | All paths verified by actual file system scan.
> Paths are relative to project root: `C:\Users\Administrator\IdeaProjects\E-commerce detail creation`

---

## Frontend Page Files

| Module | File Path | Status | Risk |
|--------|-----------|--------|------|
| Home | `frontend/src/pages/workbench/HomeWorkbenchPage.tsx` | Mojibake | P0 |
| Research Center | `frontend/src/pages/research/ResearchCenterPage.tsx` | Uses P0Scaffold | P1 |
| New Research | `frontend/src/pages/research/NewResearchTaskPage.tsx` | Uses P0Scaffold | P1 |
| Research Task Detail | `frontend/src/pages/research/ResearchTaskDetailPage.tsx` | Uses P0Scaffold | P1 |
| Research Report | `frontend/src/pages/research/ResearchReportPage.tsx` | EMPTY placeholder | P1 |
| Research Reports | `frontend/src/pages/research/ResearchReportsPage.tsx` | Exists, not in routes | Low |
| Competitor Library | `frontend/src/pages/research/CompetitorLibraryPage.tsx` | Uses P0Scaffold | P1 |
| Material List | `frontend/src/pages/materials/MaterialListPage.tsx` | Unicode escapes, mojibake | P0 |
| Material Create | `frontend/src/pages/materials/MaterialCreatePage.tsx` | Mojibake | P0 |
| Material Detail | `frontend/src/pages/materials/MaterialDetailPage.tsx` | Mojibake | P0 |
| Generate Workbench | `frontend/src/pages/generate/GenerateWorkbenchPage.tsx` | Mojibake + P0Scaffold | P0 |
| Generate Task Detail | `frontend/src/pages/generate/GenerateTaskDetailPage.tsx` | Mojibake + P0Scaffold | P0 |
| Asset Library | `frontend/src/pages/assets/AssetLibraryPage.tsx` | Mojibake + P0Scaffold | P0 |
| Assets Library (re-export) | `frontend/src/pages/assets/AssetsLibraryPage.tsx` | Re-export only | Low |
| Results Preview | `frontend/src/pages/results/ResultsPreviewPage.tsx` | Mojibake + P0Scaffold | P0 |
| Result Preview | `frontend/src/pages/results/ResultPreviewPage.tsx` | Exists, not in routes | Low |
| Detail Editor | `frontend/src/pages/details/DetailEditorPage.tsx` | Mojibake | P0 |
| Detail Review | `frontend/src/pages/detail-review/index.tsx` | Mojibake | P0 |
| Audit Center | `frontend/src/pages/audit/AuditCenterPage.tsx` | Mojibake | P0 |
| Export Records | `frontend/src/pages/exports/ExportRecordsPage.tsx` | Mojibake | P0 |
| Post-Process Tasks | `frontend/src/pages/post-process/PostProcessTasksPage.tsx` | Mojibake | P0 |
| Tool Center | `frontend/src/pages/tools/ToolCenterPage.tsx` | Mojibake | P0 |
| Tool Detail | `frontend/src/pages/tools/ToolDetailPage.tsx` | Mojibake + P0Scaffold | P0 |
| Data Import | `frontend/src/pages/tools/DataImportPage.tsx` | Mojibake + P0Scaffold | P0 |
| Design Draft | `frontend/src/pages/tools/DesignDraftPage.tsx` | Mojibake + P0Scaffold | P0 |
| Tools Center (alt) | `frontend/src/pages/tools/ToolsCenterPage.tsx` | Exists, not in routes | Low |
| Category Policies | `frontend/src/pages/visual/CategoryVisualPoliciesPage.tsx` | OK | Low |
| Model Profiles | `frontend/src/pages/visual/ModelProfilesPage.tsx` | OK | Low |
| Prompt Workbench | `frontend/src/pages/visual/PromptWorkbenchPage.tsx` | Mojibake | P0 |
| Prompt Templates | `frontend/src/pages/visual/PromptTemplatePage.tsx` | OK (new) | Low |
| Visual Plans | `frontend/src/pages/visual/VisualPlansPage.tsx` | OK | Low |
| Cost Management | `frontend/src/pages/cost/CostManagementPage.tsx` | OK | Low |
| Diagnostics | `frontend/src/pages/system/DiagnosticsPage.tsx` | OK | Low |
| Team Management | `frontend/src/pages/system/TeamManagementPage.tsx` | OK | Low |
| Audit Log | `frontend/src/pages/system/AuditLogPage.tsx` | OK | Low |
| P0Scaffold (shared) | `frontend/src/pages/p0/P0Scaffold.tsx` | Placeholder component | P1 |

---

## Route and Navigation Files

| Module | File Path | Status | Risk |
|--------|-----------|--------|------|
| Routes | `frontend/src/routes/index.tsx` | 31 routes, hardcoded /details/1 not present here (in nav only) | P1 |
| Navigation | `frontend/src/layouts/navigation.tsx` | Hardcoded `/details/1`, mixed CN/EN labels | P1 |
| getSelectedKey | `frontend/src/layouts/navigation.tsx` (function) | 25 if-else branches, works | Low |

---

## Theme and Layout Files

| Module | File Path | Status | Risk |
|--------|-----------|--------|------|
| Theme CSS | `frontend/src/styles/theme.css` | Light theme, default blue | P1 |
| Glass CSS | `frontend/src/styles/glass.css` | Glass morphism effects | P1 |
| App Layout | `frontend/src/layouts/AppLayout.tsx` | 2-column (nav+content), footer has mojibake | P0 |
| Workbench Layout | `frontend/src/layouts/WorkbenchLayout.tsx` | Re-export of AppLayout | Low |
| Side Nav | `frontend/src/layouts/SideNav.tsx` | Ant Design Menu, works | Low |
| Top Bar | `frontend/src/layouts/TopBar.tsx` | Basic top bar | P1 |

---

## Backend Service Files (Controllers)

| Module | File Path | Status | Risk |
|--------|-----------|--------|------|
| Audit | `src/.../controller/AuditController.java` | Mojibake comments | P1 |
| Export | `src/.../controller/ExportController.java` | Mojibake comments | P1 |
| Product Detail | `src/.../controller/ProductDetailController.java` | Mojibake comments | P1 |
| Product Material | `src/.../controller/ProductMaterialController.java` | Mojibake comments | P1 |
| Prompt Template | `src/.../controller/PromptTemplateController.java` | OK (new) | Low |
| Prompt Workbench | `src/.../controller/PromptWorkbenchController.java` | OK | Low |
| (22 others) | Various controllers | OK | Low |

---

## Backend Config Files

| Module | File Path | Status | Risk |
|--------|-----------|--------|------|
| Main Config | `src/main/resources/application.yml` | DB root/root hardcoded, mojibake comments | P0 |
| application-dev.yml | Does NOT exist | Need to create | P0 |
| application-prod.yml | Does NOT exist | Need to create | P0 |
| Schema | `src/main/resources/db/schema.sql` | 31 tables, mojibake comments | P1 |
| pom.xml | `pom.xml` | Has mojibake in some plugin configs | P1 |

---

## Backend Security Files

| Module | File Path | Status | Risk |
|--------|-----------|--------|------|
| SecurityUtil | `src/.../util/SecurityUtil.java` | Path scrubbing only, NO auth | P0 |
| GlobalExceptionHandler | `src/.../config/GlobalExceptionHandler.java` | Works, mojibake comments | P1 |
| WebConfig | `src/.../config/WebConfig.java` | CORS config, mojibake | P1 |
| Spring Security | Does NOT exist | Need to create | P0 |
| JWT Util | Does NOT exist | Need to create | P0 |
| Auth Controller | Does NOT exist | Need to create | P0 |
| User Account Entity | Does NOT exist | Need to create | P0 |

---

## Backend AI/Tool Files

| Module | File Path | Status | Risk |
|--------|-----------|--------|------|
| AIUtil | `src/.../util/AIUtil.java` | OpenAI relay, disabled by default | P1 |
| ToolAdapterService | `src/.../service/impl/ToolAdapterServiceImpl.java` | 12 adapters defined, all disabled | P1 |
| ToolAdapterController | `src/.../controller/ToolAdapterController.java` | Lists tools | Low |
| PromptWorkbenchService | `src/.../service/impl/PromptWorkbenchServiceImpl.java` | Guided/Expand/ImageToPrompt | Low |
| PostProcessTaskService | `src/.../service/impl/PostProcessTaskServiceImpl.java` | CRUD + tool invoke | Low |
| PostProcessPolling | `src/.../coordinator/PostProcessPollingCoordinator.java` | Async polling | Low |
| ImageJobPolling | `src/.../coordinator/ImageJobPollingCoordinator.java` | Async polling | Low |

---

## P0Scaffold Usage Map

| Page | File Path | Replace Priority |
|------|-----------|-----------------|
| Asset Library | `frontend/src/pages/assets/AssetLibraryPage.tsx` | P1 |
| Generate Workbench | `frontend/src/pages/generate/GenerateWorkbenchPage.tsx` | P1 |
| Generate Task Detail | `frontend/src/pages/generate/GenerateTaskDetailPage.tsx` | P1 |
| Results Preview | `frontend/src/pages/results/ResultsPreviewPage.tsx` | P1 |
| Research Center | `frontend/src/pages/research/ResearchCenterPage.tsx` | P1 |
| New Research Task | `frontend/src/pages/research/NewResearchTaskPage.tsx` | P1 |
| Research Report | `frontend/src/pages/research/ResearchReportPage.tsx` | P1 |
| Competitor Library | `frontend/src/pages/research/CompetitorLibraryPage.tsx` | P1 |
| Research Task Detail | `frontend/src/pages/research/ResearchTaskDetailPage.tsx` | P1 |
| Data Import | `frontend/src/pages/tools/DataImportPage.tsx` | P1 |
| Design Draft | `frontend/src/pages/tools/DesignDraftPage.tsx` | P1 |
| Tool Detail | `frontend/src/pages/tools/ToolDetailPage.tsx` | P1 |

---

## Duplicate/Orphan Files

| File | Status | Action |
|------|--------|--------|
| `frontend/src/pages/assets/AssetsLibraryPage.tsx` | Re-export of AssetLibraryPage | Keep (convenience alias) |
| `frontend/src/pages/results/ResultPreviewPage.tsx` | Not in routes | Investigate |
| `frontend/src/pages/research/ResearchReportsPage.tsx` | Not in routes | Investigate |
| `frontend/src/pages/tools/ToolsCenterPage.tsx` | Not in routes | Investigate |
