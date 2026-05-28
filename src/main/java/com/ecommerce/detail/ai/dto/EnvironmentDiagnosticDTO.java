package com.ecommerce.detail.ai.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Top-level environment diagnostic report.
 * Returned by GET /system/diagnostics so deployers can see
 * exactly what is available, not configured, or misconfigured
 * without reading source code.
 */
public class EnvironmentDiagnosticDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Overall system readiness: READY, DEGRADED, NOT_READY. */
    private String overallStatus;
    private String message;
    private AiRelayDiagnostic aiRelay;
    private List<ToolDiagnostic> tools;
    private PathDiagnostics paths;
    private ExportDiagnostics export;
    private Map<String, String> requiredEnvVars;
    private long generatedAt;

    public EnvironmentDiagnosticDTO() {
        this.generatedAt = System.currentTimeMillis();
    }

    // --- nested classes ---

    public static class AiRelayDiagnostic implements Serializable {
        private static final long serialVersionUID = 1L;
        private String status; // AVAILABLE, NOT_CONFIGURED, CONFIG_ERROR
        private String message;
        private String baseUrl;
        private String model;
        private boolean enabled;
        private boolean hasApiKey;
        private List<String> missingFields;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isHasApiKey() { return hasApiKey; }
        public void setHasApiKey(boolean hasApiKey) { this.hasApiKey = hasApiKey; }
        public List<String> getMissingFields() { return missingFields; }
        public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }
    }

    public static class ToolDiagnostic implements Serializable {
        private static final long serialVersionUID = 1L;
        private String code;
        private String name;
        private String category;
        private String integrationMode;
        private String status; // AVAILABLE, NOT_CONFIGURED, CONFIG_ERROR
        private String message;
        private boolean enabled;
        private boolean hasBaseUrl;
        private boolean hasApiKey;
        private List<String> missingFields;
        private String repository;
        private List<String> operations;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getIntegrationMode() { return integrationMode; }
        public void setIntegrationMode(String integrationMode) { this.integrationMode = integrationMode; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isHasBaseUrl() { return hasBaseUrl; }
        public void setHasBaseUrl(boolean hasBaseUrl) { this.hasBaseUrl = hasBaseUrl; }
        public boolean isHasApiKey() { return hasApiKey; }
        public void setHasApiKey(boolean hasApiKey) { this.hasApiKey = hasApiKey; }
        public List<String> getMissingFields() { return missingFields; }
        public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }
        public String getRepository() { return repository; }
        public void setRepository(String repository) { this.repository = repository; }
        public List<String> getOperations() { return operations; }
        public void setOperations(List<String> operations) { this.operations = operations; }
    }

    public static class PathDiagnostics implements Serializable {
        private static final long serialVersionUID = 1L;
        private List<PathEntry> imagemagickInputRoots;
        private List<PathEntry> imagemagickOutputRoots;
        private List<PathEntry> exportRoots;
        private String status; // OK, WARNING, ERROR
        private String message;

        public List<PathEntry> getImagemagickInputRoots() { return imagemagickInputRoots; }
        public void setImagemagickInputRoots(List<PathEntry> imagemagickInputRoots) { this.imagemagickInputRoots = imagemagickInputRoots; }
        public List<PathEntry> getImagemagickOutputRoots() { return imagemagickOutputRoots; }
        public void setImagemagickOutputRoots(List<PathEntry> imagemagickOutputRoots) { this.imagemagickOutputRoots = imagemagickOutputRoots; }
        public List<PathEntry> getExportRoots() { return exportRoots; }
        public void setExportRoots(List<PathEntry> exportRoots) { this.exportRoots = exportRoots; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class PathEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        private String configuredValue;
        private String resolvedPath;
        private boolean exists;
        private boolean writable;
        private String status; // OK, MISSING, NOT_WRITABLE
        private String message;

        public String getConfiguredValue() { return configuredValue; }
        public void setConfiguredValue(String configuredValue) { this.configuredValue = configuredValue; }
        public String getResolvedPath() { return resolvedPath; }
        public void setResolvedPath(String resolvedPath) { this.resolvedPath = resolvedPath; }
        public boolean isExists() { return exists; }
        public void setExists(boolean exists) { this.exists = exists; }
        public boolean isWritable() { return writable; }
        public void setWritable(boolean writable) { this.writable = writable; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ExportDiagnostics implements Serializable {
        private static final long serialVersionUID = 1L;
        private List<String> supportedFormats;
        private List<String> unimplementedFormats;
        private String defaultOutputPath;
        private String status; // OK, WARNING
        private String message;

        public List<String> getSupportedFormats() { return supportedFormats; }
        public void setSupportedFormats(List<String> supportedFormats) { this.supportedFormats = supportedFormats; }
        public List<String> getUnimplementedFormats() { return unimplementedFormats; }
        public void setUnimplementedFormats(List<String> unimplementedFormats) { this.unimplementedFormats = unimplementedFormats; }
        public String getDefaultOutputPath() { return defaultOutputPath; }
        public void setDefaultOutputPath(String defaultOutputPath) { this.defaultOutputPath = defaultOutputPath; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // --- top-level getters/setters ---

    public String getOverallStatus() { return overallStatus; }
    public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public AiRelayDiagnostic getAiRelay() { return aiRelay; }
    public void setAiRelay(AiRelayDiagnostic aiRelay) { this.aiRelay = aiRelay; }
    public List<ToolDiagnostic> getTools() { return tools; }
    public void setTools(List<ToolDiagnostic> tools) { this.tools = tools; }
    public PathDiagnostics getPaths() { return paths; }
    public void setPaths(PathDiagnostics paths) { this.paths = paths; }
    public ExportDiagnostics getExport() { return export; }
    public void setExport(ExportDiagnostics export) { this.export = export; }
    public Map<String, String> getRequiredEnvVars() { return requiredEnvVars; }
    public void setRequiredEnvVars(Map<String, String> requiredEnvVars) { this.requiredEnvVars = requiredEnvVars; }
    public long getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(long generatedAt) { this.generatedAt = generatedAt; }
}
