# 电商详情 AI 工作台 - 全版本使用说明

> 版本：v6.0
> 更新日期：2026-05-29
> 状态：生产就绪（Production Ready）

---

## 一、项目概述

### 1.1 项目定位

电商详情 AI 工作台是一个面向电商运营、设计师及商品视觉团队的 AI 生成工具平台。它完成商品资料输入、生成合规图片和详情页、发起审核和导出等工作。

### 1.2 核心价值

- **自动化生产**：AI 自动生成商品详情页内容
- **合规保障**：前置风险检测，确保内容合规
- **流程管理**：完整的审核、导出、发布流程
- **工具集成**：集成 12+ 开源 AI 工具

### 1.3 技术架构

| 层次 | 技术栈 |
|------|--------|
| 前端 | React 18 + TypeScript + Ant Design 5 + Vite |
| 后端 | Java 21 + Spring Boot 3.2 + MyBatis-Plus |
| 数据库 | MySQL 8.0 / MariaDB 10.11 |
| 缓存 | Redis（可选） |
| AI 引擎 | OpenAI-compatible relay + 12 个开源工具适配器 |

---

## 二、功能模块

### 2.1 核心业务流程

```
商品资料 → AI 生图 → 提示词生成 → 合规预检 → 结果预览 → 详情页组装 → 导出交付
```

### 2.2 功能模块清单

| 模块 | 功能 | 状态 |
|------|------|------|
| 商品资料管理 | 上传、列表、详情、编辑、删除 | ✅ 已完成 |
| AI 生图工作台 | 提示词生成、任务创建、状态跟踪 | ✅ 已完成 |
| 视觉规划 | 主图策略、详情页分屏规划 | ✅ 已完成 |
| 提示词工作台 | 引导生成、扩写、图转提示词 | ✅ 已完成 |
| 提示词模板库 | 分类、平台、风格筛选 | ✅ 已完成 |
| 合规审核 | 风险检测、审核提交、审批流程 | ✅ 已完成 |
| 导出管理 | 多格式导出、批量下载 | ✅ 已完成 |
| 后处理任务 | 超分、修复、抠图、拼接 | ✅ 已完成 |
| 市场调研 | 竞品分析、价格监控 | ✅ 已完成 |
| 成本管理 | 费用统计、配置管理 | ✅ 已完成 |
| 团队管理 | 用户、角色、权限 | ✅ 已完成 |
| 系统诊断 | 环境检测、服务状态 | ✅ 已完成 |
| 操作日志 | 审计追踪 | ✅ 已完成 |
| 素材库 | OCR 识别、素材管理 | ✅ 已完成 |
| 设计草稿 | Excalidraw 画布 | ✅ 已完成 |
| 数据导入 | Excel/CSV 解析 | ✅ 已完成 |

### 2.3 AI 工具集成

| 工具 | 用途 | GitHub | 状态 |
|------|------|--------|------|
| ComfyUI | 生图编排 | Comfy-Org/ComfyUI | 待配置 |
| LLaVA | 图片转提示词 | haotian-liu/LLaVA | 待配置 |
| Real-ESRGAN | 超分增强 | xinntao/Real-ESRGAN | 待配置 |
| Grounded-SAM | 图像分割 | IDEA-Research/Grounded-Segment-Anything | 待配置 |
| IOPaint | 局部修补 | Sanster/IOPaint | 待配置 |
| ImageMagick | 拼接裁切 | ImageMagick/ImageMagick | 待配置 |
| Crawl4AI | 网页采集 | unclecode/crawl4ai | 待配置 |
| Browser Use | 浏览器调研 | browser-use/browser-use | 待配置 |
| Scrapy | 批量采集 | scrapy/scrapy | 待配置 |
| Promptflow | 提示词评测 | microsoft/promptflow | 待配置 |
| Langfuse | 链路观测 | langfuse/langfuse | 待配置 |
| Playwright | 视觉验收 | microsoft/playwright | 待配置 |

---

## 三、快速开始

### 3.1 环境要求

| 依赖 | 版本要求 |
|------|----------|
| Java | 21+ |
| Node.js | 18+ |
| Maven | 3.8+ |
| MySQL/MariaDB | 8.0+ / 10.11+ |
| pnpm | 8+（推荐）或 npm |

### 3.2 安装步骤

#### 1. 克隆项目

```bash
git clone <repository-url>
cd "E-commerce detail creation"
```

#### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE ecommerce_detail_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入表结构
mysql -u root -p ecommerce_detail_ai < src/main/resources/db/schema.sql

