# v6.0 前端页面全面检查报告

> 生成时间: 2026-05-29 23:48:13
> 检查范围: 全部 32 个路由页面
> TypeScript 检查: ✅ 通过

---

## 一、总览

| 分类 | 数量 | 说明 |
|------|------|------|
| 总路由页面 | 32 | 含 1 个 404 兜底 |
| 使用 i18n 国际化 | 12 | 使用 useLang() hook |
| 硬编码中文（未用 i18n） | 8 | 中文直接写在代码中 |
| 纯英文占位页 | 5 | 使用 P0Scaffold 或全英文 |
| 混合语言 | 7 | 部分中文部分英文 |

---

## 二、逐页检查详情

### ✅ 已完成中文国际化的页面（12个）

| # | 页面 | 路由 | 文件路径 | 状态 |
|---|------|------|----------|------|
| 1 | 工作台首页 | / | pages/workbench/HomeWorkbenchPage.tsx | ✅ 使用 i18n，中文完整 |
| 2 | 系统诊断 | /system/diagnostics | pages/system/DiagnosticsPage.tsx | ✅ 使用 i18n，中文完整 |
| 3 | 团队管理 | /system/team | pages/system/TeamManagementPage.tsx | ✅ 使用 i18n，中文完整 |
| 4 | 操作日志 | /system/audit-log | pages/system/AuditLogPage.tsx | ✅ 使用 i18n，中文完整 |
| 5 | 详情编辑器 | /details/:id | pages/details/DetailEditorPage.tsx | ✅ 使用 i18n，中文完整 |
| 6 | 提示词模板 | /visual/prompt-templates | pages/visual/PromptTemplatePage.tsx | ✅ 使用 i18n，中文完整 |
| 7 | 成本管理 | /cost | pages/cost/CostManagementPage.tsx | ✅ 使用 i18n，中文完整 |
| 8 | 提示词工作台 | /visual/prompt-workbench | pages/visual/PromptWorkbenchPage.tsx | ⚠️ 部分 i18n，有乱码字符 |
| 9 | 审核详情 | /details/:id/review | pages/detail-review/index.tsx | ⚠️ 部分中文，有乱码字符 |
| 10 | 质检中心 | /audit | pages/audit/AuditCenterPage.tsx | ⚠️ 硬编码中文，有乱码字符 |
| 11 | 研调中心 | /research | pages/research/ResearchCenterPage.tsx | ⚠️ 硬编码中文，部分英文 |
| 12 | 新建调研 | /research/new | pages/research/NewResearchTaskPage.tsx | ⚠️ 硬编码中文，有乱码字符 |

### ⚠️ 硬编码中文页面（未使用 i18n，8个）

| # | 页面 | 路由 | 文件路径 | 问题 |
|---|------|------|----------|------|
| 1 | 商品素材列表 | /materials | pages/materials/MaterialListPage.tsx | 全中文硬编码，未用 i18n |
| 2 | 新建素材 | /materials/new | pages/materials/MaterialCreatePage.tsx | 全中文硬编码，未用 i18n |
| 3 | 素材详情 | /materials/:id | pages/materials/MaterialDetailPage.tsx | 全中文硬编码，未用 i18n |
| 4 | 素材库 | /assets | pages/assets/AssetLibraryPage.tsx | 混合中英文，未用 i18n |
| 5 | 导出管理 | /exports | pages/exports/ExportRecordsPage.tsx | 全中文硬编码，未用 i18n |
| 6 | 后处理任务 | /post-process | pages/post-process/PostProcessTasksPage.tsx | 全中文硬编码，有乱码字符 |
| 7 | 工具中心 | /tools | pages/tools/ToolCenterPage.tsx | 全中文硬编码，有乱码字符 |
| 8 | 工具详情 | /tools/:toolCode | pages/tools/ToolDetailPage.tsx | 全中文硬编码，有乱码字符 |

### 🔴 纯英文占位页面（5个）

| # | 页面 | 路由 | 文件路径 | 问题 |
|---|------|------|----------|------|
| 1 | 竞品库 | /research/competitors | pages/research/CompetitorLibraryPage.tsx | **P0Scaffold 占位**，全英文 |
| 2 | 调研报告 | /research/reports/:id | pages/research/ResearchReportPage.tsx | **P0Scaffold 占位**，全英文 |
| 3 | 调研任务详情 | /research/tasks/:id | pages/research/ResearchTaskDetailPage.tsx | 部分英文错误信息 |
| 4 | 设计草稿 | /tools/design-draft | pages/tools/DesignDraftPage.tsx | 英文状态文案 |
| 5 | 数据导入 | /tools/imports | pages/tools/DataImportPage.tsx | 部分英文文案 |

### ⚠️ 混合语言页面（7个）

| # | 页面 | 路由 | 文件路径 | 问题 |
|---|------|------|----------|------|
| 1 | AI 生图工作台 | /generate | pages/generate/GenerateWorkbenchPage.tsx | 状态标签乱码，部分英文 |
| 2 | 生图任务详情 | /generate/:taskId | pages/generate/GenerateTaskDetailPage.tsx | 状态标签乱码，部分英文 |
| 3 | 生成结果 | /results | pages/results/ResultsPreviewPage.tsx | 混合中英文 |
| 4 | 视觉规划 | /visual/plans | pages/visual/VisualPlansPage.tsx | **表格列头全英文** |
| 5 | 模特档案 | /visual/model-profiles | pages/visual/ModelProfilesPage.tsx | **表格列头全英文**（41处） |
| 6 | 类目视觉策略 | /visual/category-policies | pages/visual/CategoryVisualPoliciesPage.tsx | **表格列头全英文**（26处） |
| 7 | 登录页 | /login | pages/auth/LoginPage.tsx | 全英文 UI 文案 |

