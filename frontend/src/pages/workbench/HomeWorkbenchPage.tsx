import {
  AppstoreOutlined, AuditOutlined, DollarOutlined, ExportOutlined,
  FileImageOutlined, FileTextOutlined, FolderOpenOutlined, PlusOutlined,
  ProductOutlined, TagsOutlined, ToolOutlined, TeamOutlined,
  CheckCircleOutlined, ClockCircleOutlined, ArrowUpOutlined, ArrowDownOutlined,
  RightOutlined, ReloadOutlined, BellOutlined, SearchOutlined,
  ShopOutlined, BarChartOutlined, CloudUploadOutlined, RocketOutlined
} from "@ant-design/icons";
import { Button, Input, Badge, Avatar, Tooltip } from "antd";
import { useNavigate } from "react-router-dom";
import { useLang } from "../../i18n";

const metrics = [
  { labelKey: "stat.activeProjects", value: "18", trend: "+3", up: true, icon: <AppstoreOutlined />, color: "blue" },
  { labelKey: "stat.pendingAudit", value: "32", trend: "+5", up: true, icon: <AuditOutlined />, color: "orange" },
  { labelKey: "stat.exports", value: "128", trend: "+23", up: true, icon: <ExportOutlined />, color: "green" },
  { labelKey: "stat.cost", value: "¥2.45M", trend: "-0.3M", up: false, icon: <DollarOutlined />, color: "purple" }
];

const projects = [
  { name: "夏季女装详情", platform: "淘宝", progress: 65, status: "生成中", time: "2025-05-16 10:24", color: "blue" },
  { name: "北欧风沙发详情页", platform: "京东", progress: 40, status: "生成中", time: "2025-05-16 09:15", color: "blue" },
  { name: "清爽控油洗发水详情页", platform: "拼多多", progress: 20, status: "生成中", time: "2025-05-15 18:30", color: "blue" },
  { name: "运动耳机详情页", platform: "抖音", progress: 100, status: "已完成", time: "2025-05-15 14:20", color: "green" }
];

const recentTasks = [
  { name: "主视觉生成", project: "夏季女装", status: "进行中", icon: <FileImageOutlined />, color: "blue" },
  { name: "详情页文案生成", project: "北欧沙发", status: "进行中", icon: <FileTextOutlined />, color: "blue" },
  { name: "合规性检查", project: "洗发水", status: "待处理", icon: <AuditOutlined />, color: "orange" },
  { name: "素材抠图处理", project: "运动耳机", status: "已完成", icon: <FolderOpenOutlined />, color: "green" },
  { name: "后处理任务", project: "高清放大", status: "已完成", icon: <ToolOutlined />, color: "green" }
];

const quickActions = [
  { title: "商品资料上传", desc: "上传商品基础资料", icon: <ProductOutlined />, to: "/materials/new", color: "blue" },
  { title: "提示词工作台", desc: "创建与管理提示词", icon: <TagsOutlined />, to: "/visual/prompt-workbench", color: "purple" },
  { title: "视觉规划", desc: "管理视觉方案", icon: <FileImageOutlined />, to: "/visual/plans", color: "green" },
  { title: "导出管理", desc: "导出与交付文件", icon: <ExportOutlined />, to: "/exports", color: "orange" }
];

const systemStatus = [
  { name: "AI Relay 服务", status: "正常", online: true },
  { name: "图像生成服务", status: "正常", online: true },
  { name: "文件存储服务", status: "正常", online: true },
  { name: "导出服务", status: "正常", online: true },
  { name: "LLaVA 服务", status: "正常", online: true }
];

