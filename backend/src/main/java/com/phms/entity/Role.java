package com.phms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体
 */
@Data
@TableName("sys_roles")
public class Role {
    
    @TableId(type = IdType.INPUT)
    private Integer id;
    
    private String roleName;
    
    private String roleCode;
    
    private String description;
    
    private Integer sortOrder;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer isDeleted;
}
