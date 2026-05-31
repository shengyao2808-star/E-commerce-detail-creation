# 电商详情 AI 工作台

一个面向商品详情生产、合规审查、审核流转与导出的前后端项目。

## 📖 文档

- [**全版本使用说明**](USAGE_GUIDE.md) - 完整的项目使用指南
- [需求文档索引](docs/REQUIREMENTS_INDEX.md) - 所有需求文档导航
- [v6.0 改进方案](docs/IMPROVEMENT_PLAN.md) - 系统改进路线图
- [Phase 0 基线报告](docs/v6/PHASE_0_BASELINE_REPORT.md) - 项目状态审计

## 🚀 快速开始

### 环境要求

- Java 21+
- Node.js 18+
- Maven 3.8+
- MySQL 8.0+ / MariaDB 10.11+

### 安装步骤

```bash
# 1. 克隆项目
git clone <repository-url>
cd "E-commerce detail creation"

# 2. 数据库初始化
mysql -u root -p -e "CREATE DATABASE ecommerce_detail_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p ecommerce_detail_ai < src/main/resources/db/schema.sql

# 3. 配置环境变量
export DB_URL="jdbc:mysql://localhost:3306/ecommerce_detail_ai?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"

# 4. 启动后端
mvn spring-boot:run

# 5. 启动前端
cd frontend
pnpm install
pnpm dev
```

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:5173 |
| 后端 API | http://localhost:8080/api/v1 |

## 📋 功能模块

| 模块 | 状态 |
|------|------|
| 商品资料管理 | ✅ 已完成 |
| AI 生图工作台 | ✅ 已完成 |
| 提示词工作台 | ✅ 已完成 |
| 合规审核 | ✅ 已完成 |
| 导出管理 | ✅ 已完成 |
| 后处理任务 | ✅ 已完成 |
| 市场调研 | ✅ 已完成 |
| 成本管理 | ✅ 已完成 |
| 团队管理 | ✅ 已完成 |
| 系统诊断 | ✅ 已完成 |

## 🛠️ 技术栈

| 层次 | 技术 |
|------|------|
| 前端 | React 18 + TypeScript + Ant Design 5 + Vite |
| 后端 | Java 21 + Spring Boot 3.2 + MyBatis-Plus |
| 数据库 | MySQL 8.0 / MariaDB 10.11 |
| AI 引擎 | OpenAI-compatible relay + 12 个开源工具适配器 |

## 📊 测试

```bash
# 后端测试
mvn test

# 前端类型检查
cd frontend && npm test

# 前端构建
cd frontend && npm run build
```

## 📁 项目结构

```
├── frontend/                  # 前端代码
│   ├── src/
│   │   ├── components/        # 通用组件
│   │   ├── layouts/           # 布局组件
│   │   ├── pages/             # 页面组件
│   │   ├── services/          # API 服务
│   │   ├── stores/            # 状态管理
│   │   └── styles/            # 样式文件
│   └── package.json
├── src/                       # 后端代码
│   ├── main/
│   │   ├── java/              # Java 源码
│   │   └── resources/         # 配置文件
│   └── test/                  # 测试代码
├── docs/                      # 项目文档
├── pom.xml                    # Maven 配置
└── USAGE_GUIDE.md             # 使用说明
```

## 🔧 配置说明

详细配置请参考 [使用说明](USAGE_GUIDE.md) 中的配置章节。

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_URL` | 数据库连接地址 | jdbc:mysql://localhost:3306/ecommerce_detail_ai |
| `DB_USERNAME` | 数据库用户名 | root |
| `DB_PASSWORD` | 数据库密码 | （空） |
| `AI_RELAY_ENABLED` | 是否启用 AI Relay | false |
| `AI_RELAY_BASE_URL` | AI Relay 地址 | （空） |

## 📚 相关文档

- [前端 UI 需求](docs/FRONTEND_UI_REQUIREMENTS.md)
- [视觉详情页需求](docs/VISUAL_DETAIL_REQUIREMENTS.md)
- [工具库清单](docs/TOOL_LIBRARY.md)
- [工具适配器 API](docs/TOOL_ADAPTER_API.md)

## 📈 版本历史

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v6.0 | 2026-05-29 | UI 本地化、MariaDB 支持、安全认证 |
| v5.0 | 2026-05-26 | 系统管理、成本跟踪、诊断、团队权限 |
| v4.0 | 2026-05-24 | 提示词模板库、视觉规划、工具适配器 |
| v3.0 | 2026-05-22 | 导出管理、后处理任务、审核流程 |
| v2.0 | 2026-05-20 | AI 生图、详情编辑、合规检测 |
| v1.0 | 2026-05-18 | 商品资料管理、基础框架 |

## 📄 许可证

Private - 仅限内部使用

---

*最后更新：2026-05-29*