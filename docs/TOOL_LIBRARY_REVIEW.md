# TOOL_LIBRARY_REVIEW：电商 AI 详情页项目 GitHub 工具审查清单
> 生成日期：2026-05-25  
> 用途：供你审查哪些 GitHub 开源工具可以直接融入当前项目、哪些只能外部服务化、哪些需要授权/许可证重点审查。  
> 星标：为检索时的近似值，GitHub 星标会变化，请以仓库页面实时数据为准。  
> 原则：每个功能最多 3 个候选；优先选择能落地、生态成熟、许可证相对友好的项目。

## 审查结论先行

### 建议第一批直接进入项目的工具

| 优先级 | 工具 | 放入位置 | 原因 |
|---|---|---|---|
| P0 | PDF.js | frontend | PDF 预览是商品资料导入的核心能力 |
| P0 | Apache PDFBox | backend | Java 后端解析 PDF 文本/页面 |
| P0 | SheetJS / xlsx | frontend | Excel/CSV 竞品、商品资料、评论导入 |
| P0 | Apache ECharts | frontend | 市场调研图表看板 |
| P0 | dnd-kit | frontend | 详情页模块/素材拖拽排序 |
| P0 | zustand | frontend | 当前商品、任务、素材、草稿状态 |
| P0 | react-dropzone | frontend | PDF/图片/Excel 拖拽上传 |

### 建议第二批增强工具

Excalidraw、pdf-lib、tesseract.js、jsoup、DuckDB、TanStack Query、Tiptap、Puppeteer、sharp。

### 暂不建议直接拷源码进主仓库的工具

ComfyUI、Stable Diffusion WebUI、Fooocus、imgproxy、Playwright、Crawlee、PaddleOCR、Docling。它们更适合独立服务化，然后由后端 ToolAdapter 调用。

### 必须重点审查的工具

- Stable Diffusion WebUI：AGPL-3.0，不能无脑拷源码进商业项目。
- ComfyUI / Fooocus：GPL-3.0，建议外部服务化。
- tldraw：生产授权需要重点确认。
- Elasticsearch：授权策略需要单独审查。
- 没有 LICENSE、带水印/品牌戳、仅个人免费、模型权重不允许商用的项目：不要进入主仓库。

## 全量候选清单

### 01 PDF 导入 / 预览 / 编辑

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| PDF.js | ≈53.4k | https://github.com/mozilla/pdf.js | Apache-2.0 | 前端 PDF 预览、分页渲染、上传后查看 | 直接引入 frontend；配合 pdf.worker | P0 | 低 |
| pdf-lib | ≈8.5k | https://github.com/Hopding/pdf-lib | MIT | 前端/Node 创建、修改、合并 PDF，导出资料包 | 直接引入 frontend 或 Node 工具层 | P1 | 低 |
| Apache PDFBox | ≈3.1k | https://github.com/apache/pdfbox | Apache-2.0 | Java 后端提取 PDF 文本、拆分、合并、渲染 | 后端 pom.xml Maven 依赖 | P0 | 低 |

### 02 Excel / CSV 商品资料与评论导入

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| SheetJS / xlsx | ≈36.3k | https://github.com/SheetJS/sheetjs | Apache-2.0 / Community | 商品资料、竞品表、评论表导入导出 | 直接引入 frontend；复杂导出审查 Pro 边界 | P0 | 低-中 |
| ExcelJS | ≈15.3k | https://github.com/exceljs/exceljs | MIT | 复杂 Excel 模板、样式化导出、错误报告 | frontend 或 Node 服务 | P1 | 低 |
| PapaParse | ≈13.5k | https://github.com/mholt/PapaParse | MIT | CSV 评论、采样表、竞品表快速解析 | 直接引入 frontend | P1 | 低 |

### 03 市场调研 / 竞品采样 / 公开页面解析

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| Playwright | ≈89.4k | https://github.com/microsoft/playwright | Apache-2.0 | 公开页面采样、截图、竞品页面结构读取 | 独立采集服务；不做反爬绕过 | P2 | 中 |
| Crawlee | ≈23.4k | https://github.com/apify/crawlee | Apache-2.0 | 批量 URL 采集、队列、数据导出 | 独立 Node/Python 采集服务 | P2 | 中 |
| jsoup | ≈11.4k | https://github.com/jhy/jsoup | MIT | Java 后端解析公开 HTML、清洗页面文案 | 后端 Maven 依赖 | P1 | 低-中 |

