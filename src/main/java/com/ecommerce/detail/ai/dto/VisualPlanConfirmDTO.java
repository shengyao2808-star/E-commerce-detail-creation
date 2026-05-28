package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class VisualPlanConfirmDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> confirmData;
}
