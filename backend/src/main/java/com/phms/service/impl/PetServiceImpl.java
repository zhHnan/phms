package com.phms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.Order;
import com.phms.entity.Pet;
import com.phms.mapper.OrderMapper;
import com.phms.mapper.PetMapper;
import com.phms.service.OrderService;
import com.phms.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 宠物档案 Service 实现类
 *
 * @author PHMS
 */
@Service
@RequiredArgsConstructor
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements PetService {

    private final OrderMapper orderMapper;

    @Override
    public Page<Pet> pageList(Page<Pet> page, Long userId, String name, Integer type) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, Pet::getUserId, userId)
                .like(StrUtil.isNotBlank(name), Pet::getName, name)
                .eq(type != null, Pet::getType, type)
                .orderByDesc(Pet::getCreatedAt);
        return page(page, wrapper);
    }

    @Override
    public List<Pet> listByUserId(Long userId) {
        return lambdaQuery().eq(Pet::getUserId, userId).list();
    }

    @Override
    public List<Pet> getPetsByOrderId(Long orderId) {
        // 1. 根据订单ID查询订单信息
        Order order = orderMapper.selectById(orderId);
        if (order == null || StrUtil.isBlank(order.getPetIds())) {
            return new ArrayList<>();
        }

        // 2. 解析petIds（可能是JSON数组格式 [1,2] 或逗号分隔 1,2）
        String petIdsStr = order.getPetIds().replace("[", "").replace("]", "").replace("\"", "");
        if (StrUtil.isBlank(petIdsStr)) {
            return new ArrayList<>();
        }

        List<Long> petIds = Arrays.stream(petIdsStr.split(","))
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // 3. 根据petIds查询宠物信息
        if (petIds.isEmpty()) {
            return new ArrayList<>();
        }
        return listByIds(petIds);
    }
}
