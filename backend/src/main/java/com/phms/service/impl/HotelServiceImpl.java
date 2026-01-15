package com.phms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.Hotel;
import com.phms.mapper.HotelMapper;
import com.phms.service.HotelService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<Hotel> getTopHotelsByScore(Integer limit) {
        List<Hotel> allHotels = baseMapper.selectAllHotelsWithScore();

        // 添加空值检查
        if (allHotels == null || allHotels.isEmpty()) {
            return new ArrayList<>();
        }

        allHotels.sort((h1, h2) -> {
            // 确保 avgScore 不为 null
            Double score1 = h1.getAvgScore() != null ? h1.getAvgScore() : 0.0;
            Double score2 = h2.getAvgScore() != null ? h2.getAvgScore() : 0.0;

            // 先比较评分（降序）
            int scoreCompare = score2.compareTo(score1);
            if (scoreCompare != 0) {
                return scoreCompare;
            }

            // 如果评分相同，则按评论数降序排列
            Integer count1 = h1.getReviewCount() != null ? h1.getReviewCount() : 0;
            Integer count2 = h2.getReviewCount() != null ? h2.getReviewCount() : 0;
            return count2.compareTo(count1);
        });

        // 确保limit参数有效
        int actualLimit = limit != null && limit > 0 ? limit : allHotels.size();
        return allHotels.size() <= actualLimit ? allHotels : allHotels.subList(0, actualLimit);
    }
}
