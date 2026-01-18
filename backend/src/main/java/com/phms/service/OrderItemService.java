package com.phms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.OrderItem;
import com.phms.vo.OrderItemVO;

import java.util.List;
import java.util.Map;

/**
 * 订单商品明细 Service
 */
public interface OrderItemService extends IService<OrderItem> {

    Map<Long, List<OrderItemVO>> listByOrderIds(List<Long> orderIds);

    List<OrderItemVO> listByOrderId(Long orderId);
}
