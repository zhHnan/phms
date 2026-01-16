package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.common.annotation.OperateLog;
import com.phms.common.constant.Constants;
import com.phms.common.result.Result;
import com.phms.dto.CareLogSaveDTO;
import com.phms.dto.OrderCreateDTO;
import com.phms.entity.Order;
import com.phms.entity.Pet;
import com.phms.service.OrderService;
import com.phms.service.PetService;
import com.phms.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final PetService petService;

    @Operation(summary = "分页查询订单列表（B端）")
    @GetMapping("/page")
    @SaCheckPermission("order:list")
    public Result<Page<OrderVO>> page(
            @Parameter(description = "页码(兼容pageNum/page)") @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer page,
            @Parameter(description = "每页数量(兼容pageSize/size)") @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer size,
            @Parameter(description = "门店ID") @RequestParam(required = false) Long hotelId,
            @Parameter(description = "订单号/房间号（模糊）") @RequestParam(required = false) String orderNo,
            @Parameter(description = "宠物名称（模糊）") @RequestParam(required = false) String petName,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        int current = page != null ? page : (pageNum != null ? pageNum : 1);
        int pageSizeFinal = size != null ? size : (pageSize != null ? pageSize : 10);

        // 非超管只能查看自己门店
        Object roleTypeObj = StpUtil.getSession().get("roleType");
        Integer roleType = roleTypeObj == null ? null : Integer.valueOf(String.valueOf(roleTypeObj));
        if (roleType == null || !roleType.equals(Constants.ROLE_ADMIN)) {
            Object hotelIdObj = StpUtil.getSession().get("hotelId");
            if (hotelIdObj != null) {
                hotelId = Long.valueOf(String.valueOf(hotelIdObj));
            }
        }

        Page<OrderVO> pageObj = new Page<>(current, pageSizeFinal);
        return Result.success(orderService.pageListVO(pageObj, hotelId, orderNo, petName, userId, status));
    }

    @Operation(summary = "查询当前用户的订单列表（C端）")
    @GetMapping("/my-orders")
    public Result<Page<OrderVO>> myOrders(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<OrderVO> page = new Page<>(pageNum, pageSize);
        return Result.success(orderService.pageListVO(page, null, null, null, userId, status));
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

    @Operation(summary = "扫码支付回调")
    @GetMapping("/{id}/pay-scan")
    public Result<Void> payScan(@PathVariable Long id) {
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

    @Operation(summary = "用户删除订单（逻辑删除）")
    @DeleteMapping("/{id}")
    @OperateLog(module = "order", type = "delete", description = "用户删除订单")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.deleteOrderByUser(id, userId);
        return Result.success();
    }

    @Operation(summary = "查询订单关联的宠物列表")
    @GetMapping("/{id}/pets")
    @SaCheckPermission("order:checkin")
    public Result<List<Pet>> getPetsByOrderId(@PathVariable Long id) {
        List<Pet> pets = petService.getPetsByOrderId(id);
        return Result.success(pets);
    }

    @Operation(summary = "办理入住（带护理日志）")
    @PostMapping("/{id}/check-in")
    @SaCheckPermission("order:checkin")
    @OperateLog(module = "order", type = "update", description = "办理入住")
    public Result<Void> checkIn(@PathVariable Long id,
            @Valid @RequestBody(required = false) CareLogSaveDTO careLogDTO) {
        if (careLogDTO != null) {
            // 带护理日志的入住
            orderService.checkInWithCareLog(id, careLogDTO);
        } else {
            // 普通入住
            orderService.checkIn(id);
        }
        return Result.success();
    }

    @Operation(summary = "办理入住（旧端点，不带护理日志）")
    @PostMapping("/{id}/check-in-simple")
    @SaCheckPermission("order:checkin")
    @OperateLog(module = "order", type = "update", description = "办理入住")
    public Result<Void> checkInSimple(@PathVariable Long id) {
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
