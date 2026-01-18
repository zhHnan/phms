package com.phms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品库存调整 DTO
 */
@Data
@Schema(description = "商品库存调整")
public class ProductStockAdjustDTO {

    @NotNull(message = "调整数量不能为空")
    @Schema(description = "调整数量（可为负数）")
    private Integer delta;
}
