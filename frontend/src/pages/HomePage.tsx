import { Button, Space, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import {
  FileImageOutlined, TagsOutlined, ExportOutlined, SafetyOutlined,
  RocketOutlined, TeamOutlined, BarChartOutlined, CheckCircleOutlined,
  ThunderboltOutlined, ShopOutlined, PictureOutlined, EditOutlined,
  AuditOutlined, DatabaseOutlined, SyncOutlined, RobotOutlined,
  RightOutlined, PlayCircleOutlined, ArrowRightOutlined
} from "@ant-design/icons";
import iconLogo from "../assets/icon-logo.png";
import textLogo from "../assets/text-logo.png";
const { Title, Paragraph, Text } = Typography;

// 工作流节点
const workflowSteps = [
  { icon: <ShopOutlined />, title: "商品资料", desc: "采集商品基础信息" },
  { icon: <PictureOutlined />, title: "商品素材", desc: "管理图片视频素材" },
  { icon: <BarChartOutlined />, title: "视觉规划", desc: "制定视觉方案" },
  { icon: <TagsOutlined />, title: "提示词工作台", desc: "创建优化提示词" },
  { icon: <RobotOutlined />, title: "AI 生图", desc: "智能生成图片" },
  { icon: <EditOutlined />, title: "详情编辑", desc: "编辑详情页内容" },
  { icon: <AuditOutlined />, title: "合规审核", desc: "检测合规风险" },
  { icon: <ExportOutlined />, title: "导出交付", desc: "多格式导出" }
];

// 功能卡片
const features = [
  {
    icon: <ShopOutlined />,
    color: "blue",
    title: "商品资料",
    desc: "一站式管理商品基础资料，支持批量导入、OCR 识别、智能分类",
    preview: "feature-materials-preview.png"
  },
  {
    icon: <BarChartOutlined />,
    color: "purple",
    title: "视觉规划",
    desc: "制定视觉方案，管理 SKC 策略，支持批量派发生成任务",
    preview: "feature-visual-plan-preview.png"
  },
  {
    icon: <TagsOutlined />,
    color: "green",
    title: "提示词工作台",
    desc: "可视化创建提示词，支持引导式生成、扩展优化、图片反推",
    preview: "feature-prompt-preview.png"
  },
  {
    icon: <EditOutlined />,
    color: "orange",
    title: "详情编辑器",
    desc: "拖拽式编辑详情页，支持模块排序、实时预览、合规检测",
    preview: "feature-detail-editor-preview.png"
  }
];

// 数据指标
const stats = [
  { icon: <SafetyOutlined />, value: "数据安全合规", desc: "ISO 27001 认证" },
  { icon: <CheckCircleOutlined />, value: "99.9% 系统可用性", desc: "企业级 SLA 保障" },
  { icon: <SyncOutlined />, value: "70+ 平台接入", desc: "覆盖主流电商平台" },
  { icon: <TeamOutlined />, value: "10,000+ 品牌共同选择", desc: "持续增长的客户群体" }
];

export default function HomePage() {
  const navigate = useNavigate();

  return (
    <div style={{ minHeight: "100vh", background: "#ffffff" }}>
      {/* Header */}
      <header style={{
        position: "sticky",
        top: 0,
        zIndex: 100,
        background: "rgba(255, 255, 255, 0.95)",
        backdropFilter: "blur(10px)",
        borderBottom: "1px solid var(--df-border)",
        padding: "0 48px",
        height: 64,
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between"
      }}>
        <div style={{ display: "flex", alignItems: "center", gap: 32 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 4, cursor: "pointer" }} onClick={() => navigate("/")}>
            <img src={iconLogo} alt="Logo" style={{ width: 36, height: 36, borderRadius: 8 }} />
            <img src={textLogo} alt="DetailFlow" style={{ height: 80, marginLeft: -4 }} />
          </div>
          <nav style={{ display: "flex", gap: 24 }}>
            {["产品", "解决方案", "工作流", "价格", "资源"].map((item) => (
              <a key={item} style={{
                color: "var(--df-text-secondary)",
                fontSize: 14,
                cursor: "pointer",
                display: "flex",
                alignItems: "center",
                gap: 4
              }}>
                {item} <RightOutlined style={{ fontSize: 10 }} />
              </a>
            ))}
          </nav>
        </div>
        <Space size={16}>
          <Button type="text" onClick={() => navigate("/login")}>登录</Button>
          <Button>预约演示</Button>
          <Button type="primary" onClick={() => navigate("/login")}>免费试用</Button>
        </Space>
      </header>

      {/* Hero Section */}
      <section style={{
        padding: "80px 48px",
        maxWidth: 1200,
        margin: "0 auto",
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: 60,
        alignItems: "center"
      }}>
        <div>
          <div style={{
            display: "inline-flex",
            alignItems: "center",
            gap: 8,
            padding: "6px 16px",
            background: "var(--df-primary-soft)",
            color: "var(--df-primary)",
            borderRadius: 999,
            fontSize: 13,
            fontWeight: 600,
            marginBottom: 24
          }}>
            <ThunderboltOutlined /> AI 驱动的电商详情页全流程平台
          </div>
          <Title level={1} style={{
            fontSize: 44,
            fontWeight: 700,
            color: "var(--df-text)",
            marginBottom: 20,
            lineHeight: 1.2
          }}>
            让电商详情页生产，<br />像搭建工作流一样高效
          </Title>
          <Paragraph style={{
            fontSize: 16,
            color: "var(--df-text-muted)",
            marginBottom: 32,
            lineHeight: 1.8
          }}>
            从商品资料、视觉规划、提示词工作台、AI 生图、详情编辑、合规审核到导出交付，
            所有环节在一个平台无缝协同，缩短 70%+ 生产周期。
          </Paragraph>
          <Space size={16} style={{ marginBottom: 40 }}>
            <Button type="primary" size="large" onClick={() => navigate("/login")}>
              免费试用 14 天 <ArrowRightOutlined />
            </Button>
            <Button size="large" icon={<PlayCircleOutlined />}>
              观看产品演示
            </Button>
          </Space>
          <div style={{
            display: "grid",
            gridTemplateColumns: "repeat(2, 1fr)",
            gap: 16
          }}>
            {stats.map((stat, i) => (
              <div key={i} style={{
                display: "flex",
                alignItems: "center",
                gap: 10,
                padding: "10px 14px",
                background: "var(--df-bg)",
                borderRadius: 10
              }}>
                <div style={{ color: "var(--df-primary)", fontSize: 18 }}>{stat.icon}</div>
                <div>
                  <div style={{ fontSize: 13, fontWeight: 600, color: "var(--df-text)" }}>{stat.value}</div>
                  <div style={{ fontSize: 11, color: "var(--df-text-muted)" }}>{stat.desc}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
        <div style={{
          background: "var(--df-bg)",
          borderRadius: 16,
          padding: 20,
          border: "1px solid var(--df-border)"
        }}>
          <div style={{
            width: "100%",
            height: 400,
            background: "linear-gradient(135deg, var(--df-primary-soft) 0%, var(--df-purple-soft) 100%)",
            borderRadius: 12,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            color: "var(--df-text-muted)",
            fontSize: 14
          }}>
            产品界面预览图
          </div>
        </div>
      </section>

      {/* 工作流模块 */}
      <section style={{
        padding: "80px 48px",
        background: "var(--df-bg)"
      }}>
        <div style={{ maxWidth: 1200, margin: "0 auto" }}>
          <div style={{ textAlign: "center", marginBottom: 48 }}>
            <Title level={2} style={{ fontWeight: 600, marginBottom: 8 }}>电商详情页生产工作流</Title>
            <Paragraph style={{ color: "var(--df-text-muted)", fontSize: 16 }}>
              8 个环节无缝协同，一站式完成详情页生产
            </Paragraph>
          </div>
          <div style={{
            display: "grid",
            gridTemplateColumns: "repeat(8, 1fr)",
            gap: 8,
            alignItems: "center"
          }}>
            {workflowSteps.map((step, i) => (
              <div key={i} style={{ display: "flex", alignItems: "center" }}>
                <div style={{
                  flex: 1,
                  textAlign: "center",
                  padding: "20px 12px",
                  background: "var(--df-surface)",
                  borderRadius: 12,
                  border: "1px solid var(--df-border)"
                }}>
                  <div style={{
                    width: 48,
                    height: 48,
                    borderRadius: "50%",
                    background: "var(--df-purple-soft)",
                    color: "var(--df-purple)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontSize: 20,
                    margin: "0 auto 12px"
                  }}>
                    {step.icon}
                  </div>
                  <div style={{ fontSize: 13, fontWeight: 600, color: "var(--df-text)", marginBottom: 4 }}>
                    {step.title}
                  </div>
                  <div style={{ fontSize: 11, color: "var(--df-text-muted)" }}>
                    {step.desc}
                  </div>
                </div>
                {i < workflowSteps.length - 1 && (
                  <RightOutlined style={{ color: "var(--df-text-muted)", margin: "0 4px" }} />
                )}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* 功能卡片区 */}
      <section style={{ padding: "80px 48px", maxWidth: 1200, margin: "0 auto" }}>
        <div style={{ textAlign: "center", marginBottom: 48 }}>
          <Title level={2} style={{ fontWeight: 600, marginBottom: 8 }}>核心功能</Title>
          <Paragraph style={{ color: "var(--df-text-muted)", fontSize: 16 }}>
            覆盖电商详情页生产全流程
          </Paragraph>
        </div>
        <div style={{
          display: "grid",
          gridTemplateColumns: "repeat(2, 1fr)",
          gap: 24
        }}>
          {features.map((feature, i) => (
            <div key={i} style={{
              background: "var(--df-surface)",
              border: "1px solid var(--df-border)",
              borderRadius: 16,
              padding: 28,
              transition: "all 0.2s",
              cursor: "pointer"
            }}>
              <div style={{ display: "flex", alignItems: "flex-start", gap: 16, marginBottom: 20 }}>
                <div style={{
                  width: 52,
                  height: 52,
                  borderRadius: 12,
                  background: feature.color === "blue" ? "var(--df-primary-soft)" :
                    feature.color === "purple" ? "var(--df-purple-soft)" :
                    feature.color === "green" ? "var(--df-success-soft)" :
                    "var(--df-warning-soft)",
                  color: feature.color === "blue" ? "var(--df-primary)" :
                    feature.color === "purple" ? "var(--df-purple)" :
                    feature.color === "green" ? "var(--df-success)" :
                    "var(--df-warning)",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: 24
                }}>
                  {feature.icon}
                </div>
                <div>
                  <div style={{ fontSize: 18, fontWeight: 600, color: "var(--df-text)", marginBottom: 8 }}>
                    {feature.title}
                  </div>
                  <div style={{ color: "var(--df-text-muted)", fontSize: 14, lineHeight: 1.6 }}>
                    {feature.desc}
                  </div>
                </div>
              </div>
              <div style={{
                width: "100%",
                height: 200,
                background: "var(--df-bg)",
                borderRadius: 12,
                border: "1px solid var(--df-border)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: "var(--df-text-muted)",
                fontSize: 13
              }}>
                功能预览图
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* 数据·协同·AI 自动化 */}
      <section style={{
        padding: "80px 48px",
        background: "var(--df-bg)"
      }}>
        <div style={{ maxWidth: 1200, margin: "0 auto" }}>
          <div style={{ textAlign: "center", marginBottom: 48 }}>
            <Title level={2} style={{ fontWeight: 600, marginBottom: 8 }}>数据 · 协同 · AI 自动化</Title>
            <Paragraph style={{ color: "var(--df-text-muted)", fontSize: 16 }}>
              让团队协作更顺畅，让生产更智能。
            </Paragraph>
            <Button type="link" style={{ marginTop: 8 }}>了解更多 <ArrowRightOutlined /></Button>
          </div>
          <div style={{
            display: "grid",
            gridTemplateColumns: "repeat(3, 1fr)",
            gap: 24
          }}>
            {[
              { icon: <DatabaseOutlined />, color: "blue", title: "数据驱动", desc: "基于数据分析优化生产流程，实时监控成本和效率" },
              { icon: <TeamOutlined />, color: "purple", title: "团队协同", desc: "多人协作编辑，权限管理，审核流程，提升团队效率" },
              { icon: <RobotOutlined />, color: "green", title: "AI 自动化", desc: "AI 智能生成内容，自动检测合规，减少人工干预" }
            ].map((item, i) => (
              <div key={i} style={{
                background: "var(--df-surface)",
                border: "1px solid var(--df-border)",
                borderRadius: 16,
                padding: 28,
                textAlign: "center"
              }}>
                <div style={{
                  width: 64,
                  height: 64,
                  borderRadius: "50%",
                  background: item.color === "blue" ? "var(--df-primary-soft)" :
                    item.color === "purple" ? "var(--df-purple-soft)" :
                    "var(--df-success-soft)",
                  color: item.color === "blue" ? "var(--df-primary)" :
                    item.color === "purple" ? "var(--df-purple)" :
                    "var(--df-success)",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: 28,
                  margin: "0 auto 20px"
                }}>
                  {item.icon}
                </div>
                <div style={{ fontSize: 18, fontWeight: 600, color: "var(--df-text)", marginBottom: 12 }}>
                  {item.title}
                </div>
                <div style={{ color: "var(--df-text-muted)", fontSize: 14, lineHeight: 1.6 }}>
                  {item.desc}
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* 品牌背书 */}
      <section style={{
        padding: "60px 48px",
        background: "var(--df-surface)",
        borderTop: "1px solid var(--df-border)"
      }}>
        <div style={{ maxWidth: 1200, margin: "0 auto", textAlign: "center" }}>
          <div style={{ color: "var(--df-text-muted)", fontSize: 14, marginBottom: 24 }}>
            受到 10,000+ 品牌信赖
          </div>
          <div style={{
            display: "flex",
            justifyContent: "center",
            gap: 48,
            flexWrap: "wrap"
          }}>
            {["品牌 A", "品牌 B", "品牌 C", "品牌 D", "品牌 E", "品牌 F"].map((brand) => (
              <div key={brand} style={{
                padding: "12px 24px",
                background: "var(--df-bg)",
                borderRadius: 8,
                color: "var(--df-text-muted)",
                fontSize: 14,
                fontWeight: 500
              }}>
                {brand}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section style={{
        padding: "80px 48px",
        textAlign: "center",
        background: "linear-gradient(135deg, var(--df-primary) 0%, #1d4ed8 100%)"
      }}>
        <Title level={2} style={{ color: "white", fontWeight: 600, marginBottom: 16 }}>
          准备好提升效率了吗？
        </Title>
        <Paragraph style={{ color: "rgba(255,255,255,0.8)", fontSize: 16, marginBottom: 32 }}>
          立即免费试用 14 天，体验 AI 驱动的电商详情页生产平台
        </Paragraph>
        <Space size={16}>
          <Button size="large" style={{ background: "white", borderColor: "white" }} onClick={() => navigate("/login")}>
            免费试用 14 天
          </Button>
          <Button size="large" ghost style={{ borderColor: "white", color: "white" }}>
            预约演示
          </Button>
        </Space>
      </section>

      {/* Footer */}
      <footer style={{
        padding: "48px 48px 24px",
        background: "var(--df-surface)",
        borderTop: "1px solid var(--df-border)"
      }}>
        <div style={{
          maxWidth: 1200,
          margin: "0 auto",
          display: "grid",
          gridTemplateColumns: "repeat(4, 1fr)",
          gap: 48
        }}>
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 16 }}>
              <div style={{
                width: 32,
                height: 32,
                background: "var(--df-primary)",
                borderRadius: 6,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: "white",
                fontWeight: 700,
                fontSize: 16
              }}>D</div>
              <span style={{ fontSize: 16, fontWeight: 600 }}>DetailFlow</span>
            </div>
            <div style={{ color: "var(--df-text-muted)", fontSize: 13 }}>
              AI 驱动的电商详情页全流程平台
            </div>
          </div>
          {[
            { title: "产品", items: ["功能", "定价", "更新日志", "路线图"] },
            { title: "资源", items: ["文档", "教程", "博客", "社区"] },
            { title: "公司", items: ["关于", "招聘", "联系我们", "合作伙伴"] }
          ].map((col) => (
            <div key={col.title}>
              <div style={{ fontSize: 14, fontWeight: 600, color: "var(--df-text)", marginBottom: 16 }}>
                {col.title}
              </div>
              {col.items.map((item) => (
                <div key={item} style={{
                  color: "var(--df-text-muted)",
                  fontSize: 13,
                  marginBottom: 10,
                  cursor: "pointer"
                }}>
                  {item}
                </div>
              ))}
            </div>
          ))}
        </div>
        <div style={{
          maxWidth: 1200,
          margin: "32px auto 0",
          paddingTop: 24,
          borderTop: "1px solid var(--df-border)",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center"
        }}>
          <div style={{ color: "var(--df-text-muted)", fontSize: 12 }}>
            © 2025 DetailFlow. All rights reserved.
          </div>
          <Space size={16}>
            <a style={{ color: "var(--df-text-muted)", fontSize: 12, cursor: "pointer" }}>隐私政策</a>
            <a style={{ color: "var(--df-text-muted)", fontSize: 12, cursor: "pointer" }}>服务条款</a>
          </Space>
        </div>
      </footer>
    </div>
  );
}
