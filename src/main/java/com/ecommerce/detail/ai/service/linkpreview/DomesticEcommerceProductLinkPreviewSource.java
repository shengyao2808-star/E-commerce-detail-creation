package com.ecommerce.detail.ai.service.linkpreview;

import com.ecommerce.detail.ai.dto.ProductLinkPreviewDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Preview source for domestic marketplaces. Current V1 focuses on platform normalization and
 * partial metadata recognition while reserving a clean seam for a future logged-in browser worker.
 */
public class DomesticEcommerceProductLinkPreviewSource implements ProductLinkPreviewSource {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(8);

    private final HttpClient httpClient;
    private final ProductLinkPreviewHtmlSupport htmlSupport;

    public DomesticEcommerceProductLinkPreviewSource(HttpClient httpClient, ProductLinkPreviewHtmlSupport htmlSupport) {
        this.httpClient = httpClient;
        this.htmlSupport = htmlSupport;
    }

    @Override
    public String code() {
        return "domestic-ecommerce";
    }

    @Override
    public boolean supports(ProductLinkPreviewContext context) {
        return context.isDomesticEcommerce();
    }

    @Override
    public ProductLinkPreviewDTO preview(ProductLinkPreviewContext context) {
        ProductLinkPreviewDTO preview = createBasePreview(context);
        preview.setSource(code());
        preview.setMessage("已识别平台，当前将优先带入平台和规范化链接。");

        try {
            HttpResponse<String> response = sendPreviewRequest(context.canonicalUri());
            hydrateResponseBase(preview, response.uri(), response.statusCode(), context.platform());
            if (response.statusCode() >= 400) {
                preview.setMessage("已识别平台，但当前商品页没有稳定返回。已先带入平台和规范化链接，请手动补充商品名称和类目。");
                return preview;
            }

            String html = Optional.ofNullable(response.body()).orElse("");
            preview.setFetched(Boolean.TRUE);
            preview.setPageTitle(htmlSupport.extractPageTitle(html));
            preview.setProductName(htmlSupport.extractProductName(html));
            preview.setRawCategoryPath(htmlSupport.extractCategoryPath(html));
            preview.setCategory(htmlSupport.extractCategory(html));
            preview.setBrandName(htmlSupport.extractBrandName(html, preview.getPlatform(), preview.getHost()));
            preview.setLoginRequired(htmlSupport.isLoginOrVerificationPage(html));
            preview.setSource(hasDetailedFields(preview) ? "marketplace-html" : code());
            preview.setMessage(buildMessage(preview));
            return preview;
        } catch (Exception ignored) {
            preview.setMessage("已识别平台，但当前只返回了平台和规范化链接。请手动补充其余信息，或改用截图 / PDF 继续生成。");
            return preview;
        }
    }

    private HttpResponse<String> sendPreviewRequest(URI uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36")
                .header(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9,en;q=0.8")
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private ProductLinkPreviewDTO createBasePreview(ProductLinkPreviewContext context) {
        ProductLinkPreviewDTO preview = new ProductLinkPreviewDTO();
        preview.setOriginalUrl(context.rawUrl() == null ? null : context.rawUrl().trim());
        preview.setResolvedUrl(context.canonicalUri().toString());
        preview.setHost(context.host());
        preview.setPlatform(context.platform());
        preview.setFetched(Boolean.FALSE);
        preview.setLoginRequired(Boolean.FALSE);
        return preview;
    }

    private void hydrateResponseBase(ProductLinkPreviewDTO preview, URI resolvedUri, int httpStatus, String defaultPlatform) {
        preview.setHttpStatus(httpStatus);
        preview.setResolvedUrl(resolvedUri == null ? preview.getResolvedUrl() : resolvedUri.toString());
        preview.setHost(resolvedUri == null ? preview.getHost() : resolvedUri.getHost());
        if (!StringUtils.hasText(preview.getPlatform())) {
            preview.setPlatform(defaultPlatform);
        }
    }

    private boolean hasDetailedFields(ProductLinkPreviewDTO preview) {
        return StringUtils.hasText(preview.getProductName())
                || StringUtils.hasText(preview.getCategory())
                || StringUtils.hasText(preview.getBrandName())
                || StringUtils.hasText(preview.getRawCategoryPath());
    }

    private String buildMessage(ProductLinkPreviewDTO preview) {
        if (Boolean.TRUE.equals(preview.getLoginRequired())
                && !StringUtils.hasText(preview.getProductName())
                && !StringUtils.hasText(preview.getCategory())) {
            return "已识别平台，但当前商品页在直连模式下仍需要登录态解析。已先带入平台和规范化链接，请手动补充其余信息。";
        }
        if (hasDetailedFields(preview)) {
            return "已识别平台和部分商品信息。当前结果已可继续生成，你也可以手动补全品牌、类目路径或更多详情。";
        }
        return "已识别平台，但当前只返回了平台和规范化链接。请手动补充其余信息，或改用截图 / PDF 继续生成。";
    }
}
