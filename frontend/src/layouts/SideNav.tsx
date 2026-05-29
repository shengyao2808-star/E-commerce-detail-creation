import { useLocation, useNavigate } from "react-router-dom";
import {
  AppstoreOutlined, AuditOutlined, DollarOutlined, ExportOutlined,
  FileImageOutlined, FileSearchOutlined, FileTextOutlined, FolderOpenOutlined,
  ProductOutlined, RadarChartOutlined, ScissorOutlined, SettingOutlined,
  TagsOutlined, ToolOutlined
} from "@ant-design/icons";
import { useLang } from "../i18n";

type NavItem = { key: string; labelKey: string; icon: React.ReactNode; matchPrefix?: string };
type NavGroup = { labelKey: string; items: NavItem[] };

const navGroups: NavGroup[] = [
  {
    labelKey: "nav.overview",
    items: [
      { key: "/", labelKey: "nav.dashboard", icon: <AppstoreOutlined /> },
      { key: "/research", labelKey: "nav.research", icon: <RadarChartOutlined />, matchPrefix: "/research" }
    ]
  },
  {
    labelKey: "nav.production",
    items: [
      { key: "/materials", labelKey: "nav.materials", icon: <ProductOutlined />, matchPrefix: "/materials" },
      { key: "/generate", labelKey: "nav.generate", icon: <FileImageOutlined />, matchPrefix: "/generate" },
      { key: "/assets", labelKey: "nav.assets", icon: <FolderOpenOutlined /> },
      { key: "/results", labelKey: "nav.results", icon: <FileSearchOutlined /> },
      { key: "/details/1", labelKey: "nav.detail", icon: <FileTextOutlined />, matchPrefix: "/details" },
      { key: "/audit", labelKey: "nav.audit", icon: <AuditOutlined /> },
      { key: "/exports", labelKey: "nav.exports", icon: <ExportOutlined /> },
      { key: "/post-process", labelKey: "nav.postprocess", icon: <ScissorOutlined /> }
    ]
  },
  {
    labelKey: "nav.visual",
    items: [
      { key: "/visual/prompt-workbench", labelKey: "nav.promptlab", icon: <TagsOutlined />, matchPrefix: "/visual/prompt" },
      { key: "/visual/prompt-templates", labelKey: "nav.templates", icon: <TagsOutlined /> },
      { key: "/visual/plans", labelKey: "nav.plans", icon: <TagsOutlined />, matchPrefix: "/visual/plans" }
    ]
  },
  {
    labelKey: "nav.system",
    items: [
      { key: "/cost", labelKey: "nav.cost", icon: <DollarOutlined /> },
      { key: "/system/diagnostics", labelKey: "nav.diagnostics", icon: <SettingOutlined />, matchPrefix: "/system" },
      { key: "/tools", labelKey: "nav.tools", icon: <ToolOutlined />, matchPrefix: "/tools" }
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
  const { t } = useLang();

  return (
    <nav className="df-sidebar">
      {navGroups.map((group) => (
        <div className="df-nav-group" key={group.labelKey}>
          <div className="df-nav-label">{t(group.labelKey)}</div>
          {group.items.map((item) => (
            <div
              key={item.key}
              className={`df-nav-item${isActive(location.pathname, item) ? " active" : ""}`}
              onClick={() => navigate(item.key)}
            >
              <span className="df-nav-icon">{item.icon}</span>
              {t(item.labelKey)}
            </div>
          ))}
        </div>
      ))}
    </nav>
  );
};