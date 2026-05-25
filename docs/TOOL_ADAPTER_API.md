# 工具适配器调用接口

版本：v0.1  
日期：2026-05-24  
context-path：`/api/v1`

## 目标

工具适配器用于连接私有化部署的第三方工具，例如 ComfyUI、LLaVA、Crawl4AI、Grounded-SAM、IOPaint、Real-ESRGAN、ImageMagick worker 和 Playwright worker。业务代码不直接依赖这些工具，只通过统一接口调用。

## 已新增接口

| 功能 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 工具清单 | GET | `/api/v1/tool-adapters` | 返回系统支持的工具、GitHub 来源、星标、许可证、配置状态 |
| 工具详情 | GET | `/api/v1/tool-adapters/{code}` | 返回单个工具的能力和配置状态 |
| 调用工具 | POST | `/api/v1/tool-adapters/{code}/invoke` | 按工具编号和操作名转发到自部署工具服务 |

Controller 路径为 `/tool-adapters`，依赖全局 `server.servlet.context-path=/api/v1`，不要在 Controller 上重复写 `/api`。

## 工具编号

| code | 默认用途 |
|---|---|
| `crawl4ai` | 授权网页抽取、市场资料摘要 |
| `browser-use` | 浏览器型调研任务 |
| `scrapy` | 授权批量采集 worker |
| `promptflow` | prompt 评测与回归 |
| `langfuse` | prompt 和模型调用观测 |
| `llava` | 图片转提示词、视觉问答 |
| `comfyui` | 生图任务和工作流提交 |
| `controlnet` | 姿态、边缘、深度控制 |
| `grounded-sam` | 图像分割和 mask |
| `iopaint` | 局部修补、背景清理 |
| `real-esrgan` | 超分和清晰度增强 |
| `imagemagick` | 拼接、裁切、格式转换 |
| `playwright` | 详情页 HTML 视觉验收 |

## 请求格式

```json
{
  "operation": "image-generate",
  "payload": {
    "prompt": "commercial product photo",
    "ratio": "1:1"
  },
  "headers": {
    "X-Trace-Id": "optional-trace-id"
  }
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `operation` | String | 否 | 工具操作名；为空时使用工具默认操作 |
| `payload` | Object | 否 | 透传给工具服务的业务参数 |
| `headers` | Object | 否 | 额外请求头；系统配置的 Authorization 会自动追加 |

## 返回格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "toolCode": "comfyui",
    "operation": "image-generate",
    "statusCode": 200,
    "body": {
      "jobId": "img-001",
      "status": "queued"
    },
    "rawBody": "{\"jobId\":\"img-001\",\"status\":\"queued\"}"
  },
  "timestamp": 1770000000000
}
```

## 异常语义

| 场景 | HTTP | Result.code | 说明 |
|---|---:|---:|---|
| 工具未启用 | 501 | 501 | `tools.adapters.{code}.enabled=false` |
| 工具未配置 base-url | 501 | 501 | 未填写私有部署地址 |
| 工具不存在 | 404 | 404 | code 不在工具清单内 |
| 工具返回非 2xx | 502 | 502 | 自部署工具失败 |
| 工具网络失败/中断 | 502 | 502 | 工具不可达或调用中断 |

## 配置示例

```yaml
tools:
  adapters:
    comfyui:
      enabled: ${TOOL_COMFYUI_ENABLED:false}
      base-url: ${TOOL_COMFYUI_BASE_URL:}
      api-key: ${TOOL_COMFYUI_API_KEY:}
      timeout-seconds: ${TOOL_COMFYUI_TIMEOUT_SECONDS:300}
```

默认全部关闭。客户私有部署时填写内网地址，例如：

```powershell
$env:TOOL_COMFYUI_ENABLED="true"
$env:TOOL_COMFYUI_BASE_URL="http://127.0.0.1:8188"
```

## 视觉生成推荐操作契约

### 1. 图片转提示词：`llava / image-to-prompt`

```json
{
  "operation": "image-to-prompt",
  "payload": {
    "imageUrl": "file:///data/uploads/reference.png",
    "language": "zh-CN",
    "output": ["caption", "styleTags", "positivePrompt", "negativePrompt"]
  }
}
```

### 2. 生图：`comfyui / image-generate`

```json
{
  "operation": "image-generate",
  "payload": {
    "workflowCode": "fashion-model-main-image-v1",
    "prompt": "统一模特，女装主图，电商棚拍，清晰面料细节",
    "negativePrompt": "low quality, wrong logo, distorted hands",
    "ratio": "1:1",
    "count": 5,
    "modelProfileId": 12,
    "skc": {
      "color": "black",
      "spec": "M",
      "renderMode": "MODEL"
    }
  }
}
```

### 3. 分割：`grounded-sam / segment`

```json
{
  "operation": "segment",
  "payload": {
    "imageUrl": "file:///data/generated/main-01.png",
    "targets": ["model", "product", "background"]
  }
}
```

### 4. 拼接：`imagemagick / stitch`

```json
{
  "operation": "stitch",
  "payload": {
    "inputImages": [
      "file:///data/detail/screen-01.png",
      "file:///data/detail/screen-02.png"
    ],
    "outputRatio": "750xauto",
    "outputPath": "/data/export/detail-long.png"
  }
}
```

## 后续需要补的业务接口

工具适配器只是底层调用入口。真正的 UI 工作流还需要在业务层增加以下接口：

| 模块 | 建议接口 | 说明 |
|---|---|---|
| 模特库 | `/api/v1/model-profiles` | 用户自建模特三视图、身高体重三围、授权状态 |
| 类目视觉策略 | `/api/v1/category-visual-policies` | 女装、隐形眼镜、狗粮等类目差异化规则 |
| SKC 策略 | `/api/v1/skc-policies` | 颜色、规格、平铺/模特/实物图生成方式 |
| 提示词工作台 | `/api/v1/prompt-workbench/*` | 引导生成、扩写、图转提示词、版本管理 |
| 视觉页面规划 | `/api/v1/visual-plans` | 5 张 1:1、5 张 3:4 和详情长图分屏规划 |
| 生图任务 | `/api/v1/image-jobs` | 异步生图、状态、重试、人工确认 |
| 详情图拼接 | `/api/v1/detail-compositions` | 分屏图拼接为可交付长图 |

