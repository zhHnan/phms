package com.phms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 验证码记录表
 *
 * @author PHMS
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_verification_codes")
@Schema(description = "验证码记录")
public class VerificationCode extends BaseEntity {

    @Schema(description = "手机号")
    private String target;

    @Schema(description = "6位验证码")
    private String code;

    @Schema(description = "类型：1=登录 2=注册")
    private Integer type;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "是否已使用：0=未使用 1=已使用")
    private Integer isUsed;
}
