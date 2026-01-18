package com.phms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工在岗状态
 */
@Data
@TableName("sys_staff_status")
@Schema(description = "员工在岗状态")
public class StaffStatus {

    @TableId(value = "staff_id", type = IdType.INPUT)
    @Schema(description = "员工ID")
    private Long staffId;

    @Schema(description = "门店ID")
    private Long hotelId;

    @Schema(description = "状态：1=在岗 2=离岗 3=忙碌 4=离线")
    private Integer status;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
