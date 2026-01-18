package com.phms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_products")
@Schema(description = "商品")
public class Product extends BaseEntity {

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "图片JSON")
    private String images;

    @Schema(description = "状态 1=上架 0=下架")
    private Integer status;
}
