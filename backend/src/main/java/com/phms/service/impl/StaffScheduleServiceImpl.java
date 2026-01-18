package com.phms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.StaffSchedule;
import com.phms.mapper.StaffScheduleMapper;
import com.phms.service.StaffScheduleService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StaffScheduleServiceImpl extends ServiceImpl<StaffScheduleMapper, StaffSchedule>
        implements StaffScheduleService {

    @Override
    public List<StaffSchedule> listByStaff(Long staffId, LocalDate from, LocalDate to) {
        LambdaQueryWrapper<StaffSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffSchedule::getStaffId, staffId)
                .ge(from != null, StaffSchedule::getWorkDate, from)
                .le(to != null, StaffSchedule::getWorkDate, to)
                .orderByAsc(StaffSchedule::getWorkDate)
                .orderByAsc(StaffSchedule::getStartTime);
        return list(wrapper);
    }
}
