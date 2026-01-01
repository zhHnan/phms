package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 仪表盘数据 VO
 *
 * @author PHMS
 */
@Data
@Schema(description = "仪表盘数据")
public class DashboardVO {

    @Schema(description = "今日预订数")
    private Long todayOrders;

    @Schema(description = "当前在住宠物数")
    private Long checkedInPets;

    @Schema(description = "空闲房间数")
    private Long freeRooms;

    @Schema(description = "已预订房间数")
    private Long reservedRooms;

    @Schema(description = "入住中房间数")
    private Long occupiedRooms;

    @Schema(description = "待清洁房间数")
    private Long cleaningRooms;

    @Schema(description = "维修中房间数")
    private Long maintenanceRooms;

    @Schema(description = "总房间数")
    private Long totalRooms;
}
