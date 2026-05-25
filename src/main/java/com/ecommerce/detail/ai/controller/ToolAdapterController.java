package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tool-adapters")
public class ToolAdapterController {

    @Autowired
    private ToolAdapterService toolAdapterService;

    @GetMapping
    public Result<List<ToolAdapterInfoDTO>> listTools() {
        return Result.success(toolAdapterService.listTools());
    }

    @GetMapping("/{code}")
    public Result<ToolAdapterInfoDTO> getTool(@PathVariable String code) {
        return Result.success(toolAdapterService.getTool(code));
    }

    @PostMapping("/{code}/invoke")
    public Result<ToolInvokeResponseDTO> invokeTool(
            @PathVariable String code,
            @RequestBody ToolInvokeRequestDTO request) {
        return Result.success(toolAdapterService.invoke(code, request));
    }
}
