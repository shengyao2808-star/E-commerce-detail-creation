package com.ecommerce.detail.ai.util;

import com.ecommerce.detail.ai.dto.ProductDetailDTO;
import com.ecommerce.detail.ai.dto.ProductMaterialDTO;
import com.ecommerce.detail.ai.entity.BrandTemplate;
import com.ecommerce.detail.ai.exception.AIServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * AI utility backed by an OpenAI-compatible relay.
 */
@Slf4j
@Component
public class AIUtil {

    private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final boolean relayEnabled;
    private final String relayBaseUrl;
    private final String relayApiKey;
    private final String relayModel;
    private final double temperature;
    private final int maxTokens;
    private final Duration timeout;

    @Autowired
    public AIUtil(
            ObjectMapper objectMapper,
            @Value("${ai.relay.enabled:false}") boolean relayEnabled,
            @Value("${ai.relay.base-url:}") String relayBaseUrl,
            @Value("${ai.relay.api-key:}") String relayApiKey,
            @Value("${ai.relay.model:}") String relayModel,
            @Value("${ai.relay.temperature:0.7}") double temperature,
            @Value("${ai.relay.max-tokens:4000}") int maxTokens,
            @Value("${ai.relay.timeout-seconds:120}") long timeoutSeconds) {
        this(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeoutSeconds)).build(),
                objectMapper,
                relayEnabled,
                relayBaseUrl,
                relayApiKey,
                relayModel,
                temperature,
                maxTokens,
                Duration.ofSeconds(timeoutSeconds)
        );
    }

    AIUtil(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            boolean relayEnabled,
            String relayBaseUrl,
            String relayApiKey,
            String relayModel,
            double temperature,
            int maxTokens,
            Duration timeout) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.relayEnabled = relayEnabled;
        this.relayBaseUrl = relayBaseUrl;
        this.relayApiKey = relayApiKey;
        this.relayModel = relayModel;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.timeout = timeout;
    }

    public String generateTitle(String productName, String category, String features) {
        return callRelay(
                "你是电商详情页文案助手，请生成简洁、有转化力且合规的商品标题。",
                "商品名称: " + nullToEmpty(productName)
                        + "\n类目: " + nullToEmpty(category)
                        + "\n特征: " + nullToEmpty(features)
        );
    }

    public String generateSubtitle(String productName, String sellingPoints) {
        return callRelay(
                "你是电商详情页文案助手，请生成一句适合作为商品副标题的中文文案。",
                "商品名称: " + nullToEmpty(productName)
                        + "\n卖点: " + nullToEmpty(sellingPoints)
        );
    }

    public String generateSellingPoints(String productInfo) {
        return callRelay(
                "你是电商详情页文案助手，请提炼 3 到 5 条清晰、真实、合规的商品卖点。",
                nullToEmpty(productInfo)
        );
    }

    public String generateDetailContent(String productInfo, String brandStyle) {
        return callRelay(
                "你是电商详情页文案助手，请按品牌风格生成结构化、合规、可直接使用的商品详情内容。品牌风格: "
                        + nullToEmpty(brandStyle),
                nullToEmpty(productInfo)
        );
    }

    public String generateFAQ(String productInfo) {
        return callRelay(
                "你是电商客服内容助手，请生成适用于商品详情页的常见问题和回答。",
                nullToEmpty(productInfo)
        );
    }

    public String generateSEOKeywords(String productName, String category) {
        return callRelay(
                "你是电商 SEO 助手，请生成适用于商品详情页的中文搜索关键词，用逗号分隔。",
                "商品名称: " + nullToEmpty(productName) + "\n类目: " + nullToEmpty(category)
        );
    }

    public Map<String, Object> detectRisk(String content, String sensitiveWords) {
        throw new UnsupportedOperationException("AI风险检测暂未接入，请使用 RiskCheckUtil 进行规则化风险检测。");
    }

    public String generateMultipleVersions(String productInfo, int versionCount) {
        return callRelay(
                "你是电商详情页文案助手，请生成 " + versionCount + " 个风格不同但均合规的文案版本。",
                nullToEmpty(productInfo)
        );
    }

    public String generateProductDetail(ProductMaterialDTO dto, BrandTemplate template) {
        return callRelay(
                "你是电商详情页文案助手，请根据商品资料生成完整详情页内容。",
                buildMaterialPrompt(dto, template)
        );
    }

    public String generateProductDetail(ProductDetailDTO dto, BrandTemplate template) {
        return callRelay(
                "你是电商详情页文案助手，请根据详情草稿和品牌模板生成完整详情页内容。",
                buildDetailPrompt(dto, template)
        );
    }

    private String callRelay(String systemPrompt, String userPrompt) {
        ensureRelayConfigured();

        try {
            Map<String, Object> requestPayload = Map.of(
                    "model", relayModel,
                    "temperature", temperature,
                    "max_tokens", maxTokens,
                    "stream", false,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    )
            );

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(resolveRelayUri())
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(requestPayload),
                            StandardCharsets.UTF_8
                    ));

            if (StringUtils.hasText(relayApiKey)) {
                builder.header("Authorization", "Bearer " + relayApiKey);
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AIServiceException("AI relay returned HTTP " + response.statusCode());
            }

            String content = extractContent(response.body());
            if (!StringUtils.hasText(content)) {
                throw new AIServiceException("AI relay response did not contain message content");
            }
            return content;
        } catch (AIServiceException e) {
            throw e;
        } catch (IOException e) {
            throw new AIServiceException("AI relay call failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AIServiceException("AI relay call interrupted", e);
        }
    }

    private void ensureRelayConfigured() {
        if (!relayEnabled || !StringUtils.hasText(relayBaseUrl) || !StringUtils.hasText(relayModel)) {
            throw new UnsupportedOperationException(
                    "AI relay is not configured. Set AI_RELAY_ENABLED=true, AI_RELAY_BASE_URL, and AI_RELAY_MODEL."
            );
        }
    }

    private URI resolveRelayUri() {
        String baseUrl = relayBaseUrl;
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return URI.create(baseUrl + CHAT_COMPLETIONS_PATH);
    }

    private String extractContent(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return null;
        }
        return choices.get(0).path("message").path("content").asText(null);
    }

    private String buildMaterialPrompt(ProductMaterialDTO dto, BrandTemplate template) {
        if (dto == null) {
            return "";
        }
        return "商品名称: " + nullToEmpty(dto.getProductName())
                + "\nSKU: " + nullToEmpty(dto.getSku())
                + "\n类目: " + nullToEmpty(dto.getCategory())
                + "\n品牌: " + nullToEmpty(dto.getBrandName())
                + "\n描述: " + nullToEmpty(dto.getDescription())
                + "\n品牌模板: " + templateDescription(template);
    }

    private String buildDetailPrompt(ProductDetailDTO dto, BrandTemplate template) {
        if (dto == null) {
            return "";
        }
        return "标题: " + nullToEmpty(dto.getTitle())
                + "\n副标题: " + nullToEmpty(dto.getSubtitle())
                + "\n描述: " + nullToEmpty(dto.getDescription())
                + "\n类目: " + nullToEmpty(dto.getCategory())
                + "\n品牌模板: " + templateDescription(template);
    }

    private String templateDescription(BrandTemplate template) {
        return template == null ? "" : nullToEmpty(template.getStyleDescription());
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
