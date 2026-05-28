import {
  AppstoreOutlined,
  AuditOutlined,
  DollarOutlined,
  ExportOutlined,
  FileImageOutlined,
  FileSearchOutlined,
  FileTextOutlined,
  FolderOpenOutlined,
  ProductOutlined,
  RadarChartOutlined,
  ScissorOutlined,
  SettingOutlined,
  TagsOutlined,
  ToolOutlined
} from "@ant-design/icons";
import type { MenuProps } from "antd";

export const navigationItems: MenuProps["items"] = [
  { key: "/", icon: <AppstoreOutlined />, label: "Home" },
  {
    key: "research-group",
    icon: <RadarChartOutlined />,
    label: "Research",
    children: [
      { key: "/research", label: "Research Center" },
      { key: "/research/new", label: "New Research" },
      { key: "/research/competitors", label: "Competitors" }
    ]
  },
  {
    key: "materials-group",
    icon: <ProductOutlined />,
    label: "Materials",
    children: [
      { key: "/materials", label: "Material List" },
      { key: "/materials/new", label: "New Material" }
    ]
  },
  { key: "/generate", icon: <FileImageOutlined />, label: "Generate" },
  { key: "/assets", icon: <FolderOpenOutlined />, label: "Assets" },
  { key: "/results", icon: <FileSearchOutlined />, label: "Results" },
  { key: "/details/1", icon: <FileTextOutlined />, label: "Detail Editor" },
  { key: "/audit", icon: <AuditOutlined />, label: "Audit" },
  { key: "/exports", icon: <ExportOutlined />, label: "Exports" },
  { key: "/post-process", icon: <ScissorOutlined />, label: "Post-Process" },
  {
    key: "visual-group",
    icon: <TagsOutlined />,
    label: "Visual Planning",
    children: [
      { key: "/visual/category-policies", label: "Category Policies" },
      { key: "/visual/model-profiles", label: "Model Profiles" },
      { key: "/visual/prompt-workbench", label: "Prompt Workbench" },
      { key: "/visual/plans", label: "Visual Plans" }
    ]
  },
  {
    key: "system-group",
    icon: <SettingOutlined />,
    label: "System",
    children: [
      { key: "/cost", label: "Cost Management" },
      { key: "/system/diagnostics", label: "Diagnostics" },
      { key: "/system/team", label: "Team" },
      { key: "/system/audit-log", label: "Audit Log" }
    ]
  },
  {
    key: "tools-group",
    icon: <ToolOutlined />,
    label: "Tools",
    children: [
      { key: "/tools", label: "Adapter Center" },
      { key: "/tools/imports", label: "Data Import" },
      { key: "/tools/design-draft", label: "Design Draft" }
    ]
  }
];

export const getSelectedKey = (pathname: string) => {
  if (pathname === "/") return "/";
  if (pathname.startsWith("/research/new")) return "/research/new";
  if (pathname.startsWith("/research/competitors")) return "/research/competitors";
  if (pathname.startsWith("/research")) return "/research";
  if (pathname.startsWith("/materials/new")) return "/materials/new";
  if (pathname.startsWith("/materials")) return "/materials";
  if (pathname.startsWith("/generate")) return "/generate";
  if (pathname.startsWith("/assets")) return "/assets";
  if (pathname.startsWith("/results")) return "/results";
  if (pathname.startsWith("/details")) return "/details/1";
  if (pathname.startsWith("/audit")) return "/audit";
  if (pathname.startsWith("/exports")) return "/exports";
  if (pathname.startsWith("/post-process")) return "/post-process";
  if (pathname.startsWith("/cost")) return "/cost";
  if (pathname.startsWith("/system/diagnostics")) return "/system/diagnostics";
  if (pathname.startsWith("/system/team")) return "/system/team";
  if (pathname.startsWith("/system/audit-log")) return "/system/audit-log";
  if (pathname.startsWith("/visual/category-policies")) return "/visual/category-policies";
  if (pathname.startsWith("/visual/model-profiles")) return "/visual/model-profiles";
  if (pathname.startsWith("/visual/prompt-workbench")) return "/visual/prompt-workbench";
  if (pathname.startsWith("/visual/plans")) return "/visual/plans";
  if (pathname.startsWith("/tools/imports")) return "/tools/imports";
  if (pathname.startsWith("/tools/design-draft")) return "/tools/design-draft";
  if (pathname.startsWith("/tools")) return "/tools";
  return "/";
};

export const defaultOpenKeys = ["research-group", "materials-group", "visual-group", "system-group", "tools-group"];