### 04 市场数据分析 / 价格带 / 关键词聚合

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| DuckDB | ≈38.4k | https://github.com/duckdb/duckdb | MIT | 本地分析 Excel/CSV：价格带、销量段、词频聚合 | 后端/本地分析引擎或 WASM 方案 | P1 | 低 |
| Apache ECharts | ≈66.4k | https://github.com/apache/echarts | Apache-2.0 | 价格带图、关键词排行、竞品矩阵、痛点排行 | 直接引入 frontend | P0 | 低 |
| Recharts | ≈27.2k | https://github.com/recharts/recharts | MIT | React 工作台轻量图表 | 直接引入 frontend；若已有 ECharts 可不装 | P2 | 低 |

### 05 OCR / 文档解析 / 图片文字合规

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| PaddleOCR | ≈78.5k | https://github.com/PaddlePaddle/PaddleOCR | Apache-2.0 | 高质量 OCR：商品图、详情页图、PDF 图中文字 | 独立 OCR 服务或 Python 服务 | P2 | 低-中 |
| Docling | ≈60.3k | https://github.com/docling-project/docling | MIT | PDF/Word/图片转结构化 Markdown/JSON | 独立文档解析服务 | P2 | 低-中 |
| tesseract.js | ≈38.1k | https://github.com/naptha/tesseract.js | Apache-2.0 | 前端/Node 轻量 OCR，图片文字风险检测 | 直接引入 frontend 或 Node | P1 | 低 |

### 06 图片处理 / 压缩 / 裁剪 / 格式转换

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| sharp | ≈32.3k | https://github.com/lovell/sharp | Apache-2.0 | 压缩、裁剪、WebP/AVIF、缩略图、水印 | Node 图片处理服务；不适合直接塞 Java | P1 | 低 |
| ImageMagick | ≈16.5k | https://github.com/ImageMagick/ImageMagick | ImageMagick License | 格式转换、合成、批处理，支持广 | 服务端命令行/容器工具 | P2 | 中 |
| imgproxy | ≈10.8k | https://github.com/imgproxy/imgproxy | MIT | 动态缩略图、裁剪、CDN 式图片访问 | 独立服务；后端生成签名 URL | P2 | 低-中 |

### 07 AI 生图 / 图生图 / 局部重绘 / 高清放大

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| Stable Diffusion WebUI | ≈163k | https://github.com/AUTOMATIC1111/stable-diffusion-webui | AGPL-3.0 | 文生图、图生图、inpaint、upscale | 外部自托管服务；后端 API 调用 | P3 | 高 |
| ComfyUI | ≈114k | https://github.com/Comfy-Org/ComfyUI | GPL-3.0 | 工作流式生图、商品图生产链路、节点编排 | 外部自托管服务；后端 API 调用 | P3 | 中-高 |
| Fooocus | ≈48.9k | https://github.com/lllyasviel/Fooocus | GPL-3.0 | 简化生图体验，快速验证商品图生成 | 外部服务；谨慎评估模型和插件授权 | P3 | 中-高 |

### 08 设计画布 / 批注 / 素材编辑

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| Excalidraw | ≈124k | https://github.com/excalidraw/excalidraw | MIT | 设计草稿、运营批注、素材白板、导出 PNG/SVG | 直接嵌入 frontend | P1 | 低 |
| tldraw | ≈47.4k | https://github.com/tldraw/tldraw | SDK 生产授权需审查 | 高级无限画布、设计编辑、图片标注 | 原型可试；生产前确认 license key | P3 | 高 |
| Fabric.js | ≈31.2k | https://github.com/fabricjs/fabric.js | MIT | 商品图编辑器、贴纸、文字、图层、画布编辑 | 直接嵌入 frontend | P2 | 低 |

