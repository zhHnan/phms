package com.phms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.Permission;
import com.phms.entity.RolePermission;
import com.phms.mapper.PermissionMapper;
import com.phms.mapper.RolePermissionMapper;
import com.phms.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 权限 Service 实现类
 *
 * @author PHMS
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final RolePermissionMapper rolePermissionMapper;

    @Override
    @Cacheable(value = "permissions", key = "#roleId")
    public List<String> getPermCodesByRoleId(Integer roleId) {
        return baseMapper.selectPermCodesByRoleId(roleId);
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Integer roleId) {
        return baseMapper.selectByRoleId(roleId);
    }

    @Override
    public List<Permission> getAllPermissions() {
        return lambdaQuery()
                .orderByAsc(Permission::getSortOrder)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "permissions", key = "#roleId")
    public void updateRolePermissions(Integer roleId, List<Long> permIds) {
        // 先删除该角色的所有权限
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(wrapper);

        // 再批量插入新权限
        if (permIds != null && !permIds.isEmpty()) {
            rolePermissionMapper.batchInsert(roleId, permIds);
        }
    }
}
