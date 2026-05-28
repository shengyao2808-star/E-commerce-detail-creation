# DetailFlow v5.0 → v6.0 改进方案

> 基于源代码全量审计，从高频使用者视角制定的系统性改进路线图。
> 目标：从"开发脚手架"升级为"可交付的 AI SaaS 产品"。

---

## 一、问题全景

### 问题分布

| 类别 | 数量 | 严重度 |
|------|------|--------|
| 安全缺失 | 3 项 | P0 致命 |
| 功能空壳 | 6 个页面 | P0 阻塞 |
| 代码重复 | 5 个工具函数 x7 处 | P1 严重 |
| 视觉偏离 | 主题/乱码/组件滥用 | P1 严重 |
| 功能缺失 | 4 项核心能力 | P2 重要 |
| 体验细节 | 8 项 | P3 优化 |

### 当前状态量化

```
后端:  27 Controller | 27 ServiceImpl | 152 Tests (全部通过)
前端:  37 页面 | 29 路由 | 23 导航项 | TypeScript 编译通过
工具:  12 适配器定义 (全部 disabled)
AI:    Relay 配置存在 (enabled=false)
安全:  无认证 | 无鉴权 | 明文密码
```

---

## 二、改进路线图

### Phase 1: 基础修复（预计 3 天）

> 目标：消除阻塞级问题，让系统可以安全运行、中文正常显示。

#### 1.1 修复中文乱码 [P0]

**范围：** 所有包含 mojibake 的前端页面和后端 Java 文件

**前端涉及文件（约 15 个）：**
- `HomeWorkbenchPage.tsx` — 首页工作台
- `ToolCenterPage.tsx` — 工具中心
- `AuditCenterPage.tsx` — 审核中心
- `PromptWorkbenchPage.tsx` — 提示词工作台
- `GenerateWorkbenchPage.tsx` — 生图工作台
- `GenerateTaskDetailPage.tsx` — 任务详情
- `MaterialListPage.tsx` — 素材列表
- `MaterialDetailPage.tsx` — 素材详情
- `ResultsPreviewPage.tsx` — 结果预览
- `AssetLibraryPage.tsx` — 素材库
- `DesignDraftPage.tsx` — 设计草稿
- `DataImportPage.tsx` — 数据导入
- `DetailEditorPage.tsx` — 详情编辑器
- `ExportRecordsPage.tsx` — 导出记录
- `PostProcessTasksPage.tsx` — 后处理任务

**后端涉及文件（约 8 个）：**
- `Result.java` — 统一响应
- `PageResult.java` — 分页响应
- `GlobalExceptionHandler.java` — 异常处理
- 各 ServiceImpl 的日志消息

**方案：**
1. 逐文件用 `[System.IO.File]::ReadAllText()` 以 `windows-1252` 编码读取
2. 将乱码字符替换为正确的 UTF-8 中文
3. 用 `[System.Text.UTF8Encoding]::new($false)` 无 BOM 写回
4. 逐文件 `npm test` / `mvn test` 验证

**验收标准：**
- 所有页面中文正常显示
- `mvn test` 通过
- `npm test` 通过

---

#### 1.2 添加安全认证 [P0]

**新增依赖：**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
```

**新增文件：**
- `config/SecurityConfig.java` — Spring Security 配置
- `config/JwtUtil.java` — JWT 工具类
- `config/JwtAuthFilter.java` — JWT 认证过滤器
- `controller/AuthController.java` — 登录/注册/刷新 Token
- `service/UserAccountService.java` — 用户账户服务
- `entity/UserAccount.java` — 用户账户实体
- DB 表 `user_account`

**安全策略：**
```
公开接口 (无需认证):
  POST /auth/login
  POST /auth/register
  GET  /actuator/health

