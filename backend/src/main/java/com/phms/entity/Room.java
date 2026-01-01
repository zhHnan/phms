package com.phms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 房间配置表
 *
 * @author PHMS
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_rooms")
@Schema(description = "房间信息")
public class Room extends BaseEntity {

    @Schema(description = "所属门店ID")
    private Long hotelId;

    @Schema(description = "所属门店名称")
    @TableField(exist = false)
    private String hotelName;

    @Schema(description = "房间号（如A-101）")
    private String roomNo;

    @Schema(description = "房型（豪华猫屋/标准狗舍）")
    private String typeName;

    @Schema(description = "每晚价格")
    private BigDecimal pricePerNight;

    @Schema(description = "最大容纳宠物数")
    private Integer maxPetNum;

    @Schema(description = "设施标签（JSON格式）")
    private String features;

    @Schema(description = "状态：0=空闲 1=已预订 2=入住中 3=待清洁 4=维修")
    private Integer status;

    @Schema(description = "描述")
    private String description;
}
