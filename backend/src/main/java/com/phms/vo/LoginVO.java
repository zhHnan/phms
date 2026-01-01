package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * C端用户登录结果 VO
 *
 * @author PHMS
 */
@Data
@Schema(description = "C端用户登录结果")
public class LoginVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "Token")
    private String token;
}
