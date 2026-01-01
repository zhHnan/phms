package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.phms.common.annotation.OperateLog;
import com.phms.common.result.Result;
import com.phms.dto.RolePermissionDTO;
import com.phms.entity.Permission;
import com.phms.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限管理控制器
 *
 * @author PHMS
 */
@Tag(name = "权限管理", description = "权限配置接口")
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "获取所有权限列表")
    @GetMapping("/list")
    @SaCheckPermission("permission:manage")
    public Result<List<Permission>> list() {
        return Result.success(permissionService.getAllPermissions());
    }

    @Operation(summary = "按模块分组获取权限列表")
    @GetMapping("/group")
    @SaCheckPermission("permission:manage")
    public Result<Map<String, List<Permission>>> groupByModule() {
        List<Permission> permissions = permissionService.getAllPermissions();
        Map<String, List<Permission>> grouped = permissions.stream()
                .collect(Collectors.groupingBy(Permission::getModule));
        return Result.success(grouped);
    }

    @Operation(summary = "获取指定角色的权限")
    @GetMapping("/role/{roleId}")
    @SaCheckPermission("permission:manage")
    public Result<List<Permission>> getByRole(
            @Parameter(description = "角色ID：1=员工 2=店长 9=超管") @PathVariable Integer roleId) {
        return Result.success(permissionService.getPermissionsByRoleId(roleId));
    }

    @Operation(summary = "获取指定角色的权限ID列表")
    @GetMapping("/role/{roleId}/ids")
    @SaCheckPermission("permission:manage")
    public Result<List<Long>> getPermIdsByRole(
            @Parameter(description = "角色ID：1=员工 2=店长 9=超管") @PathVariable Integer roleId) {
        List<Permission> permissions = permissionService.getPermissionsByRoleId(roleId);
        List<Long> ids = permissions.stream().map(Permission::getId).collect(Collectors.toList());
        return Result.success(ids);
    }

    @Operation(summary = "更新角色权限")
    @PutMapping("/role/{roleId}")
    @SaCheckPermission("permission:manage")
    @OperateLog(module = "permission", type = "update", description = "更新角色权限")
    public Result<Void> updateRolePermissions(
            @Parameter(description = "角色ID：1=员工 2=店长 9=超管") @PathVariable Integer roleId,
            @RequestBody RolePermissionDTO dto) {
        // 禁止修改超管权限
        if (roleId == 9) {
            return Result.fail("超级管理员权限不允许修改");
        }
        permissionService.updateRolePermissions(roleId, dto.getPermIds());
        return Result.success();
    }
}
