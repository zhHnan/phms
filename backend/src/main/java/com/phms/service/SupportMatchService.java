package com.phms.service;

import java.time.LocalDateTime;

public interface SupportMatchService {
    Long matchOnDutyStaff(Long hotelId, LocalDateTime now);
}
