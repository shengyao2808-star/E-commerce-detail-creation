# 需求文档索引

日期：2026-05-24

## 当前有效文档

| 文档 | 用途 |
|---|---|
| `API_CONTRACT_REVIEW.md` | 当前后端 REST API 对第一版 UI 的支撑情况 |
| `docs/FRONTEND_UI_REQUIREMENTS_NEW.md` | 第一版前端 UI 需求，限定只接真实可用后端能力 |
| `docs/TOOL_LIBRARY.md` | 可自部署、可镜像、可替换的 GitHub 主工具库清单 |
| `docs/TOOL_ADAPTER_API.md` | 工具适配器统一调用接口 |
| `docs/VISUAL_DETAIL_REQUIREMENTS.md` | 视觉详情页、生图、模特库、SKC、图转 prompt 需求补充 |

## 重要口径

1. 当前 `AIUtil` 文本生成仍未实现，调用会抛 `UnsupportedOperationException`。
2. 新增的 `tool-adapter` 是第三方工具统一调用入口，不等于已经内置 ComfyUI、LLaVA 等工具。
3. 所有工具默认关闭，必须由客户私有部署后配置 `tools.adapters.*.base-url` 才能调用。
4. GitHub 工具只选择星标不少于 10k 的项目；低于 10k 的项目不作为主工具推荐。
5. 市场调研不做反扒绕过，只支持客户授权数据、公开允许数据、人工导入和合规 API。
6. 大型 AI/Python 工具不直接合并进 Java 主仓库源码，采用私有镜像/独立服务/worker 方式接入。

## 下一步实现顺序建议

1. 完成第一版 UI 所需的后端 CRUD、列表、下载、风险检查接口。
2. 接入文本模型中转站，替换 `AIUtil` 的未实现状态。
3. 实现业务级视觉模块：类目视觉策略、模特库、SKC 策略、提示词工作台。
4. 使用 `tool-adapter` 对接 LLaVA 图片转 prompt 和 ComfyUI 生图任务。
5. 接入 Grounded-SAM、IOPaint、Real-ESRGAN、ImageMagick 完成出图后处理。
6. 用 Playwright 做详情页预览和视觉验收。

