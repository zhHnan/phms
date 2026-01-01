package com.phms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 员工 DTO
 *
 * @author PHMS
 */
@Data
@Schema(description = "员工信息")
public class StaffDTO {

    @Schema(description = "员工ID（修改时必填）")
    private Long id;

    @Schema(description = "门店ID")
    private Long hotelId;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "密码（新增时必填）")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名")
    private String realName;

    @NotNull(message = "角色类型不能为空")
    @Schema(description = "角色类型：1=普通员工 2=店长 9=平台超管")
    private Integer roleType;

    @Schema(description = "手机号")
    @NotNull(message = "手机号不能为空")
    private String phone;

    @Schema(description = "状态：1=正常 0=禁用")
    private Integer status;

}
