package com.phms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限实体
 *
 * @author PHMS
 */
@Data
@TableName("sys_permissions")
public class Permission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限编码（如 hotel:list）
     */
    private String permCode;

    /**
     * 权限名称
     */
    private String permName;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDeleted;
}
