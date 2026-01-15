package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.common.result.Result;
import com.phms.entity.LoginLog;
import com.phms.entity.OperationLog;
import com.phms.service.LoginLogService;
import com.phms.service.OperationLogService;
import com.phms.vo.LoginLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    public Result<Page<LoginLogVO>> loginLogPage(
            @Parameter(description = "页码(兼容pageNum/page)") @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer page,
            @Parameter(description = "每页数量(兼容pageSize/size)") @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer size,
            @Parameter(description = "用户(用户名/邮箱/手机号)") @RequestParam(required = false) String userName,
            @Parameter(description = "登录IP") @RequestParam(required = false) String loginIp,
            @Parameter(description = "登录类型") @RequestParam(required = false) Integer loginType,
            @Parameter(description = "登录方式") @RequestParam(required = false) Integer loginWay,
            @Parameter(description = "登录状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String endDate) {

        int current = page != null ? page : (pageNum != null ? pageNum : 1);
        int pageSizeFinal = size != null ? size : (pageSize != null ? pageSize : 10);

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (startDate != null && !startDate.isBlank()) {
            startTime = LocalDate.parse(startDate).atStartOfDay();
        }
        if (endDate != null && !endDate.isBlank()) {
            endTime = LocalDate.parse(endDate).atTime(23, 59, 59);
        }

        Page<LoginLogVO> pageObj = new Page<>(current, pageSizeFinal);
        return Result.success(loginLogService.pageListVO(pageObj, userName, loginIp, loginType, loginWay, status, startTime, endTime));
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
