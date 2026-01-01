package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.phms.common.annotation.OperateLog;
import com.phms.common.result.Result;
import com.phms.entity.Role;
import com.phms.mapper.RoleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 *
 * @author PHMS
 */
@Tag(name = "角色管理", description = "角色接口")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleMapper roleMapper;

    @Operation(summary = "获取所有角色列表")
    @GetMapping("/list")
    public Result<List<Role>> list() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Role::getSortOrder);
        List<Role> roles = roleMapper.selectList(wrapper);
        return Result.success(roles);
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    @SaCheckPermission("role:list")
    public Result<Role> getById(@Parameter(description = "角色ID") @PathVariable Integer id) {
        Role role = roleMapper.selectById(id);
        return Result.success(role);
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @SaCheckPermission("role:add")
    @OperateLog(module = "role", type = "add", description = "新增角色")
    public Result<Void> add(@RequestBody Role role) {
        // 检查角色编码是否已存在
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, role.getRoleCode());
        if (roleMapper.selectCount(wrapper) > 0) {
            return Result.fail("角色编码已存在");
        }
        roleMapper.insert(role);
        return Result.success();
    }

    @Operation(summary = "修改角色")
    @PutMapping
    @SaCheckPermission("role:edit")
    @OperateLog(module = "role", type = "update", description = "修改角色")
    public Result<Void> update(@RequestBody Role role) {
        // 检查角色编码是否被其他角色使用
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, role.getRoleCode());
        wrapper.ne(Role::getId, role.getId());
        if (roleMapper.selectCount(wrapper) > 0) {
            return Result.fail("角色编码已存在");
        }
        roleMapper.updateById(role);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @SaCheckPermission("role:delete")
    @OperateLog(module = "role", type = "delete", description = "删除角色")
    public Result<Void> delete(@Parameter(description = "角色ID") @PathVariable Integer id) {
        // 内置角色不允许删除
        if (id == 1 || id == 2 || id == 9) {
            return Result.fail("内置角色不允许删除");
        }
        roleMapper.deleteById(id);
        return Result.success();
    }
}
