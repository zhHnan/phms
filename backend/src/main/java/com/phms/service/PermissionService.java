package com.phms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.Permission;

import java.util.List;

/**
 * 权限 Service
 *
 * @author PHMS
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 根据角色ID获取权限编码列表
     *
     * @param roleId 角色ID
     * @return 权限编码列表
     */
    List<String> getPermCodesByRoleId(Integer roleId);

    /**
     * 根据角色ID获取权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByRoleId(Integer roleId);

    /**
     * 获取所有权限列表（按模块分组）
     *
     * @return 权限列表
     */
    List<Permission> getAllPermissions();

    /**
     * 更新角色权限
     *
     * @param roleId  角色ID
     * @param permIds 权限ID列表
     */
    void updateRolePermissions(Integer roleId, List<Long> permIds);
}
