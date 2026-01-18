package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 酒店评价 VO
 */
@Data
@Schema(description = "酒店评价")
public class HotelReviewVO {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "酒店ID")
    private Long hotelId;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "满意度评分：1-5")
    private Integer score;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
