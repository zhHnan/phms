package com.phms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 *
 * @author PHMS
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    /**
     * 订单交换机
     */
    public static final String ORDER_EXCHANGE = "phms.order.exchange";

    /**
     * 订单队列
     */
    public static final String ORDER_QUEUE = "phms.order.queue";

    /**
     * 订单路由键
     */
    public static final String ORDER_ROUTING_KEY = "order.status.change";

    /**
     * 通知交换机
     */
    public static final String NOTIFICATION_EXCHANGE = "phms.notification.exchange";

    /**
     * 通知队列
     */
    public static final String NOTIFICATION_QUEUE = "phms.notification.queue";

    /**
     * 通知路由键
     */
    public static final String NOTIFICATION_ROUTING_KEY = "notification.send";

    /**
     * 订单超时延迟队列（死信队列）
     */
    public static final String ORDER_TIMEOUT_DELAY_QUEUE = "phms.order.timeout.delay.queue";

    /**
     * 订单超时处理队列
     */
    public static final String ORDER_TIMEOUT_QUEUE = "phms.order.timeout.queue";

    /**
     * 订单超时交换机
     */
    public static final String ORDER_TIMEOUT_EXCHANGE = "phms.order.timeout.exchange";

    /**
     * 订单超时路由键
     */
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";

    /**
     * 消息转换器 - 使用 JSON 格式
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        
        // 消息发送成功回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("消息发送成功: {}", correlationData);
            } else {
                log.error("消息发送失败: {}, 原因: {}", correlationData, cause);
            }
        });
        
        // 消息返回回调（消息无法路由时）
        template.setReturnsCallback(returned -> {
            log.error("消息无法路由: {}, 交换机: {}, 路由键: {}", 
                    returned.getMessage(), 
                    returned.getExchange(), 
                    returned.getRoutingKey());
        });
        
        log.info("RabbitTemplate 初始化成功");
        return template;
    }

    /**
     * 监听器容器工厂配置
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(1);
        return factory;
    }

    // ==================== 订单相关队列 ====================

    /**
     * 订单交换机
     */
    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder
                .directExchange(ORDER_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 订单队列
     */
    @Bean
    public Queue orderQueue() {
        return QueueBuilder
                .durable(ORDER_QUEUE)
                .build();
    }

    /**
     * 绑定订单队列到交换机
     */
    @Bean
    public Binding orderBinding() {
        return BindingBuilder
                .bind(orderQueue())
                .to(orderExchange())
                .with(ORDER_ROUTING_KEY);
    }

    // ==================== 通知相关队列 ====================

    /**
     * 通知交换机
     */
    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder
                .topicExchange(NOTIFICATION_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 通知队列
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_QUEUE)
                .build();
    }

    /**
     * 绑定通知队列到交换机
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    // ==================== 订单超时延迟队列（基于死信队列实现） ====================

    /**
     * 订单超时交换机
     */
    @Bean
    public DirectExchange orderTimeoutExchange() {
        return ExchangeBuilder
                .directExchange(ORDER_TIMEOUT_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 订单超时处理队列（真正消费的队列）
     */
    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder
                .durable(ORDER_TIMEOUT_QUEUE)
                .build();
    }

    /**
     * 订单超时延迟队列（使用死信机制实现延迟）
     * 消息在此队列中停留指定时间后，会自动转发到处理队列
     */
    @Bean
    public Queue orderTimeoutDelayQueue() {
        return QueueBuilder
                .durable(ORDER_TIMEOUT_DELAY_QUEUE)
                // 设置死信交换机
                .withArgument("x-dead-letter-exchange", ORDER_TIMEOUT_EXCHANGE)
                // 设置死信路由键
                .withArgument("x-dead-letter-routing-key", ORDER_TIMEOUT_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定延迟队列到超时交换机（用于接收延迟消息）
     */
    @Bean
    public Binding orderTimeoutDelayBinding() {
        return BindingBuilder
                .bind(orderTimeoutDelayQueue())
                .to(orderTimeoutExchange())
                .with("order.timeout.delay");
    }

    /**
     * 绑定处理队列到超时交换机（用于接收死信消息）
     */
    @Bean
    public Binding orderTimeoutBinding() {
        return BindingBuilder
                .bind(orderTimeoutQueue())
                .to(orderTimeoutExchange())
                .with(ORDER_TIMEOUT_ROUTING_KEY);
    }
}
