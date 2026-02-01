package com.phms.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.phms.common.constant.Constants;
import com.phms.common.exception.BusinessException;
import com.phms.common.result.Result;
import com.phms.common.result.ResultCode;
import com.phms.dto.StaffScheduleDTO;
import com.phms.entity.Staff;
import com.phms.entity.StaffSchedule;
import com.phms.service.StaffScheduleService;
import com.phms.service.StaffService;
import com.phms.dto.StaffScheduleBatchDeleteDTO;
import com.phms.vo.StaffSchedulePlanVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "员工排班", description = "员工排班管理")
@RestController
@RequestMapping("/staff-schedule")
public class StaffScheduleController {

    private final StaffScheduleService staffScheduleService;
    private final StaffService staffService;

    public StaffScheduleController(StaffScheduleService staffScheduleService, StaffService staffService) {
        this.staffScheduleService = staffScheduleService;
        this.staffService = staffService;
    }

    @Operation(summary = "查询员工排班")
    @GetMapping("/list")
    @SaCheckLogin
    public Result<List<StaffSchedule>> list(
            @Parameter(description = "员工ID") @RequestParam(required = false) Long staffId,
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDate from,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDate to) {
        Integer roleType = getRoleType();
        Long loginId = StpUtil.getLoginIdAsLong();
        if (roleType != null && roleType == 1) {
            staffId = loginId;
        }
        if (staffId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "缺少员工ID");
        }
        if (!canAccessStaff(staffId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "无权限查看该员工排班");
        }
        // 默认只查询今天及以后的排班
        if (from == null) {
            from = LocalDate.now();
        }
        return Result.success(staffScheduleService.listByStaff(staffId, from, to));
    }

    @Operation(summary = "排班总览（未来7天）")
    @GetMapping("/plan")
    @SaCheckLogin
    public Result<List<StaffSchedulePlanVO>> plan(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDate from,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDate to) {
        Integer roleType = getRoleType();
        if (roleType == null) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "无权限查看排班总览");
        }
        LocalDate minDate = LocalDate.now().plusDays(1);
        LocalDate start = from != null ? from : minDate;
        LocalDate end = to != null ? to : start.plusDays(6);
        if (start.isBefore(minDate)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅支持查看明天及之后的排班");
        }
        if (end.isBefore(start)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "结束日期不能早于开始日期");
        }
        if (end.isAfter(start.plusDays(6))) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "最多仅支持查看7天排班");
        }

        Long hotelId = null;
        if (!roleType.equals(Constants.ROLE_ADMIN)) {
            hotelId = getHotelId();
            if (hotelId == null) {
                throw new BusinessException(ResultCode.NO_PERMISSION, "无权限查看排班总览");
            }
        }

        List<StaffSchedule> schedules = staffScheduleService.list(new LambdaQueryWrapper<StaffSchedule>()
                .eq(hotelId != null, StaffSchedule::getHotelId, hotelId)
                .ge(StaffSchedule::getWorkDate, start)
                .le(StaffSchedule::getWorkDate, end)
                .orderByAsc(StaffSchedule::getWorkDate)
                .orderByAsc(StaffSchedule::getStartTime));

        Set<Long> staffIds = schedules.stream()
                .map(StaffSchedule::getStaffId)
                .collect(Collectors.toSet());
        Map<Long, Staff> staffMap = staffIds.isEmpty()
                ? Map.of()
                : staffService.listByIds(staffIds).stream()
                        .collect(Collectors.toMap(Staff::getId, s -> s));

        List<StaffSchedulePlanVO> result = schedules.stream().map(s -> {
            StaffSchedulePlanVO vo = new StaffSchedulePlanVO();
            vo.setId(s.getId());
            vo.setStaffId(s.getStaffId());
            Staff staff = staffMap.get(s.getStaffId());
            if (staff != null) {
                vo.setStaffName(staff.getRealName());
                vo.setRoleType(staff.getRoleType());
            }
            vo.setHotelId(s.getHotelId());
            vo.setWorkDate(s.getWorkDate());
            vo.setShiftType(s.getShiftType());
            vo.setStartTime(s.getStartTime());
            vo.setEndTime(s.getEndTime());
            return vo;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @Operation(summary = "新增排班")
    @PostMapping
    @SaCheckLogin
    public Result<Void> add(@RequestBody StaffScheduleDTO dto) {
        if (dto.getStaffId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "缺少员工ID");
        }
        if (dto.getShiftType() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "缺少班次类型");
        }
        if (canManageStaff(dto.getStaffId())) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "无权限设置该员工排班");
        }
        Staff staff = staffService.getById(dto.getStaffId());
        if (staff == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "员工不存在");
        }
        LocalDate minDate = LocalDate.now().plusDays(1);
        LocalTime[] timeRange = resolveTimeRange(dto);
        Long hotelId = staff.getHotelId();
        if (dto.getStartDate() != null || dto.getEndDate() != null) {
            if (dto.getStartDate() == null || dto.getEndDate() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "请选择完整的日期范围");
            }
            if (dto.getStartDate().isBefore(minDate)) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "排班日期不能早于明天");
            }
            if (dto.getEndDate().isBefore(dto.getStartDate())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "结束日期不能早于开始日期");
            }
            List<StaffSchedule> schedules = new ArrayList<>();
            List<LocalDate> conflictDates = new ArrayList<>();
            LocalDate current = dto.getStartDate();
            while (!current.isAfter(dto.getEndDate())) {
                if (hasConflict(hotelId, dto.getStaffId(), current, dto.getShiftType(), timeRange[0], timeRange[1],
                        null)) {
                    conflictDates.add(current);
                } else {
                    schedules.add(buildSchedule(dto, hotelId, current, timeRange));
                }
                current = current.plusDays(1);
            }
            if (!schedules.isEmpty()) {
                staffScheduleService.saveBatch(schedules);
            }
            if (!conflictDates.isEmpty()) {
                return Result.success(buildConflictMessage(conflictDates), null);
            }
        } else {
            if (dto.getWorkDate() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "缺少排班日期");
            }
            if (dto.getWorkDate().isBefore(minDate)) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "排班日期不能早于明天");
            }
            if (hasConflict(hotelId, dto.getStaffId(), dto.getWorkDate(), dto.getShiftType(), timeRange[0],
                    timeRange[1], null)) {
                return Result.success(buildConflictMessage(List.of(dto.getWorkDate())), null);
            }
            StaffSchedule schedule = buildSchedule(dto, hotelId, dto.getWorkDate(), timeRange);
            staffScheduleService.save(schedule);
        }
        return Result.success();
    }

    @Operation(summary = "编辑排班")
    @PutMapping("/{id}")
    @SaCheckLogin
    public Result<Void> update(@PathVariable Long id, @RequestBody StaffScheduleDTO dto) {
        StaffSchedule exist = staffScheduleService.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "排班不存在");
        }
        if (dto.getShiftType() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "缺少班次类型");
        }
        if (canManageStaff(exist.getStaffId())) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "无权限编辑该员工排班");
        }
        Staff staff = staffService.getById(exist.getStaffId());
        LocalDate workDate = dto.getWorkDate() != null ? dto.getWorkDate() : exist.getWorkDate();
        if (workDate == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "缺少排班日期");
        }
        LocalDate minDate = LocalDate.now().plusDays(1);
        if (workDate.isBefore(minDate)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "排班日期不能早于明天");
        }
        LocalTime[] timeRange = resolveTimeRange(dto);
        Long hotelId = staff != null ? staff.getHotelId() : exist.getHotelId();
        if (hasConflict(hotelId, exist.getStaffId(), workDate, dto.getShiftType(), timeRange[0], timeRange[1], id)) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "该日期时间冲突，请调整后重试");
        }
        StaffSchedule schedule = buildSchedule(dto, hotelId, workDate, timeRange);
        schedule.setId(id);
        staffScheduleService.updateById(schedule);
        return Result.success();
    }

    @Operation(summary = "删除排班")
    @DeleteMapping("/{id}")
    @SaCheckLogin
    public Result<Void> delete(@PathVariable Long id) {
        Integer roleType = getRoleType();
        if (roleType == null || (!roleType.equals(Constants.ROLE_ADMIN) && !roleType.equals(Constants.ROLE_MANAGER))) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "无权限删除排班");
        }
        staffScheduleService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除排班")
    @PostMapping("/batch-delete")
    @SaCheckLogin
    public Result<Void> batchDelete(@RequestBody StaffScheduleBatchDeleteDTO dto) {
        Integer roleType = getRoleType();
        if (roleType == null || (!roleType.equals(Constants.ROLE_ADMIN) && !roleType.equals(Constants.ROLE_MANAGER))) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "无权限删除排班");
        }
        if (dto == null || dto.getIds() == null || dto.getIds().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "缺少排班ID");
        }
        if (dto.getStaffId() != null) {
            List<StaffSchedule> schedules = staffScheduleService.listByIds(dto.getIds());
            boolean sameStaff = schedules.stream().allMatch(s -> dto.getStaffId().equals(s.getStaffId()));
            if (!sameStaff) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "仅支持单个员工批量删除");
            }
        }
        staffScheduleService.removeByIds(dto.getIds());
        return Result.success();
    }

    private StaffSchedule buildSchedule(StaffScheduleDTO dto, Long hotelId, LocalDate workDate, LocalTime[] timeRange) {
        StaffSchedule schedule = new StaffSchedule();
        schedule.setStaffId(dto.getStaffId());
        schedule.setHotelId(hotelId);
        schedule.setWorkDate(workDate != null ? workDate : dto.getWorkDate());
        schedule.setShiftType(dto.getShiftType());
        schedule.setStartTime(timeRange[0]);
        schedule.setEndTime(timeRange[1]);
        return schedule;
    }

    private LocalTime[] defaultShiftTime(Integer shiftType) {
        if (shiftType == null)
            return new LocalTime[] { null, null };
        return switch (shiftType) {
            case 1 -> new LocalTime[] { LocalTime.of(9, 0), LocalTime.of(18, 0) };
            case 2 -> new LocalTime[] { LocalTime.of(12, 0), LocalTime.of(21, 0) };
            case 3 -> new LocalTime[] { LocalTime.of(17, 0), LocalTime.of(23, 59) };
            case 4 -> new LocalTime[] { LocalTime.of(0, 0), LocalTime.of(23, 59) };
            default -> new LocalTime[] { null, null };
        };
    }

    private Integer getRoleType() {
        Object roleTypeObj = StpUtil.getSession().get("roleType");
        return roleTypeObj == null ? null : Integer.valueOf(String.valueOf(roleTypeObj));
    }

    private Long getHotelId() {
        Object hotelIdObj = StpUtil.getSession().get("hotelId");
        return hotelIdObj == null ? null : Long.valueOf(String.valueOf(hotelIdObj));
    }

    private LocalTime[] resolveTimeRange(StaffScheduleDTO dto) {
        if (dto.getShiftType() != null && dto.getShiftType() == 5) {
            return new LocalTime[] { null, null };
        }
        LocalTime[] def = defaultShiftTime(dto.getShiftType());
        LocalTime start = dto.getStartTime() != null ? dto.getStartTime() : def[0];
        LocalTime end = dto.getEndTime() != null ? dto.getEndTime() : def[1];
        return new LocalTime[] { start, end };
    }

    private boolean hasConflict(Long hotelId, Long staffId, LocalDate workDate, Integer shiftType, LocalTime startTime,
            LocalTime endTime, Long excludeId) {
        if (staffId != null) {
            boolean existsSameDay = staffScheduleService.lambdaQuery()
                    .eq(StaffSchedule::getStaffId, staffId)
                    .eq(StaffSchedule::getWorkDate, workDate)
                    .ne(excludeId != null, StaffSchedule::getId, excludeId)
                    .exists();
            if (existsSameDay) {
                return true;
            }
        }
        if (hotelId == null) {
            return false;
        }
        boolean isRest = shiftType != null && shiftType == 5;
        if (isRest) {
            return false;
        }
        if (startTime == null || endTime == null) {
            return false;
        }
        List<StaffSchedule> exists = staffScheduleService.lambdaQuery()
                .eq(StaffSchedule::getHotelId, hotelId)
                .eq(StaffSchedule::getWorkDate, workDate)
                .ne(excludeId != null, StaffSchedule::getId, excludeId)
                .list();
        if (exists.isEmpty()) {
            return false;
        }
        for (StaffSchedule exist : exists) {
            if (exist.getShiftType() != null && exist.getShiftType() == 5) {
                continue;
            }
            LocalTime es = exist.getStartTime();
            LocalTime ee = exist.getEndTime();
            if (es == null || ee == null) {
                continue;
            }
            if (startTime.isBefore(ee) && endTime.isAfter(es)) {
                return true;
            }
        }
        return false;
    }

    private String buildConflictMessage(List<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return "操作成功";
        }
        String joined = dates.stream().map(LocalDate::toString).collect(Collectors.joining("、"));
        return dates.size() + "天时间冲突，已过滤：" + joined;
    }

    private boolean canAccessStaff(Long staffId) {
        Integer roleType = getRoleType();
        if (roleType == null || roleType.equals(Constants.ROLE_ADMIN))
            return true;
        if (roleType == 1)
            return Long.valueOf(StpUtil.getLoginIdAsLong()).equals(staffId);
        if (roleType == 2) {
            Staff staff = staffService.getById(staffId);
            return staff != null && staff.getHotelId() != null && staff.getHotelId().equals(getHotelId());
        }
        return false;
    }

    private boolean canManageStaff(Long staffId) {
        Integer roleType = getRoleType();
        if (roleType == null)
            return true;
        if (roleType.equals(Constants.ROLE_ADMIN))
            return false;
        if (roleType == 2) {
            Staff staff = staffService.getById(staffId);
            return staff == null || staff.getHotelId() == null || !staff.getHotelId().equals(getHotelId());
        }
        return true;
    }
}
