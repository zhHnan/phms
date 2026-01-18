package com.phms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "护理日志新增/编辑DTO")
public class CareLogSaveDTO {

    @Schema(description = "订单ID（用于CareLogController）")
    private Long orderId;

    @Schema(description = "宠物ID（用于OrderController check-in，单只）")
    private Long petId;

    @Schema(description = "宠物ID列表（用于OrderController check-in，多只）")
    private List<Long> petIds;

    @Schema(description = "宠物名称列表（前端展示用）")
    private String petNames;

    @NotNull(message = "护理类型不能为空")
    @Schema(description = "护理类型：1=喂食 2=遛弯 3=清洁 4=体检 5=其他 6=入住登记")
    private Integer careType;

    @Schema(description = "护理详情")
    private String content;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "图片URL列表（逗号分隔）")
    private String images;
}
