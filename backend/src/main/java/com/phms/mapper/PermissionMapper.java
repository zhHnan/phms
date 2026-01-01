package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据角色ID查询权限编码列表
     *
     * @param roleId 角色ID
     * @return 权限编码列表
     */
    @Select("SELECT p.perm_code FROM sys_permissions p " +
            "INNER JOIN sys_role_permissions rp ON p.id = rp.perm_id " +
            "WHERE rp.role_id = #{roleId} AND p.is_deleted = 0 " +
            "ORDER BY p.sort_order")
    List<String> selectPermCodesByRoleId(@Param("roleId") Integer roleId);

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT p.* FROM sys_permissions p " +
            "INNER JOIN sys_role_permissions rp ON p.id = rp.perm_id " +
            "WHERE rp.role_id = #{roleId} AND p.is_deleted = 0 " +
            "ORDER BY p.sort_order")
    List<Permission> selectByRoleId(@Param("roleId") Integer roleId);
}
