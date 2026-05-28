package com.ecommerce.detail.ai.util;

import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Security utility for P5.3 compliance.
 * Provides path-traversal detection, file-type/size validation,
 * error-message scrubbing of local absolute paths, and
 * pre-publish compliance checks.
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    private static final Pattern ABSOLUTE_PATH_PATTERN = Pattern.compile(
            "([A-Za-z]:\\\\[\\\\\\S]+|/[\\S]+)");

    private static final int MAX_PATH_COMPONENT_LENGTH = 255;
    private static final long DEFAULT_MAX_FILE_SIZE_BYTES = 100L * 1024 * 1024; // 100 MB

    /** Characters that are never allowed in user-supplied file paths. */
    private static final Set<String> DANGEROUS_PATH_SEGMENTS = Set.of(
            "..", "~", "\\", "\0"
    );

    // ── Path-traversal detection ──────────────────────────────────

    /**
     * Returns true when {@code rawPath} contains path-traversal sequences
     * ({@code ..}, null bytes, backslash escapes, or tilde expansion).
     */
    public static boolean containsPathTraversal(String rawPath) {
        if (!StringUtils.hasText(rawPath)) {
            return false;
        }
        String normalized = rawPath.replace('\\', '/');
        for (String segment : normalized.split("/")) {
            if (!StringUtils.hasText(segment)) {
                continue;
            }
            if ("..".equals(segment) || "~".equals(segment)) {
                return true;
            }
            if (segment.contains("\0")) {
                return true;
            }
        }
        // Also block raw backslash sequences on non-Windows paths
        if (rawPath.contains("\\..") || rawPath.contains("/../")) {
            return true;
        }
        return false;
    }

    /**
     * Throws {@link IllegalArgumentException} if the path contains traversal
     * sequences.
     */
    public static void rejectPathTraversal(String rawPath, String label) {
        if (containsPathTraversal(rawPath)) {
            throw new IllegalArgumentException(
                    label + " contains illegal path traversal sequences");
        }
    }

    /**
     * Validates every path in a list for path traversal.
     */
    public static void rejectPathTraversalInList(List<String> paths, String label) {
        if (paths == null || paths.isEmpty()) {
            return;
        }
        for (String path : paths) {
            rejectPathTraversal(path, label);
        }
    }

    // ── File-type validation ──────────────────────────────────────

    /**
     * Returns true when the file's extension is in the allowed set.
     * Comparison is case-insensitive; extension is the part after the last dot.
     */
    public static boolean isAllowedExtension(String fileName, Set<String> allowedExtensions) {
        if (!StringUtils.hasText(fileName) || allowedExtensions == null) {
            return false;
        }
        String extension = FileUtil.getFileExtension(fileName);
        if (extension.isEmpty()) {
            return false;
        }
        return allowedExtensions.contains(extension.toLowerCase(Locale.ROOT));
    }

    /**
     * Throws if the file extension is not in the allowed set.
     */
    public static void requireAllowedExtension(String fileName, Set<String> allowedExtensions, String label) {
        if (!isAllowedExtension(fileName, allowedExtensions)) {
            String ext = FileUtil.getFileExtension(fileName);
            throw new IllegalArgumentException(
                    label + " has unsupported file type: " + (ext.isEmpty() ? "(none)" : ext));
        }
    }

    // ── File-size validation ──────────────────────────────────────

    /**
     * Throws if {@code sizeBytes} exceeds {@code maxBytes}.
     */
    public static void requireFileSizeWithinLimit(long sizeBytes, long maxBytes, String label) {
        if (sizeBytes > maxBytes) {
            throw new IllegalArgumentException(
                    label + " exceeds the maximum allowed size (" + (maxBytes / 1024 / 1024) + " MB)");
        }
    }

    /**
     * Default max-file-size check (100 MB).
     */
    public static void requireFileSizeWithinLimit(long sizeBytes, String label) {
        requireFileSizeWithinLimit(sizeBytes, DEFAULT_MAX_FILE_SIZE_BYTES, label);
    }

    // ── Error-message scrubbing ───────────────────────────────────

    /**
     * Removes local absolute filesystem paths from an error message so they
     * are never exposed to API callers. Replaces matched paths with
     * {@code "[path]"}.
     */
    public static String scrubLocalPaths(String message) {
        if (!StringUtils.hasText(message)) {
            return message;
        }
        return ABSOLUTE_PATH_PATTERN.matcher(message).replaceAll("[path]");
    }

    /**
     * Produces a safe, client-facing error message: strips local paths and
     * caps length to prevent oversized responses.
     */
    public static String safeClientMessage(String rawMessage) {
        String scrubbed = scrubLocalPaths(rawMessage);
        if (scrubbed != null && scrubbed.length() > 500) {
            scrubbed = scrubbed.substring(0, 500) + "...";
        }
        return scrubbed;
    }

    // ── Pre-export / pre-publish compliance gate ──────────────────

    /**
     * Returns a user-facing label for the given audit status code.
     */
    public static String auditStatusLabel(Integer code) {
        if (code == null) {
            return "unknown";
        }
        return switch (code) {
            case 0 -> "PENDING";
            case 1 -> "AUDITING";
            case 2 -> "APPROVED";
            case 3 -> "REJECTED";
            case 4 -> "NEED_MODIFY";
            default -> "UNKNOWN";
        };
    }
}
