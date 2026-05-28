# 工具库清单

版本：`v0.2`  
日期：`2026-05-26`

## 接入原则

1. 主业务系统只通过 `tool-adapter` 或前端本地工具入口调用能力，不直接依赖第三方工具内部实现。
2. 未接入、未配置或无真实输入数据时，只显示空态、待接入或禁用态，不伪造结果。
3. 工具版本必须固定，记录依赖版本、集成位置和用途，避免交付环境漂移。
4. 后端 Controller 不因前端工具接入而被绕改；前端只消费现有接口或本地浏览器能力。
5. 能力分层清晰：数据采集、AI 生成、素材处理、可视化、编辑器与工作流状态管理分别登记。

## 现有主工具

| 环节 | 工具 | GitHub | 许可 | 集成方式 | 备注 |
|---|---|---|---|---|---|
| 授权资料采集 | Crawl4AI | <https://github.com/unclecode/crawl4ai> | Apache-2.0 | 私有部署 HTTP 服务 | 用于授权页面、公开资料、链接抓取 |
| 浏览器调研 | Browser Use | <https://github.com/browser-use/browser-use> | MIT | 私有部署 HTTP 服务 | 用于人工授权调研，不做绕过 |
| 批量采集 worker | Scrapy | <https://github.com/scrapy/scrapy> | BSD-3-Clause | 独立 worker | 用于客户自有站点、授权站点采集 |
| Prompt 流程评测 | Promptflow | <https://github.com/microsoft/promptflow> | MIT | 私有部署服务 | 用于提示词模板、A/B 和回归评测 |
| Prompt/模型观测 | Langfuse | <https://github.com/langfuse/langfuse> | 商业复核 | 可选自部署服务 | 用于链路观测与版本跟踪 |
| 图片转提示词 | LLaVA | <https://github.com/haotian-liu/LLaVA> | Apache-2.0 | 视觉语言服务 | 用于图片描述和提示词抽取 |
| 生图编排 | ComfyUI | <https://github.com/Comfy-Org/ComfyUI> | GPL-3.0 | 独立服务 | 仅进程外调用，不并入主工程 |
| 姿态/结构控制 | ControlNet | <https://github.com/lllyasviel/ControlNet> | Apache-2.0 | ComfyUI 工作流 / 独立服务 | 用于姿态、边缘、深度控制 |
| 分割与遮罩 | Grounded-SAM | <https://github.com/IDEA-Research/Grounded-Segment-Anything> | Apache-2.0 | 独立服务 | 用于主体分割 |
| 修补与清理 | IOPaint | <https://github.com/Sanster/IOPaint> | Apache-2.0 | 独立服务 | 用于背景清理和修补 |
| 超分增强 | Real-ESRGAN | <https://github.com/xinntao/Real-ESRGAN> | BSD-3-Clause | 独立服务 | 用于图片增强 |
| 拼接/裁切/转换 | ImageMagick | <https://github.com/ImageMagick/ImageMagick> | 商业复核 | CLI / worker | 用于长图拼接、格式转换 |
| 视觉验收 | Playwright | <https://github.com/microsoft/playwright> | Apache-2.0 | CLI / worker | 用于预览截图、布局验收 |

## P1 前端工具接入登记

本轮仅修改 `frontend/` 与本文件，不触碰后端 Controller，不伪造平台数据或 AI 结果。

| 工具 / 依赖 | 版本 | 用途 | 当前接入位置 | 当前状态 |
|---|---:|---|---|---|
| `xlsx` / SheetJS | `0.18.5` | 解析商品资料 Excel、竞品 Excel、评论 Excel | `frontend/src/lib/fileParsers.ts`、`frontend/src/pages/tools/DataImportPage.tsx` | 已接入前端预览 |
| `papaparse` | `5.5.3` | 解析评论 CSV | `frontend/src/lib/fileParsers.ts`、`frontend/src/types/papaparse.d.ts` | 已接入前端预览 |
| `echarts` | `6.1.0` | 市场调研图表占位 | `frontend/src/components/charts/PlaceholderChart.tsx`、`frontend/src/pages/research/ResearchCenterPage.tsx` | 已接入占位图，不伪造数据 |
| `zustand` | `5.0.13` | 管理当前商品、调研任务、生图任务、选中素材、详情页草稿、工具状态 | `frontend/src/stores/workbenchStore.ts` | 已接入 |
| `@tanstack/react-query` | `5.100.14` | API 请求状态、loading / error / retry、轮询基础能力 | `frontend/src/lib/queryClient.ts`、`frontend/src/App.tsx`、`frontend/src/pages/tools/ToolCenterPage.tsx` | 已接入基础查询能力 |
| `@dnd-kit/core` | `6.3.1` | 详情页模块拖拽能力 | `frontend/src/components/dnd/SortableModuleBoard.tsx`、`frontend/src/pages/details/DetailEditorPage.tsx` | 已接入拖拽骨架 |
| `@dnd-kit/sortable` | `10.0.0` | 模块排序 | 同上 | 已接入拖拽骨架 |
| `@dnd-kit/utilities` | `3.2.2` | 拖拽样式与位移工具 | 同上 | 已接入拖拽骨架 |
| `pdfjs-dist` | `5.7.284` | 商品资料 PDF 上传后的前端预览 | `frontend/src/lib/pdfPreview.ts`、`frontend/src/pages/tools/DataImportPage.tsx` | 已接入首页预览 |
| `tesseract.js` | `7.0.0` | 素材库图片 OCR 检测入口 | `frontend/src/pages/assets/AssetLibraryPage.tsx` | 已接入入口，无图片时空态 |
| `@excalidraw/excalidraw` | `0.18.1` | 设计草稿 / 素材批注入口 | `frontend/src/pages/tools/DesignDraftPage.tsx` | 已接入空白草稿画布 |

## 本轮能力边界

- 只接入前端入口、预览、状态和页面骨架。
- 不伪造真实平台数据、竞品结论、评论分析结果或 AI 生成结果。
- 没有真实文件、真实图片、真实任务结果时，统一显示空态、待接入或禁用状态。
- 后端轮询、AI 任务执行、OCR 结果持久化、图表真实数据绑定、模块排序落库，均留待后续阶段。
