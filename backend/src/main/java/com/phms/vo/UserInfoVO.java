package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * C端用户信息 VO（用于前端刷新用户资料）
 */
@Data
@Schema(description = "C端用户信息")
public class UserInfoVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像")
    private String avatar;
}