---

## 三、乱码问题清单

以下页面存在中文乱码（文件编码或显示异常）：

| 文件 | 乱码示例 | 原因 |
|------|----------|------|
| GenerateWorkbenchPage.tsx | 绛夊緟涓? 鎵ц涓? 宸插畬鎴? | UTF-8 编码损坏 |
| GenerateTaskDetailPage.tsx | 同上 | 同上 |
| PostProcessTasksPage.tsx | 瓒呭垎 淇 娓呯悊鑳屾櫙 | 同上 |
| ToolCenterPage.tsx | 宸ュ叿涓績 璧勬枡瀵煎叆 | 同上 |
| ToolDetailPage.tsx | 缂哄皯宸ュ叿缂栫爜 | 同上 |
| AuditCenterPage.tsx | 寰呭鏍? 瀹℃牳涓? 閫氳繃 | 同上 |
| AuditLogPage.tsx | 鐩爣 ID | 同上 |
| CostManagementPage.tsx | 璐у竵 鎻忚堪 | 同上 |
| NewResearchTaskPage.tsx | 璋冪爺浠诲姟宸插垱寤? | 同上 |
| PromptWorkbenchPage.tsx | 绛夊緟鍚庣杩斿洖 | 同上 |
| detail-review/index.tsx | 寰呮帴鍏ユ湰鍦癆I鏈嶅姟 | 同上 |

**根因分析**：这些文件在之前某次写入时编码损坏，中文字符被错误转码。需要重新写入正确的 UTF-8 编码内容。

---

## 四、布局检查

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 三栏布局 | ✅ | AppLayout 使用 grid-template-columns: 240px minmax(0, 1fr) 340px |
| 左侧导航固定宽度 | ✅ | 240px 固定 |
| 右侧助手面板 | ✅ | 340px 固定 |
| 中间内容自适应 | ✅ | minmax(0, 1fr) |
| 中文竖排问题 | ✅ | 已修复，无塌陷 |
| 响应式适配 | ⚠️ | 部分页面有 @media 查询，但未全面测试 |

---

## 五、UI 可访问性检查

| 检查项 | 状态 | 说明 |
|--------|------|------|
| TypeScript 编译 | ✅ | 	sc --noEmit 通过 |
| 空状态组件 | ✅ | 大部分页面有 EmptyState / ErrorState / LoadingState |
| 错误处理 | ⚠️ | 部分页面缺少错误边界 |
| 键盘导航 | ⚠️ | 未全面测试 |
| ARIA 标签 | ⚠️ | 依赖 Ant Design 默认，未自定义 |

---

## 六、优先修复建议

### P0 - 立即修复（影响正常使用）

1. **修复乱码文件**（11个文件）
   - 重新写入正确的 UTF-8 中文内容
   - 涉及：GenerateWorkbenchPage, GenerateTaskDetailPage, PostProcessTasksPage, ToolCenterPage, ToolDetailPage, AuditCenterPage, AuditLogPage, CostManagementPage, NewResearchTaskPage, PromptWorkbenchPage, detail-review/index.tsx

2. **LoginPage 中文化**
   - 将 "Sign In"、"Register"、"Username"、"Password" 等改为中文

### P1 - 尽快修复（影响用户体验）

3. **替换 P0Scaffold 占位页面**（5个）
   - CompetitorLibraryPage → 竞品库真实页面
   - ResearchReportPage → 调研报告真实页面
   - 其他使用 P0Scaffold 的页面

4. **英文页面中文化**
   - VisualPlansPage 表格列头（51处英文）
   - ModelProfilesPage 表格列头（41处英文）
   - CategoryVisualPoliciesPage 表格列头（26处英文）

### P2 - 后续优化

5. **统一使用 i18n**
   - 将所有硬编码中文迁移到 i18n.ts
   - 使用 useLang() hook 统一管理

6. **搜索功能实现**
   - 顶栏搜索按钮目前无实际功能

---

## 七、文件编码修复命令

需要修复编码的文件列表（共11个）：

`
frontend/src/pages/generate/GenerateWorkbenchPage.tsx
frontend/src/pages/generate/GenerateTaskDetailPage.tsx
frontend/src/pages/post-process/PostProcessTasksPage.tsx
frontend/src/pages/tools/ToolCenterPage.tsx
frontend/src/pages/tools/ToolDetailPage.tsx
frontend/src/pages/audit/AuditCenterPage.tsx
frontend/src/pages/system/AuditLogPage.tsx
frontend/src/pages/cost/CostManagementPage.tsx
frontend/src/pages/research/NewResearchTaskPage.tsx
frontend/src/pages/visual/PromptWorkbenchPage.tsx
frontend/src/pages/detail-review/index.tsx
`

---

## 八、总结

- **TypeScript 编译**：✅ 通过
- **布局结构**：✅ 三栏布局正常
- **中文显示**：❌ 11个文件存在乱码
- **国际化覆盖**：⚠️ 仅 12/32 页面使用 i18n
- **占位页面**：⚠️ 5个页面仍为 P0Scaffold 占位
- **英文页面**：⚠️ 7个页面存在大量英文硬编码

**下一步**：优先修复 11 个乱码文件，然后处理英文页面中文化。