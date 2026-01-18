package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "酒店评分汇总")
public class HotelReviewSummaryVO {

    @Schema(description = "酒店ID")
    private Long hotelId;

    @Schema(description = "酒店平均评分")
    private Double hotelAvgScore;

    @Schema(description = "酒店评价数")
    private Integer hotelReviewCount;

    @Schema(description = "各房间评分统计")
    private List<RoomReviewStatVO> rooms;
}