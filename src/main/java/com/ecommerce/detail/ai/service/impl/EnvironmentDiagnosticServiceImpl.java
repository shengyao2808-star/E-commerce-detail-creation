package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.common.enums.ExportFormat;
import com.ecommerce.detail.ai.dto.EnvironmentDiagnosticDTO;
import com.ecommerce.detail.ai.dto.EnvironmentDiagnosticDTO.*;
import com.ecommerce.detail.ai.service.EnvironmentDiagnosticService;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import com.ecommerce.detail.ai.util.LocalPathPolicy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnvironmentDiagnosticServiceImpl implements EnvironmentDiagnosticService {

    private static final List<String> TOOL_CODES = List.of(
            "crawl4ai", "browser-use", "scrapy", "promptflow", "langfuse",
            "llava", "comfyui", "controlnet", "grounded-sam", "iopaint",
            "real-esrgan", "imagemagick", "playwright"
    );

    private static final Map<String, String> REQUIRED_ENV_VARS;

    static {
        Map<String, String> vars = new LinkedHashMap<>();
        // Database
        vars.put("SPRING_DATASOURCE_URL", "JDBC URL for MySQL (e.g. jdbc:mysql://localhost:3306/ecommerce_detail_ai)");
        vars.put("SPRING_DATASOURCE_USERNAME", "Database username");
        vars.put("SPRING_DATASOURCE_PASSWORD", "Database password");
        // AI relay
        vars.put("AI_RELAY_ENABLED", "Set to true to enable AI relay (OpenAI-compatible /v1/chat/completions)");
        vars.put("AI_RELAY_BASE_URL", "Base URL of the AI relay service (e.g. https://api.openai.com)");
        vars.put("AI_RELAY_API_KEY", "API key for the AI relay service");
        vars.put("AI_RELAY_MODEL", "Model name (e.g. gpt-4o)");
        // Tool adapters (each has ENABLED / BASE_URL / API_KEY)
        for (String code : TOOL_CODES) {
            String prefix = "TOOL_" + code.toUpperCase(Locale.ROOT).replace("-", "_");
            vars.put(prefix + "_ENABLED", "Set to true to enable the " + code + " tool adapter");
            vars.put(prefix + "_BASE_URL", "Base URL for the " + code + " service");
            vars.put(prefix + "_API_KEY", "API key for the " + code + " service (if required)");
        }
        // ImageMagick path config
        vars.put("TOOL_IMAGEMAGICK_ALLOWED_INPUT_ROOTS", "Comma-separated allowed input roots (default: exports,uploads)");
        vars.put("TOOL_IMAGEMAGICK_ALLOWED_OUTPUT_ROOTS", "Comma-separated allowed output roots (default: exports/detail-compositions)");
        REQUIRED_ENV_VARS = Collections.unmodifiableMap(vars);
    }

    private final Environment environment;
    private final ToolAdapterService toolAdapterService;

    public EnvironmentDiagnosticServiceImpl(Environment environment, ToolAdapterService toolAdapterService) {
        this.environment = environment;
        this.toolAdapterService = toolAdapterService;
    }

    @Override
    public EnvironmentDiagnosticDTO diagnose() {
        EnvironmentDiagnosticDTO dto = new EnvironmentDiagnosticDTO();

        AiRelayDiagnostic relayDiag = diagnoseRelay();
        dto.setAiRelay(relayDiag);

        List<ToolDiagnostic> toolDiags = diagnoseTools();
        dto.setTools(toolDiags);

        PathDiagnostics pathDiag = diagnosePaths();
        dto.setPaths(pathDiag);

        ExportDiagnostics exportDiag = diagnoseExport();
        dto.setExport(exportDiag);

        dto.setRequiredEnvVars(REQUIRED_ENV_VARS);

        // Compute overall status
        boolean hasError = "CONFIG_ERROR".equals(relayDiag.getStatus())
                || toolDiags.stream().anyMatch(t -> "CONFIG_ERROR".equals(t.getStatus()))
                || "ERROR".equals(pathDiag.getStatus());
        boolean hasAvailable = "AVAILABLE".equals(relayDiag.getStatus())
                || toolDiags.stream().anyMatch(t -> "AVAILABLE".equals(t.getStatus()));

        if (hasError) {
            dto.setOverallStatus("DEGRADED");
            dto.setMessage("Some components have configuration errors. Check individual tool and path statuses.");
        } else if (hasAvailable) {
            dto.setOverallStatus("READY");
            dto.setMessage("At least one AI relay or tool adapter is configured and available.");
        } else {
            dto.setOverallStatus("NOT_READY");
            dto.setMessage("No AI relay or tool adapter is configured. Set the required environment variables listed in requiredEnvVars.");
        }

        return dto;
    }

    private AiRelayDiagnostic diagnoseRelay() {
        AiRelayDiagnostic diag = new AiRelayDiagnostic();
        boolean enabled = Boolean.parseBoolean(env("ai.relay.enabled", "false"));
        String baseUrl = env("ai.relay.base-url", "");
        String apiKey = env("ai.relay.api-key", "");
        String model = env("ai.relay.model", "");

        diag.setEnabled(enabled);
        diag.setBaseUrl(maskUrl(baseUrl));
        diag.setModel(model);
        diag.setHasApiKey(StringUtils.hasText(apiKey));

        List<String> missing = new ArrayList<>();
        if (!enabled) missing.add("AI_RELAY_ENABLED");
        if (!StringUtils.hasText(baseUrl)) missing.add("AI_RELAY_BASE_URL");
        if (!StringUtils.hasText(model)) missing.add("AI_RELAY_MODEL");
        diag.setMissingFields(missing);

        if (!enabled) {
            diag.setStatus("NOT_CONFIGURED");
            diag.setMessage("AI relay is disabled. Set AI_RELAY_ENABLED=true and provide AI_RELAY_BASE_URL + AI_RELAY_MODEL.");
        } else if (missing.isEmpty()) {
            diag.setStatus("AVAILABLE");
            diag.setMessage("AI relay is configured. Calls will go to " + maskUrl(baseUrl) + "/v1/chat/completions.");
        } else {
            diag.setStatus("CONFIG_ERROR");
            diag.setMessage("AI relay is enabled but missing required fields: " + String.join(", ", missing));
        }
        return diag;
    }

    private List<ToolDiagnostic> diagnoseTools() {
        return TOOL_CODES.stream().map(code -> {
            ToolDiagnostic diag = new ToolDiagnostic();
            diag.setCode(code);

            // Pull static metadata from the adapter service
            try {
                var info = toolAdapterService.getTool(code);
                diag.setName(info.getName());
                diag.setCategory(info.getCategory());
                diag.setIntegrationMode(info.getIntegrationMode());
                diag.setRepository(info.getRepository());
                diag.setOperations(info.getOperations());
            } catch (Exception e) {
                diag.setName(code);
                diag.setCategory("UNKNOWN");
            }

            boolean enabled = Boolean.parseBoolean(env("tools.adapters." + code + ".enabled", "false"));
            String baseUrl = env("tools.adapters." + code + ".base-url", "");
            String apiKey = env("tools.adapters." + code + ".api-key", "");

            diag.setEnabled(enabled);
            diag.setHasBaseUrl(StringUtils.hasText(baseUrl));
            diag.setHasApiKey(StringUtils.hasText(apiKey));

            List<String> missing = new ArrayList<>();
            if (!enabled) missing.add(envVarName(code, "ENABLED"));
            if (!StringUtils.hasText(baseUrl)) missing.add(envVarName(code, "BASE_URL"));
            diag.setMissingFields(missing);

            if (!enabled) {
                diag.setStatus("NOT_CONFIGURED");
                diag.setMessage("Tool adapter is disabled. Set " + envVarName(code, "ENABLED") + "=true and provide " + envVarName(code, "BASE_URL") + ".");
            } else if (!StringUtils.hasText(baseUrl)) {
                diag.setStatus("CONFIG_ERROR");
                diag.setMessage("Tool adapter is enabled but base-url is missing. Set " + envVarName(code, "BASE_URL") + ".");
            } else {
                diag.setStatus("AVAILABLE");
                diag.setMessage("Tool adapter is configured at " + maskUrl(baseUrl) + ".");
            }
            return diag;
        }).collect(Collectors.toList());
    }

    private PathDiagnostics diagnosePaths() {
        PathDiagnostics diag = new PathDiagnostics();

        String inputRootsRaw = env("tools.adapters.imagemagick.allowed-input-roots", "");
        String outputRootsRaw = env("tools.adapters.imagemagick.allowed-output-roots", "");
        String exportRootsRaw = env("security.allowed-export-roots", "");

        List<Path> inputRoots = LocalPathPolicy.parseAllowedRoots(inputRootsRaw, List.of("exports", "uploads"));
        List<Path> outputRoots = LocalPathPolicy.parseAllowedRoots(outputRootsRaw, List.of("exports/detail-compositions"));
        List<Path> exportRoots = LocalPathPolicy.parseAllowedRoots(exportRootsRaw, List.of("exports"));

        diag.setImagemagickInputRoots(inputRoots.stream().map(this::toPathEntry).collect(Collectors.toList()));
        diag.setImagemagickOutputRoots(outputRoots.stream().map(this::toPathEntry).collect(Collectors.toList()));
        diag.setExportRoots(exportRoots.stream().map(this::toPathEntry).collect(Collectors.toList()));

        boolean anyError = java.util.stream.Stream.of(
                        diag.getImagemagickInputRoots(),
                        diag.getImagemagickOutputRoots(),
                        diag.getExportRoots()
                )
                .flatMap(Collection::stream)
                .anyMatch(p -> "NOT_WRITABLE".equals(p.getStatus()) || "MISSING".equals(p.getStatus()));

        diag.setStatus(anyError ? "WARNING" : "OK");
        diag.setMessage(anyError
                ? "Some configured path roots are missing or not writable. Check individual paths."
                : "All configured path roots exist and are writable.");
        return diag;
    }

    private PathEntry toPathEntry(Path path) {
        PathEntry entry = new PathEntry();
        entry.setConfiguredValue(path.toString());
        entry.setResolvedPath(path.toAbsolutePath().normalize().toString());
        entry.setExists(Files.isDirectory(path));
        try {
            entry.setWritable(Files.isDirectory(path) && Files.isWritable(path));
        } catch (SecurityException e) {
            entry.setWritable(false);
        }
        if (!entry.isExists()) {
            entry.setStatus("MISSING");
            entry.setMessage("Directory does not exist. Create it before using dependent features.");
        } else if (!entry.isWritable()) {
            entry.setStatus("NOT_WRITABLE");
            entry.setMessage("Directory exists but is not writable. Check file system permissions.");
        } else {
            entry.setStatus("OK");
            entry.setMessage("Directory exists and is writable.");
        }
        return entry;
    }

    private ExportDiagnostics diagnoseExport() {
        ExportDiagnostics diag = new ExportDiagnostics();
        List<String> supported = new ArrayList<>();
        List<String> unimplemented = new ArrayList<>();
        for (ExportFormat format : ExportFormat.values()) {
            if (format.isImplemented()) {
                supported.add(format.name());
            } else {
                unimplemented.add(format.name());
            }
        }
        diag.setSupportedFormats(supported);
        diag.setUnimplementedFormats(unimplemented);
        diag.setDefaultOutputPath("exports");
        diag.setStatus(unimplemented.isEmpty() ? "OK" : "WARNING");
        diag.setMessage(unimplemented.isEmpty()
                ? "All declared export formats are implemented."
                : "Formats not yet implemented: " + String.join(", ", unimplemented) + ". Attempts to use them will throw UnsupportedOperationException.");
        return diag;
    }

    private String env(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }

    private static String envVarName(String toolCode, String suffix) {
        return "TOOL_" + toolCode.toUpperCase(Locale.ROOT).replace("-", "_") + "_" + suffix;
    }

    private static String maskUrl(String url) {
        if (!StringUtils.hasText(url)) return "";
        // Just return as-is for diagnostics; API keys are separate
        return url;
    }
}
