package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "房间评分统计")
public class RoomReviewStatVO {

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "房型名称")
    private String roomType;

    @Schema(description = "评分人数")
    private Integer reviewCount;

    @Schema(description = "平均评分 1-5")
    private Double avgScore;
}