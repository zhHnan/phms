package com.phms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * B端员工登录 DTO
 *
 * @author PHMS
 */
@Data
@Schema(description = "B端员工登录请求")
public class StaffLoginDTO {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;
}
