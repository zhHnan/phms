package com.phms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * C端用户资料更新 DTO
 */
@Data
@Schema(description = "C端用户资料更新")
public class UserProfileUpdateDTO {

    @Schema(description = "昵称")
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @Schema(description = "头像URL")
    @Size(max = 255, message = "头像URL长度不能超过255")
    private String avatar;
}
