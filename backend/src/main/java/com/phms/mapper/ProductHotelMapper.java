package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.ProductHotel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品-门店关联 Mapper
 */
@Mapper
public interface ProductHotelMapper extends BaseMapper<ProductHotel> {
}
