package com.ecommerce.detail.ai.service.linkpreview;

import com.ecommerce.detail.ai.dto.ProductLinkPreviewDTO;

/**
 * Preview source for a normalized product link.
 */
public interface ProductLinkPreviewSource {

    String code();

    boolean supports(ProductLinkPreviewContext context);

    ProductLinkPreviewDTO preview(ProductLinkPreviewContext context);
}
