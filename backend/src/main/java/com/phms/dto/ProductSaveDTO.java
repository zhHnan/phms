package com.phms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品新增/编辑 DTO
 */
@Data
@Schema(description = "商品新增/编辑")
public class ProductSaveDTO {

    @Schema(description = "商品ID（编辑时必填）")
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "分类")
    private String category;

    @NotNull(message = "单价不能为空")
    @Schema(description = "单价")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "图片JSON")
    private String images;

    @Schema(description = "状态 1=上架 0=下架")
    private Integer status;

    @Schema(description = "关联门店ID列表")
    private List<Long> hotelIds;
}
