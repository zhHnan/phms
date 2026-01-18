package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品VO
 */
@Data
@Schema(description = "商品")
public class ProductVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "名称")
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

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "关联门店ID")
    private List<Long> hotelIds;

    @Schema(description = "关联门店名称")
    private List<String> hotelNames;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
