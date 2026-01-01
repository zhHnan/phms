package com.phms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 宠物护理日志表
 *
 * @author PHMS
 */
@Data
@TableName("biz_care_logs")
@Schema(description = "宠物护理日志")
public class CareLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "关联酒店ID")
    private Long hotelId;

    @Schema(description = "操作员工ID")
    private Long staffId;

    @Schema(description = "护理类型：1=喂食 2=遛弯 3=清洁 4=体检")
    private Integer careType;

    @Schema(description = "护理详情")
    private String content;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("is_deleted")
    @Schema(description = "逻辑删除")
    private Integer isDeleted;
}
