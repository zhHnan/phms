package com.phms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.OrderItem;
import com.phms.mapper.OrderItemMapper;
import com.phms.service.OrderItemService;
import com.phms.vo.OrderItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单商品明细 Service 实现
 */
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {

    private final OrderItemMapper orderItemMapper;

    @Override
    public Map<Long, List<OrderItemVO>> listByOrderIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(OrderItem::getOrderId, orderIds)
                .eq(OrderItem::getIsDeleted, 0);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }
        return items.stream().map(this::toVO)
                .collect(Collectors.groupingBy(OrderItemVO::getOrderId));
    }

    @Override
    public List<OrderItemVO> listByOrderId(Long orderId) {
        if (orderId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId)
                .eq(OrderItem::getIsDeleted, 0);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream().map(this::toVO).toList();
    }

    private OrderItemVO toVO(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setPrice(item.getPrice());
        vo.setQuantity(item.getQuantity());
        vo.setSubtotal(item.getSubtotal());
        vo.setOrderId(item.getOrderId());
        return vo;
    }
}
