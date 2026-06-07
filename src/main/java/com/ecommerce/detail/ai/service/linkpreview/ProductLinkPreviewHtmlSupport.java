package com.ecommerce.detail.ai.service.linkpreview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared HTML / JSON-LD parsing helpers for product-link preview sources.
 */
public class ProductLinkPreviewHtmlSupport {

    private static final Pattern TITLE_PATTERN = Pattern.compile("(?is)<title[^>]*>(.*?)</title>");
    private static final Pattern META_TAG_PATTERN = Pattern.compile("(?is)<meta\\s+([^>]*?(?:property|name)\\s*=\\s*['\"][^'\"]+['\"][^>]*)>");
    private static final Pattern ATTR_PATTERN = Pattern.compile("(?is)([a-zA-Z:_-]+)\\s*=\\s*(['\"])(.*?)\\2");
    private static final Pattern JSON_LD_PATTERN = Pattern.compile("(?is)<script[^>]*type\\s*=\\s*['\"]application/ld\\+json['\"][^>]*>(.*?)</script>");

    private final ObjectMapper objectMapper;

    public ProductLinkPreviewHtmlSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String extractPageTitle(String html) {
        Matcher matcher = TITLE_PATTERN.matcher(Optional.ofNullable(html).orElse(""));
        if (!matcher.find()) {
            return null;
        }
        return cleanPreviewText(matcher.group(1));
    }

    public String extractProductName(String html) {
        return cleanPreviewText(firstNonBlank(
                extractJsonLdProductField(html, "name"),
                extractMetaContent(html, "property", "og:title"),
                extractMetaContent(html, "name", "twitter:title"),
                extractPageTitle(html)
        ));
    }

    public String extractCategory(String html) {
        String category = cleanPreviewText(firstNonBlank(
                extractJsonLdProductField(html, "category"),
                extractBreadcrumbLeafCategory(html)
        ));
        if (StringUtils.hasText(category)) {
            return category;
        }
        String path = extractCategoryPath(html);
        return lastCategorySegment(path);
    }

    public String extractCategoryPath(String html) {
        List<String> breadcrumb = extractBreadcrumbPath(html);
        if (!breadcrumb.isEmpty()) {
            return String.join(" / ", breadcrumb);
        }
        String normalized = Optional.ofNullable(html).orElse("");
        Matcher matcher = Pattern.compile("([\\u4e00-\\u9fa5A-Za-z0-9]+(?:>[\\u4e00-\\u9fa5A-Za-z0-9]+){2,})").matcher(normalized);
        if (matcher.find()) {
            return cleanPreviewText(matcher.group(1).replace(">", " / "));
        }
        return null;
    }

    public String extractBrandName(String html, String platform, String host) {
        String brand = cleanPreviewText(firstNonBlank(
                extractJsonLdBrand(html),
                extractMetaContent(html, "property", "og:site_name")
        ));
        if (StringUtils.hasText(brand) && !brand.equals(platform)) {
            return brand;
        }
        if (!StringUtils.hasText(host)) {
            return null;
        }
        String normalizedHost = host.toLowerCase(Locale.ROOT);
        if ("独立站".equals(platform)) {
            String root = normalizedHost.replaceFirst("^www\\.", "");
            int dotIndex = root.indexOf('.');
            if (dotIndex > 0) {
                root = root.substring(0, dotIndex);
            }
            if (!root.matches(".*[a-zA-Z\\u4e00-\\u9fa5].*")) {
                return null;
            }
            return cleanPreviewText(root);
        }
        return null;
    }

    public boolean isLoginOrVerificationPage(String html) {
        if (!StringUtils.hasText(html)) {
            return false;
        }
        String normalized = html.toLowerCase(Locale.ROOT);
        return normalized.contains("login.taobao.com")
                || normalized.contains("login.m.taobao.com")
                || normalized.contains("member/login.jhtml")
                || normalized.contains("\"action\": \"login\"")
                || normalized.contains("/page/login_jump")
                || normalized.contains("请先登录")
                || normalized.contains("请登录");
    }

    public String cleanPreviewText(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String cleaned = HtmlUtils.htmlUnescape(raw)
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        if (!StringUtils.hasText(cleaned)) {
            return null;
        }
        List<String> removableSuffixes = Arrays.asList(
                "_淘宝搜索", "-淘宝网", "_京东", "-京东", "- 1688", "_阿里巴巴", "-拼多多", "_拼多多");
        for (String suffix : removableSuffixes) {
            if (cleaned.endsWith(suffix) && cleaned.length() > suffix.length()) {
                cleaned = cleaned.substring(0, cleaned.length() - suffix.length()).trim();
            }
        }
        return cleaned;
    }

