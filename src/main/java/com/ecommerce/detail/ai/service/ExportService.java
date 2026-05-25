package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.ExportDTO;
import com.ecommerce.detail.ai.entity.ExportRecord;

/**
 * 导出服务接口
 * 
 * @author Administrator
 * @version 1.0.0
 */
public interface ExportService extends IService<ExportRecord> {

    /**
     * 导出商品详情页
     * 
     * @param dto 导出DTO
     * @return 导出记录ID
     */
    Long exportProductDetail(ExportDTO dto);

    /**
     * 批量导出商品详情页
     * 
     * @param dtos 导出列表
     * @return 成功导出数量
     */
    int batchExportProductDetails(java.util.List<ExportDTO> dtos);

    /**
     * 根据ID获取导出记录
     * 
     * @param id 导出记录ID
     * @return 导出记录
     */
    ExportRecord getExportById(Long id);

    /**
     * 分页查询导出记录
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param status 导出状态
     * @param exporter 导出人
     * @return 分页结果
     */
    PageResult<ExportRecord> listExportRecords(int pageNum, int pageSize, Integer status, String exporter);

    /**
     * 重新导出
     * 
     * @param id 导出记录ID
     * @return 是否成功
     */
    boolean reexport(Long id);

    /**
     * 删除导出记录
     * 
     * @param id 导出记录ID
     * @return 是否成功
     */
    boolean deleteExport(Long id);

    /**
     * 下载导出文件
     *
     * @param id 导出记录ID
     * @return 导出记录
     */
    ExportRecord downloadExportFile(Long id);
}
