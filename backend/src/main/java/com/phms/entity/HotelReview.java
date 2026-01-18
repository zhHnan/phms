package com.phms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 酒店评价表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_hotel_reviews")
@Schema(description = "酒店评价")
public class HotelReview extends BaseEntity {

    @Schema(description = "关联订单ID（唯一）")
    private Long orderId;

    @Schema(description = "酒店ID")
    private Long hotelId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "满意度评分：1-5")
    private Integer score;

    @Schema(description = "评价内容")
    private String content;
}
