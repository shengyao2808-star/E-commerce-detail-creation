package com.ecommerce.detail.ai.service.linkpreview;

import com.ecommerce.detail.ai.dto.ProductLinkPreviewDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Preview source for public websites / independent sites where metadata parsing is enough.
 */
public class WebsiteMetadataProductLinkPreviewSource implements ProductLinkPreviewSource {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(8);

    private final HttpClient httpClient;
    private final ProductLinkPreviewHtmlSupport htmlSupport;

    public WebsiteMetadataProductLinkPreviewSource(HttpClient httpClient, ProductLinkPreviewHtmlSupport htmlSupport) {
        this.httpClient = httpClient;
        this.htmlSupport = htmlSupport;
    }

    @Override
    public String code() {
        return "website-metadata";
    }

    @Override
    public boolean supports(ProductLinkPreviewContext context) {
        return !context.isDomesticEcommerce();
    }

    @Override
    public ProductLinkPreviewDTO preview(ProductLinkPreviewContext context) {
        ProductLinkPreviewDTO preview = createBasePreview(context);
        preview.setSource(code());
        preview.setMessage("已识别平台，正在读取商品页信息。");

        try {
            HttpResponse<String> response = sendPreviewRequest(context.canonicalUri());
            hydrateResponseBase(preview, response.uri(), response.statusCode(), context.platform());

            if (response.statusCode() >= 400) {
                preview.setMessage("已识别平台，但商品页暂时没有正常返回。你仍可以手动补充商品名称和类目。");
                return preview;
            }

            String html = Optional.ofNullable(response.body()).orElse("");
            preview.setFetched(Boolean.TRUE);
            preview.setPageTitle(htmlSupport.extractPageTitle(html));
            preview.setProductName(htmlSupport.extractProductName(html));
            preview.setCategory(htmlSupport.extractCategory(html));
            preview.setRawCategoryPath(htmlSupport.extractCategoryPath(html));
            preview.setBrandName(htmlSupport.extractBrandName(html, preview.getPlatform(), preview.getHost()));
            preview.setLoginRequired(htmlSupport.isLoginOrVerificationPage(html));
            preview.setMessage(buildMessage(preview));
            return preview;
        } catch (Exception ignored) {
            preview.setMessage("已识别平台，但暂时没有读取到商品页信息。你仍可以手动补充商品名称和类目。");
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

    private String buildMessage(ProductLinkPreviewDTO preview) {
        if (Boolean.TRUE.equals(preview.getLoginRequired())
                && !StringUtils.hasText(preview.getProductName())
                && !StringUtils.hasText(preview.getCategory())) {
            return "已识别平台，但当前商品页需要登录后才能读取商品名称和类目。已先带入平台，请手动补充其余信息。";
        }
        List<String> filledFields = new ArrayList<>();
        if (StringUtils.hasText(preview.getPlatform())) {
            filledFields.add("平台");
        }
        if (StringUtils.hasText(preview.getProductName())) {
            filledFields.add("商品名称");
        }
        if (StringUtils.hasText(preview.getCategory())) {
            filledFields.add("商品类目");
        }
        if (StringUtils.hasText(preview.getBrandName())) {
            filledFields.add("品牌名称");
        }
        if (!filledFields.isEmpty()) {
            return "已识别" + String.join("、", filledFields) + "。若结果不完整，你仍可继续手动补充。";
        }
        return "已识别平台，但商品页没有返回可直接使用的名称或类目信息。你仍可以手动补充。";
    }
}
