package com.phms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.StaffStatus;

public interface StaffStatusService extends IService<StaffStatus> {
    StaffStatus getByStaffId(Long staffId);

    void upsertStatus(Long staffId, Long hotelId, Integer status);
}
