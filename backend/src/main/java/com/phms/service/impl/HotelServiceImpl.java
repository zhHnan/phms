package com.phms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.Hotel;
import com.phms.mapper.HotelMapper;
import com.phms.service.HotelService;
import org.springframework.stereotype.Service;

/**
 * 门店信息 Service 实现类
 *
 * @author PHMS
 */
@Service
public class HotelServiceImpl extends ServiceImpl<HotelMapper, Hotel> implements HotelService {

    @Override
    public Page<Hotel> pageList(Page<Hotel> page, String name, Integer status) {
        LambdaQueryWrapper<Hotel> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(name), Hotel::getName, name)
                .eq(status != null, Hotel::getStatus, status)
                .orderByDesc(Hotel::getCreatedAt);
        return page(page, wrapper);
    }

    @Override
    public Hotel getByCode(String code) {
        return lambdaQuery().eq(Hotel::getCode, code).one();
    }
}
