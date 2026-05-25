package com.ecommerce.detail.ai.dto.tool;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolInvokeResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String toolCode;
    private String operation;
    private Integer statusCode;
    private Object body;
    private String rawBody;
}
