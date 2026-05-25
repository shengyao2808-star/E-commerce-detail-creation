package com.ecommerce.detail.ai.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果封装类
 * 
 * @param <T> 数据类型
 * @author Administrator
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends Result<List<T>> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer pages;

    public PageResult() {
        super();
    }

    public PageResult(Integer code, String message, List<T> data, 
                     Integer pageNum, Integer pageSize, Long total) {
        super(code, message, data);
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 简化构造函数
     */
    public PageResult(Long total, List<T> data, Integer pageNum, Integer pageSize) {
        super(200, "查询成功", data);
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 成功分页响应
     */
    public static <T> PageResult<T> success(List<T> data, Integer pageNum, 
                                           Integer pageSize, Long total) {
        return new PageResult<>(200, "查询成功", data, pageNum, pageSize, total);
    }

    /**
     * 空数据分页响应
     */
    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return new PageResult<>(200, "查询成功，无数据", List.of(), pageNum, pageSize, 0L);
    }
}