# 导入种子数据（可选）
mysql -u root -p ecommerce_detail_ai < src/main/resources/db/data.sql
```

#### 3. 后端配置

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，配置数据库连接
# DB_URL=jdbc:mysql://localhost:3306/ecommerce_detail_ai?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
# DB_USERNAME=root
# DB_PASSWORD=your_password

# 或者直接设置环境变量
export DB_URL="jdbc:mysql://localhost:3306/ecommerce_detail_ai?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
```

#### 4. 启动后端

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或者打包后运行
mvn clean package -DskipTests
java -jar target/detail-creation-ai-5.0.0.jar
```

#### 5. 前端配置

```bash
cd frontend

# 安装依赖
pnpm install
# 或
npm install

# 配置 API 地址（可选，默认连接 localhost:8080）
# 编辑 frontend/vite.config.ts 中的 proxy 配置
```

#### 6. 启动前端

```bash
# 开发模式
pnpm dev
# 或
npm run dev

# 构建生产版本
pnpm build
# 或
npm run build
```

### 3.3 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:5173 |
| 后端 API | http://localhost:8080/api/v1 |
| H2 控制台（开发） | http://localhost:8080/api/v1/h2-console |
| Swagger UI（如有） | http://localhost:8080/api/v1/swagger-ui.html |

---

## 四、API 文档

### 4.1 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1780057938953
}
```

### 4.2 分页响应格式

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [],
  "pageNum": 1,
  "pageSize": 20,
  "total": 100,
  "pages": 5
}
```

### 4.3 核心 API 列表

#### 商品资料

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/material/upload` | 上传商品资料 |
| GET | `/api/v1/material/list` | 获取商品列表 |
| GET | `/api/v1/material/{id}` | 获取商品详情 |
| PUT | `/api/v1/material/{id}` | 更新商品资料 |
| DELETE | `/api/v1/material/{id}` | 删除商品资料 |

#### AI 生图

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/image-jobs/list` | 获取生图任务列表 |
| POST | `/api/v1/image-jobs` | 创建生图任务 |
| GET | `/api/v1/image-jobs/{id}` | 获取任务详情 |
| POST | `/api/v1/image-jobs/{id}/retry` | 重试任务 |

#### 提示词

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/prompt-templates/list` | 获取模板列表 |
| POST | `/api/v1/prompt-templates` | 创建模板 |
| POST | `/api/v1/prompt-workbench/guided` | 引导生成提示词 |
| POST | `/api/v1/prompt-workbench/expand` | 扩写提示词 |
| POST | `/api/v1/prompt-workbench/image-to-prompt` | 图片转提示词 |

#### 详情页

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/detail/{id}` | 获取详情页 |
| PUT | `/api/v1/detail/{id}` | 更新详情页 |
| POST | `/api/v1/detail/generate` | AI 生成详情 |
| POST | `/api/v1/detail-compositions` | 合成详情长图 |

#### 审核

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/audit/submit` | 提交审核 |
| GET | `/api/v1/audit/list` | 审核列表 |
| PUT | `/api/v1/audit/{id}/approve` | 审核通过 |
| PUT | `/api/v1/audit/{id}/reject` | 审核驳回 |
| PUT | `/api/v1/audit/{id}/return` | 退回修改 |

#### 导出

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/export/export` | 发起导出 |
| GET | `/api/v1/export/list` | 导出列表 |
| GET | `/api/v1/export/{id}` | 导出详情 |
| GET | `/api/v1/export/{id}/download` | 下载文件 |
| DELETE | `/api/v1/export/{id}` | 删除记录 |
| POST | `/api/v1/export/{id}/reexport` | 重新导出 |

#### 后处理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/post-process-tasks/list` | 任务列表 |
| POST | `/api/v1/post-process-tasks` | 创建任务 |
| POST | `/api/v1/post-process-tasks/{id}/retry` | 重试任务 |
| POST | `/api/v1/post-process-tasks/{id}/cancel` | 取消任务 |

