package com.phms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志表
 *
 * @author PHMS
 */
@Data
@TableName("sys_operation_logs")
@Schema(description = "操作日志")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "操作人类型：1=C端用户 2=B端员工")
    private Integer operatorType;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名/昵称")
    private String operatorName;

    @Schema(description = "操作模块")
    private String operationModule;

    @Schema(description = "操作类型：add/update/delete/query")
    private String operationType;

    @Schema(description = "操作入参（JSON格式）")
    private String operationParam;

    @Schema(description = "操作结果：1=成功 0=失败")
    private Integer operationResult;

    @Schema(description = "失败信息")
    private String failMsg;

    @Schema(description = "操作IP地址")
    private String operationIp;

    @Schema(description = "请求接口地址")
    private String operationUrl;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Schema(description = "操作时间")
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("is_deleted")
    @Schema(description = "逻辑删除")
    private Integer isDeleted;
}
