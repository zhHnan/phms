package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.common.annotation.OperateLog;
import com.phms.common.constant.Constants;
import com.phms.common.exception.BusinessException;
import com.phms.common.result.Result;
import com.phms.common.result.ResultCode;
import com.phms.dto.CareLogSaveDTO;
import com.phms.entity.CareLog;
import com.phms.entity.Order;
import com.phms.service.CareLogService;
import com.phms.service.OrderService;
import com.phms.vo.CareLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 护理日志控制器
 *
 * @author PHMS
 */
@Tag(name = "护理日志", description = "宠物护理日志的增删改查接口")
@RestController
@RequestMapping("/care-log")
@RequiredArgsConstructor
public class CareLogController {

    private final CareLogService careLogService;
    private final OrderService orderService;

    private Integer toCareType(String logType) {
        if (logType == null || logType.isBlank()) return null;
        return switch (logType) {
            case "feeding" -> 1;
            case "walking" -> 2;
            case "cleaning" -> 3;
            case "health_check" -> 4;
            case "other" -> 5;
            default -> null;
        };
    }

    private boolean isAdmin() {
        Object roleTypeObj = StpUtil.getSession().get("roleType");
        Integer roleType = roleTypeObj == null ? null : Integer.valueOf(String.valueOf(roleTypeObj));
        return roleType != null && roleType.equals(Constants.ROLE_ADMIN);
    }

    private Long currentHotelIdRequiredIfNotAdmin() {
        if (isAdmin()) return null;
        Object hotelIdObj = StpUtil.getSession().get("hotelId");
        if (hotelIdObj == null) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "无门店权限");
        }
        return Long.valueOf(String.valueOf(hotelIdObj));
    }

    @Operation(summary = "分页查询护理日志")
    @GetMapping("/page")
    @SaCheckPermission("care:list")
    public Result<Page<CareLogVO>> page(
            @Parameter(description = "页码(兼容pageNum/page)") @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer page,
            @Parameter(description = "每页数量(兼容pageSize/size)") @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer size,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "宠物名称") @RequestParam(required = false) String petName,
            @Parameter(description = "日志类型") @RequestParam(required = false) String logType) {
        Long hotelId = currentHotelIdRequiredIfNotAdmin();
        Integer careType = toCareType(logType);
        int current = page != null ? page : (pageNum != null ? pageNum : 1);
        int pageSizeFinal = size != null ? size : (pageSize != null ? pageSize : 10);
        Page<CareLogVO> pageObj = new Page<>(current, pageSizeFinal);
        return Result.success(careLogService.pageListVO(pageObj, hotelId, orderNo, petName, careType));
    }

    @Operation(summary = "根据订单ID查询护理日志列表")
    @GetMapping("/order/{orderId}")
    public Result<List<CareLogVO>> listByOrderId(@PathVariable Long orderId) {
        return Result.success(careLogService.listByOrderId(orderId));
    }

    @Operation(summary = "新增护理日志")
    @PostMapping
    @SaCheckPermission("care:add")
    @OperateLog(module = "care", type = "add", description = "新增护理日志")
    public Result<Void> add(@Valid @RequestBody CareLogSaveDTO dto) {
        Order order = orderService.getById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅可为入住中的订单添加护理日志");
        }
        Long hotelIdLimit = currentHotelIdRequiredIfNotAdmin();
        if (hotelIdLimit != null && !hotelIdLimit.equals(order.getHotelId())) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "无权限操作该门店订单");
        }

        CareLog careLog = new CareLog();
        careLog.setOrderId(dto.getOrderId());
        careLog.setHotelId(order.getHotelId());
        careLog.setStaffId(StpUtil.getLoginIdAsLong());
        careLog.setCareType(toCareType(dto.getLogType()));
        careLog.setContent(dto.getContent());
        careLog.setImages(dto.getImages());

        careLogService.save(careLog);
        return Result.success();
    }

    @Operation(summary = "编辑护理日志")
    @PutMapping("/{id}")
    @SaCheckPermission("care:add")
    @OperateLog(module = "care", type = "update", description = "编辑护理日志")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CareLogSaveDTO dto) {
        CareLog exist = careLogService.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "护理日志不存在");
        }

        Order order = orderService.getById(exist.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        Long hotelIdLimit = currentHotelIdRequiredIfNotAdmin();
        if (hotelIdLimit != null && !hotelIdLimit.equals(order.getHotelId())) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "无权限操作该门店订单");
        }

        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setCareType(toCareType(dto.getLogType()));
        careLog.setContent(dto.getContent());
        careLog.setImages(dto.getImages());
        careLogService.updateById(careLog);
        return Result.success();
    }

    @Operation(summary = "删除护理日志")
    @DeleteMapping("/{id}")
    @SaCheckPermission("care:add")
    @OperateLog(module = "care", type = "delete", description = "删除护理日志")
    public Result<Void> delete(@PathVariable Long id) {
        CareLog exist = careLogService.getById(id);
        if (exist == null) {
            return Result.success();
        }
        Order order = orderService.getById(exist.getOrderId());
        if (order != null) {
            Long hotelIdLimit = currentHotelIdRequiredIfNotAdmin();
            if (hotelIdLimit != null && !hotelIdLimit.equals(order.getHotelId())) {
                throw new BusinessException(ResultCode.NO_PERMISSION, "无权限操作该门店订单");
            }
        }
        careLogService.removeById(id);
        return Result.success();
    }
}
