# API 合约审查报告（P2 后）

> 审查日期：2026-05-26  
> 审查范围：后端 Controller 暴露接口与前端 `frontend/src/services/api.ts` 对齐情况  
> 全局前缀：`/api/v1`

## 1. 结论摘要

当前前后端合约已覆盖 P2 要求，且以下四类接口已经存在并接入：

1. 研究任务 API（research tasks）
2. OCR 任务 API（asset OCR tasks）
3. 设计草稿 API（design drafts）
4. 详情模块顺序 API（detail module-order）

同时，本报告不将“接口存在”误写为“全链路能力完全生产化”。

## 2. P2 关键接口对齐结果

### 2.1 研究任务（已对齐）

| 接口 | 后端暴露 | 前端 service |
|---|---|---|
| `GET /api/v1/research/tasks/list` | 是 | `researchTaskApi.list` |
| `POST /api/v1/research/tasks` | 是 | `researchTaskApi.create` |
| `GET /api/v1/research/tasks/{id}` | 是 | `researchTaskApi.get` |
| `PUT /api/v1/research/tasks/{id}/status` | 是 | `researchTaskApi.updateStatus` |
| `PUT /api/v1/research/tasks/{id}/result` | 是 | `researchTaskApi.updateResult` |
| `GET /api/v1/research/tasks/{id}/charts` | 是 | `researchTaskApi.charts` |

### 2.2 OCR 任务（已对齐）

| 接口 | 后端暴露 | 前端 service |
|---|---|---|
| `GET /api/v1/assets/ocr-tasks/list` | 是 | `assetOcrTaskApi.list` |
| `POST /api/v1/assets/ocr-tasks` | 是 | `assetOcrTaskApi.create` |
| `GET /api/v1/assets/ocr-tasks/{id}` | 是 | `assetOcrTaskApi.get` |
| `PUT /api/v1/assets/ocr-tasks/{id}/status` | 是 | `assetOcrTaskApi.updateStatus` |
| `PUT /api/v1/assets/ocr-tasks/{id}/result` | 是 | `assetOcrTaskApi.updateResult` |

### 2.3 设计草稿（已对齐）

| 接口 | 后端暴露 | 前端 service |
|---|---|---|
| `GET /api/v1/design-drafts/list` | 是 | `designDraftApi.list` |
| `POST /api/v1/design-drafts` | 是 | `designDraftApi.create` |
| `GET /api/v1/design-drafts/{id}` | 是 | `designDraftApi.get` |
| `PUT /api/v1/design-drafts/{id}` | 是 | `designDraftApi.update` |

### 2.4 详情模块顺序（已对齐）

| 接口 | 后端暴露 | 前端 service |
|---|---|---|
| `GET /api/v1/detail/{id}/module-order` | 是 | `detailApi.getModuleOrder` |
| `PUT /api/v1/detail/{id}/module-order` | 是 | `detailApi.updateModuleOrder` |

## 3. 其余核心域简要校对

- Material：上传/详情/列表/更新/删除已对齐。
- Detail：生成、详情、列表、更新、删除、风控检测、风控结果、再生成已对齐。
- Audit：提交、按 ID 查询、按商品查询、列表、approve/reject/return/withdraw/reaudit 已对齐。
- Export：创建、详情、列表、下载、删除、重导出已对齐。

## 4. 不应过度承诺的点

以下属于“存在接口但不代表全链路完成”的范畴：

1. 任务执行编排：接口已可持久化任务状态/结果，但不等于所有任务都有完整 worker 调度与自动轮询闭环。
2. AI 生成依赖环境配置：`/detail/generate` 与 `/detail/{id}/regenerate` 在未完成 AI 配置时会失败。
3. 跨系统能力（如 CMS、SSO、多租户等）不在本次 P2 合约完成声明范围内。

## 5. 本次修订说明

- 已清理文档乱码（编码异常文本）。
- 已修正错误结论：`GET /api/v1/audit/{id}` 实际已存在，不应标记为缺失。
- 已补齐并明确 P2 新增四类接口的“已存在且已接入”状态。