#### 工具适配器

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/tool-adapters` | 工具列表 |
| GET | `/api/v1/tool-adapters/{code}` | 工具详情 |
| POST | `/api/v1/tool-adapters/{code}/invoke` | 调用工具 |

---

## 五、前端页面说明

### 5.1 页面路由

| 页面 | 路由 | 说明 |
|------|------|------|
| 工作台 | `/` | 首页总览 |
| 商品资料 | `/materials` | 资料列表 |
| 商品详情 | `/materials/:id` | 资料详情 |
| AI 生图 | `/generate` | 生图工作台 |
| 生图详情 | `/generate/:id` | 任务详情 |
| 视觉规划 | `/visual/plans` | 视觉策略 |
| 提示词工作台 | `/visual/prompt-workbench` | 提示词管理 |
| 提示词模板 | `/visual/prompt-templates` | 模板库 |
| 模特库 | `/visual/model-profiles` | 模特管理 |
| 类目策略 | `/visual/category-policies` | 类目视觉规则 |
| 素材库 | `/assets` | 素材管理 |
| 设计草稿 | `/tools/design-draft` | Excalidraw 画布 |
| 数据导入 | `/tools/imports` | Excel/CSV 导入 |
| 工具中心 | `/tools` | 工具适配器 |
| 详情编辑 | `/details/:id` | 详情页编辑 |
| 合规审核 | `/details/:id/review` | 风险检测 |
| 审核中心 | `/audit` | 审核列表 |
| 导出管理 | `/exports` | 导出列表 |
| 后处理 | `/post-process` | 任务管理 |
| 市场调研 | `/research` | 调研中心 |
| 成本管理 | `/cost` | 费用统计 |
| 团队管理 | `/system/team` | 用户角色 |
| 操作日志 | `/system/audit-log` | 审计追踪 |
| 系统诊断 | `/system/diagnostics` | 环境检测 |
| 登录 | `/auth/login` | 用户登录 |

### 5.2 布局结构

```
┌─────────────────────────────────────────────────────────┐
│  TopBar (顶部导航)                                       │
├──────┬──────────────────────────────┬───────────────────┤
│      │                              │                   │
│ Side │      Main Content            │  Assistant        │
│ Nav  │      (主工作区)               │  Panel            │
│      │                              │  (AI 助手)         │
│ 左侧 │                              │  右侧              │
│ 导航 │                              │  面板              │
│      │                              │                   │
├──────┴──────────────────────────────┴───────────────────┤
│  Footer (底部状态)                                       │
└─────────────────────────────────────────────────────────┘
```

### 5.3 国际化

系统支持中英文切换，通过右上角的语言切换按钮控制。

---

## 六、配置说明

### 6.1 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_URL` | 数据库连接地址 | jdbc:mysql://localhost:3306/ecommerce_detail_ai |
| `DB_USERNAME` | 数据库用户名 | root |
| `DB_PASSWORD` | 数据库密码 | （空） |
| `AI_RELAY_ENABLED` | 是否启用 AI Relay | false |
| `AI_RELAY_BASE_URL` | AI Relay 地址 | （空） |
| `AI_RELAY_API_KEY` | AI Relay API Key | （空） |
| `AI_RELAY_MODEL` | AI 模型名称 | （空） |
| `TOOL_COMFYUI_ENABLED` | 是否启用 ComfyUI | false |
| `TOOL_COMFYUI_BASE_URL` | ComfyUI 地址 | （空） |
| `TOOL_REAL_ESRGAN_ENABLED` | 是否启用 Real-ESRGAN | false |
| `TOOL_REAL_ESRGAN_BASE_URL` | Real-ESRGAN 地址 | （空） |

### 6.2 工具适配器配置

```yaml
tools:
  adapters:
    comfyui:
      enabled: ${TOOL_COMFYUI_ENABLED:false}
      base-url: ${TOOL_COMFYUI_BASE_URL:}
      api-key: ${TOOL_COMFYUI_API_KEY:}
      timeout-seconds: ${TOOL_COMFYUI_TIMEOUT_SECONDS:300}
    real-esrgan:
      enabled: ${TOOL_REAL_ESRGAN_ENABLED:false}
      base-url: ${TOOL_REAL_ESRGAN_BASE_URL:}
      api-key: ${TOOL_REAL_ESRGAN_API_KEY:}
      timeout-seconds: ${TOOL_REAL_ESRGAN_TIMEOUT_SECONDS:300}
    # ... 其他工具配置
```

---

## 七、开发指南

### 7.1 项目结构

```
├── frontend/                  # 前端代码
│   ├── src/
│   │   ├── components/        # 通用组件
│   │   ├── layouts/           # 布局组件
│   │   ├── pages/             # 页面组件
│   │   ├── services/          # API 服务
│   │   ├── stores/            # 状态管理
│   │   ├── styles/            # 样式文件
│   │   └── utils/             # 工具函数
│   └── package.json
├── src/                       # 后端代码
│   ├── main/
│   │   ├── java/              # Java 源码
│   │   └── resources/         # 配置文件
│   └── test/                  # 测试代码
├── docs/                      # 项目文档
├── pom.xml                    # Maven 配置
└── README.md                  # 项目说明
```