### 09 详情页拖拽 / 模块排序 / 页面搭建

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| react-grid-layout | ≈22.3k | https://github.com/react-grid-layout/react-grid-layout | MIT | 网格布局、运营看板、模块拖拽 | frontend 页面搭建/工作台看板 | P2 | 低 |
| dnd-kit | ≈17.2k | https://github.com/clauderic/dnd-kit | MIT | 详情页模块排序、素材排序、拖拽交互 | 直接引入 frontend | P0 | 低 |
| Craft.js | ≈8.7k | https://github.com/prevwong/craft.js | MIT | React 可视化页面搭建器，详情页编辑器底座 | frontend；需要二次封装 | P2 | 中 |

### 10 前端状态 / 接口请求 / 任务流

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| axios | ≈109k | https://github.com/axios/axios | MIT | 统一 API 请求、拦截器、错误处理 | frontend 已可直接接入/复用 | P0 | 低 |
| zustand | ≈58.1k | https://github.com/pmndrs/zustand | MIT | 当前商品、任务、素材、草稿、调研结果状态 | 直接引入 frontend | P0 | 低 |
| TanStack Query | ≈49.5k | https://github.com/TanStack/query | MIT | 接口缓存、任务轮询、加载/失败态管理 | frontend；适合生成任务轮询 | P1 | 低 |

### 11 富文本 / 详情页文案编辑 / 报告编辑

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| Tiptap | ≈36.9k | https://github.com/ueberdosis/tiptap | MIT core / 商业扩展另审 | 商品详情文案、调研报告编辑、富文本模块 | frontend；只用开源 core | P1 | 中 |
| Editor.js | ≈31k | https://github.com/codex-team/editor.js | Apache-2.0 | 块编辑器，适合详情页内容块/调研报告块 | frontend；插件逐个审查 | P2 | 低-中 |
| Slate | ≈30k | https://github.com/ianstormtaylor/slate | MIT | 高度自定义富文本编辑器 | frontend；开发成本较高 | P3 | 低 |

### 12 文件上传 / 拖拽上传 / 素材管理

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| Uppy | ≈30k | https://github.com/transloadit/uppy | MIT | 多文件上传、断点续传、仪表盘组件 | frontend 上传中心 | P1 | 低 |
| react-dropzone | ≈10.5k | https://github.com/react-dropzone/react-dropzone | MIT | 拖拽上传 PDF/图片/Excel | frontend 轻量上传入口 | P0 | 低 |
| FilePond | ≈16k | https://github.com/pqina/filepond | MIT core / 插件审查 | 漂亮上传组件、图片预览、校验 | frontend；品牌/插件授权需审查 | P2 | 中 |

### 13 导出 / 截图 / 生成报告

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| html2canvas | ≈31k | https://github.com/niklasvh/html2canvas | MIT | 页面区域转图片，用于详情页预览导出 | frontend | P2 | 低 |
| jsPDF | ≈30k | https://github.com/parallax/jsPDF | MIT | 浏览器生成 PDF 报告/导出单页 | frontend；复杂中文字体需处理 | P2 | 低-中 |
| Puppeteer | ≈92k | https://github.com/puppeteer/puppeteer | Apache-2.0 | 服务端截图、HTML 转 PDF、导出详情页快照 | 独立 Node 导出服务 | P2 | 低-中 |

### 14 搜索 / 中文分词 / 文本分析

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| jieba-analysis | ≈12k | https://github.com/huaban/jieba-analysis | MIT | 中文分词、标题词根、评论高频词 | Java 后端接入 | P1 | 低 |
| HanLP | ≈36k | https://github.com/hankcs/HanLP | Apache-2.0 | 中文 NLP、关键词、聚类、情感分析基础 | 后端/独立 NLP 服务 | P2 | 低-中 |
| Elasticsearch | ≈73k | https://github.com/elastic/elasticsearch | Elastic License / SSPL 需审查 | 商品资料、竞品、素材全文检索 | 后期独立服务；授权谨慎 | P3 | 高 |

