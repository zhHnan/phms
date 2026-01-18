package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.phms.common.constant.Constants;
import com.phms.common.exception.BusinessException;
import com.phms.common.result.Result;
import com.phms.common.result.ResultCode;
import com.phms.entity.Staff;
import com.phms.entity.StaffStatus;
import com.phms.service.StaffService;
import com.phms.service.StaffStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "员工在岗状态", description = "员工在岗状态管理")
@RestController
@RequestMapping("/staff-status")
public class StaffStatusController {

    private final StaffStatusService staffStatusService;
    private final StaffService staffService;

    public StaffStatusController(StaffStatusService staffStatusService, StaffService staffService) {
        this.staffStatusService = staffStatusService;
        this.staffService = staffService;
    }

    @Operation(summary = "查询我的在岗状态")
    @GetMapping("/my")
    @SaCheckLogin
    public Result<StaffStatus> myStatus() {
        Long staffId = StpUtil.getLoginIdAsLong();
        return Result.success(staffStatusService.getByStaffId(staffId));
    }

    @Operation(summary = "更新我的在岗状态")
    @PutMapping("/my")
    @SaCheckLogin
    public Result<Void> updateMyStatus(@RequestParam Integer status) {
        Long staffId = StpUtil.getLoginIdAsLong();
        Staff staff = staffService.getById(staffId);
        if (staff == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "员工不存在");
        }
        staffStatusService.upsertStatus(staffId, staff.getHotelId(), status);
        return Result.success();
    }

    @Operation(summary = "查询门店员工在岗状态")
    @GetMapping("/hotel")
    @SaCheckLogin
    public Result<List<StaffStatus>> listByHotel(
            @Parameter(description = "门店ID") @RequestParam(required = false) Long hotelId) {
        Integer roleType = getRoleType();
        if (roleType == null || roleType.equals(Constants.ROLE_ADMIN)) {
            if (hotelId == null) {
                return Result.success(List.of());
            }
            return Result.success(staffStatusService.lambdaQuery().eq(StaffStatus::getHotelId, hotelId).list());
        }
        if (roleType == 2) {
            Long hid = getHotelId();
            return Result.success(staffStatusService.lambdaQuery().eq(StaffStatus::getHotelId, hid).list());
        }
        throw new BusinessException(ResultCode.NO_PERMISSION, "无权限查看");
    }

    private Integer getRoleType() {
        Object roleTypeObj = StpUtil.getSession().get("roleType");
        return roleTypeObj == null ? null : Integer.valueOf(String.valueOf(roleTypeObj));
    }

    private Long getHotelId() {
        Object hotelIdObj = StpUtil.getSession().get("hotelId");
        return hotelIdObj == null ? null : Long.valueOf(String.valueOf(hotelIdObj));
    }
}
