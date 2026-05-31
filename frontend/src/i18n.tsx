import { useState, useCallback, useContext, createContext, type ReactNode } from "react";

export type Lang = "zh" | "en";

const STORAGE_KEY = "detailflow_lang";

const translations: Record<string, { zh: string; en: string }> = {
  // Navigation
  "nav.dashboard": { zh: "工作台", en: "Dashboard" },
  "nav.research": { zh: "市场调研", en: "Research" },
  "nav.materials": { zh: "商品素材", en: "Materials" },
  "nav.generate": { zh: "AI 生图", en: "AI Generate" },
  "nav.assets": { zh: "素材库", en: "Assets" },
  "nav.results": { zh: "生成结果", en: "Results" },
  "nav.detail": { zh: "详情编辑", en: "Detail Editor" },
  "nav.audit": { zh: "合规审核", en: "Audit" },
  "nav.exports": { zh: "导出管理", en: "Exports" },
  "nav.postprocess": { zh: "后处理", en: "Post-Process" },
  "nav.promptlab": { zh: "提示词工作台", en: "Prompt Lab" },
  "nav.templates": { zh: "模板库", en: "Templates" },
  "nav.plans": { zh: "视觉规划", en: "Visual Plans" },
  "nav.cost": { zh: "成本管理", en: "Cost" },
  "nav.diagnostics": { zh: "系统诊断", en: "Diagnostics" },
  "nav.tools": { zh: "工具中心", en: "Tools" },
  "nav.system": { zh: "系统管理", en: "System" },
  "nav.overview": { zh: "概览", en: "Overview" },
  "nav.production": { zh: "生产", en: "Production" },
  "nav.visual": { zh: "视觉", en: "Visual" },
  "nav.contentMgmt": { zh: "内容管理", en: "Content Mgmt" },
  "nav.team": { zh: "团队管理", en: "Team" },
  "nav.auditlog": { zh: "操作日志", en: "Audit Log" },
  
  // Topbar
  "topbar.search": { zh: "搜索素材、任务、模板...", en: "Search materials, tasks, templates..." },
  "topbar.newProject": { zh: "新建项目", en: "New Project" },
  "topbar.settings": { zh: "设置", en: "Settings" },
  "topbar.user": { zh: "用户", en: "User" },
  "topbar.language": { zh: "中/EN", en: "EN/中" },
  
  // Dashboard
  "dashboard.title": { zh: "生产工作台", en: "Production Dashboard" },
  "dashboard.desc": { zh: "电商详情页生产流水线总览", en: "Overview of your e-commerce detail page production pipeline" },
  "stat.activeProjects": { zh: "进行中项目", en: "Active Projects" },
  "stat.pendingAudit": { zh: "待审核任务", en: "Pending Audit" },
  "stat.exports": { zh: "本月导出", en: "Exports (Month)" },
  "stat.cost": { zh: "本月成本", en: "Cost (Month)" },
  "stat.materials": { zh: "素材总数", en: "Materials" },
  "section.projects": { zh: "最近项目", en: "Recent Projects" },
  "section.tasks": { zh: "最近任务", en: "Recent Tasks" },
  "section.actions": { zh: "快捷操作", en: "Quick Actions" },
  "section.system": { zh: "系统状态", en: "System Status" },
  "section.viewAll": { zh: "查看全部", en: "View All" },
  "status.running": { zh: "进行中", en: "Running" },
  "status.done": { zh: "已完成", en: "Done" },
  "status.pending": { zh: "排队中", en: "Queued" },
  
  // Actions
  "action.newProject": { zh: "新建项目", en: "New Project" },
  "action.newProject.desc": { zh: "从素材上传开始", en: "Start from material upload" },
  "action.research": { zh: "市场调研", en: "Research" },
  "action.research.desc": { zh: "市场与竞品分析", en: "Market & competitor scan" },
  "action.generate": { zh: "AI 生图", en: "AI Generate" },
  "action.generate.desc": { zh: "视觉生成工作台", en: "Visual generation workbench" },
  "action.detail": { zh: "详情编辑", en: "Detail Editor" },
  "action.detail.desc": { zh: "编辑商品详情页", en: "Edit product detail pages" },
  "action.prompt": { zh: "提示词工作台", en: "Prompt Lab" },
  "action.prompt.desc": { zh: "创建与管理提示词", en: "Create & manage prompts" },
  "action.template": { zh: "模板库", en: "Templates" },
  "action.template.desc": { zh: "提示词模板管理", en: "Prompt template library" },
  "action.export": { zh: "导出交付", en: "Export" },
  "action.export.desc": { zh: "导出与交付文件", en: "Export & deliver files" },
  "action.tools": { zh: "工具中心", en: "Tools" },
  "action.tools.desc": { zh: "工具适配器设置", en: "Tool adapter settings" },
  
  // Assistant
  "assistant.title": { zh: "智能助手", en: "PromptPilot" },
  "assistant.insights": { zh: "洞察", en: "Insights" },
  "assistant.prompts": { zh: "推荐提示词", en: "Suggested Prompts" },
  "assistant.activity": { zh: "最近动态", en: "Recent Activity" },
  "assistant.actions": { zh: "快捷操作", en: "Quick Actions" },
  "assistant.insight1": { zh: "3 个商品缺少 SEO 关键词，可基于类目分析自动生成。", en: "3 products missing SEO keywords. Auto-generate from category analysis available." },
  "assistant.insight2": { zh: "Real-ESRGAN 批量超分已完成：12 张图片，平均质量分 94.2。", en: "Real-ESRGAN batch upscale completed: 12 images, average quality score 94.2." },
  "assistant.prompt1": { zh: "优雅连衣裙，阳光露台，自然光，生活方式摄影，暖色调，4K 细节", en: "Elegant dress, sunny terrace, natural light, lifestyle photography, warm tones, 4K details" },
  "assistant.activity1": { zh: "夏季女装详情页已通过合规审核", en: "Summer dress detail page passed compliance audit" },
  "assistant.activity2": { zh: "审核 #247 已通过，质量分 96", en: "Audit #247 approved with quality score 96" },
  "assistant.act.prompt": { zh: "生成提示词", en: "Generate Prompt" },
  "assistant.act.upscale": { zh: "批量超分", en: "Batch Upscale" },
  "assistant.act.pdf": { zh: "导出 PDF", en: "Export PDF" },
  "assistant.act.scan": { zh: "竞品扫描", en: "Competitor Scan" },
  "diag.goto": { zh: "系统诊断", en: "Diagnostics" },
  
  // Common
  "common.status": { zh: "状态", en: "Status" },
  "common.action": { zh: "操作", en: "Action" },
  "common.actions": { zh: "操作", en: "Actions" },
  "common.create": { zh: "创建", en: "Create" },
  "common.edit": { zh: "编辑", en: "Edit" },
  "common.delete": { zh: "删除", en: "Delete" },
  "common.save": { zh: "保存", en: "Save" },
  "common.cancel": { zh: "取消", en: "Cancel" },
  "common.confirm": { zh: "确认", en: "Confirm" },
  "common.submit": { zh: "提交", en: "Submit" },
  "common.reset": { zh: "重置", en: "Reset" },
  "common.search": { zh: "搜索", en: "Search" },
  "common.filter": { zh: "筛选", en: "Filter" },
  "common.refresh": { zh: "刷新", en: "Refresh" },
  "common.back": { zh: "返回", en: "Back" },
  "common.loading": { zh: "加载中", en: "Loading" },
  "common.noData": { zh: "暂无数据", en: "No Data" },
  "common.success": { zh: "成功", en: "Success" },
  "common.failed": { zh: "失败", en: "Failed" },
  "common.yes": { zh: "是", en: "Yes" },
  "common.no": { zh: "否", en: "No" },
  "common.enabled": { zh: "已启用", en: "Enabled" },
  "common.info": { zh: "信息", en: "Info" },
  "common.time": { zh: "时间", en: "Time" },
  "common.user": { zh: "用户", en: "User" },
  "common.id": { zh: "ID", en: "ID" },
  "common.name": { zh: "名称", en: "Name" },
  "common.type": { zh: "类型", en: "Type" },
  "common.description": { zh: "描述", en: "Description" },
  "common.createdAt": { zh: "创建时间", en: "Created At" },
  "common.updatedAt": { zh: "更新时间", en: "Updated At" }
};

