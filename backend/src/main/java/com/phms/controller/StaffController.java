package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.common.annotation.OperateLog;
import com.phms.common.constant.Constants;
import com.phms.common.exception.BusinessException;
import com.phms.common.result.Result;
import com.phms.common.result.ResultCode;
import com.phms.dto.StaffDTO;
import com.phms.entity.Hotel;
import com.phms.entity.Staff;
import com.phms.service.HotelService;
import com.phms.service.StaffService;
import com.phms.vo.StaffInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 员工管理控制器
 *
 * @author PHMS
 */
@Tag(name = "员工管理", description = "员工的增删改查接口")
@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    private final HotelService hotelService;

    @Operation(summary = "分页查询员工列表")
    @GetMapping("/page")
    @SaCheckPermission("staff:list")
    public Result<Page<StaffInfoVO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "门店ID") @RequestParam(required = false) Long hotelId,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "角色类型") @RequestParam(required = false) Integer roleType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        // 店长、员工只能查看本门店员工
        Object currentRoleType = StpUtil.getSession().get("roleType");
        if (currentRoleType != null && (Integer) currentRoleType != Constants.ROLE_ADMIN) {
            hotelId = (Long) StpUtil.getSession().get("hotelId");
        }
        
        Page<Staff> page = new Page<>(pageNum, pageSize);
        Page<StaffInfoVO> result = staffService.pageList(page, hotelId, realName, roleType, status);
        // 清除密码
        result.getRecords().forEach(staff -> staff.setPassword(null));
        
        return Result.success(result);
    }

    @Operation(summary = "根据ID查询员工详情")
    @GetMapping("/{id}")
    @SaCheckPermission("staff:list")
    public Result<Staff> getById(@PathVariable Long id) {
        Staff staff = staffService.getById(id);
        if (staff != null) {
            staff.setPassword(null);
        }
        return Result.success(staff);
    }

    @Operation(summary = "新增员工")
    @PostMapping
    @SaCheckPermission("staff:add")
    @OperateLog(module = "staff", type = "add", description = "新增员工")
    public Result<Void> add(@Valid @RequestBody StaffDTO dto) {
        // 校验邮箱唯一性
        if (!staffService.checkEmailUnique(dto.getEmail(), null)) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // 店长只能添加本门店员工
        Object roleType = StpUtil.getSession().get("roleType");
        if (roleType != null && (Integer) roleType == Constants.ROLE_MANAGER) {
            Long hotelId = (Long) StpUtil.getSession().get("hotelId");
            dto.setHotelId(hotelId);
            dto.setRoleType(Constants.ROLE_STAFF); // 店长只能添加普通员工
        }

        // 如果添加的是店长，校验该酒店是否已存在店长
        if (dto.getRoleType() != null && dto.getRoleType() == Constants.ROLE_MANAGER) {
            if (staffService.hasManager(dto.getHotelId(), null)) {
                return Result.fail("该酒店已存在店长，每个酒店只能有一位店长");
            }
            // 若 修改的是店长，则同步修改门店联系方式
            Hotel hotel = hotelService.getById(dto.getHotelId());
            if (hotel != null) {
                hotel.setPhone(dto.getPhone());
                hotelService.updateById(hotel);
            }
        }

        Staff staff = new Staff();
        staff.setHotelId(dto.getHotelId());
        staff.setEmail(dto.getEmail());
        staff.setPhone(dto.getPhone());
        staff.setPassword(BCrypt.hashpw(dto.getPassword()));
        staff.setRealName(dto.getRealName());
        staff.setRoleType(dto.getRoleType());
        staff.setStatus(Constants.STATUS_ENABLE);
        
        staffService.save(staff);
        return Result.success();
    }

    @Operation(summary = "修改员工")
    @PutMapping
    @SaCheckPermission("staff:edit")
    @OperateLog(module = "staff", type = "update", description = "修改员工")
    public Result<Void> update(@Valid @RequestBody StaffDTO dto) {
        // 校验邮箱唯一性
        if (!staffService.checkEmailUnique(dto.getEmail(), dto.getId())) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        Staff staff = staffService.getById(dto.getId());
        if (staff == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "员工不存在");
        }
        // 不能修改为超管角色
        if (dto.getRoleType() != null && dto.getRoleType() == Constants.ROLE_ADMIN) {
            return Result.fail("超管只能有一位！不能修改为超管");
        }
        // 如果要修改为店长，校验该酒店是否已存在其他店长
        if (dto.getRoleType() != null && dto.getRoleType() == Constants.ROLE_MANAGER) {
            // 需要检查的酒店ID：优先使用新的hotelId，否则使用原来的
            Long checkHotelId = dto.getHotelId() != null ? dto.getHotelId() : staff.getHotelId();
            if (staffService.hasManager(checkHotelId, dto.getId())) {
                return Result.fail("该酒店已存在店长，每个酒店只能有一位店长");
            }
            // 若 修改的是店长，则同步修改门店联系方式
            Hotel hotel = hotelService.getById(checkHotelId);
            if (hotel != null) {
                hotel.setPhone(dto.getPhone());
                hotelService.updateById(hotel);
            }
        }
        staff.setEmail(dto.getEmail());
        staff.setRealName(dto.getRealName());
        staff.setPhone(dto.getPhone());
        staff.setStatus(dto.getStatus());
        // 如果密码不为空则更新密码
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            staff.setPassword(BCrypt.hashpw(dto.getPassword()));
        }
        // 只有超管可以修改角色类型和门店
        Object roleType = StpUtil.getSession().get("roleType");
        if (roleType != null && (Integer) roleType != Constants.ROLE_ADMIN) {
            if (!Objects.equals(staff.getRoleType(), dto.getRoleType())) {
                throw new BusinessException(ResultCode.NO_MANAGER_PERMISSION);
            }
            if (!Objects.equals(staff.getHotelId(), dto.getHotelId())){
                throw new BusinessException(ResultCode.NO_MANAGER_PERMISSION);
            }
        }
        staff.setRoleType(dto.getRoleType());
        staff.setHotelId(dto.getHotelId());

        staffService.updateById(staff);
        return Result.success();
    }

    @Operation(summary = "删除员工")
    @DeleteMapping("/{id}")
    @SaCheckPermission("staff:delete")
    @OperateLog(module = "staff", type = "delete", description = "删除员工")
    public Result<Void> delete(@PathVariable Long id) {
        staffService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "修改员工状态")
    @PutMapping("/{id}/status/{status}")
    @SaCheckPermission("staff:edit")
    @OperateLog(module = "staff", type = "update", description = "修改员工状态")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Staff staff = new Staff();
        staff.setId(id);
        staff.setStatus(status);
        staffService.updateById(staff);
        return Result.success();
    }
}
