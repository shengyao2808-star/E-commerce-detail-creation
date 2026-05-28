package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ResearchTaskChartsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Map<String, Object>> priceBands = List.of();

    private List<Map<String, Object>> keywordRanking = List.of();

    private List<Map<String, Object>> painPointRanking = List.of();

    private List<Map<String, Object>> competitorMatrix = List.of();
}
