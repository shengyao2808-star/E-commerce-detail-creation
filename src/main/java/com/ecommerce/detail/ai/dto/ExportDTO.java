package com.ecommerce.detail.ai.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Export request DTO with P3.12 manifest linkage.
 */
@Data
public class ExportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Product detail ID */
    @NotNull(message = "productDetailId must not be null")
    private Long productDetailId;

    /** Export format (WORD/MARKDOWN/JSON/HTML/TXT) */
    @NotBlank(message = "exportFormat must not be blank")
    private String exportFormat;

    /** Exporter */
    @NotBlank(message = "exporter must not be blank")
    private String exporter;

    /** P3.12: optional detail composition ID to link export to a specific composition */
    private Long detailCompositionId;
}