package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.common.constant.Constants;
import com.phms.common.result.Result;
import com.phms.entity.MessageCenter;
import com.phms.service.MessageCenterService;
import com.phms.vo.MessageReadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消息中心控制器
 *
 * @author PHMS
 */
@Tag(name = "消息中心", description = "消息中心查询与已读")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageCenterController {

    private final MessageCenterService messageCenterService;

    @Operation(summary = "分页查询我的消息")
    @GetMapping("/page")
    @SaCheckLogin
    public Result<Page<MessageCenter>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "是否已读：0=未读 1=已读") @RequestParam(required = false) Integer isRead) {

        // 目前消息中心用于员工端（B端，含超管/店长/员工）
        Object userType = StpUtil.getSession().get("userType");
        if (userType == null || (Integer) userType != Constants.USER_TYPE_STAFF) {
            return Result.fail("仅员工端支持消息中心");
        }

        Page<MessageCenter> page = new Page<>(pageNum, pageSize);
        return Result.success(messageCenterService.pageMyMessages(page, isRead));
    }

    @Operation(summary = "点击消息：标记已读")
    @PutMapping("/{id}/read")
    @SaCheckLogin
    public Result<MessageReadVO> read(@PathVariable Long id) {
        Object userType = StpUtil.getSession().get("userType");
        if (userType == null || (Integer) userType != Constants.USER_TYPE_STAFF) {
            return Result.fail("仅员工端支持消息中心");
        }
        return Result.success(messageCenterService.markRead(id));
    }

    @Operation(summary = "一键已读")
    @PutMapping("/read-all")
    @SaCheckLogin
    public Result<Integer> readAll() {
        Object userType = StpUtil.getSession().get("userType");
        if (userType == null || (Integer) userType != Constants.USER_TYPE_STAFF) {
            return Result.fail("仅员工端支持消息中心");
        }
        int affected = messageCenterService.markAllRead();
        return Result.success(affected);
    }
}