    public String lastCategorySegment(String rawCategoryPath) {
        if (!StringUtils.hasText(rawCategoryPath)) {
            return null;
        }
        String[] segments = rawCategoryPath.split("\\s*/\\s*");
        if (segments.length == 0) {
            return cleanPreviewText(rawCategoryPath);
        }
        return cleanPreviewText(segments[segments.length - 1]);
    }

    private String extractMetaContent(String html, String attrName, String attrValue) {
        Matcher matcher = META_TAG_PATTERN.matcher(Optional.ofNullable(html).orElse(""));
        while (matcher.find()) {
            String tag = matcher.group(1);
            Map<String, String> attributes = parseAttributes(tag);
            String candidate = attributes.get(attrName);
            if (candidate != null && candidate.equalsIgnoreCase(attrValue)) {
                return cleanPreviewText(attributes.get("content"));
            }
        }
        return null;
    }

    private Map<String, String> parseAttributes(String rawTag) {
        if (!StringUtils.hasText(rawTag)) {
            return Collections.emptyMap();
        }
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        Matcher matcher = ATTR_PATTERN.matcher(rawTag);
        while (matcher.find()) {
            attributes.put(matcher.group(1).toLowerCase(Locale.ROOT), matcher.group(3));
        }
        return attributes;
    }

    private String extractJsonLdProductField(String html, String fieldName) {
        for (JsonNode node : extractJsonLdNodes(html)) {
            if (isType(node, "Product")) {
                String direct = firstJsonText(node.path(fieldName));
                if (StringUtils.hasText(direct)) {
                    return direct;
                }
            }
        }
        return null;
    }

    private String extractJsonLdBrand(String html) {
        for (JsonNode node : extractJsonLdNodes(html)) {
            if (isType(node, "Product")) {
                JsonNode brandNode = node.path("brand");
                String brand = firstJsonText(brandNode.path("name"));
                if (StringUtils.hasText(brand)) {
                    return brand;
                }
                brand = firstJsonText(brandNode);
                if (StringUtils.hasText(brand)) {
                    return brand;
                }
            }
        }
        return null;
    }

    private String extractBreadcrumbLeafCategory(String html) {
        List<String> path = extractBreadcrumbPath(html);
        if (path.isEmpty()) {
            return null;
        }
        if (path.size() >= 2) {
            return path.get(path.size() - 1);
        }
        return path.get(0);
    }

    private List<String> extractBreadcrumbPath(String html) {
        for (JsonNode node : extractJsonLdNodes(html)) {
            if (!isType(node, "BreadcrumbList")) {
                continue;
            }
            JsonNode itemList = node.path("itemListElement");
            if (!itemList.isArray()) {
                continue;
            }
            List<String> names = new ArrayList<>();
            for (JsonNode element : itemList) {
                String name = firstJsonText(element.path("name"));
                if (!StringUtils.hasText(name)) {
                    name = firstJsonText(element.path("item").path("name"));
                }
                if (StringUtils.hasText(name)) {
                    names.add(cleanPreviewText(name));
                }
            }
            if (!names.isEmpty()) {
                return names;
            }
        }
        return List.of();
    }

    private List<JsonNode> extractJsonLdNodes(String html) {
        if (!StringUtils.hasText(html)) {
            return List.of();
        }
        List<JsonNode> nodes = new ArrayList<>();
        Matcher matcher = JSON_LD_PATTERN.matcher(html);
        while (matcher.find()) {
            String payload = Optional.ofNullable(matcher.group(1)).orElse("").trim();
            if (!StringUtils.hasText(payload)) {
                continue;
            }
            try {
                JsonNode root = objectMapper.readTree(payload);
                collectJsonLdNodes(root, nodes);
            } catch (Exception ignored) {
                // Ignore malformed JSON-LD blocks and continue scanning other scripts.
            }
        }
        return nodes;
    }

    private void collectJsonLdNodes(JsonNode node, List<JsonNode> nodes) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return;
        }
        if (node.isArray()) {
            node.forEach(item -> collectJsonLdNodes(item, nodes));
            return;
        }
        if (node.isObject()) {
            nodes.add(node);
            JsonNode graph = node.path("@graph");
            if (graph.isArray()) {
                graph.forEach(item -> collectJsonLdNodes(item, nodes));
            }
        }
    }

    private boolean isType(JsonNode node, String expectedType) {
        if (node == null || node.isMissingNode()) {
            return false;
        }
        JsonNode typeNode = node.path("@type");
        if (typeNode.isArray()) {
            for (JsonNode item : typeNode) {
                if (expectedType.equalsIgnoreCase(item.asText())) {
                    return true;
                }
            }
            return false;
        }
        return expectedType.equalsIgnoreCase(typeNode.asText());
    }

    private String firstJsonText(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
            return node.asText();
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                String value = firstJsonText(item);
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
