package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单商品明细 Mapper
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
