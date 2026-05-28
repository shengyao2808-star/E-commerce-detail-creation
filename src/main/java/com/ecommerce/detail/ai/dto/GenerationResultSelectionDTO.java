package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GenerationResultSelectionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean selected;

    private String selectionReason;
}
