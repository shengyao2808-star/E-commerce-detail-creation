package com.ecommerce.detail.ai.service;

import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;

import java.util.List;

public interface ToolAdapterService {

    List<ToolAdapterInfoDTO> listTools();

    ToolAdapterInfoDTO getTool(String code);

    ToolInvokeResponseDTO invoke(String code, ToolInvokeRequestDTO request);
}
