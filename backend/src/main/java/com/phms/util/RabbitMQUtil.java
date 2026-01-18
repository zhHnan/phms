package com.phms.util;

import com.phms.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 消息发送工具类
 *
 * @author PHMS
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQUtil {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送订单状态变更消息
     *
     * @param orderId 订单ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    public void sendOrderStatusChangeMessage(Long orderId, Integer oldStatus, Integer newStatus) {
        try {
            OrderStatusMessage message = new OrderStatusMessage();
            message.setOrderId(orderId);
            message.setOldStatus(oldStatus);
            message.setNewStatus(newStatus);
            message.setTimestamp(System.currentTimeMillis());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_ROUTING_KEY,
                    message
            );

            log.info("发送订单状态变更消息: orderId={}, {} -> {}", orderId, oldStatus, newStatus);
        } catch (Exception e) {
            log.error("发送订单状态变更消息失败: orderId={}", orderId, e);
        }
    }

    /**
     * 发送通知消息
     *
     * @param userId 用户ID
     * @param title 通知标题
     * @param content 通知内容
     */
    public void sendNotification(Long userId, String title, String content) {
        try {
            NotificationMessage message = new NotificationMessage();
            message.setUserId(userId);
            message.setTitle(title);
            message.setContent(content);
            message.setTimestamp(System.currentTimeMillis());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    message
            );

            log.info("发送通知消息: userId={}, title={}", userId, title);
        } catch (Exception e) {
            log.error("发送通知消息失败: userId={}", userId, e);
        }
    }

    /**
     * 发送订单超时检查消息（延迟消息）
     *
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param delayMillis 延迟时间（毫秒）
     */
    public void sendOrderTimeoutMessage(Long orderId, String orderNo, long delayMillis) {
        try {
            OrderTimeoutMessage message = new OrderTimeoutMessage();
            message.setOrderId(orderId);
            message.setOrderNo(orderNo);
            message.setCreateTime(System.currentTimeMillis());

            // 发送到延迟队列，并设置消息过期时间
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_TIMEOUT_EXCHANGE,
                    "order.timeout.delay",  // 路由到延迟队列
                    message,
                    messagePostProcessor -> {
                        // 设置消息过期时间（TTL）
                        messagePostProcessor.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                        return messagePostProcessor;
                    }
            );

            log.info("发送订单超时检查消息: orderId={}, orderNo={}, 延迟={}ms", orderId, orderNo, delayMillis);
        } catch (Exception e) {
            log.error("发送订单超时检查消息失败: orderId={}", orderId, e);
        }
    }

    /**
     * 订单状态变更消息
     */
    @lombok.Data
    public static class OrderStatusMessage {
        private Long orderId;
        private Integer oldStatus;
        private Integer newStatus;
        private Long timestamp;
    }

    /**
     * 通知消息
     */
    @lombok.Data
    public static class NotificationMessage {
        private Long userId;
        private String title;
        private String content;
        private Long timestamp;
    }

    /**
     * 订单超时消息
     */
    @lombok.Data
    public static class OrderTimeoutMessage {
        private Long orderId;
        private String orderNo;
        private Long createTime;
    }
}
