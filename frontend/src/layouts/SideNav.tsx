import { useMemo } from "react";
import { useLocation, useNavigate } from "react-router-dom";
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

type NavItem = {
  key: string;
  label: string;
  icon: React.ReactNode;
  matchPrefix?: string;
};

type NavGroup = {
  label: string;
  items: NavItem[];
};

const navGroups: NavGroup[] = [
  {
    label: "Overview",
    items: [
      { key: "/", label: "Dashboard", icon: <AppstoreOutlined /> },
      { key: "/research", label: "Market Research", icon: <RadarChartOutlined />, matchPrefix: "/research" }
    ]
  },
  {
    label: "Production",
    items: [
      { key: "/materials", label: "Materials", icon: <ProductOutlined />, matchPrefix: "/materials" },
      { key: "/generate", label: "AI Generate", icon: <FileImageOutlined />, matchPrefix: "/generate" },
      { key: "/assets", label: "Asset Library", icon: <FolderOpenOutlined /> },
      { key: "/results", label: "Results", icon: <FileSearchOutlined /> },
      { key: "/details/1", label: "Detail Editor", icon: <FileTextOutlined />, matchPrefix: "/details" },
      { key: "/audit", label: "Audit", icon: <AuditOutlined /> },
      { key: "/exports", label: "Exports", icon: <ExportOutlined /> },
      { key: "/post-process", label: "Post-Process", icon: <ScissorOutlined /> }
    ]
  },
  {
    label: "Visual",
    items: [
      { key: "/visual/prompt-workbench", label: "Prompt Lab", icon: <TagsOutlined />, matchPrefix: "/visual/prompt" },
      { key: "/visual/prompt-templates", label: "Templates", icon: <TagsOutlined /> },
      { key: "/visual/plans", label: "Visual Plans", icon: <TagsOutlined />, matchPrefix: "/visual/plans" }
    ]
  },
  {
    label: "System",
    items: [
      { key: "/cost", label: "Cost", icon: <DollarOutlined /> },
      { key: "/system/diagnostics", label: "Diagnostics", icon: <SettingOutlined />, matchPrefix: "/system" },
      { key: "/tools", label: "Tools", icon: <ToolOutlined />, matchPrefix: "/tools" }
    ]
  }
];

function isActive(pathname: string, item: NavItem): boolean {
  const prefix = item.matchPrefix ?? item.key;
  if (prefix === "/") return pathname === "/";
  return pathname.startsWith(prefix);
}

export const SideNav = () => {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <nav className="df-sidebar">
      {navGroups.map((group) => (
        <div className="df-nav-group" key={group.label}>
          <div className="df-nav-label">{group.label}</div>
          {group.items.map((item) => (
            <div
              key={item.key}
              className={`df-nav-item${isActive(location.pathname, item) ? " active" : ""}`}
              onClick={() => navigate(item.key)}
            >
              <span className="df-nav-icon">{item.icon}</span>
              {item.label}
            </div>
          ))}
        </div>
      ))}
    </nav>
  );
};