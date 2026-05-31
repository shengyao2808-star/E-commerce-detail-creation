import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  FileImageOutlined, AuditOutlined, ExportOutlined,
  CheckCircleOutlined, SettingOutlined, ThunderboltOutlined
} from "@ant-design/icons";
import { useLang } from "../i18n";

type TabKey = "activity" | "alerts" | "suggestions";

const tabs: { key: TabKey; label: string }[] = [
  { key: "activity", label: "动态" },
  { key: "alerts", label: "提醒" },
  { key: "suggestions", label: "建议" }
];

const activities = [
  {
    id: 1,
    icon: <FileImageOutlined />,
    color: "blue" as const,
    title: "夏季女装详情生成中",
    desc: "模特图生成（第2批）正在使用 ComfyUI 生成中，预计剩余 8 分钟",
    time: "2 分钟前",
    status: "进行中",
    statusColor: "blue" as const
  },
  {
    id: 2,
    icon: <AuditOutlined />,
    color: "orange" as const,
    title: "审核处理",
    desc: "您有 32 条待审核内容",
    time: "15 分钟前",
    status: "待处理",
    statusColor: "orange" as const
  },
  {
    id: 3,
    icon: <ExportOutlined />,
    color: "green" as const,
    title: "导出完成",
    desc: "北欧风沙发详情页（高清）已导出完成",
    time: "1 小时前",
    status: "已完成",
    statusColor: "green" as const
  },
  {
    id: 4,
    icon: <CheckCircleOutlined />,
    color: "green" as const,
    title: "LLaVA 服务状态",
    desc: "LLaVA 服务运行正常",
    time: "2 小时前",
    status: "正常",
    statusColor: "green" as const
  },
  {
    id: 5,
    icon: <ThunderboltOutlined />,
    color: "purple" as const,
    title: "素材更新",
    desc: "新增 12 张素材，已自动同步到素材库",
    time: "3 小时前",
    status: "已同步",
    statusColor: "purple" as const
  }
];

const alerts = [
  {
    id: 1,
    icon: <AuditOutlined />,
    color: "orange" as const,
    title: "待审核提醒",
    desc: "32 条内容等待审核，请及时处理",
    time: "10 分钟前"
  },
  {
    id: 2,
    icon: <SettingOutlined />,
    color: "red" as const,
    title: "素材质量告警",
    desc: "5 张图片分辨率低于 800x800，建议重新上传",
    time: "30 分钟前"
  }
];

const suggestions = [
  {
    id: 1,
    icon: <ThunderboltOutlined />,
    color: "purple" as const,
    title: "补充 SEO 关键词",
    desc: "3 个商品缺少 SEO 关键词，可基于类目分析自动生成"
  },
  {
    id: 2,
    icon: <FileImageOutlined />,
    color: "blue" as const,
    title: "优化提示词",
    desc: "当前提示词可增加风格标签以提升生成质量"
  }
];

export const AssistantPanel = () => {
  const navigate = useNavigate();
  const { t } = useLang();
  const [activeTab, setActiveTab] = useState<TabKey>("activity");

  return (
    <aside className="df-assistant">
      <div className="df-assistant-header">
        <div className="df-assistant-pulse" />
        {t("assistant.title")}
      </div>

      <div className="df-assistant-tabs">
        {tabs.map((tab) => (
          <div
            key={tab.key}
            className={`df-assistant-tab${activeTab === tab.key ? " active" : ""}`}
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </div>
        ))}
      </div>

      {activeTab === "activity" && (
        <>
          {activities.map((item) => (
            <div key={item.id} className="df-activity-item">
              <div className={`df-activity-icon ${item.color}`}>{item.icon}</div>
              <div className="df-activity-body">
                <div className="df-activity-title">{item.title}</div>
                <div className="df-activity-desc">{item.desc}</div>
                <div className="df-activity-meta">
                  <span>{item.time}</span>
                  <span className={`df-status ${item.statusColor}`}>{item.status}</span>
                </div>
              </div>
            </div>
          ))}
        </>
      )}

      {activeTab === "alerts" && (
        <>
          {alerts.map((item) => (
            <div key={item.id} className="df-activity-item">
              <div className={`df-activity-icon ${item.color}`}>{item.icon}</div>
              <div className="df-activity-body">
                <div className="df-activity-title">{item.title}</div>
                <div className="df-activity-desc">{item.desc}</div>
                <div className="df-activity-meta">
                  <span>{item.time}</span>
                </div>
              </div>
            </div>
          ))}
        </>
      )}

      {activeTab === "suggestions" && (
        <>
          {suggestions.map((item) => (
            <div key={item.id} className="df-activity-item">
              <div className={`df-activity-icon ${item.color}`}>{item.icon}</div>
              <div className="df-activity-body">
                <div className="df-activity-title">{item.title}</div>
                <div className="df-activity-desc">{item.desc}</div>
              </div>
            </div>
          ))}
        </>
      )}
    </aside>
  );
};