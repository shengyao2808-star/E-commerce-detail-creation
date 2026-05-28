package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ProductContentTaskApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> fields;
}
