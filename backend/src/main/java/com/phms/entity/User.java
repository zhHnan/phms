package com.phms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * C端用户表
 *
 * @author PHMS
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_users")
@Schema(description = "C端用户信息")
public class User extends BaseEntity {

    @Schema(description = "手机号（登录/验证码）")
    private String phone;

    @Schema(description = "BCrypt加密密码（选填）")
    private String password;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "账户余额")
    private BigDecimal balance;

    @Schema(description = "状态：1=正常 0=禁用")
    private Integer status;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
}