### 15 后台任务 / 队列 / 调度

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| BullMQ | ≈6k | https://github.com/taskforcesh/bullmq | MIT | Node 任务队列，生图/OCR/导出异步任务 | 若引入 Node worker 时使用 | P3 | 低 |
| Quartz | ≈6k | https://github.com/quartz-scheduler/quartz | Apache-2.0 | Java 定时任务、调研任务调度 | 后端 Maven 依赖 | P2 | 低 |
| Temporal | ≈13k | https://github.com/temporalio/temporal | MIT | 复杂工作流编排：调研→生成→导出 | 后期大型化再用 | P3 | 中 |

### 16 API 文档 / 测试 / 质量保障

| 工具 | 星标 | GitHub 地址 | 许可证/授权 | 可用于项目的功能 | 建议接入方式 | 优先级 | 风险 |
|---|---:|---|---|---|---|---|---|
| Swagger UI | ≈28k | https://github.com/swagger-api/swagger-ui | Apache-2.0 | 后端接口文档、调试页面 | Springdoc/OpenAPI 配合使用 | P1 | 低 |
| Postman Newman | ≈7k | https://github.com/postmanlabs/newman | Apache-2.0 | 接口自动化测试、回归检查 | CI 或本地脚本 | P2 | 低 |
| Vitest | ≈14k | https://github.com/vitest-dev/vitest | MIT | 前端单元测试、组件逻辑测试 | frontend 测试体系 | P2 | 低 |

## 推荐项目目录规划

```text
E-commerce detail creation
├─ frontend
│  ├─ PDF.js / pdf-lib / SheetJS / ECharts / dnd-kit / zustand / Excalidraw
│  └─ 页面：市场调研、商品资料、素材库、结果预览、详情页编辑、导出记录
├─ src/main/java
│  ├─ PDFBox / jsoup / ToolAdapter / ResearchService / ExportService
│  └─ 接口：/api/v1/research、/api/v1/tools、/api/v1/export
├─ external-tools
│  ├─ comfyui-service
│  ├─ sd-webui-service
│  ├─ ocr-service
│  └─ crawler-service
└─ docs
   ├─ TOOL_LIBRARY_REVIEW.md
   └─ TOOL_ADAPTER_API.md
```

## 给 Codex 的落地任务

```text
请读取 docs/TOOL_LIBRARY_REVIEW.md、docs/frontend_ui_requirements_merged.md、docs/TOOL_ADAPTER_API.md。

目标：执行“工具融合第一阶段”。

第一批允许直接进入项目的工具：
1. frontend：pdfjs-dist、xlsx、echarts、@dnd-kit/core、@dnd-kit/sortable、@dnd-kit/utilities、zustand、react-dropzone。
2. backend：Apache PDFBox、jsoup。

需要实现：
1. 新建商品资料页面增加 PDF 导入和预览入口。
2. 商品资料列表增加 Excel/CSV 批量导入入口。
3. 市场调研中心增加 Excel/CSV 上传、竞品链接录入、价格带图表、关键词排行、痛点排行空态页面。
4. 详情页编辑页面使用 dnd-kit 做模块拖拽排序。
5. 使用 zustand 管理当前商品、当前任务、选中素材、详情页草稿、调研结果。
6. 后端用 PDFBox 规划 PDF 文本提取接口，用 jsoup 规划公开页面解析接口。

限制：
1. 不直接拷贝 GPL/AGPL 项目源码。
2. 不做反爬绕过、登录绕过、平台限制绕过。
3. 不伪造真实调研数据；没有接口时显示空态、禁用态和待接入提示。
4. 所有新增工具必须在 docs/TOOL_LIBRARY_REVIEW.md 中保留许可证和接入方式说明。
```

## 审查 checklist

- [ ] 仓库是否有明确 LICENSE？
- [ ] 是否允许商用？
- [ ] 是否要求保留版权声明？
- [ ] 是否带水印、标签戳、品牌露出？
- [ ] 是否存在 GPL/AGPL 传染风险？
- [ ] 是否需要生产 license key？
- [ ] 是否依赖云 API 免费额度？
- [ ] 是否需要模型权重？模型权重是否允许商用？
- [ ] 是否可自托管？部署成本是否可接受？
- [ ] 是否能与当前 Spring Boot + React + Ant Design 项目自然融合？
