package com.phms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.CareLog;
import com.phms.mapper.CareLogMapper;
import com.phms.service.CareLogService;
import com.phms.vo.CareLogVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 护理日志 Service 实现类
 *
 * @author PHMS
 */
@Service
public class CareLogServiceImpl extends ServiceImpl<CareLogMapper, CareLog> implements CareLogService {

    @Override
    public Page<CareLog> pageList(Page<CareLog> page, Long orderId, Integer careType) {
        LambdaQueryWrapper<CareLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(orderId != null, CareLog::getOrderId, orderId)
                .eq(careType != null, CareLog::getCareType, careType)
                .orderByDesc(CareLog::getCreatedAt);
        return page(page, wrapper);
    }

    @Override
    public Page<CareLogVO> pageListVO(Page<CareLogVO> page, Long hotelId, String orderNo, String petName, Integer careType) {
        return baseMapper.selectCareLogVOPage(page, hotelId, orderNo, petName, careType);
    }

    @Override
    public List<CareLogVO> listByOrderId(Long orderId) {
        return baseMapper.selectCareLogVOByOrderId(orderId);
    }
}
