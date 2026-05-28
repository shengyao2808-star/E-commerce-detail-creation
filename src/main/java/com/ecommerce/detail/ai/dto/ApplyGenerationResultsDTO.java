package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ApplyGenerationResultsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Long> generationResultIds;

    private Boolean selectedOnly;
}
