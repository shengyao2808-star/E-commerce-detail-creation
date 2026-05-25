package com.ecommerce.detail.ai.dto.tool;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolAdapterInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;
    private String name;
    private String category;
    private String repository;
    private Integer stars;
    private String license;
    private String integrationMode;
    private String commercialPolicy;
    private String defaultOperation;
    private String defaultPath;
    private List<String> operations;
    private boolean configured;
    private String status;
}
