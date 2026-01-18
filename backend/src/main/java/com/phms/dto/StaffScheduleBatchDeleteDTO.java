package com.phms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "排班批量删除DTO")
public class StaffScheduleBatchDeleteDTO {

    @Schema(description = "员工ID（单个员工批量删除时必填）")
    private Long staffId;

    @Schema(description = "排班ID列表")
    private List<Long> ids;
}
