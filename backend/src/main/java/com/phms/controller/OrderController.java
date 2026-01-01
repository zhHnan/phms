package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.common.annotation.OperateLog;
import com.phms.common.result.Result;
import com.phms.dto.OrderCreateDTO;
import com.phms.entity.Order;
import com.phms.service.OrderService;
import com.phms.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理控制器
 *
 * @author PHMS
 */
@Tag(name = "订单管理", description = "订单的增删改查及状态管理接口")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "分页查询订单列表（B端）")
    @GetMapping("/page")
    @SaCheckPermission("order:list")
    public Result<Page<OrderVO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "门店ID") @RequestParam(required = false) Long hotelId,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<OrderVO> page = new Page<>(pageNum, pageSize);
        return Result.success(orderService.pageListVO(page, hotelId, userId, status));
    }

    @Operation(summary = "查询当前用户的订单列表（C端）")
    @GetMapping("/my-orders")
    public Result<Page<OrderVO>> myOrders(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<OrderVO> page = new Page<>(pageNum, pageSize);
        return Result.success(orderService.pageListVO(page, null, userId, status));
    }

    @Operation(summary = "根据ID查询订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> getById(@PathVariable Long id) {
        return Result.success(orderService.getOrderVOById(id));
    }

    @Operation(summary = "根据订单号查询订单详情")
    @GetMapping("/no/{orderNo}")
    public Result<Order> getByOrderNo(@PathVariable String orderNo) {
        return Result.success(orderService.getByOrderNo(orderNo));
    }

    @Operation(summary = "创建订单")
    @PostMapping
    @OperateLog(module = "order", type = "add", description = "创建订单")
    public Result<Order> create(@Valid @RequestBody OrderCreateDTO dto) {
        // 设置当前用户ID
        Order order = orderService.createOrder(dto);
        return Result.success(order);
    }

    @Operation(summary = "支付订单（模拟）")
    @PostMapping("/{id}/pay")
    @OperateLog(module = "order", type = "update", description = "支付订单")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.payOrder(id);
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    @OperateLog(module = "order", type = "update", description = "取消订单")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success();
    }

    @Operation(summary = "办理入住")
    @PostMapping("/{id}/check-in")
    @SaCheckPermission("order:checkin")
    @OperateLog(module = "order", type = "update", description = "办理入住")
    public Result<Void> checkIn(@PathVariable Long id) {
        orderService.checkIn(id);
        return Result.success();
    }

    @Operation(summary = "办理退房")
    @PostMapping("/{id}/check-out")
    @SaCheckPermission("order:checkout")
    @OperateLog(module = "order", type = "update", description = "办理退房")
    public Result<Void> checkOut(@PathVariable Long id) {
        orderService.checkOut(id);
        return Result.success();
    }
}
