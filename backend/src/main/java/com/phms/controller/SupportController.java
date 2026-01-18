package com.phms.controller;

import com.phms.common.result.Result;
import com.phms.service.SupportMatchService;
import com.phms.ws.SupportWebSocketHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 客服支持控制器
 */
@Tag(name = "客服支持", description = "在线客服相关接口")
@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {
    @Operation(summary = "获取在线客服", description = "返回当前在线的客服员工ID")
    @GetMapping("/online-staff")
    public Result<Map<String, Object>> getOnlineStaff(@RequestParam(required = false) Long hotelId) {
        Long staffId = supportMatchService.matchOnDutyStaff(hotelId, LocalDateTime.now());

        Map<String, Object> result = new HashMap<>();
        if (staffId != null) {
            result.put("staffId", staffId);
            result.put("id", staffId);
            return Result.success("已匹配在线客服", result);
        } else {
            return Result.fail(404, "当前暂无在线客服");
        }
    }
    private final SupportMatchService supportMatchService;
    private final SupportWebSocketHandler webSocketHandler;



    @Operation(summary = "获取等待用户列表", description = "获取当前在线等待的用户列表")
    @GetMapping("/waiting-users")
    public Result<List<Map<String, Object>>> getWaitingUsers() {
        List<Map<String, Object>> users = webSocketHandler.getConnectedUsers();
        return Result.success("获取成功", users);
    }
}
