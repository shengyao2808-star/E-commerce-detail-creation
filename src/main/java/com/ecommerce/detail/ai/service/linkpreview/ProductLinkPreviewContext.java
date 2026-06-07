package com.ecommerce.detail.ai.service.linkpreview;

import org.springframework.util.StringUtils;

import java.net.URI;

/**
 * Unified normalized context for product-link preview sources.
 */
public record ProductLinkPreviewContext(
        String rawUrl,
        URI normalizedUri,
        URI canonicalUri,
        String host,
        String platform
) {

    public boolean isDomesticEcommerce() {
        return StringUtils.hasText(platform) && !"独立站".equals(platform);
    }
}
