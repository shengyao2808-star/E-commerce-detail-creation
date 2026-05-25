package com.ecommerce.detail.ai.dto.tool;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class ToolInvokeRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String operation;
    private Map<String, Object> payload;
    private Map<String, String> headers;
}
