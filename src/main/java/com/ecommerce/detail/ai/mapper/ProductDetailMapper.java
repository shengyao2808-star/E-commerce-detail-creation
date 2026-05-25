package com.ecommerce.detail.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.detail.ai.entity.ProductDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品详情页Mapper接口
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Mapper
public interface ProductDetailMapper extends BaseMapper<ProductDetail> {
}
