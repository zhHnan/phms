package com.phms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建订单 DTO
 *
 * @author PHMS
 */
@Data
@Schema(description = "创建订单请求")
public class OrderCreateDTO {

    @NotNull(message = "门店ID不能为空")
    @Schema(description = "门店ID")
    private Long hotelId;

    @NotEmpty(message = "至少需要选择一只宠物")
    @Schema(description = "宠物ID列表")
    private List<Long> petIds;

    @NotNull(message = "房间ID不能为空")
    @Schema(description = "房间ID")
    private Long roomId;

    @NotNull(message = "入住日期不能为空")
    @Schema(description = "入住日期")
    private LocalDate checkInDate;

    @NotNull(message = "离店日期不能为空")
    @Schema(description = "离店日期")
    private LocalDate checkOutDate;

    @Schema(description = "商品明细")
    private List<OrderItemCreateDTO> items;

    @Schema(description = "备注")
    private String remark;
}
