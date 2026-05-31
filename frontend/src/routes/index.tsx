import { Suspense, lazy, type ReactNode } from "react";
import { createBrowserRouter, type RouteObject, type RouterProviderProps } from "react-router-dom";
import { EmptyState, LoadingState } from "../components/common";
import WorkbenchLayout from "../layouts/WorkbenchLayout";
import HomeWorkbenchPage from "../pages/workbench/HomeWorkbenchPage";

const AssetLibraryPage = lazy(() => import("../pages/assets/AssetLibraryPage"));
const AuditCenterPage = lazy(() => import("../pages/audit/AuditCenterPage"));
const DetailReviewPlaceholder = lazy(() => import("../pages/detail-review"));
const DetailEditorPage = lazy(() => import("../pages/details/DetailEditorPage"));
const ExportRecordsPage = lazy(() => import("../pages/exports/ExportRecordsPage"));
const GenerateTaskDetailPage = lazy(() => import("../pages/generate/GenerateTaskDetailPage"));
const GenerateWorkbenchPage = lazy(() => import("../pages/generate/GenerateWorkbenchPage"));
const MaterialCreatePage = lazy(() => import("../pages/materials/MaterialCreatePage"));
const MaterialDetailPage = lazy(() => import("../pages/materials/MaterialDetailPage"));
const MaterialListPage = lazy(() => import("../pages/materials/MaterialListPage"));
const PostProcessTasksPage = lazy(() => import("../pages/post-process/PostProcessTasksPage"));
const CompetitorLibraryPage = lazy(() => import("../pages/research/CompetitorLibraryPage"));
const NewResearchTaskPage = lazy(() => import("../pages/research/NewResearchTaskPage"));
const ResearchCenterPage = lazy(() => import("../pages/research/ResearchCenterPage"));
const ResearchReportPage = lazy(() => import("../pages/research/ResearchReportPage"));
const ResearchTaskDetailPage = lazy(() => import("../pages/research/ResearchTaskDetailPage"));
const ResultsPreviewPage = lazy(() => import("../pages/results/ResultsPreviewPage"));
const DataImportPage = lazy(() => import("../pages/tools/DataImportPage"));
const DesignDraftPage = lazy(() => import("../pages/tools/DesignDraftPage"));
const ToolCenterPage = lazy(() => import("../pages/tools/ToolCenterPage"));
const ToolDetailPage = lazy(() => import("../pages/tools/ToolDetailPage"));
const CategoryVisualPoliciesPage = lazy(() => import("../pages/visual/CategoryVisualPoliciesPage"));
const ModelProfilesPage = lazy(() => import("../pages/visual/ModelProfilesPage"));
const PromptWorkbenchPage = lazy(() => import("../pages/visual/PromptWorkbenchPage"));
const PromptTemplatePage = lazy(() => import("../pages/visual/PromptTemplatePage"));
const VisualPlansPage = lazy(() => import("../pages/visual/VisualPlansPage"));
const HomePage = lazy(() => import("../pages/HomePage"));
const LoginPage = lazy(() => import("../pages/auth/LoginPage"));
const CostManagementPage = lazy(() => import("../pages/cost/CostManagementPage"));
const DiagnosticsPage = lazy(() => import("../pages/system/DiagnosticsPage"));
const TeamManagementPage = lazy(() => import("../pages/system/TeamManagementPage"));
const AuditLogPage = lazy(() => import("../pages/system/AuditLogPage"));

const lazyRouteElement = (element: ReactNode) => (
  <Suspense fallback={<LoadingState title="Loading" description="Loading page" compact />}>{element}</Suspense>
);

const routes: RouteObject[] = [
  {
    path: "/",
    element: lazyRouteElement(<HomePage />)
  },
  {
    path: "/login",
    element: lazyRouteElement(<LoginPage />)
  },
  {
    path: "/",
    element: <WorkbenchLayout />,
    children: [
      { path: "dashboard", element: <HomeWorkbenchPage /> },
      { path: "research", element: lazyRouteElement(<ResearchCenterPage />) },
      { path: "research/new", element: lazyRouteElement(<NewResearchTaskPage />) },
      { path: "research/tasks/:id", element: lazyRouteElement(<ResearchTaskDetailPage />) },
      { path: "research/competitors", element: lazyRouteElement(<CompetitorLibraryPage />) },
      { path: "research/reports/:id", element: lazyRouteElement(<ResearchReportPage />) },
      { path: "materials", element: lazyRouteElement(<MaterialListPage />) },
      { path: "materials/new", element: lazyRouteElement(<MaterialCreatePage />) },
      { path: "materials/:id", element: lazyRouteElement(<MaterialDetailPage />) },
      { path: "generate", element: lazyRouteElement(<GenerateWorkbenchPage />) },
      { path: "generate/:taskId", element: lazyRouteElement(<GenerateTaskDetailPage />) },
      { path: "assets", element: lazyRouteElement(<AssetLibraryPage />) },
      { path: "results", element: lazyRouteElement(<ResultsPreviewPage />) },
      { path: "details/:id", element: lazyRouteElement(<DetailEditorPage />) },
      { path: "details/:id/review", element: lazyRouteElement(<DetailReviewPlaceholder />) },
      { path: "audit", element: lazyRouteElement(<AuditCenterPage />) },
      { path: "exports", element: lazyRouteElement(<ExportRecordsPage />) },
      { path: "post-process", element: lazyRouteElement(<PostProcessTasksPage />) },
      { path: "tools", element: lazyRouteElement(<ToolCenterPage />) },
      { path: "tools/imports", element: lazyRouteElement(<DataImportPage />) },
      { path: "tools/design-draft", element: lazyRouteElement(<DesignDraftPage />) },
      { path: "tools/:toolCode", element: lazyRouteElement(<ToolDetailPage />) },
      { path: "visual/category-policies", element: lazyRouteElement(<CategoryVisualPoliciesPage />) },
      { path: "visual/model-profiles", element: lazyRouteElement(<ModelProfilesPage />) },
      { path: "visual/prompt-workbench", element: lazyRouteElement(<PromptWorkbenchPage />) },
      { path: "visual/prompt-templates", element: lazyRouteElement(<PromptTemplatePage />) },
      { path: "visual/plans", element: lazyRouteElement(<VisualPlansPage />) },
      { path: "cost", element: lazyRouteElement(<CostManagementPage />) },
      { path: "system/diagnostics", element: lazyRouteElement(<DiagnosticsPage />) },
      { path: "system/team", element: lazyRouteElement(<TeamManagementPage />) },
      { path: "system/audit-log", element: lazyRouteElement(<AuditLogPage />) },
      {
        path: "*",
        element: (
          <EmptyState
            title="Page not found"
            description="Navigate from the sidebar"
          />
        )
      }
    ]
  }
];

export const router: RouterProviderProps["router"] = createBrowserRouter(routes);
