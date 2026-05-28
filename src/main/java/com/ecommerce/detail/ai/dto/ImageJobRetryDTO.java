package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ImageJobRetryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String retryReason;
}
