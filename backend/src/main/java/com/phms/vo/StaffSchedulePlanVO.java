package com.phms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "员工排班总览")
public class StaffSchedulePlanVO {

    @Schema(description = "排班ID")
    private Long id;

    @Schema(description = "员工ID")
    private Long staffId;

    @Schema(description = "员工姓名")
    private String staffName;

    @Schema(description = "角色类型")
    private Integer roleType;

    @Schema(description = "门店ID")
    private Long hotelId;

    @Schema(description = "排班日期")
    private LocalDate workDate;

    @Schema(description = "班次：1=早班 2=中班 3=晚班 4=全天班 5=休息")
    private Integer shiftType;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;
}
