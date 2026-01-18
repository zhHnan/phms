package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单展示对象
 *
 * @author PHMS
 */
@Data
@Schema(description = "订单展示信息")
public class OrderVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号（唯一）")
    private String orderNo;

    @Schema(description = "门店ID")
    private Long hotelId;

    @Schema(description = "门店名称")
    private String hotelName;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "用户手机号")
    private String userPhone;

    @Schema(description = "宠物ID列表（JSON格式）")
    private String petIds;

    @Schema(description = "宠物名称列表（逗号分隔）")
    private String petNames;

    @Schema(description = "宠物名称（兼容字段）")
    private String petName;

    @Schema(description = "宠物列表（简要）")
    private List<PetSimpleVO> pets;

    @Schema(description = "用户评分（1-5）")
    private Integer reviewScore;

    @Schema(description = "用户评价内容")
    private String reviewContent;

    @Schema(description = "评价时间")
    private LocalDateTime reviewCreatedAt;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "房型名称")
    private String roomType;

    @Schema(description = "房间单价")
    private BigDecimal roomPrice;

    @Schema(description = "预计入住日期")
    private LocalDate checkInDate;

    @Schema(description = "预计离店日期")
    private LocalDate checkOutDate;

    @Schema(description = "入住天数")
    private Long days;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "商品明细")
    private List<OrderItemVO> items;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态：0=待支付 1=待入住 2=入住中 3=已完成 4=已取消")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
