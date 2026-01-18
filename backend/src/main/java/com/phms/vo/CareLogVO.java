package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "护理日志展示VO")
public class CareLogVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "宠物名称")
    private String petName;

    @Schema(description = "日志类型（feeding/cleaning/walking/health_check/check_in/other）")
    private String careType;

    @Schema(description = "照料内容")
    private String content;

    @Schema(description = "图片URL列表（逗号分隔）")
    private String images;

    @Schema(description = "记录人")
    private String staffName;

    @Schema(description = "记录时间")
    private LocalDateTime createdAt;
}
