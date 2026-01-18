package com.phms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.StaffStatus;
import com.phms.mapper.StaffStatusMapper;
import com.phms.service.StaffStatusService;
import org.springframework.stereotype.Service;

@Service
public class StaffStatusServiceImpl extends ServiceImpl<StaffStatusMapper, StaffStatus> implements StaffStatusService {

    @Override
    public StaffStatus getByStaffId(Long staffId) {
        return getById(staffId);
    }

    @Override
    public void upsertStatus(Long staffId, Long hotelId, Integer status) {
        StaffStatus exist = getById(staffId);
        if (exist == null) {
            StaffStatus ss = new StaffStatus();
            ss.setStaffId(staffId);
            ss.setHotelId(hotelId);
            ss.setStatus(status);
            save(ss);
        } else {
            exist.setStatus(status);
            exist.setHotelId(hotelId);
            updateById(exist);
        }
    }
}
