package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志展示 VO（给后台管理端使用）
 */
@Data
@Schema(description = "登录日志展示VO")
public class LoginLogVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID（C端为userId，B端为staffId）")
    private Long userId;

    @Schema(description = "用户名/邮箱")
    private String userName;

    @Schema(description = "用户类型：user=客户 staff=员工")
    private String userType;

    @Schema(description = "登录IP")
    private String loginIp;

    @Schema(description = "浏览器/设备信息")
    private String userAgent;

    @Schema(description = "状态：1=成功 0=失败")
    private Integer status;

    @Schema(description = "消息（失败原因/成功提示）")
    private String message;

    @Schema(description = "登录时间")
    private LocalDateTime createdAt;
}
