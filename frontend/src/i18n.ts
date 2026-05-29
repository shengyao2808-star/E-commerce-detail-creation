import { useState, useCallback } from "react";

export type Lang = "zh" | "en";

const STORAGE_KEY = "detailflow_lang";

const translations: Record<string, { zh: string; en: string }> = {
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
  "topbar.search": { zh: "搜索素材、任务、模板...", en: "Search materials, tasks, templates..." },
  "topbar.newProject": { zh: "新建项目", en: "New Project" },
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
  "assistant.title": { zh: "智能助手", en: "PromptPilot" },
  "assistant.insights": { zh: "洞察", en: "Insights" },
  "assistant.prompts": { zh: "推荐提示词", en: "Suggested Prompts" },
  "assistant.activity": { zh: "最近动态", en: "Recent Activity" },
  "assistant.actions": { zh: "快捷操作", en: "Quick Actions" },
  "assistant.insight1": { zh: "3 个商品缺少 SEO 关键词，可基于类目分析自动生成。", en: "3 products missing SEO keywords. Auto-generate from category analysis available." },
  "assistant.insight2": { zh: "Real-ESRGAN 批量超分已完成：12 张图片，平均质量分 94.2。", en: "Real-ESRGAN batch upscale completed: 12 images, average quality score 94.2." },
  "assistant.prompt1": { zh: "\"优雅连衣裙，阳光露台，自然光，生活方式摄影，暖色调，4K 细节\"", en: "\"Elegant summer dress on sunlit terrace, natural light, lifestyle photography, warm tones, 4K detail shot\"" },
  "assistant.activity1": { zh: "厨具详情页已导出（5 个文件，Word 格式）", en: "Kitchen Tools detail pages exported (5 files, Word format)" },
  "assistant.activity2": { zh: "审核 #247 已通过，质量分 96", en: "Audit #247 approved with quality score 96" },
  "assistant.act.prompt": { zh: "生成提示词", en: "Generate Prompt" },
  "assistant.act.upscale": { zh: "批量超分", en: "Batch Upscale" },
  "assistant.act.pdf": { zh: "导出 PDF", en: "Export PDF" },
  "assistant.act.scan": { zh: "竞品扫描", en: "Competitor Scan" },
  "diag.goto": { zh: "系统诊断", en: "Diagnostics" }
};

export function getLang(): Lang {
  const stored = localStorage.getItem(STORAGE_KEY);
  return stored === "en" ? "en" : "zh";
}

export function setLang(lang: Lang) {
  localStorage.setItem(STORAGE_KEY, lang);
}

export function t(key: string, lang?: Lang): string {
  const l = lang ?? getLang();
  const entry = translations[key];
  if (!entry) return key;
  return entry[l] ?? entry.en ?? key;
}

export function useLang() {
  const [lang, setLangState] = useState<Lang>(getLang);
  const toggle = useCallback(() => {
    const next = lang === "zh" ? "en" : "zh";
    setLang(next);
    setLangState(next);
  }, [lang]);
  return { lang, toggle, t: useCallback((key: string) => t(key, lang), [lang]) };
}