function getLang(): Lang {
  if (typeof window === "undefined") return "zh";
  const stored = localStorage.getItem(STORAGE_KEY);
  return stored === "en" ? "en" : "zh";
}

function saveLang(lang: Lang) {
  if (typeof window !== "undefined") {
    localStorage.setItem(STORAGE_KEY, lang);
  }
}

function translate(key: string, lang: Lang): string {
  const entry = translations[key];
  if (!entry) return key;
  return entry[lang] ?? entry.en ?? key;
}

// Context for sharing language state across components
interface LangContextValue {
  lang: Lang;
  toggle: () => void;
  t: (key: string) => string;
}

const LangContext = createContext<LangContextValue>({
  lang: "zh",
  toggle: () => {},
  t: (key: string) => key
});

export function LangProvider({ children }: { children: ReactNode }) {
  const [lang, setLang] = useState<Lang>(getLang);
  
  const toggle = useCallback(() => {
    setLang((prev) => {
      const next = prev === "zh" ? "en" : "zh";
      saveLang(next);
      return next;
    });
  }, []);
  
  const t = useCallback((key: string) => translate(key, lang), [lang]);
  
  return (
    <LangContext.Provider value={{ lang, toggle, t }}>
      {children}
    </LangContext.Provider>
  );
}

export function useLang() {
  return useContext(LangContext);
}

// Keep backward compatibility
export { translate as t };