受保护接口 (需要 JWT):
  所有其他 /api/v1/* 接口

CORS 策略:
  仅允许配置的前端域名
```

**前端改动：**
- 新增 `services/auth.ts` — 登录/登出/Token 管理
- 新增 `pages/auth/LoginPage.tsx` — 登录页
- `api.ts` 中所有请求自动附加 `Authorization: Bearer <token>`
- 401 响应自动跳转登录页

**验收标准：**
- 未登录访问任何 API 返回 401
- 登录后正常访问所有接口
- Token 过期自动刷新

---

#### 1.3 数据库凭据安全 [P0]

**改动：**
- `application.yml` 中密码改为环境变量引用 `${DB_PASSWORD}`
- 新增 `application-dev.yml` 用于本地开发
- `.gitignore` 确保不提交敏感配置
- 新增 `docker-compose.yml` 示例，含环境变量模板

---

### Phase 2: 代码治理（预计 2 天）

> 目标：消除技术债，建立可维护的代码基础。

#### 2.1 提取前端公共工具 [P1]

**新建 `utils/format.ts`：**
```typescript
// 从 7 个文件中提取
export function formatDateTime(value?: string): string
export function formatProgress(value?: number): string
export function safeJsonStringify(value: unknown): string
export function normalizeStatus(status?: string): string
export function isTerminalStatus(status?: string): boolean
```

**新建 `utils/statusMeta.ts`：**
```typescript
// 统一状态元数据
export function getStatusMeta(status?: string): StatusMeta
export function getAuditStatusMeta(status: AuditStatusCode): StatusMeta
export function getRiskMeta(code?: RiskLevelCode): RiskMeta
```

**新建 `utils/notice.ts`：**
```typescript
// 统一通知类型
export type NoticeTone = "info" | "success" | "warning" | "error"
export type Notice = { tone: NoticeTone; title: string; message: string }
```

**涉及重构的文件：**
- `GenerateWorkbenchPage.tsx` — 删除本地 formatDateTime, normalizeStatus, Notice
- `GenerateTaskDetailPage.tsx` — 同上
- `DetailEditorPage.tsx` — 删除本地 toList, toMultiline
- `ResultsPreviewPage.tsx` — 删除本地 formatDateTime, safeJsonStringify
- `MaterialListPage.tsx` — 删除本地 formatDateTime
- `MaterialDetailPage.tsx` — 删除本地 formatDateTime
- `AuditCenterPage.tsx` — 删除本地 formatDateTime, 状态元数据

**验收标准：**
- 以上文件不再包含重复函数
- 所有页面使用统一 import
- `npm test` 通过

---

#### 2.2 移除 P0Scaffold 占位组件 [P1]

**现状：** 13 个页面使用 `P0Scaffold`，向用户展示"API 状态"、"能力可用性"等开发信息。

**方案：**
- 保留 `P0Scaffold.tsx` 作为内部开发工具（可选）
- 所有面向用户的页面改用标准 `PageHeader` 组件

**新建 `components/PageHeader.tsx`：**
```tsx
type PageHeaderProps = {
  title: string;
  subtitle?: string;
  breadcrumb?: BreadcrumbItem[];
  actions?: React.ReactNode;
};
```

**涉及改造的页面（13 个）：**
- ResearchCenterPage, NewResearchTaskPage, ResearchTaskDetailPage
- ResearchReportPage, CompetitorLibraryPage
- AssetLibraryPage
- GenerateWorkbenchPage, GenerateTaskDetailPage
- ResultsPreviewPage
- DesignDraftPage, DataImportPage, ToolDetailPage
- DetailReviewPage

---

#### 2.3 修复硬编码路由 [P1]

**问题：** `navigation.tsx` 中 `{ key: "/details/1", label: "Detail Editor" }` 写死了 ID。

**方案：**
- 导航改为 `{ key: "/details", label: "Detail Editor" }` 无 ID
- 路由 `/details` 默认显示"请选择商品"空状态
- 路由 `/details/:id` 显示具体详情编辑器
- 从工作台/素材列表跳转时带上实际 ID

---

### Phase 3: 视觉升级（预计 2 天）

> 目标：实现用户要求的"DetailFlow"深色科技风格。

#### 3.1 深色主题 [P1]

**重写 `styles/theme.css`：**
```css
:root {
  color-scheme: dark;
  --bg-primary: #0a0e1a;
  --bg-secondary: #111827;
  --bg-card: #1a1f35;
  --bg-elevated: #232a42;
  --border-subtle: rgba(99, 115, 146, 0.2);
  --text-primary: #e8ecf4;
  --text-secondary: #8b95a8;
  --accent-blue: #3b82f6;
  --accent-purple: #8b5cf6;
  --accent-gradient: linear-gradient(135deg, #3b82f6, #8b5cf6);
}
```

**视觉要求对照：**
- [x] 低饱和配色，主色偏蓝紫/科技蓝
- [x] 卡片化布局，圆角适中，阴影克制
- [x] 信息层级清楚，主要按钮突出，次要操作弱化
- [x] 三栏布局：左侧导航 + 中间工作区 + 右侧智能辅助

**Ant Design 主题配置：**
```typescript
const theme = {
  token: {
    colorPrimary: "#3b82f6",
    colorBgContainer: "#1a1f35",
    colorBgElevated: "#232a42",
    colorText: "#e8ecf4",
    colorTextSecondary: "#8b95a8",
    borderRadius: 8,
    fontFamily: "'Inter', 'Source Han Sans SC', system-ui",
  },
  algorithm: theme.darkAlgorithm,
};
```

---

#### 3.2 三栏布局 [P1]

**新建 `layouts/MainLayout.tsx`：**
```
┌──────────────────────────────────────────────────────┐
│  Top Bar (Logo + Search + User + Notifications)      │
├──────────┬───────────────────────┬───────────────────┤
│          │                       │                   │
│  Left    │   Center Workspace    │   Right Panel     │
│  Nav     │                       │   (AI Assistant)  │
│          │   Main Content Area   │                   │
│  220px   │   flex: 1             │   320px           │
│          │                       │   (可折叠)         │
│          │                       │                   │
├──────────┴───────────────────────┴───────────────────┤
│  Status Bar (Tool Status + Sync + Version)           │
└──────────────────────────────────────────────────────┘
```

**右侧智能辅助面板内容：**
- 当前页面上下文帮助
- AI 提示词建议
- 快捷操作入口
- 最近使用记录

---

#### 3.3 修复乱码后的中文界面统一 [P1]

**统一语言策略：**
- 所有面向用户的文本统一为中文
- 代码中的变量名/函数名保持英文
- 导航菜单改为中文：
  - Research → 市场调研
  - Materials → 商品素材
  - Generate → AI 生图
  - Assets → 素材库
  - Results → 生成结果
  - Detail Editor → 详情编辑
  - Audit → 合规审核
  - Exports → 导出管理
  - Post-Process → 后处理
  - Visual Planning → 视觉规划
  - System → 系统管理
  - Tools → 工具中心

---

### Phase 4: AI 能力接入（预计 3-5 天）

> 目标：让"AI 工作台"真正具备 AI 能力。

#### 4.1 AI Relay 接入 [P1]

**现状：** `AIUtil.java` 已实现 OpenAI-compatible `/v1/chat/completions` 调用，但默认关闭。

**改动：**
- 提供 `.env.example` 模板，降低配置门槛
- 新增"AI 配置向导"页面，引导用户填写 API Key
- 支持多个 AI Provider 切换（OpenAI / 通义千问 / 文心一言 / 本地模型）
- PromptWorkbench 的 guided/expand 功能实际调用 AI

**前端新增 `pages/system/AIConfigPage.tsx`：**
- API Key 配置（加密存储）
- 模型选择
- 连接测试
- 用量统计

---

#### 4.2 至少 1 个工具适配器真实接入 [P1]

**优先接入 Real-ESRGAN（图片超分）：**
- 开源、部署简单、效果好
- 对电商图片质量提升直接

**接入方案：**
```yaml
tools:
  adapters:
    real-esrgan:
      enabled: true
      base-url: http://localhost:5001
```

**前端改动：**
- ToolCenterPage 展示真实工具状态
- PostProcessPage 的"超分"操作可用
- 生成结果页可直接调用后处理

---

#### 4.3 提示词模板种子数据 [P2]

**从 GitHub 仓库导入内置模板：**
- `rockbenben/img-prompt` — 5000+ 多语言提示词
- `Dalabad/stable-diffusion-prompt-templates` — SD 模板
- `pen9un/art-prompt-system` — 电商绘画模板

**实现：**
- 新增 `data/seed_templates.json` 种子数据文件
- `PromptTemplateServiceImpl` 启动时检查并导入 SYSTEM 源模板
- 预设 7 个分类各 10+ 条模板

---

### Phase 5: 功能补全（预计 5-7 天）

> 目标：补全核心业务链路。

#### 5.1 研究报告页实现 [P2]

**当前状态：** 完全空白占位。

**实现：**
- 接入 `ResearchTask` 的 `resultJson` 数据
- 展示：价格区间分布图、关键词排名、痛点分析、竞品矩阵
- 支持导出研究报告为 PDF
- 图表使用 ECharts（项目已有依赖）

---

#### 5.2 PDF 导出 [P2]

**当前状态：** ExportRecordsPage 中 PDF 显示 `available: false`。

**方案：**
- 后端使用 `itext7` 或 `openhtmltopdf` 生成 PDF
- 支持详情页 → PDF 完整导出
- 支持研究报告 → PDF 导出
- 前端导出记录页 PDF 标记为可用

---

#### 5.3 批量操作 [P2]

**涉及页面：**
- 素材列表 — 批量删除、批量打标签
- 生成结果 — 批量后处理、批量导出
- 提示词模板 — 批量删除、批量修改分类
- 导出记录 — 批量下载

**实现：**
- Table 组件增加 rowSelection
- 新增批量操作工具栏
- 后端新增批量 API（`POST /batch/delete` 等）

---

#### 5.4 平台直发接口 [P3]

**优先级：** 淘宝 > 抖音 > 拼多多 > 京东 > Amazon

**实现：**
- 新增 `PlatformPublisherService` 接口
- 每个平台一个适配器实现
- 前端新增"发布到平台"弹窗
- 发布状态跟踪

---

### Phase 6: 体验优化（预计 3 天）

> 目标：高频使用者的日常效率提升。

#### 6.1 键盘快捷键

| 快捷键 | 功能 |
|--------|------|
| `Ctrl+K` | 全局搜索 |
| `Ctrl+N` | 新建素材 |
| `Ctrl+Enter` | 提交当前表单 |
| `Esc` | 关闭弹窗/抽屉 |

---

#### 6.2 全局搜索

- 新建 `components/GlobalSearch.tsx`
- 支持搜索：素材、生成结果、模板、导出记录
- 快捷键 `Ctrl+K` 唤起

---

#### 6.3 最近访问

- 左侧导航底部显示"最近访问"列表
- 记录最近 10 个访问的页面/商品
- 点击快速跳转

---

#### 6.4 空状态优化

- 每个列表页空状态提供"创建第一个 xxx"引导
- 连接引导：未配置 AI 时显示配置向导入口
- 工具未连接时显示一键部署指南

---

## 三、执行计划总览

```
Week 1:  Phase 1 (基础修复) + Phase 2 (代码治理)
         └─ 乱码修复 → 安全认证 → 公共工具提取 → 占位组件替换

Week 2:  Phase 3 (视觉升级) + Phase 4 (AI 接入)
         └─ 深色主题 → 三栏布局 → AI Relay → Real-ESRGAN → 模板种子

Week 3:  Phase 5 (功能补全) + Phase 6 (体验优化)
         └─ 研究报告 → PDF 导出 → 批量操作 → 快捷键 → 全局搜索
```

---

## 四、验收标准

### v6.0 Release Checklist

- [ ] 所有页面中文正常显示，无乱码
- [ ] 未登录用户无法访问任何 API
- [ ] 深色主题 + 蓝紫科技风格完整呈现
- [ ] 三栏布局（导航 + 工作区 + AI 面板）
- [ ] AI Relay 可实际调用，提示词生成可用
- [ ] 至少 1 个工具适配器真实工作
- [ ] 研究报告页展示真实图表
- [ ] PDF 导出可用
- [ ] 无硬编码 ID 路由
- [ ] 无 P0Scaffold 占位组件暴露给用户
- [ ] 公共工具函数零重复
- [ ] `mvn test` 全部通过
- [ ] `npm run build` 无错误
- [ ] 前端无 TypeScript 类型错误

---

## 五、风险提示

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 安全框架引入可能破坏现有 API 行为 | 高 | 逐 Controller 测试，白名单公开接口 |
| 深色主题可能遗漏某些组件 | 中 | 逐页面走查，使用 Ant Design token 统一 |
| AI Provider API 变更 | 中 | 抽象 AI 接口层，支持多 Provider 切换 |
| 大规模文件编码修复可能引入回归 | 中 | 逐文件修复 + 立即跑测试 |
| 工具适配器部署依赖外部服务 | 低 | 提供 Docker Compose 一键部署方案 |

---

*文档版本: v1.0 | 生成时间: 2026-05-29*