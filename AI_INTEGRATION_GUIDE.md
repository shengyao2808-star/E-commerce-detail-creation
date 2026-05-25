# AI能力集成建议

## 当前状态

内容生成类 AI 方法已接入 OpenAI-compatible relay，默认关闭。未启用 relay 或缺少 `base-url` / `model` 时会抛出 `UnsupportedOperationException`，不会返回假内容。

relay 调用目标为 `{AI_RELAY_BASE_URL}/v1/chat/completions`。中转站可指向 Ollama、LocalAI、vLLM、TGI 等兼容 OpenAI Chat Completions 协议的本地或私有化服务。

风险检测 `detectRisk` 不接 AI，仍建议使用 `RiskCheckUtil` 做规则化风险检测。

---

## 集成方案建议

### 方案1: 本地部署开源大模型（推荐）

#### 1.1 Ollama + 开源模型
- **优点**: 完全免费、本地运行、无网络依赖
- **模型选择**:
  - Qwen2.5 (阿里通义千问开源版)
  - Llama 3.1
  - Mistral
  - DeepSeek Coder

#### 1.2 部署步骤
```bash
# 安装Ollama
curl -fsSL https://ollama.com/install.sh | sh

# 拉取模型
ollama pull qwen2.5:7b

# 启动服务
ollama serve
```

#### 1.3 代码集成
```java
// AIUtil.java修改示例
public String callAI(String prompt) {
    // 调用本地Ollama API
    String ollamaUrl = "http://localhost:11434/api/generate";
    
    Map<String, Object> request = new HashMap<>();
    request.put("model", "qwen2.5:7b");
    request.put("prompt", prompt);
    request.put("stream", false);
    
    // 使用HttpClient发送请求
    // 解析响应并返回
}
```

---

### 方案2: 私有化部署商业模型

#### 2.1 私有化部署方案
- 阿里云百炼私有化部署
- 腾讯云混元私有化部署
- 百度千帆私有化部署

#### 2.2 优点
- 企业级支持
- 数据安全
- 可定制化

#### 2.3 缺点
- 需要付费授权
- 需要硬件资源

---

### 方案3: 自建模型服务

#### 3.1 技术栈
- vLLM (高性能推理引擎)
- TGI (Text Generation Inference)
- FastChat

#### 3.2 部署架构
```
客户端 -> AIUtil -> 自建模型服务 -> GPU服务器
```

---

## 推荐方案

**对于商业化交付，推荐方案1: Ollama + Qwen2.5**

### 理由
1. ✅ 完全免费，无付费API
2. ✅ 本地部署，无网络依赖
3. ✅ 开源协议，可商用
4. ✅ 中文能力强（Qwen2.5）
5. ✅ 部署简单，维护成本低

### 集成步骤
1. 安装Ollama服务
2. 拉取Qwen2.5模型
3. 修改AIUtil.java，调用本地API
4. 配置模型参数（temperature, max_tokens等）
5. 测试验证

---

## 配置建议

### application.yml
```yaml
ai:
  relay:
    enabled: ${AI_RELAY_ENABLED:false}
    base-url: ${AI_RELAY_BASE_URL:}
    api-key: ${AI_RELAY_API_KEY:}
    model: ${AI_RELAY_MODEL:}
    temperature: ${AI_RELAY_TEMPERATURE:0.7}
    max-tokens: ${AI_RELAY_MAX_TOKENS:4000}
    timeout-seconds: ${AI_RELAY_TIMEOUT_SECONDS:120}
```

### 环境变量
```bash
export AI_RELAY_ENABLED=true
export AI_RELAY_BASE_URL=http://localhost:11434
export AI_RELAY_API_KEY=
export AI_RELAY_MODEL=qwen2.5:7b
export AI_RELAY_TEMPERATURE=0.7
export AI_RELAY_MAX_TOKENS=4000
export AI_RELAY_TIMEOUT_SECONDS=120
```

---

## 性能优化建议

1. **模型选择**
   - 开发环境: qwen2.5:7b (平衡性能和质量)
   - 生产环境: qwen2.5:14b 或更大模型

2. **硬件要求**
   - 7B模型: 8GB显存
   - 14B模型: 16GB显存
   - 32B模型: 32GB显存

3. **并发处理**
   - 使用线程池处理并发请求
   - 考虑模型推理队列

---

## 成本估算

### Ollama方案（推荐）
- **软件成本**: 0元（完全免费）
- **硬件成本**: 
  - 云GPU服务器: 约2000-5000元/月
  - 本地GPU服务器: 约10000-30000元（一次性）
- **维护成本**: 低

### 私有化部署方案
- **授权费用**: 需咨询厂商
- **硬件成本**: 较高
- **维护成本**: 中等

---

## 下一步行动

1. 评估硬件资源
2. 选择合适的模型
3. 部署Ollama服务
4. 修改AIUtil.java集成本地API
5. 编写集成测试
6. 性能测试和优化
7. 文档更新

---

## 参考资源

- Ollama官网: https://ollama.com
- Qwen2.5模型: https://github.com/QwenLM/Qwen2.5
- vLLM项目: https://github.com/vllm-project/vllm
- Apache 2.0协议: https://www.apache.org/licenses/LICENSE-2.0
