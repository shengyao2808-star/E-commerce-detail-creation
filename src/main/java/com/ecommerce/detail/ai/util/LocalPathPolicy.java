package com.ecommerce.detail.ai.util;

import org.springframework.util.StringUtils;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class LocalPathPolicy {

    private LocalPathPolicy() {
    }

    public static List<Path> parseAllowedRoots(String configuredRoots, List<String> defaultRoots) {
        List<String> rawRoots = new ArrayList<>();
        if (StringUtils.hasText(configuredRoots)) {
            for (String part : configuredRoots.split(",")) {
                if (StringUtils.hasText(part)) {
                    rawRoots.add(part.trim());
                }
            }
        }
        if (rawRoots.isEmpty() && defaultRoots != null) {
            rawRoots.addAll(defaultRoots);
        }

        Set<Path> normalized = new LinkedHashSet<>();
        for (String rawRoot : rawRoots) {
            normalized.add(toAbsolutePath(rawRoot, "allowed root"));
        }
        return List.copyOf(normalized);
    }

    public static Path requirePathWithinRoots(String rawPath, List<Path> allowedRoots, String label) {
        Path path = toAbsolutePath(rawPath, label);
        if (!isWithinAllowedRoots(path, allowedRoots)) {
            throw new IllegalArgumentException(label + " is outside the allowed local roots: " + rawPath);
        }
        return path;
    }

    public static boolean isWithinAllowedRoots(Path path, List<Path> allowedRoots) {
        if (path == null || allowedRoots == null || allowedRoots.isEmpty()) {
            return false;
        }
        Path normalized = path.toAbsolutePath().normalize();
        for (Path root : allowedRoots) {
            Path normalizedRoot = root.toAbsolutePath().normalize();
            if (normalized.startsWith(normalizedRoot)) {
                return true;
            }
        }
        return false;
    }

    public static Path toAbsolutePath(String rawPath, String label) {
        if (!StringUtils.hasText(rawPath)) {
            throw new IllegalArgumentException(label + " must not be blank");
        }

        try {
            String trimmed = rawPath.trim();
            Path path;
            if (trimmed.startsWith("file:")) {
                URI uri = URI.create(trimmed);
                if (!"file".equalsIgnoreCase(uri.getScheme())) {
                    throw new IllegalArgumentException(label + " must use the file:// scheme");
                }
                path = Paths.get(uri);
            } else {
                path = Paths.get(trimmed);
            }
            if (!path.isAbsolute()) {
                path = Paths.get("").toAbsolutePath().resolve(path);
            }
            return path.normalize();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid local path for " + label + ": " + rawPath, e);
        }
    }

    public static String toFileUri(Path path) {
        return path.toAbsolutePath().normalize().toUri().toString();
    }
}
