import {
  createBrowserRouter,
  Navigate,
  type RouteObject,
  type RouterProviderProps
} from "react-router-dom";
import WorkbenchLayout from "../layouts/WorkbenchLayout";
import { EmptyState } from "../components/common";
import AuditCenterPage from "../pages/audit/AuditCenterPage";
import DetailEditorPage from "../pages/details/DetailEditorPage";
import ExportRecordsPage from "../pages/exports/ExportRecordsPage";
import MaterialListPage from "../pages/materials/MaterialListPage";
import DetailReviewPlaceholder from "../pages/detail-review";
import MaterialDetailPage from "../pages/materials/MaterialDetailPage";

const routes: RouteObject[] = [
  {
    path: "/",
    element: <WorkbenchLayout />,
    children: [
      { index: true, element: <Navigate to="/materials" replace /> },
      { path: "materials", element: <MaterialListPage /> },
      { path: "materials/:id", element: <MaterialDetailPage /> },
      { path: "details/:id", element: <DetailEditorPage /> },
      { path: "details/:id/review", element: <DetailReviewPlaceholder /> },
      { path: "audit", element: <AuditCenterPage /> },
      { path: "exports", element: <ExportRecordsPage /> },
      {
        path: "*",
        element: (
          <EmptyState
            title="页面不存在"
            description="请从左侧导航进入已配置的工作台路由。"
          />
        )
      }
    ]
  }
];

export const router: RouterProviderProps["router"] = createBrowserRouter(routes);
