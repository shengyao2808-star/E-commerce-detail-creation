package com.ecommerce.detail.ai.util;

import com.ecommerce.detail.ai.exception.AIServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AIUtil relay integration")
class AIUtilTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void generateDetailContentCallsOpenAICompatibleRelayWithApiKeyAndBody() throws Exception {
        AtomicReference<String> path = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> requestBody = new AtomicReference<>();
        String baseUrl = startServer(exchange -> {
            path.set(exchange.getRequestURI().getPath());
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 200, """
                    {"choices":[{"message":{"content":"生成的详情内容"}}]}
                    """);
        });

        AIUtil aiUtil = relayUtil(baseUrl, "test-key", "qwen2.5:7b");

        String content = aiUtil.generateDetailContent("商品信息", "品牌风格");

        assertEquals("生成的详情内容", content);
        assertEquals("/v1/chat/completions", path.get());
        assertEquals("Bearer test-key", authorization.get());

        JsonNode body = objectMapper.readTree(requestBody.get());
        assertEquals("qwen2.5:7b", body.get("model").asText());
        assertEquals(0.3D, body.get("temperature").asDouble());
        assertEquals(128, body.get("max_tokens").asInt());
        assertFalse(body.get("stream").asBoolean());
        assertTrue(body.get("messages").get(0).get("content").asText().contains("品牌风格"));
        assertTrue(body.get("messages").get(1).get("content").asText().contains("商品信息"));
    }

    @Test
    void relayRequestOmitsAuthorizationWhenApiKeyIsBlank() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>("missing");
        String baseUrl = startServer(exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            respond(exchange, 200, """
                    {"choices":[{"message":{"content":"ok"}}]}
                    """);
        });

        AIUtil aiUtil = relayUtil(baseUrl, "", "qwen2.5:7b");

        assertEquals("ok", aiUtil.generateTitle("商品", "类目", "特点"));
        assertEquals(null, authorization.get());
    }

    @Test
    void relayHttpErrorThrowsAIServiceException() throws Exception {
        String baseUrl = startServer(exchange -> respond(exchange, 500, "{\"error\":\"boom\"}"));
        AIUtil aiUtil = relayUtil(baseUrl, "test-key", "qwen2.5:7b");

        AIServiceException exception = assertThrows(
                AIServiceException.class,
                () -> aiUtil.generateFAQ("商品信息")
        );

        assertTrue(exception.getMessage().contains("HTTP 500"));
    }

    @Test
    void relayResponseWithoutContentThrowsAIServiceException() throws Exception {
        String baseUrl = startServer(exchange -> respond(exchange, 200, "{\"choices\":[]}"));
        AIUtil aiUtil = relayUtil(baseUrl, "test-key", "qwen2.5:7b");

        assertThrows(AIServiceException.class, () -> aiUtil.generateSellingPoints("商品信息"));
    }

    @Test
    void unconfiguredRelayThrowsUnsupportedOperationException() {
        AIUtil disabled = new AIUtil(
                java.net.http.HttpClient.newHttpClient(),
                objectMapper,
                false,
                "",
                "",
                "",
                0.3D,
                128,
                Duration.ofSeconds(2)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> disabled.generateDetailContent("商品信息", "品牌风格")
        );
    }

    @Test
    void detectRiskRemainsUnsupported() {
        AIUtil aiUtil = relayUtil("http://127.0.0.1:1", "test-key", "qwen2.5:7b");

        assertThrows(UnsupportedOperationException.class, () -> aiUtil.detectRisk("内容", "敏感词"));
    }

    private AIUtil relayUtil(String baseUrl, String apiKey, String model) {
        return new AIUtil(
                java.net.http.HttpClient.newHttpClient(),
                objectMapper,
                true,
                baseUrl,
                apiKey,
                model,
                0.3D,
                128,
                Duration.ofSeconds(2)
        );
    }

    private String startServer(ExchangeHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            try {
                handler.handle(exchange);
            } finally {
                exchange.close();
            }
        });
        server.start();
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().put("Content-Type", java.util.List.of("application/json"));
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    @FunctionalInterface
    private interface ExchangeHandler {
        void handle(HttpExchange exchange) throws IOException;
    }
}
