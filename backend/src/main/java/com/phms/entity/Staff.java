package com.phms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 内部员工表
 *
 * @author PHMS
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_staff")
@Schema(description = "内部员工信息")
public class Staff extends BaseEntity {

    @Schema(description = "归属门店ID（超管为NULL）")
    private Long hotelId;

    @Schema(description = "登录邮箱")
    private String email;

    @Schema(description = "BCrypt加密密码")
    private String password;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "角色类型：1=普通员工 2=店长 9=平台超管")
    private Integer roleType;

    @Schema(description = "状态：1=正常 0=禁用")
    private Integer status;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
}
