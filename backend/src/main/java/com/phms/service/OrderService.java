package com.phms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.Order;
import com.phms.dto.OrderCreateDTO;
import com.phms.vo.OrderVO;

/**
 * 订单 Service
 *
 * @author PHMS
 */
public interface OrderService extends IService<Order> {

    /**
     * 分页查询订单列表
     *
     * @param page    分页参数
     * @param hotelId 门店ID
     * @param userId  用户ID
     * @param status  状态
     * @return 分页结果
     */
    Page<Order> pageList(Page<Order> page, Long hotelId, Long userId, Integer status);

    /**
     * 分页查询订单列表（带酒店和房间信息）
     *
     * @param page    分页参数
     * @param hotelId 门店ID
     * @param userId  用户ID
     * @param status  状态
     * @return 分页结果
     */
    Page<OrderVO> pageListVO(Page<OrderVO> page,
                             Long hotelId,
                             String orderNo,
                             String petName,
                             Long userId,
                             Integer status);

    /**
     * 根据ID查询订单详情（带酒店和房间信息）
     *
     * @param id 订单ID
     * @return 订单详情
     */
    OrderVO getOrderVOById(Long id);

    /**
     * 创建订单
     *
     * @param dto 创建订单DTO
     * @return 订单信息
     */
    Order createOrder(OrderCreateDTO dto);

    /**
     * 支付订单（模拟支付）
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean payOrder(Long orderId);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean cancelOrder(Long orderId);

    /**
     * 办理入住
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean checkIn(Long orderId);

    /**
     * 办理退房
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean checkOut(Long orderId);

    /**
     * 根据订单号查询
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    Order getByOrderNo(String orderNo);

    /**
     * 统计门店今日订单数
     *
     * @param hotelId 门店ID
     * @return 订单数
     */
    long countTodayOrders(Long hotelId);

    /**
     * 统计门店当前在住宠物数
     *
     * @param hotelId 门店ID
     * @return 在住数
     */
    long countCheckedInPets(Long hotelId);

    /**
     * 统计门店今日收入
     *
     * @param hotelId 门店ID
     * @return 收入金额
     */
    java.math.BigDecimal calculateTodayRevenue(Long hotelId);

    /**
     * 统计门店本月收入
     *
     * @param hotelId 门店ID
     * @return 收入金额
     */
    java.math.BigDecimal calculateMonthRevenue(Long hotelId);

    /**
     * 统计门店总收入
     *
     * @param hotelId 门店ID
     * @return 收入金额
     */
    java.math.BigDecimal calculateTotalRevenue(Long hotelId);
}