export default function HomeWorkbenchPage() {
  const navigate = useNavigate();
  const { t } = useLang();

  return (
    <div style={{ padding: "0 24px 24px" }}>
      {/* Page title bar */}
      <div style={{
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        marginBottom: 24,
        paddingTop: 16
      }}>
        <div>
          <h1 style={{
            fontSize: 24,
            fontWeight: 700,
            color: "var(--df-text)",
            margin: 0,
            lineHeight: 1.3
          }}>
            工作台
          </h1>
          <p style={{
            fontSize: 14,
            color: "var(--df-text-muted)",
            margin: "4px 0 0"
          }}>
            欢迎回来，这是您的电商详情自动化工作台
          </p>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <Input
            placeholder="搜索..."
            prefix={<SearchOutlined style={{ color: "var(--df-text-muted)" }} />}
            style={{ width: 240 }}
            allowClear
          />
          <Tooltip title="刷新">
            <Button icon={<ReloadOutlined />} />
          </Tooltip>
          <Badge count={3} size="small">
            <Button icon={<BellOutlined />} />
          </Badge>
        </div>
      </div>

      {/* Metrics cards */}
      <div className="df-grid-4" style={{ marginBottom: 24 }}>
        {metrics.map((m) => (
          <div key={m.labelKey} className="df-metric-card">
            <div className={`df-metric-icon ${m.color}`}>{m.icon}</div>
            <div className="df-metric-body">
              <div className="df-metric-label">{t(m.labelKey)}</div>
              <div className="df-metric-value">{m.value}</div>
              <div className={`df-metric-trend ${m.up ? "up" : "down"}`}>
                {m.up ? <ArrowUpOutlined /> : <ArrowDownOutlined />}
                {m.trend}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Main content - left/right layout */}
      <div style={{
        display: "grid",
        gridTemplateColumns: "1fr 360px",
        gap: 24,
        marginBottom: 24
      }}>
        {/* Left: Project progress */}
        <div className="df-card">
          <div className="df-section-header">
            <span className="df-section-title">{t("section.projects")}</span>
            <span className="df-section-link" onClick={() => navigate("/materials")}>
              {t("section.viewAll")} <RightOutlined style={{ fontSize: 10 }} />
            </span>
          </div>
          <div>
            {projects.map((p, i) => (
              <div key={i} className="df-project-item">
                <div className="df-project-thumb">
                  <ShopOutlined style={{ color: "var(--df-primary)" }} />
                </div>
                <div className="df-project-info">
                  <div className="df-project-name">{p.name}</div>
                  <div className="df-project-meta">{p.platform} · {p.time}</div>
                </div>
                <div className="df-project-progress">
                  <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 4 }}>
                    <span style={{ fontSize: 12, color: "var(--df-text-muted)" }}>{p.progress}%</span>
                    <span className={`df-status ${p.color}`}>{p.status}</span>
                  </div>
                  <div className="df-progress-bar">
                    <div className={`df-progress-fill ${p.color}`} style={{ width: `${p.progress}%` }} />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Right: Quick actions + System status */}
        <div style={{ display: "flex", flexDirection: "column", gap: 24 }}>
          {/* Quick actions */}
          <div className="df-card">
            <div className="df-section-header">
              <span className="df-section-title">{t("section.actions")}</span>
            </div>
            <div className="df-grid-2">
              {quickActions.map((action, i) => (
                <div key={i} className="df-quick-action" onClick={() => navigate(action.to)}>
                  <div className={`df-quick-action-icon df-metric-icon ${action.color}`}>{action.icon}</div>
                  <div className="df-quick-action-text">
                    <div className="df-quick-action-title">{action.title}</div>
                    <div className="df-quick-action-desc">{action.desc}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* System status */}
          <div className="df-card">
            <div className="df-section-header">
              <span className="df-section-title">{t("section.system")}</span>
            </div>
            <div>
              {systemStatus.map((s, i) => (
                <div key={i} className="df-system-item">
                  <div style={{ display: "flex", alignItems: "center" }}>
                    <span className={`df-system-dot ${s.online ? "green" : "red"}`} />
                    <span style={{ fontSize: 13 }}>{s.name}</span>
                  </div>
                  <span className={`df-status ${s.online ? "green" : "red"}`}>{s.status}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Bottom: Recent tasks */}
      <div className="df-card">
        <div className="df-section-header">
          <span className="df-section-title">{t("section.tasks")}</span>
          <Button type="primary" icon={<PlusOutlined />} size="small">
            新建任务
          </Button>
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: 12 }}>
          {recentTasks.map((task, i) => (
            <div key={i} className="df-task-item" style={{ padding: "12px 16px" }}>
              <div className={`df-task-icon df-metric-icon ${task.color}`}>{task.icon}</div>
              <div className="df-task-info">
                <div className="df-task-name">{task.name}</div>
                <div style={{ fontSize: 11, color: "var(--df-text-muted)" }}>{task.project}</div>
              </div>
              <span className={`df-status ${task.color}`}>{task.status}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Bottom CTA */}
      <div style={{
        marginTop: 24,
        padding: "16px 24px",
        borderRadius: 14,
        background: "linear-gradient(135deg, rgba(91, 82, 224, 0.08), rgba(139, 92, 246, 0.06))",
        border: "1px solid rgba(91, 82, 224, 0.15)",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between"
      }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <RocketOutlined style={{ fontSize: 20, color: "var(--df-primary)" }} />
          <span style={{ fontSize: 15, fontWeight: 600, color: "var(--df-text)" }}>
            开始创建您的第一个电商详情页
          </span>
        </div>
        <Button type="primary" onClick={() => navigate("/materials/new")}>
          立即开始 <RightOutlined />
        </Button>
      </div>
    </div>
  );
}
