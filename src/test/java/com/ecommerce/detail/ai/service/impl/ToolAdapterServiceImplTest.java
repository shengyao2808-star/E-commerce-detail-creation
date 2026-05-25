package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.exception.ToolAdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ToolAdapterServiceImplTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void listToolsReturnsSelfHostedAdaptersAndConfigurationStatus() {
        ToolAdapterServiceImpl service = new ToolAdapterServiceImpl(new MockEnvironment(), new ObjectMapper());

        List<ToolAdapterInfoDTO> tools = service.listTools();

        assertTrue(tools.stream().anyMatch(tool -> "comfyui".equals(tool.getCode())));
        assertTrue(tools.stream().allMatch(tool -> tool.getStars() >= 10000));
        assertTrue(tools.stream().anyMatch(tool -> "llava".equals(tool.getCode())
                && "IMAGE_TO_PROMPT".equals(tool.getCategory())));
        assertFalse(service.getTool("comfyui").isConfigured());
    }

    @Test
    void disabledAdapterThrowsUnsupportedOperation() {
        ToolAdapterServiceImpl service = new ToolAdapterServiceImpl(new MockEnvironment(), new ObjectMapper());

        ToolInvokeRequestDTO request = new ToolInvokeRequestDTO();
        request.setOperation("image-generate");

        assertThrows(UnsupportedOperationException.class, () -> service.invoke("comfyui", request));
    }

    @Test
    void configuredAdapterPostsPayloadAndReturnsParsedBody() throws Exception {
        AtomicReference<String> receivedMethod = new AtomicReference<>();
        AtomicReference<String> receivedPath = new AtomicReference<>();
        AtomicReference<String> receivedAuthorization = new AtomicReference<>();
        AtomicReference<String> receivedBody = new AtomicReference<>();
        startServer(exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            receivedPath.set(exchange.getRequestURI().getPath());
            receivedAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            send(exchange, 200, "{\"jobId\":\"img-001\",\"status\":\"queued\"}");
        });

        MockEnvironment environment = new MockEnvironment()
                .withProperty("tools.adapters.comfyui.enabled", "true")
                .withProperty("tools.adapters.comfyui.base-url", baseUrl())
                .withProperty("tools.adapters.comfyui.api-key", "secret-token")
                .withProperty("tools.adapters.comfyui.operations.image-generate.path", "/prompt");
        ToolAdapterServiceImpl service = new ToolAdapterServiceImpl(environment, new ObjectMapper());

        ToolInvokeRequestDTO request = new ToolInvokeRequestDTO();
        request.setOperation("image-generate");
        request.setPayload(Map.of("prompt", "commercial product photo", "ratio", "1:1"));

        ToolInvokeResponseDTO response = service.invoke("comfyui", request);

        assertEquals("POST", receivedMethod.get());
        assertEquals("/prompt", receivedPath.get());
        assertEquals("Bearer secret-token", receivedAuthorization.get());
        assertTrue(receivedBody.get().contains("commercial product photo"));
        assertEquals(200, response.getStatusCode());
        assertEquals("comfyui", response.getToolCode());
        assertTrue(response.getBody() instanceof Map);
    }

    @Test
    void nonSuccessStatusThrowsToolAdapterException() throws Exception {
        startServer(exchange -> send(exchange, 500, "{\"error\":\"failed\"}"));
        MockEnvironment environment = new MockEnvironment()
                .withProperty("tools.adapters.llava.enabled", "true")
                .withProperty("tools.adapters.llava.base-url", baseUrl());
        ToolAdapterServiceImpl service = new ToolAdapterServiceImpl(environment, new ObjectMapper());

        ToolInvokeRequestDTO request = new ToolInvokeRequestDTO();
        request.setOperation("image-to-prompt");
        request.setPayload(Map.of("imageUrl", "file:///tmp/test.png"));

        assertThrows(ToolAdapterException.class, () -> service.invoke("llava", request));
    }

    @Test
    void unknownOperationThrowsClearIllegalArgumentException() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("tools.adapters.llava.enabled", "true")
                .withProperty("tools.adapters.llava.base-url", "http://127.0.0.1:18080");
        ToolAdapterServiceImpl service = new ToolAdapterServiceImpl(environment, new ObjectMapper());

        ToolInvokeRequestDTO request = new ToolInvokeRequestDTO();
        request.setOperation("not-a-real-op");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.invoke("llava", request));
        assertTrue(ex.getMessage().contains("Unsupported operation"));
        assertTrue(ex.getMessage().contains("llava"));
    }

    @Test
    void nonDefaultOperationWithoutPathConfigurationThrowsClearException() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("tools.adapters.llava.enabled", "true")
                .withProperty("tools.adapters.llava.base-url", "http://127.0.0.1:18080");
        ToolAdapterServiceImpl service = new ToolAdapterServiceImpl(environment, new ObjectMapper());

        ToolInvokeRequestDTO request = new ToolInvokeRequestDTO();
        request.setOperation("visual-qa");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.invoke("llava", request));
        assertTrue(ex.getMessage().contains("path"));
        assertTrue(ex.getMessage().contains("visual-qa"));
    }

    private void startServer(ExchangeHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", handler::handle);
        server.start();
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @FunctionalInterface
    private interface ExchangeHandler {
        void handle(HttpExchange exchange) throws IOException;
    }
}
