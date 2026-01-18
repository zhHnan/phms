package com.phms.listener;

import cn.hutool.json.JSONUtil;
import com.phms.common.constant.Constants;
import com.phms.config.RabbitMQConfig;
import com.phms.entity.Order;
import com.phms.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时监听器
 * 处理订单超时自动取消逻辑
 *
 * @author PHMS
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutListener {

    private final OrderService orderService;

    /**
     * 监听订单超时消息
     * 当订单创建5分钟后，自动检查订单状态，如果仍未支付则自动取消
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_TIMEOUT_QUEUE)
    public void handleOrderTimeout(OrderTimeoutMessage timeoutMessage) {
        log.info("====== 收到订单超时检查消息 ======");
        log.info("消息内容: orderId={}, orderNo={}", timeoutMessage.getOrderId(), timeoutMessage.getOrderNo());
        
        try {
            Long orderId = timeoutMessage.getOrderId();
            
            log.info("开始处理订单超时: orderId={}, orderNo={}", orderId, timeoutMessage.getOrderNo());
            
            // 查询订单当前状态
            Order order = orderService.getById(orderId);
            if (order == null) {
                log.warn("订单不存在，忽略超时处理: orderId={}", orderId);
                return;
            }
            
            log.info("订单当前状态: orderId={}, status={}", orderId, order.getStatus());
            
            // 检查订单状态，只有待支付状态才需要取消
            if (order.getStatus() == Constants.ORDER_STATUS_PENDING) {
                log.info("订单超时未支付，开始自动取消: orderId={}, orderNo={}", orderId, order.getOrderNo());
                
                // 调用取消订单服务
                boolean success = orderService.cancelOrder(orderId);
                if (success) {
                    log.info("✓ 订单自动取消成功: orderId={}, orderNo={}", orderId, order.getOrderNo());
                } else {
                    log.error("✗ 订单自动取消失败: orderId={}", orderId);
                }
            } else {
                log.info("订单状态已变更，无需取消: orderId={}, status={}, statusName={}", 
                    orderId, order.getStatus(), getStatusName(order.getStatus()));
            }
            
        } catch (Exception e) {
            log.error("处理订单超时消息失败: orderId={}", timeoutMessage.getOrderId(), e);
            // 注意：这里不抛出异常，避免消息重复消费
        }
        
        log.info("====== 订单超时检查处理完成 ======");
    }
    
    private String getStatusName(int status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "待入住";
            case 2: return "入住中";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }

    /**
     * 订单超时消息
     */
    @Data
    public static class OrderTimeoutMessage {
        /**
         * 订单ID
         */
        private Long orderId;
        
        /**
         * 订单号
         */
        private String orderNo;
        
        /**
         * 创建时间戳
         */
        private Long createTime;
    }
}
