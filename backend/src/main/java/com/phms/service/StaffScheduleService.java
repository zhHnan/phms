package com.phms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.StaffSchedule;

import java.time.LocalDate;
import java.util.List;

public interface StaffScheduleService extends IService<StaffSchedule> {
    List<StaffSchedule> listByStaff(Long staffId, LocalDate from, LocalDate to);
}
