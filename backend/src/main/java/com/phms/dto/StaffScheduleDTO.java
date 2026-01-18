package com.phms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "员工排班DTO")
public class StaffScheduleDTO {

    @NotNull
    @Schema(description = "员工ID")
    private Long staffId;

    @Schema(description = "排班日期")
    private LocalDate workDate;

    @Schema(description = "开始日期（批量新增时使用）")
    private LocalDate startDate;

    @Schema(description = "结束日期（批量新增时使用）")
    private LocalDate endDate;

    @NotNull
    @Schema(description = "班次：1=早班 2=中班 3=晚班 4=全天班 5=休息")
    private Integer shiftType;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;
}
