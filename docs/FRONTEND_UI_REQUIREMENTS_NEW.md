# 电商详情页 AI 工作台前端 UI 需求文档

版本: v1.1
日期: 2026-05-25
适用范围: 当前 P0 完成态前端

## 1. 当前结论

P0 主链路已完成。商品资料、详情页、审核、导出都已接入真实后端接口；AI 入口和少量非 P0 能力继续保留为未来工作，不再把已实现的 P0 接口标成“缺失/待实现”。

## 2. 页面状态

| 页面 | 路由 | 状态 | 说明 |
|---|---|---|---|
| 商品资料列表 | `/materials` | 已完成 | 支持加载、查询、上传、编辑、删除 |
| 商品资料详情 | `/materials/:id` | 已完成 | 支持查看、删除、返回列表 |
| 详情页编辑器 | `/details/:id` | 已完成 | 支持加载、编辑、保存、提交审核、合规审查 |
| 合规审查页 | `/details/:id/review` | 已完成 | 支持风险结果展示与审核提交 |
| 审核中心 | `/audit` | 已完成 | 支持列表、通过、驳回、退回 |
| 导出记录 | `/exports` | 已完成 | 支持列表、发起、查看、下载、删除、重新导出 |
| AI 入口 | 全局 | 预留 | 继续显示“待接入本地 AI 服务” |

## 3. 接口状态

### 3.1 已完成

| 模块 | 功能 | 方法 | 接口路径 |
|---|---|---|---|
| 商品资料 | 上传 | POST | `/api/v1/material/upload` |
| 商品资料 | 列表查询 | GET | `/api/v1/material/list` |
| 商品资料 | 详情查询 | GET | `/api/v1/material/{id}` |
| 商品资料 | 更新 | PUT | `/api/v1/material/{id}` |
| 商品资料 | 删除 | DELETE | `/api/v1/material/{id}` |
| 详情页 | 详情查询 | GET | `/api/v1/detail/{id}` |
| 详情页 | 保存草稿 | PUT | `/api/v1/detail/{id}` |
| 详情页 | 删除 | DELETE | `/api/v1/detail/{id}` |
| 详情页 | 风险检测 | POST | `/api/v1/detail/{id}/risk-check` |
| 详情页 | 获取风险结果 | GET | `/api/v1/detail/{id}/risk` |
| 审核 | 提交审核 | POST | `/api/v1/audit/submit` |
| 审核 | 查询审核记录 | GET | `/api/v1/audit/product/{productDetailId}` |
| 审核 | 列表 | GET | `/api/v1/audit/list` |
| 审核 | 通过 | PUT | `/api/v1/audit/{id}/approve` |
| 审核 | 驳回 | PUT | `/api/v1/audit/{id}/reject` |
| 审核 | 退回 | PUT | `/api/v1/audit/{id}/return` |
| 导出 | 发起导出 | POST | `/api/v1/export/export` |
| 导出 | 查询导出记录 | GET | `/api/v1/export/{id}` |
| 导出 | 列表 | GET | `/api/v1/export/list` |
| 导出 | 下载 | GET | `/api/v1/export/{id}/download` |
| 导出 | 删除 | DELETE | `/api/v1/export/{id}` |
| 导出 | 重新导出 | POST | `/api/v1/export/{id}/reexport` |

### 3.2 仍保留为未来工作

| 模块 | 功能 | 状态 |
|---|---|---|
| 详情页 | `POST /api/v1/detail/generate` | 保留入口，需本地 AI 服务配置后可用 |
| AI | `/api/v1/ai/*` | 未来工作 |
| 导出 | PDF | 未来工作 |
| 企业能力 | CMS / SSO / 租户隔离 / 完整权限与审计 | 未来工作 |

## 4. 前端规则

- `materials` 页查询按钮必须走 `api.material.list(...)`。
- 如果后端只支持 keyword，品牌/类目/状态筛选只做前端过滤。
- 编辑必须复用现有上传表单或等价轻量 UI，调用 `api.material.update(id, payload)`。
- 删除必须调用 `api.material.remove(id)` 后刷新列表。
- AI 按钮保持禁用态与提示文案，不展示“已实现”。

## 5. 版本边界

- P0: 已完成并可用。
- P1/P2: 仅保留明确的未来工作标记。

## 6. P2 Backend Task APIs

| Domain | Method | Path | Notes |
|---|---|---|---|
| Research tasks | GET | `/api/v1/research/tasks/list` | Real persisted task list, no fake research rows |
| Research tasks | POST | `/api/v1/research/tasks` | Create a real task record |
| Research tasks | GET | `/api/v1/research/tasks/{id}` | Load task detail |
| Research tasks | PUT | `/api/v1/research/tasks/{id}/status` | Update task status only |
| Research tasks | PUT | `/api/v1/research/tasks/{id}/result` | Persist real chart result JSON |
| Research tasks | GET | `/api/v1/research/tasks/{id}/charts` | Return stored chart data or empty arrays |
| OCR tasks | GET | `/api/v1/assets/ocr-tasks/list` | Real persisted OCR task list |
| OCR tasks | POST | `/api/v1/assets/ocr-tasks` | Create OCR task record |
| OCR tasks | GET | `/api/v1/assets/ocr-tasks/{id}` | Load OCR task detail |
| OCR tasks | PUT | `/api/v1/assets/ocr-tasks/{id}/status` | Update OCR status/progress |
| OCR tasks | PUT | `/api/v1/assets/ocr-tasks/{id}/result` | Persist real OCR text/confidence |
| Design drafts | GET | `/api/v1/design-drafts/list` | List saved Excalidraw drafts |
| Design drafts | POST | `/api/v1/design-drafts` | Create a draft |
| Design drafts | GET | `/api/v1/design-drafts/{id}` | Load a draft |
| Design drafts | PUT | `/api/v1/design-drafts/{id}` | Update draft scene JSON |
| Detail module order | GET | `/api/v1/detail/{id}/module-order` | Load persisted module order |
| Detail module order | PUT | `/api/v1/detail/{id}/module-order` | Save module order JSON array |
