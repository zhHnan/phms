package com.phms.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色权限DTO
 *
 * @author PHMS
 */
@Data
public class RolePermissionDTO {

    /**
     * 权限ID列表
     */
    private List<Long> permIds;
}
