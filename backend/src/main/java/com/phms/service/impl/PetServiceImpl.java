package com.phms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.Pet;
import com.phms.mapper.PetMapper;
import com.phms.service.PetService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 宠物档案 Service 实现类
 *
 * @author PHMS
 */
@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements PetService {

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
}
