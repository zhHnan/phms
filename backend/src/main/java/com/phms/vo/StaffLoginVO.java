package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * B端员工登录结果 VO
 *
 * @author PHMS
 */
@Data
@Schema(description = "B端员工登录结果")
public class StaffLoginVO {

    @Schema(description = "员工ID")
    private Long staffId;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "角色类型：1=普通员工 2=店长 9=平台超管")
    private Integer roleType;

    @Schema(description = "门店ID")
    private Long hotelId;

    @Schema(description = "门店名称")
    private String hotelName;

    @Schema(description = "权限码列表")
    private List<String> permissions;

    @Schema(description = "Token")
    private String token;
}