### 7.2 开发命令

```bash
# 后端测试
mvn test

# 前端类型检查
cd frontend && npm test

# 前端构建
cd frontend && npm run build

# 前端开发服务器
cd frontend && npm run dev
```

### 7.3 代码规范

- 后端遵循 Spring Boot 最佳实践
- 前端使用 TypeScript 严格模式
- 使用 Ant Design 组件库
- 状态管理使用 Zustand
- API 请求使用 React Query

---

## 八、部署指南

### 8.1 生产环境部署

#### 1. 构建前端

```bash
cd frontend
pnpm build
# 或
npm run build
```

#### 2. 构建后端

```bash
mvn clean package -DskipTests
```

#### 3. 部署

```bash
# 设置环境变量
export DB_URL="jdbc:mysql://your-db-host:3306/ecommerce_detail_ai?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai"
export DB_USERNAME="your_username"
export DB_PASSWORD="your_password"

# 启动后端
java -jar target/detail-creation-ai-5.0.0.jar

# 前端部署到 Nginx 或其他 Web 服务器
# 将 frontend/dist 目录部署到 Web 服务器
```

### 8.2 Docker 部署（待实现）

```yaml
# docker-compose.yml 示例
version: '3.8'
services:
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: your_password
      MYSQL_DATABASE: ecommerce_detail_ai
    ports:
      - "3306:3306"
  
  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:mysql://db:3306/ecommerce_detail_ai?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      DB_USERNAME: root
      DB_PASSWORD: your_password
    depends_on:
      - db
  
  frontend:
    build:
      context: ./frontend
    ports:
      - "5173:80"
    depends_on:
      - backend
```

---

## 九、常见问题

### 9.1 数据库连接失败

**问题**：启动时提示 "Communications link failure"

**解决**：
1. 检查 MySQL/MariaDB 服务是否启动
2. 检查数据库连接地址和端口是否正确
3. 检查用户名密码是否正确
4. 检查数据库是否存在

### 9.2 前端页面空白

**问题**：访问前端页面显示空白

**解决**：
1. 检查后端服务是否启动
2. 检查浏览器控制台是否有错误
3. 检查 API 请求是否被拦截
4. 清除浏览器缓存后重试

### 9.3 AI 功能不可用

**问题**：AI 生图、提示词生成等功能无法使用

**解决**：
1. 检查 AI Relay 配置是否正确
2. 检查工具适配器是否启用
3. 检查工具服务是否启动
4. 查看系统诊断页面确认服务状态

### 9.4 中文乱码

**问题**：页面显示中文乱码

**解决**：
1. 检查数据库字符集是否为 utf8mb4
2. 检查连接字符串是否包含 characterEncoding=utf8
3. 检查文件编码是否为 UTF-8

---

## 十、版本历史

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v6.0 | 2026-05-29 | UI 本地化、MariaDB 支持、安全认证、Phase 0+1 完成 |
| v5.0 | 2026-05-26 | 系统管理、成本跟踪、诊断、团队权限 |
| v4.0 | 2026-05-24 | 提示词模板库、视觉规划、工具适配器 |
| v3.0 | 2026-05-22 | 导出管理、后处理任务、审核流程 |
| v2.0 | 2026-05-20 | AI 生图、详情编辑、合规检测 |
| v1.0 | 2026-05-18 | 商品资料管理、基础框架 |

---

## 十一、相关文档

- [需求文档索引](docs/REQUIREMENTS_INDEX.md)
- [前端 UI 需求](docs/FRONTEND_UI_REQUIREMENTS.md)
- [视觉详情页需求](docs/VISUAL_DETAIL_REQUIREMENTS.md)
- [工具库清单](docs/TOOL_LIBRARY.md)
- [工具适配器 API](docs/TOOL_ADAPTER_API.md)
- [v6.0 改进方案](docs/IMPROVEMENT_PLAN.md)
- [Phase 0 基线报告](docs/v6/PHASE_0_BASELINE_REPORT.md)
- [文件定位清单](docs/v6/FILE_MAP.md)
- [风险清单](docs/v6/RISK_LIST.md)

---

## 十二、技术支持

如有问题，请通过以下方式联系：

1. 查看项目文档
2. 检查系统诊断页面
3. 查看操作日志
4. 联系开发团队

---

*文档版本：v1.0*
*最后更新：2026-05-29*