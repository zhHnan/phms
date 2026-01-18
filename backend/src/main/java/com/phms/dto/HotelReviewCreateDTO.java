package com.phms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 酒店评价创建 DTO
 */
@Data
@Schema(description = "酒店评价创建")
public class HotelReviewCreateDTO {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID")
    private Long orderId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    @Schema(description = "满意度评分：1-5")
    private Integer score;

    @Size(max = 500, message = "评价内容长度不能超过500")
    @Schema(description = "评价内容")
    private String content;
}
