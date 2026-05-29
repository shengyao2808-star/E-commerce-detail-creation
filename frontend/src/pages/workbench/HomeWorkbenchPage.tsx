import {
  AuditOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloudUploadOutlined,
  DollarOutlined,
  ExportOutlined,
  FileImageOutlined,
  FileTextOutlined,
  FolderOpenOutlined,
  ProductOutlined,
  RadarChartOutlined,
  ScissorOutlined,
  TagsOutlined,
  ToolOutlined,
  WarningOutlined
} from "@ant-design/icons";
import { Button, Progress, Space, Typography } from "antd";
import { useNavigate } from "react-router-dom";

const { Text } = Typography;

const stats = [
  { label: "Active Projects", value: "12", change: "+3", up: true },
  { label: "Pending Audit", value: "8", change: "-2", up: false },
  { label: "Exports (Month)", value: "156", change: "+23", up: true },
  { label: "Cost (Month)", value: "$247", change: "+$31", up: true },
  { label: "Materials", value: "1,247", change: "+89", up: true }
];

const projects = [
  { name: "Summer Dress Collection", platform: "Taobao", stage: "AI Generate", progress: 72, updated: "10 min ago", status: "running" as const },
  { name: "Kitchen Tools Set", platform: "JD", stage: "Export", progress: 100, updated: "2 hrs ago", status: "done" as const },
  { name: "Sports Equipment", platform: "PDD", stage: "Audit", progress: 45, updated: "30 min ago", status: "running" as const },
  { name: "Beauty Products Line", platform: "Douyin", stage: "Detail Edit", progress: 88, updated: "1 hr ago", status: "running" as const }
];

const tasks = [
  { name: "Summer Dress - Main Visual", type: "Image Gen", status: "running" as const },
  { name: "Kitchen Tools - Copywriting", type: "Content", status: "done" as const },
  { name: "Sports Gear - Compliance Check", type: "Audit", status: "pending" as const },
  { name: "Beauty Line - Background Removal", type: "Post-Process", status: "running" as const },
  { name: "Kitchen Tools - PDF Export", type: "Export", status: "done" as const }
];

const quickEntries = [
  { icon: <ProductOutlined />, title: "New Project", desc: "Start from material upload", to: "/materials/new" },
  { icon: <RadarChartOutlined />, title: "Research", desc: "Market & competitor scan", to: "/research" },
  { icon: <FileImageOutlined />, title: "AI Generate", desc: "Visual generation workbench", to: "/generate" },
  { icon: <FileTextOutlined />, title: "Detail Editor", desc: "Edit product detail pages", to: "/details/1" },
  { icon: <TagsOutlined />, title: "Prompt Lab", desc: "Create & manage prompts", to: "/visual/prompt-workbench" },
  { icon: <FolderOpenOutlined />, title: "Templates", desc: "Prompt template library", to: "/visual/prompt-templates" },
  { icon: <ExportOutlined />, title: "Export", desc: "Export & deliver files", to: "/exports" },
  { icon: <ToolOutlined />, title: "Tools", desc: "Tool adapter settings", to: "/tools" }
];

const systemStatus = [
  { name: "AI Relay", status: "offline" as const },
  { name: "Image Gen", status: "offline" as const },
  { name: "File Storage", status: "online" as const },
  { name: "Export Service", status: "online" as const },
  { name: "Real-ESRGAN", status: "offline" as const },
  { name: "LLaVA", status: "offline" as const }
];

const statusLabel: Record<string, string> = {
  running: "Running",
  done: "Done",
  pending: "Queued"
};

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

  return (
    <>
      {/* Page Header */}
      <div className="df-page-header">
        <h1 className="df-page-title">Production Dashboard</h1>
        <div className="df-page-desc">Overview of your e-commerce detail page production pipeline</div>
      </div>

      {/* Stats */}
      <div className="df-stats">
        {stats.map((s) => (
          <div className="df-stat" key={s.label}>
            <div className="df-stat-label">{s.label}</div>
            <div className="df-stat-value">{s.value}</div>
            <div className={`df-stat-change ${s.up ? "up" : "down"}`}>
              {s.up ? "↑" : "↓"} {s.change} this period
            </div>
          </div>
        ))}
      </div>

      {/* Two-column: Projects + Tasks */}
      <div className="df-grid-2">
        {/* Projects */}
        <div className="df-card">
          <div className="df-card-header">
            <span className="df-card-title">Recent Projects</span>
            <Button type="link" size="small" onClick={() => navigate("/visual/plans")}>View All</Button>
          </div>
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            {projects.map((p) => (
              <div
                key={p.name}
                style={{
                  display: "grid",
                  gridTemplateColumns: "1fr 80px 90px 100px 70px",
                  alignItems: "center",
                  gap: 8,
                  padding: "10px 12px",
                  background: "var(--bg-muted)",
                  borderRadius: "var(--radius-sm)",
                  fontSize: 13
                }}
              >
                <div style={{ minWidth: 0 }}>
                  <div style={{ fontWeight: 500, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{p.name}</div>
                  <div style={{ fontSize: 11, color: "var(--text-muted)", marginTop: 2 }}>{p.platform} · {p.updated}</div>
                </div>
                <div style={{ fontSize: 12, color: "var(--text-secondary)" }}>{p.stage}</div>
                <div>
                  <Progress
                    percent={p.progress}
                    size="small"
                    strokeColor={p.progress === 100 ? "var(--color-success)" : "var(--color-primary)"}
                    style={{ marginBottom: 0 }}
                  />
                </div>
                <div
                  className="df-task-status"
                  style={{ background: statusBg[p.status], color: statusColor[p.status], textAlign: "center" }}
                >
                  {statusLabel[p.status]}
                </div>
                <Button type="link" size="small" style={{ padding: 0 }}>Open</Button>
              </div>
            ))}
          </div>
        </div>

        {/* Tasks */}
        <div className="df-card">
          <div className="df-card-header">
            <span className="df-card-title">Recent Tasks</span>
            <Button type="link" size="small" onClick={() => navigate("/generate")}>View All</Button>
          </div>
          <div className="df-task-list">
            {tasks.map((t, i) => (
              <div className="df-task-item" key={i}>
                <div className="df-task-dot" style={{ background: statusColor[t.status] }} />
                <span className="df-task-name">{t.name}</span>
                <span style={{ fontSize: 11, color: "var(--text-muted)", flexShrink: 0 }}>{t.type}</span>
                <span
                  className="df-task-status"
                  style={{ background: statusBg[t.status], color: statusColor[t.status] }}
                >
                  {statusLabel[t.status]}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Quick Entry + System Status */}
      <div className="df-grid-2">
        {/* Quick Entry */}
        <div className="df-card">
          <div className="df-card-header">
            <span className="df-card-title">Quick Actions</span>
          </div>
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

        {/* System Status */}
        <div className="df-card">
          <div className="df-card-header">
            <span className="df-card-title">System Status</span>
            <Button type="link" size="small" onClick={() => navigate("/system/diagnostics")}>Diagnostics</Button>
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