# API 契约审查报告

> **审查日期**: 2026-05-25  
> **审查范围**: 后端 REST API 与前端 service 层契约同步  
> **当前前缀**: `/api/v1`

## 1. 当前结论

当前 Controller 统一依赖全局 context-path `/api/v1`，Controller 内未重复拼接 `/api`。

前端 `frontend/src/services/api.ts` 已按 `/api/v1` 接入 `material / detail / audit / export` 四个 service 面，并补齐了 risk 接口调用。

当前状态可以概括为:
- `已暴露`: Controller 与前端 service 都已出现对应入口
- `待联调`: 接口已暴露，但仍受 AI 配置、返回形态或并行模块阻塞影响，尚未完成运行验证
- `仍缺口`: 后端/前端契约层还没有对应接口

`已联调`：暂无。当前只完成静态契约对齐，后端全量编译仍受并行 Audit 服务实现问题影响。

## 2. 已暴露接口清单

### 2.1 Material

| 接口 | 状态 | 前端 service | 备注 |
|---|---|---|---|
| `POST /api/v1/material/upload` | 已暴露 | `materialApi.upload` | 上传入口可用 |
| `GET /api/v1/material/{id}` | 已暴露 | `materialApi.get` | 详情查询可用 |
| `GET /api/v1/material/list` | 已暴露 | `materialApi.list` | 支持 `pageNum/pageSize/keyword` |
| `PUT /api/v1/material/{id}` | 已暴露 | `materialApi.update` | 编辑入口已补齐 |
| `DELETE /api/v1/material/{id}` | 已暴露 | `materialApi.remove` | 删除入口已补齐 |

### 2.2 Detail / Risk

| 接口 | 状态 | 前端 service | 备注 |
|---|---|---|---|
| `POST /api/v1/detail/generate` | 待联调 | `detailApi.generate` | 依赖本地 AI 中转站 / `AIUtil`，未配置时会真实失败 |
| `GET /api/v1/detail/{id}` | 已暴露 | `detailApi.get` | 详情查询可用 |
| `GET /api/v1/detail/list` | 已暴露 | `detailApi.list` | 支持 `pageNum/pageSize/keyword/status` |
| `PUT /api/v1/detail/{id}` | 已暴露 | `detailApi.update` | 草稿保存已补齐 |
| `DELETE /api/v1/detail/{id}` | 已暴露 | `detailApi.remove` | 删除入口已补齐 |
| `POST /api/v1/detail/{id}/risk-check` | 已暴露 | `detailApi.riskCheck` | 风险检测接口已补齐 |
| `GET /api/v1/detail/{id}/risk` | 已暴露 | `detailApi.getRisk` | 风险结果查询已补齐 |
| `POST /api/v1/detail/{id}/regenerate` | 待联调 | `detailApi.regenerate` | 仍依赖本地 AI 中转站，未配置时会失败 |

### 2.3 Audit

| 接口 | 状态 | 前端 service | 备注 |
|---|---|---|---|
| `POST /api/v1/audit/submit` | 已暴露 | `auditApi.submit` | 提交审核可用 |
| `GET /api/v1/audit/product/{productDetailId}` | 已暴露 | `auditApi.getByProduct` | 按详情 ID 查询审核记录 |
| `GET /api/v1/audit/list` | 已暴露 | `auditApi.list` | 支持 `pageNum/pageSize/status/auditor` |
| `PUT /api/v1/audit/{id}/approve` | 已暴露 | `auditApi.approve` | 审核通过可用 |
| `PUT /api/v1/audit/{id}/reject` | 已暴露 | `auditApi.reject` | 审核驳回可用 |
| `PUT /api/v1/audit/{id}/return` | 已暴露 | `auditApi.returnForRevision` | 退回修改可用 |
| `GET /api/v1/audit/{id}` | 仍缺口 | 无 | 审核记录按 ID 详情查询仍未提供 |

### 2.4 Export

| 接口 | 状态 | 前端 service | 备注 |
|---|---|---|---|
| `POST /api/v1/export/export` | 已暴露 | `exportApi.create` | 请求体已对齐为 `productDetailId/exportFormat/exporter` |
| `GET /api/v1/export/{id}` | 已暴露 | `exportApi.get` | 导出记录详情可用 |
| `GET /api/v1/export/list` | 已暴露 | `exportApi.list` | 支持 `pageNum/pageSize/status/exporter` |
| `GET /api/v1/export/{id}/download` | 待联调 | `exportApi.download` | 当前返回文件路径字符串，前端仍需完成真实下载动作 |
| `DELETE /api/v1/export/{id}` | 已暴露 | `exportApi.remove` | 删除导出记录已补齐 |
| `POST /api/v1/export/{id}/reexport` | 已暴露 | `exportApi.reexport` | 重新导出接口已补齐 |

## 3. 返回包装约定

- Material / Detail 的列表接口按 `Result.success(PageResult)` 返回
- Audit / Export 的列表接口按 `PageResult` 直接返回
- 单条查询、提交、删除、审核、风险检测等接口按 `Result.success(...)` 返回

前端 `frontend/src/services/api.ts` 已分别按上述返回形态接入。

## 4. 仍未完成能力

以下能力仍不能作为已完成契约承诺:

1. AI 生成依赖本地 AI 中转站配置，未配置时 `detail/generate` 与 `detail/{id}/regenerate` 会真实失败
2. PDF 导出未完成
3. CMS 对接未完成
4. SSO 集成未完成
5. 租户隔离未完成
6. OCR 解析未完成
7. Word 解析链路未完成

## 5. 需要继续保留的风险

- `GET /api/v1/audit/{id}` 仍缺口，审核详情页如果需要按审核记录 ID 查看详情，后端还要补接口
- `GET /api/v1/export/{id}/download` 目前还是路径级返回，不是文件流下载
- 全量后端编译仍受并行 Audit 实现问题影响，当前无法完成整仓运行验证

## 6. 结论

Material、Detail、Audit、Export、Risk 的核心接口已经从“缺失/TODO”同步为“已暴露/待联调/仍缺口”的准确状态。

当前最关键的剩余缺口是:
- 审核记录按 ID 查询
- AI 生成的运行联调
- PDF / CMS / SSO / 租户隔离 / OCR / Word 解析
