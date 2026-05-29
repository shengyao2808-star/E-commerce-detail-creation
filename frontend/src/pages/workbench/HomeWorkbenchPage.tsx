import {
  AuditOutlined, ExportOutlined, FileImageOutlined, FileTextOutlined,
  FolderOpenOutlined, ProductOutlined, RadarChartOutlined,
  TagsOutlined, ToolOutlined
} from "@ant-design/icons";
import { Button, Progress } from "antd";
import { useNavigate } from "react-router-dom";
import { useLang } from "../../i18n";

const statusColor: Record<string, string> = {
  running: "var(--color-info)",
  done: "var(--color-success)",
  pending: "var(--color-warning)"
};

const statusBg: Record<string, string> = {
  running: "var(--color-info-light)",
  done: "var(--color-success-light)",
  pending: "var(--color-warning-light)"
};

export default function HomeWorkbenchPage() {
  const navigate = useNavigate();
  const { t } = useLang();

  const stats = [
    { label: t("stat.activeProjects"), value: "12", change: "+3", up: true },
    { label: t("stat.pendingAudit"), value: "8", change: "-2", up: false },
    { label: t("stat.exports"), value: "156", change: "+23", up: true },
    { label: t("stat.cost"), value: "$247", change: "+$31", up: true },
    { label: t("stat.materials"), value: "1,247", change: "+89", up: true }
  ];

  const projects = [
    { name: "Summer Dress Collection", platform: "Taobao", stage: t("status.running"), progress: 72, updated: "10 min ago", status: "running" as const },
    { name: "Kitchen Tools Set", platform: "JD", stage: t("status.done"), progress: 100, updated: "2 hrs ago", status: "done" as const },
    { name: "Sports Equipment", platform: "PDD", stage: t("status.running"), progress: 45, updated: "30 min ago", status: "running" as const },
    { name: "Beauty Products Line", platform: "Douyin", stage: t("status.running"), progress: 88, updated: "1 hr ago", status: "running" as const }
  ];

  const tasks = [
    { name: "Summer Dress - Main Visual", type: "Image Gen", status: "running" as const },
    { name: "Kitchen Tools - Copywriting", type: "Content", status: "done" as const },
    { name: "Sports Gear - Compliance Check", type: "Audit", status: "pending" as const },
    { name: "Beauty Line - Background Removal", type: "Post-Process", status: "running" as const },
    { name: "Kitchen Tools - PDF Export", type: "Export", status: "done" as const }
  ];

  const quickEntries = [
    { icon: <ProductOutlined />, title: t("action.newProject"), desc: t("action.newProject.desc"), to: "/materials/new" },
    { icon: <RadarChartOutlined />, title: t("action.research"), desc: t("action.research.desc"), to: "/research" },
    { icon: <FileImageOutlined />, title: t("action.generate"), desc: t("action.generate.desc"), to: "/generate" },
    { icon: <FileTextOutlined />, title: t("action.detail"), desc: t("action.detail.desc"), to: "/details/1" },
    { icon: <TagsOutlined />, title: t("action.prompt"), desc: t("action.prompt.desc"), to: "/visual/prompt-workbench" },
    { icon: <FolderOpenOutlined />, title: t("action.template"), desc: t("action.template.desc"), to: "/visual/prompt-templates" },
    { icon: <ExportOutlined />, title: t("action.export"), desc: t("action.export.desc"), to: "/exports" },
    { icon: <ToolOutlined />, title: t("action.tools"), desc: t("action.tools.desc"), to: "/tools" }
  ];

  const systemStatus = [
    { name: "AI Relay", status: "offline" as const },
    { name: "Image Gen", status: "offline" as const },
    { name: "File Storage", status: "online" as const },
    { name: "Export Service", status: "online" as const },
    { name: "Real-ESRGAN", status: "offline" as const },
    { name: "LLaVA", status: "offline" as const }
  ];

  return (
    <>
      <div className="df-page-header">
        <h1 className="df-page-title">{t("dashboard.title")}</h1>
        <div className="df-page-desc">{t("dashboard.desc")}</div>
      </div>

      <div className="df-stats">
        {stats.map((s) => (
          <div className="df-stat" key={s.label}>
            <div className="df-stat-label">{s.label}</div>
            <div className="df-stat-value">{s.value}</div>
            <div className={`df-stat-change ${s.up ? "up" : "down"}`}>
              {s.up ? "↑" : "↓"} {s.change}
            </div>
          </div>
        ))}
      </div>

      <div className="df-grid-2">
        <div className="df-card">
          <div className="df-card-header">
            <span className="df-card-title">{t("section.projects")}</span>
            <Button type="link" size="small" onClick={() => navigate("/visual/plans")}>{t("section.viewAll")}</Button>
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            {projects.map((p) => (
              <div
                key={p.name}
                style={{
                  display: "grid", gridTemplateColumns: "1fr 70px 90px 80px 60px",
                  alignItems: "center", gap: 8, padding: "10px 12px",
                  background: "var(--bg-muted)", borderRadius: "var(--radius-sm)", fontSize: 13
                }}
              >
                <div style={{ minWidth: 0 }}>
                  <div style={{ fontWeight: 500, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{p.name}</div>
                  <div style={{ fontSize: 11, color: "var(--text-muted)", marginTop: 2 }}>{p.platform} · {p.updated}</div>
                </div>
                <div style={{ fontSize: 12, color: "var(--text-secondary)" }}>{p.stage}</div>
                <Progress percent={p.progress} size="small" strokeColor={p.progress === 100 ? "var(--color-success)" : "var(--color-primary)"} />
                <div className="df-task-status" style={{ background: statusBg[p.status], color: statusColor[p.status], textAlign: "center" }}>
                  {t(`status.${p.status}`)}
                </div>
                <Button type="link" size="small" style={{ padding: 0 }}>→</Button>
              </div>
            ))}
          </div>
        </div>

        <div className="df-card">
          <div className="df-card-header">
            <span className="df-card-title">{t("section.tasks")}</span>
            <Button type="link" size="small" onClick={() => navigate("/generate")}>{t("section.viewAll")}</Button>
          </div>
          <div className="df-task-list">
            {tasks.map((task, i) => (
              <div className="df-task-item" key={i}>
                <div className="df-task-dot" style={{ background: statusColor[task.status] }} />
                <span className="df-task-name">{task.name}</span>
                <span style={{ fontSize: 11, color: "var(--text-muted)", flexShrink: 0 }}>{task.type}</span>
                <span className="df-task-status" style={{ background: statusBg[task.status], color: statusColor[task.status] }}>
                  {t(`status.${task.status}`)}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="df-grid-2">
        <div className="df-card">
          <div className="df-card-header"><span className="df-card-title">{t("section.actions")}</span></div>
          <div className="df-entry-grid">
            {quickEntries.map((e) => (
              <div className="df-entry" key={e.to} onClick={() => navigate(e.to)}>
                <div className="df-entry-icon">{e.icon}</div>
                <div className="df-entry-title">{e.title}</div>
                <div className="df-entry-desc">{e.desc}</div>
              </div>
            ))}
          </div>
        </div>

        <div className="df-card">
          <div className="df-card-header">
            <span className="df-card-title">{t("section.system")}</span>
            <Button type="link" size="small" onClick={() => navigate("/system/diagnostics")}>{t("diag.goto")}</Button>
          </div>
          <div className="df-system-grid">
            {systemStatus.map((s) => (
              <div className="df-system-item" key={s.name}>
                <div className={`df-system-dot ${s.status}`} />
                <span style={{ flex: 1 }}>{s.name}</span>
                <span style={{ fontSize: 11, color: s.status === "online" ? "var(--color-success)" : "var(--text-muted)" }}>
                  {s.status === "online" ? "Online" : "Offline"}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  );
}