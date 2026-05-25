package com.ecommerce.detail.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

/**
 * Export format enum.
 */
@Getter
@AllArgsConstructor
public enum ExportFormat {

    WORD("docx", "Word", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", true, new String[]{"WORD", "DOCX"}),
    MARKDOWN("md", "Markdown", "text/markdown", true, new String[]{"MARKDOWN", "MD"}),
    JSON("json", "JSON", "application/json", true, new String[]{"JSON"}),
    HTML("html", "HTML", "text/html", true, new String[]{"HTML", "HTM"}),
    TXT("txt", "TXT", "text/plain", true, new String[]{"TXT", "TEXT"}),
    PDF("pdf", "PDF", "application/pdf", false, new String[]{"PDF"});

    private final String extension;
    private final String name;
    private final String mimeType;
    private final boolean implemented;
    private final String[] aliases;

    public static ExportFormat getByExtension(String extension) {
        return fromValue(extension);
    }

    public static ExportFormat fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Unsupported export format: " + value);
        }

        String normalizedValue = value.trim().toUpperCase(Locale.ROOT);
        for (ExportFormat format : values()) {
            if (format.getExtension().equalsIgnoreCase(value)
                    || format.name().equalsIgnoreCase(value)
                    || Arrays.stream(format.getAliases()).anyMatch(alias -> alias.equalsIgnoreCase(normalizedValue))) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unsupported export format: " + value);
    }
}
