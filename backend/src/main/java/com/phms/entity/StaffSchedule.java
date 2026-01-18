package com.phms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 员工排班表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_staff_schedule")
@Schema(description = "员工排班")
public class StaffSchedule extends BaseEntity {

    @Schema(description = "员工ID")
    private Long staffId;

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
