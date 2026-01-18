package com.phms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.phms.entity.Staff;
import com.phms.entity.StaffSchedule;
import com.phms.entity.StaffStatus;
import com.phms.service.StaffScheduleService;
import com.phms.service.StaffService;
import com.phms.service.StaffStatusService;
import com.phms.service.SupportMatchService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class SupportMatchServiceImpl implements SupportMatchService {

    private final StaffScheduleService staffScheduleService;
    private final StaffStatusService staffStatusService;
    private final StaffService staffService;

    public SupportMatchServiceImpl(StaffScheduleService staffScheduleService,
            StaffStatusService staffStatusService,
            StaffService staffService) {
        this.staffScheduleService = staffScheduleService;
        this.staffStatusService = staffStatusService;
        this.staffService = staffService;
    }

    @Override
    public Long matchOnDutyStaff(Long hotelId, LocalDateTime now) {
        LocalDate date = now.toLocalDate();
        LocalTime time = now.toLocalTime();

        List<StaffSchedule> schedules = staffScheduleService.list(new LambdaQueryWrapper<StaffSchedule>()
                .eq(hotelId != null, StaffSchedule::getHotelId, hotelId)
                .eq(StaffSchedule::getWorkDate, date)
                .ne(StaffSchedule::getShiftType, 5));

        Optional<StaffSchedule> matched = schedules.stream()
                .filter(s -> isWithinShift(s, time))
                .filter(s -> isStaffOnDuty(s.getStaffId()))
                .sorted(Comparator.comparing(StaffSchedule::getStartTime,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .findFirst();

        if (matched.isPresent()) {
            return matched.get().getStaffId();
        }

        // 兜底：店长 -> 超管
        Long managerId = staffService.list(new LambdaQueryWrapper<Staff>()
                .eq(hotelId != null, Staff::getHotelId, hotelId)
                .eq(Staff::getRoleType, 2)
                .eq(Staff::getStatus, 1))
                .stream()
                .filter(s -> isStaffOnDuty(s.getId()))
                .map(Staff::getId)
                .findFirst()
                .orElse(null);

        if (managerId != null) {
            return managerId;
        }

        return staffService.list(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getRoleType, 9)
                .eq(Staff::getStatus, 1))
                .stream()
                .filter(s -> isStaffOnDuty(s.getId()))
                .map(Staff::getId)
                .findFirst()
                .orElse(null);
    }

    private boolean isStaffOnDuty(Long staffId) {
        StaffStatus status = staffStatusService.getByStaffId(staffId);
        return status != null && status.getStatus() != null && status.getStatus() == 1;
    }

    private boolean isWithinShift(StaffSchedule s, LocalTime now) {
        LocalTime start = s.getStartTime();
        LocalTime end = s.getEndTime();
        if (start == null || end == null) {
            LocalTime[] def = defaultShiftTime(s.getShiftType());
            start = def[0];
            end = def[1];
        }
        if (start == null || end == null) {
            return false;
        }
        return !now.isBefore(start) && !now.isAfter(end);
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
}
