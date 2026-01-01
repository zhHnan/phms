package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.common.result.Result;
import com.phms.entity.LoginLog;
import com.phms.entity.OperationLog;
import com.phms.service.LoginLogService;
import com.phms.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 日志管理控制器
 *
 * @author PHMS
 */
@Tag(name = "日志管理", description = "登录日志和操作日志查询接口")
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class LogController {

    private final LoginLogService loginLogService;
    private final OperationLogService operationLogService;

    @Operation(summary = "分页查询登录日志")
    @GetMapping("/login/page")
    @SaCheckPermission("log:login")
    public Result<Page<LoginLog>> loginLogPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "登录类型") @RequestParam(required = false) Integer loginType,
            @Parameter(description = "登录方式") @RequestParam(required = false) Integer loginWay,
            @Parameter(description = "登录状态") @RequestParam(required = false) Integer status) {
        Page<LoginLog> page = new Page<>(pageNum, pageSize);
        return Result.success(loginLogService.pageList(page, loginType, loginWay, status));
    }

    @Operation(summary = "分页查询操作日志")
    @GetMapping("/operation/page")
    @SaCheckPermission("log:operation")
    public Result<Page<OperationLog>> operationLogPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "操作人类型") @RequestParam(required = false) Integer operatorType,
            @Parameter(description = "操作模块") @RequestParam(required = false) String module,
            @Parameter(description = "操作类型") @RequestParam(required = false) String type) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        return Result.success(operationLogService.pageList(page, operatorType, module, type));
    }
}
