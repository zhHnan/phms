package com.phms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志表
 *
 * @author PHMS
 */
@Data
@TableName("sys_login_logs")
@Schema(description = "登录日志")
public class LoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "登录主体类型：1=C端用户 2=B端员工")
    private Integer loginType;

    @Schema(description = "C端用户ID")
    private Long userId;

    @Schema(description = "B端员工ID")
    private Long staffId;

    @Schema(description = "登录方式：1=手机号验证码 2=邮箱密码 3=第三方登录")
    private Integer loginWay;

    @Schema(description = "登录IP地址")
    private String loginIp;

    @Schema(description = "设备信息")
    private String deviceInfo;

    @Schema(description = "登录状态：1=成功 0=失败")
    private Integer loginStatus;

    @Schema(description = "失败原因")
    private String failReason;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Schema(description = "登录时间")
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("is_deleted")
    @Schema(description = "逻辑删除")
    private Integer isDeleted;
}
