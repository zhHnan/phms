package com.phms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单表
 *
 * @author PHMS
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_orders")
@Schema(description = "订单信息")
public class Order extends BaseEntity {

    @Schema(description = "订单号（唯一）")
    private String orderNo;

    @Schema(description = "门店ID")
    private Long hotelId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "宠物ID列表（JSON格式）")
    private String petIds;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "预计入住日期")
    private LocalDate checkInDate;

    @Schema(description = "预计离店日期")
    private LocalDate checkOutDate;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态：0=待支付 1=待入住 2=入住中 3=已完成 4=已取消")
    private Integer status;
}
