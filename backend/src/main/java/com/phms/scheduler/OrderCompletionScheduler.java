package com.phms.scheduler;

import com.phms.common.constant.Constants;
import com.phms.entity.Order;
import com.phms.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCompletionScheduler {

    private final OrderService orderService;

    /**
     * 每天 00:00 执行
     * Cron: 秒 分 时 日 月 星期
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void completeYesterdayCheckoutOrders() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("[Scheduler] 开始处理昨日到期订单，日期={}", yesterday);

        // 查询状态为“入住中”，退房日期等于昨天的订单
        List<Order> needComplete = orderService.lambdaQuery()
                .eq(Order::getStatus, Constants.ORDER_STATUS_CHECKED_IN)
                .eq(Order::getCheckOutDate, yesterday)
                .list();

        if (needComplete.isEmpty()) {
            log.info("[Scheduler] 昨日到期订单数为 0，无需处理");
            return;
        }

        int success = 0;
        for (Order order : needComplete) {
            try {
                boolean ok = orderService.checkOut(order.getId());
                if (ok) success++;
            } catch (Exception ex) {
                log.error("[Scheduler] 订单退房完成失败, orderId={}, err={}", order.getId(), ex.getMessage(), ex);
            }
        }

        log.info("[Scheduler] 昨日到期订单处理完成，总数={}，成功={}，失败={}", needComplete.size(), success, needComplete.size() - success);
    }
}
