# 工具库清单

版本：v0.1  
日期：2026-05-24  
原则：只纳入 GitHub 星标不少于 10k、可自部署或可镜像化、可被替换的工具。默认不依赖外部 SaaS，不把大型 Python/AI 仓库源码直接塞进 Java 主工程。

## 接入原则

1. 主业务系统只通过 `tool-adapter` 调用工具，不直接依赖第三方工具内部实现。
2. 工具优先用私有化 Docker 镜像、内网 HTTP 服务或独立 worker 运行。
3. 每个工具锁定版本、镜像、许可证和调用契约，避免远程项目下线影响交付。
4. 未配置工具时必须返回 501，不允许返回假内容。
5. 工具调用失败返回 502，业务草稿、任务记录和人工流程不能丢失。

## 主工具选择

| 环节 | 主工具 | GitHub | 星标 | 许可证 | 接入方式 | 说明 |
|---|---|---|---:|---|---|---|
| 市场资料授权采集 | Crawl4AI | https://github.com/unclecode/crawl4ai | 66171 | Apache-2.0 | 自部署 HTTP 服务 | 用于授权页面、公开资料、客户导入链接的结构化抽取 |
| 浏览器型调研任务 | Browser Use | https://github.com/browser-use/browser-use | 95302 | MIT | 自部署 HTTP 服务 | 用于需要浏览器执行的人工授权调研，不做反扒绕过 |
| 批量采集 worker | Scrapy | https://github.com/scrapy/scrapy | 61801 | BSD-3-Clause | 私有 worker | 用于客户自有站点、授权站点、历史资料库批量采集 |
| 提示词流程评测 | Promptflow | https://github.com/microsoft/promptflow | 11131 | MIT | 自部署 HTTP 服务 | 用于提示词模板、A/B 评测、回归测试 |
| 提示词与模型观测 | Langfuse | https://github.com/langfuse/langfuse | 27817 | NOASSERTION | 可选自部署服务 | 许可证需商务复核，首版只作为可选观测工具 |
| 图片转提示词 | LLaVA | https://github.com/haotian-liu/LLaVA | 24828 | Apache-2.0 | 自部署视觉语言服务 | 用于用户导入图片后生成视觉描述和生图提示词 |
| 生图编排 | ComfyUI | https://github.com/Comfy-Org/ComfyUI | 114287 | GPL-3.0 | 独立自部署服务 | GPL 工具只作为进程外服务调用，不链接进闭源主工程 |
| 姿态/结构控制 | ControlNet | https://github.com/lllyasviel/ControlNet | 33893 | Apache-2.0 | ComfyUI 工作流节点/独立服务 | 女装模特姿态、产品结构、边缘/深度控制 |
| 分割与遮罩 | Grounded-SAM | https://github.com/IDEA-Research/Grounded-Segment-Anything | 17582 | Apache-2.0 | 自部署服务 | 商品主体、模特、背景分割 |
| 修补/清理 | IOPaint | https://github.com/Sanster/IOPaint | 23131 | Apache-2.0 | 自部署服务 | 背景清理、瑕疵修补、局部重绘 |
| 超分增强 | Real-ESRGAN | https://github.com/xinntao/Real-ESRGAN | 35525 | BSD-3-Clause | 自部署服务 | 主图、长图切片最终增强 |
| 拼接/裁切/格式转换 | ImageMagick | https://github.com/ImageMagick/ImageMagick | 16510 | NOASSERTION | 本地 CLI 或 worker | 长图拼接、尺寸归一、格式转换；许可证需随发行包复核 |
| 视觉验收 | Playwright | https://github.com/microsoft/playwright | 89319 | Apache-2.0 | 本地 CLI 或 worker | 详情页 HTML 预览、截图验收、布局检查 |

## 每个环节使用的最恰当工具

| 业务环节 | 工具 | 输入 | 输出 | 是否 P0 |
|---|---|---|---|---|
| 授权资料导入和网页摘要 | Crawl4AI | URL、客户授权说明、抓取范围 | Markdown、结构化字段、来源 URL | 否 |
| 市场视觉/热点资料人工授权调研 | Browser Use | 调研任务、平台、关键词、限制条件 | 调研摘要、截图引用、证据链 | 否 |
| 大批量资料采集 | Scrapy | 种子 URL、站点规则、频率限制 | 结构化 JSON、采集日志 | 否 |
| 提示词引导和扩写 | 中转站文本模型 + Promptflow | 商品资料、类目策略、视觉风格、目标画幅 | 标准化 prompt、negative prompt、镜头脚本 | 是 |
| 图片转提示词 | LLaVA | 用户导入图片、分析目标 | 图片描述、风格标签、可复用 prompt | 是 |
| 生图任务编排 | ComfyUI | prompt、参考图、模特库、ControlNet 条件、画幅 | 生成图片、任务状态、历史记录 | 是 |
| 女装统一模特控制 | ControlNet + ComfyUI | 模特三视图、姿态、身高体重三围、服装 SKC | 统一模特、多姿态主图和详情图 | 是 |
| 商品/模特分割 | Grounded-SAM | 原图、目标描述 | mask、分割图层 | 是 |
| 瑕疵修补和背景清理 | IOPaint | 原图、mask、修补说明 | 清理后的图片 | 是 |
| 清晰度增强 | Real-ESRGAN | 生成图或实物图 | 高清图 | 是 |
| 长图切片拼接 | ImageMagick | 多屏详情图、主图集合、尺寸规范 | 1:1 主图、3:4 主图、详情长图 | 是 |
| 视觉回归验收 | Playwright | HTML 预览、导出文件 | 截图、布局检查报告 | 否 |
| 提示词版本复盘 | Langfuse | prompt、模型、输出、人工评分 | 版本记录、效果趋势 | 否 |

## 许可证和商业风险

| 工具 | 风险 | 处理策略 |
|---|---|---|
| ComfyUI / Fooocus | GPL-3.0 | 不把源码并入 Java 主工程；只作为客户私有环境中的独立服务调用 |
| Langfuse / ImageMagick | GitHub API 返回 NOASSERTION | 商业化前由法务/交付复核实际发行包许可证 |
| 市场调研工具 | 平台条款风险 | 只处理客户授权、公开允许、人工导入或合规 API 数据，不做反扒绕过 |
| 生图模型权重 | 模型授权风险 | 模型注册表必须记录来源、许可证、商用范围和客户确认 |

## 版本固化要求

每个客户私有部署必须记录：

- 工具名称和 GitHub URL
- 具体 release/tag/commit
- 镜像名和 digest
- 模型权重来源、许可证、hash
- 调用接口版本
- 回滚版本
- 健康检查地址

