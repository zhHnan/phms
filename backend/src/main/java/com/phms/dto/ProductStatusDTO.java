package com.phms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品状态 DTO
 */
@Data
@Schema(description = "商品状态")
public class ProductStatusDTO {

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态 1=上架 0=下架")
    private Integer status;
}
