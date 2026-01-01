package com.phms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色权限关联实体
 *
 * @author PHMS
 */
@Data
@TableName("sys_role_permissions")
public class RolePermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID：1=普通员工 2=店长 9=超管
     */
    private Integer roleId;

    /**
     * 权限ID
     */
    private Long permId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
