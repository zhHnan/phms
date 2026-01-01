package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author：hnz
 * @Project：backend
 * @name：StaffInfoVO
 * @Date：2025/12/20 19:38
 * @Filename：StaffInfoVO
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "员工信息展示")
public class StaffInfoVO {

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

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "所属酒店")
    private String hotelName;

    @Schema(description = "员工ID")
    private Long id;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "is_deleted")
    private Integer isDeleted;
}
