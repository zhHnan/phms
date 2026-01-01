package com.phms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 宠物档案表
 *
 * @author PHMS
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pets")
@Schema(description = "宠物档案")
public class Pet extends BaseEntity {

    @Schema(description = "主人ID")
    private Long userId;

    @Schema(description = "宠物名字")
    private String name;

    @Schema(description = "宠物类型：1=猫 2=狗 3=异宠")
    private Integer type;

    @Schema(description = "宠物年龄（岁）")
    private Integer age;

    @Schema(description = "宠物体重（kg）")
    private BigDecimal weight;

    @Schema(description = "性格/健康备注")
    private String notes;

    @Schema(description = "宠物照片URL")
    private String photoUrl;
}
