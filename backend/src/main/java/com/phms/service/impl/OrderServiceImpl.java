package com.phms.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.common.constant.Constants;
import com.phms.common.exception.BusinessException;
import com.phms.common.result.ResultCode;
import com.phms.dto.OrderCreateDTO;
import com.phms.entity.Order;
import com.phms.entity.Room;
import com.phms.mapper.OrderMapper;
import com.phms.service.OrderService;
import com.phms.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 订单 Service 实现类
 *
 * @author PHMS
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final RoomService roomService;

    @Override
    public Page<Order> pageList(Page<Order> page, Long hotelId, Long userId, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(hotelId != null, Order::getHotelId, hotelId)
                .eq(userId != null, Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreatedAt);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(OrderCreateDTO dto) {
        // 检查房间是否可用
        Room room = roomService.getById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "房间不存在");
        }
        if (room.getStatus() != Constants.ROOM_STATUS_FREE) {
            throw new BusinessException(ResultCode.ROOM_UNAVAILABLE);
        }

        // 计算天数和金额
        long days = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        if (days <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "离店日期必须晚于入住日期");
        }
        BigDecimal totalAmount = room.getPricePerNight().multiply(BigDecimal.valueOf(days));

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setHotelId(dto.getHotelId());
        order.setPetId(dto.getPetId());
        order.setRoomId(dto.getRoomId());
        order.setCheckInDate(dto.getCheckInDate());
        order.setCheckOutDate(dto.getCheckOutDate());
        order.setTotalAmount(totalAmount);
        order.setStatus(Constants.ORDER_STATUS_PENDING);
        save(order);

        // 更新房间状态为已预订
        roomService.updateStatus(dto.getRoomId(), Constants.ROOM_STATUS_RESERVED);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != Constants.ORDER_STATUS_PENDING) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单状态不正确，无法支付");
        }

        // 更新订单状态和支付时间
        order.setStatus(Constants.ORDER_STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != Constants.ORDER_STATUS_PENDING && 
            order.getStatus() != Constants.ORDER_STATUS_PAID) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单状态不正确，无法取消");
        }

        // 更新订单状态
        order.setStatus(Constants.ORDER_STATUS_CANCELLED);
        updateById(order);

        // 释放房间
        roomService.updateStatus(order.getRoomId(), Constants.ROOM_STATUS_FREE);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkIn(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != Constants.ORDER_STATUS_PAID) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单状态不正确，无法办理入住");
        }

        // 更新订单状态
        order.setStatus(Constants.ORDER_STATUS_CHECKED_IN);
        updateById(order);

        // 更新房间状态为入住中
        roomService.updateStatus(order.getRoomId(), Constants.ROOM_STATUS_OCCUPIED);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkOut(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != Constants.ORDER_STATUS_CHECKED_IN) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单状态不正确，无法办理退房");
        }

        // 更新订单状态
        order.setStatus(Constants.ORDER_STATUS_COMPLETED);
        updateById(order);

        // 更新房间状态为待清洁
        roomService.updateStatus(order.getRoomId(), Constants.ROOM_STATUS_CLEANING);

        return true;
    }

    @Override
    public Order getByOrderNo(String orderNo) {
        return lambdaQuery().eq(Order::getOrderNo, orderNo).one();
    }

    @Override
    public long countTodayOrders(Long hotelId) {
        LocalDate today = LocalDate.now();
        return lambdaQuery()
                .eq(hotelId != null, Order::getHotelId, hotelId)
                .ge(Order::getCreatedAt, today.atStartOfDay())
                .lt(Order::getCreatedAt, today.plusDays(1).atStartOfDay())
                .count();
    }

    @Override
    public long countCheckedInPets(Long hotelId) {
        return lambdaQuery()
                .eq(hotelId != null, Order::getHotelId, hotelId)
                .eq(Order::getStatus, Constants.ORDER_STATUS_CHECKED_IN)
                .count();
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "PH" + LocalDateTimeUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") 
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
    }
}
