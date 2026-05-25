package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.exception.ResourceNotFoundException;
import com.ecommerce.detail.ai.exception.ToolAdapterException;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ToolAdapterServiceImpl implements ToolAdapterService {

    private static final Map<String, ToolDefinition> TOOL_DEFINITIONS = createToolDefinitions();
    private static final Set<String> ALLOWED_HTTP_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE");

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ToolAdapterServiceImpl(Environment environment, ObjectMapper objectMapper) {
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public List<ToolAdapterInfoDTO> listTools() {
        return TOOL_DEFINITIONS.values().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public ToolAdapterInfoDTO getTool(String code) {
        return toDTO(requireTool(code));
    }

    @Override
    public ToolInvokeResponseDTO invoke(String code, ToolInvokeRequestDTO request) {
        ToolDefinition tool = requireTool(code);
        if (!isEnabled(tool.code())) {
            throw new UnsupportedOperationException("Tool adapter is disabled: " + tool.code());
        }

        String baseUrl = property(tool.code(), "base-url", "");
        if (!StringUtils.hasText(baseUrl)) {
            throw new UnsupportedOperationException("Tool adapter base-url is not configured: " + tool.code());
        }

        String operation = resolveOperation(tool, request.getOperation());
        String method = resolveMethod(tool.code(), operation);
        String path = resolvePath(tool, operation);
        int timeoutSeconds = integerProperty(tool.code(), "timeout-seconds", 120);

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(resolveUri(baseUrl, path, request.getPayload()))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json");

            String apiKey = property(tool.code(), "api-key", "");
            if (StringUtils.hasText(apiKey)) {
                builder.header("Authorization", "Bearer " + apiKey);
            }
            if (request.getHeaders() != null) {
                request.getHeaders().forEach(builder::header);
            }

            if ("GET".equals(method)) {
                builder.GET();
            } else {
                String body = objectMapper.writeValueAsString(
                        request.getPayload() == null ? Map.of() : request.getPayload());
                builder.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ToolAdapterException("Tool adapter returned HTTP " + response.statusCode()
                        + " for " + tool.code() + "/" + operation);
            }

            String rawBody = response.body();
            Object parsedBody = parseBody(rawBody);
            return new ToolInvokeResponseDTO(tool.code(), operation, response.statusCode(), parsedBody, rawBody);
        } catch (IOException e) {
            throw new ToolAdapterException("Tool adapter call failed: " + tool.code() + "/" + operation, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ToolAdapterException("Tool adapter call interrupted: " + tool.code() + "/" + operation, e);
        }
    }

    private String resolveOperation(ToolDefinition tool, String requestOperation) {
        String operation = StringUtils.hasText(requestOperation)
                ? requestOperation.trim()
                : tool.defaultOperation();
        if (!tool.operationSet().contains(operation)) {
            throw new IllegalArgumentException("Unsupported operation '" + operation
                    + "' for tool adapter: " + tool.code());
        }
        return operation;
    }

    private String resolveMethod(String code, String operation) {
        String method = operationProperty(code, operation, "method", "POST").trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_HTTP_METHODS.contains(method)) {
            throw new IllegalArgumentException("Unsupported HTTP method '" + method
                    + "' for tool adapter operation: " + code + "/" + operation);
        }
        return method;
    }

    private String resolvePath(ToolDefinition tool, String operation) {
        String configuredPath = operationProperty(tool.code(), operation, "path", null);
        if (StringUtils.hasText(configuredPath)) {
            return configuredPath.trim();
        }
        if (tool.defaultOperation().equals(operation)) {
            return tool.defaultPath();
        }
        throw new IllegalStateException("Tool adapter operation path is not configured: "
                + tool.code() + "/" + operation);
    }

    private ToolAdapterInfoDTO toDTO(ToolDefinition tool) {
        boolean configured = isEnabled(tool.code()) && StringUtils.hasText(property(tool.code(), "base-url", ""));
        String status = configured ? "CONFIGURED" : "NOT_CONFIGURED";
        return new ToolAdapterInfoDTO(
                tool.code(),
                tool.name(),
                tool.category(),
                tool.repository(),
                tool.stars(),
                tool.license(),
                tool.integrationMode(),
                tool.commercialPolicy(),
                tool.defaultOperation(),
                tool.defaultPath(),
                tool.operations(),
                configured,
                status
        );
    }

    private ToolDefinition requireTool(String code) {
        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("Tool code must not be blank");
        }
        ToolDefinition tool = TOOL_DEFINITIONS.get(code.toLowerCase(Locale.ROOT));
        if (tool == null) {
            throw new ResourceNotFoundException("Tool adapter not found: " + code);
        }
        return tool;
    }

    private boolean isEnabled(String code) {
        return Boolean.parseBoolean(property(code, "enabled", "false"));
    }

    private String property(String code, String key, String defaultValue) {
        return environment.getProperty("tools.adapters." + code + "." + key, defaultValue);
    }

    private int integerProperty(String code, String key, int defaultValue) {
        String value = property(code, key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String operationProperty(String code, String operation, String key, String defaultValue) {
        return environment.getProperty("tools.adapters." + code + ".operations." + operation + "." + key, defaultValue);
    }

    private URI resolveUri(String baseUrl, String path, Map<String, Object> payload) {
        String normalizedBase = trimRight(baseUrl, "/");
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        String url = normalizedBase + normalizedPath;

        if (payload != null) {
            for (Map.Entry<String, Object> entry : payload.entrySet()) {
                String token = "{" + entry.getKey() + "}";
                if (url.contains(token)) {
                    url = url.replace(token, encode(String.valueOf(entry.getValue())));
                }
            }
        }
        return URI.create(url);
    }

    private String trimRight(String value, String suffix) {
        String result = value;
        while (result.endsWith(suffix)) {
            result = result.substring(0, result.length() - suffix.length());
        }
        return result;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private Object parseBody(String rawBody) throws IOException {
        if (!StringUtils.hasText(rawBody)) {
            return null;
        }
        String trimmed = rawBody.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return objectMapper.readValue(trimmed, Object.class);
        }
        return rawBody;
    }

    private static Map<String, ToolDefinition> createToolDefinitions() {
        List<ToolDefinition> tools = new ArrayList<>();
        tools.add(new ToolDefinition("crawl4ai", "Crawl4AI", "MARKET_RESEARCH",
                "https://github.com/unclecode/crawl4ai", 66171, "Apache-2.0",
                "SELF_HOSTED_HTTP_SERVICE", "Can be self-hosted; keep as replaceable crawler service.",
                "crawl-url", "/crawl",
                List.of("crawl-url", "extract-markdown", "market-snapshot")));
        tools.add(new ToolDefinition("browser-use", "Browser Use", "MARKET_RESEARCH",
                "https://github.com/browser-use/browser-use", 95302, "MIT",
                "SELF_HOSTED_HTTP_SERVICE", "Can be self-hosted; use only for authorized browsing tasks.",
                "research-agent", "/run",
                List.of("research-agent", "page-observe", "competitor-snapshot")));
        tools.add(new ToolDefinition("scrapy", "Scrapy", "MARKET_RESEARCH",
                "https://github.com/scrapy/scrapy", 61801, "BSD-3-Clause",
                "SELF_HOSTED_WORKER", "Can be embedded in a private crawler worker; obey platform terms.",
                "crawl-job", "/crawl",
                List.of("crawl-job", "crawl-status", "crawl-result")));
        tools.add(new ToolDefinition("promptflow", "Promptflow", "PROMPT_WORKFLOW",
                "https://github.com/microsoft/promptflow", 11131, "MIT",
                "SELF_HOSTED_HTTP_SERVICE", "Use for prompt workflow evaluation; optional in first release.",
                "score", "/score",
                List.of("score", "evaluate-prompt", "batch-evaluate")));
        tools.add(new ToolDefinition("langfuse", "Langfuse", "PROMPT_OBSERVABILITY",
                "https://github.com/langfuse/langfuse", 27817, "NOASSERTION",
                "SELF_HOSTED_HTTP_SERVICE", "Review license before commercial distribution; use as optional telemetry.",
                "trace", "/api/public/ingestion",
                List.of("trace", "prompt-version", "run-metrics")));
        tools.add(new ToolDefinition("llava", "LLaVA", "IMAGE_TO_PROMPT",
                "https://github.com/haotian-liu/LLaVA", 24828, "Apache-2.0",
                "SELF_HOSTED_HTTP_SERVICE", "Run as a private vision-language service for image-to-prompt.",
                "image-to-prompt", "/v1/chat/completions",
                List.of("image-to-prompt", "image-caption", "visual-qa")));
        tools.add(new ToolDefinition("comfyui", "ComfyUI", "IMAGE_GENERATION",
                "https://github.com/Comfy-Org/ComfyUI", 114287, "GPL-3.0",
                "SELF_HOSTED_HTTP_SERVICE", "GPL tool; keep as standalone service, not linked into proprietary code.",
                "image-generate", "/prompt",
                List.of("image-generate", "workflow-submit", "history", "asset-fetch")));
        tools.add(new ToolDefinition("controlnet", "ControlNet", "IMAGE_CONTROL",
                "https://github.com/lllyasviel/ControlNet", 33893, "Apache-2.0",
                "COMFYUI_WORKFLOW_NODE", "Use inside image workflow for pose, edge, depth, and layout control.",
                "control-image", "/control",
                List.of("control-image", "pose-control", "depth-control", "edge-control")));
        tools.add(new ToolDefinition("grounded-sam", "Grounded Segment Anything", "SEGMENTATION",
                "https://github.com/IDEA-Research/Grounded-Segment-Anything", 17582, "Apache-2.0",
                "SELF_HOSTED_HTTP_SERVICE", "Run as private segmentation service for product and model masks.",
                "segment", "/segment",
                List.of("segment", "mask-product", "mask-model")));
        tools.add(new ToolDefinition("iopaint", "IOPaint", "IMAGE_CLEANUP",
                "https://github.com/Sanster/IOPaint", 23131, "Apache-2.0",
                "SELF_HOSTED_HTTP_SERVICE", "Run as private cleanup/inpaint service.",
                "inpaint", "/inpaint",
                List.of("inpaint", "cleanup-background", "remove-object")));
        tools.add(new ToolDefinition("real-esrgan", "Real-ESRGAN", "IMAGE_ENHANCE",
                "https://github.com/xinntao/Real-ESRGAN", 35525, "BSD-3-Clause",
                "SELF_HOSTED_HTTP_SERVICE", "Run as private upscaling service for final asset enhancement.",
                "upscale", "/upscale",
                List.of("upscale", "restore-face", "enhance-main-image")));
        tools.add(new ToolDefinition("imagemagick", "ImageMagick", "COMPOSITION_EXPORT",
                "https://github.com/ImageMagick/ImageMagick", 16510, "NOASSERTION",
                "LOCAL_CLI_OR_WORKER", "Review license package; use for stitching, resizing, and format conversion.",
                "compose", "/compose",
                List.of("compose", "resize", "stitch", "convert")));
        tools.add(new ToolDefinition("playwright", "Playwright", "VISUAL_QA",
                "https://github.com/microsoft/playwright", 89319, "Apache-2.0",
                "LOCAL_CLI_OR_WORKER", "Use for UI and generated HTML visual verification.",
                "verify-page", "/verify",
                List.of("verify-page", "screenshot", "layout-check")));

        Map<String, ToolDefinition> map = new LinkedHashMap<>();
        for (ToolDefinition tool : tools) {
            map.put(tool.code(), tool);
        }
        return map;
    }

    private record ToolDefinition(
            String code,
            String name,
            String category,
            String repository,
            Integer stars,
            String license,
            String integrationMode,
            String commercialPolicy,
            String defaultOperation,
            String defaultPath,
            List<String> operations
    ) {
        private Set<String> operationSet() {
            return new LinkedHashSet<>(operations);
        }
    }
}
