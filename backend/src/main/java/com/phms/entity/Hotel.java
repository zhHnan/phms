package com.phms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门店信息表
 *
 * @author PHMS
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_hotels")
@Schema(description = "门店信息")
public class Hotel extends BaseEntity {

    @Schema(description = "门店名称")
    private String name;

    @Schema(description = "门店编码（唯一标识）")
    private String code;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "店长姓名")
    private String managerName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "状态：1=营业 0=停业")
    private Integer status;

    @Schema(description = "酒店图片URL列表（JSON数组）")
    private String images;
}
