package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.common.annotation.OperateLog;
import com.phms.common.result.Result;
import com.phms.entity.CareLog;
import com.phms.service.CareLogService;
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

    @Operation(summary = "分页查询护理日志")
    @GetMapping("/page")
    @SaCheckPermission("care:list")
    public Result<Page<CareLog>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "订单ID") @RequestParam(required = false) Long orderId,
            @Parameter(description = "护理类型") @RequestParam(required = false) Integer careType) {
        Page<CareLog> page = new Page<>(pageNum, pageSize);
        return Result.success(careLogService.pageList(page, orderId, careType));
    }

    @Operation(summary = "根据订单ID查询护理日志列表")
    @GetMapping("/order/{orderId}")
    public Result<List<CareLog>> listByOrderId(@PathVariable Long orderId) {
        return Result.success(careLogService.listByOrderId(orderId));
    }

    @Operation(summary = "新增护理日志")
    @PostMapping
    @SaCheckPermission("care:add")
    @OperateLog(module = "care", type = "add", description = "新增护理日志")
    public Result<Void> add(@Valid @RequestBody CareLog careLog) {
        // 设置当前员工ID
        careLog.setStaffId(StpUtil.getLoginIdAsLong());
        careLogService.save(careLog);
        return Result.success();
    }

    @Operation(summary = "删除护理日志")
    @DeleteMapping("/{id}")
    @SaCheckPermission("care:add")
    @OperateLog(module = "care", type = "delete", description = "删除护理日志")
    public Result<Void> delete(@PathVariable Long id) {
        careLogService.removeById(id);
        return Result.success();
    }
}
