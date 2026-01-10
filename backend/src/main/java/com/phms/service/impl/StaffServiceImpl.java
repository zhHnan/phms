package com.phms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.common.constant.Constants;
import com.phms.entity.Hotel;
import com.phms.entity.Staff;
import com.phms.mapper.StaffMapper;
import com.phms.service.HotelService;
import com.phms.service.StaffService;
import com.phms.vo.StaffInfoVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * 员工 Service 实现类
 *
 * @author PHMS
 */
@Service
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements StaffService {

    @Resource
    private HotelService hotelService;
    @Override
    public Page<StaffInfoVO> pageList(Page<Staff> page, Long hotelId, String realName, Integer roleType, Integer status) {
        Page<StaffInfoVO> res = new Page<>(page.getCurrent(), page.getSize());
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(hotelId != null, Staff::getHotelId, hotelId)
                .like(StrUtil.isNotBlank(realName), Staff::getRealName, realName)
                .eq(roleType != null, Staff::getRoleType, roleType)
                .eq(status != null, Staff::getStatus, status)
                .orderByDesc(Staff::getCreatedAt);
        Page<Staff> staffPage = page(page, wrapper);
         // 获取门店名称并设置
        res.setRecords(staffPage.getRecords().stream()
                .map(staff -> {
                    StaffInfoVO vo = new StaffInfoVO();
                    BeanUtil.copyProperties(staff, vo);
                    // 从酒店服务获取实际名称
                    Hotel byId = hotelService.getById(staff.getHotelId());
                    if(ObjectUtil.isNotNull(byId)){
                        vo.setHotelName(byId.getName());
                    }else {
                        vo.setHotelName("无");
                    }
                    return vo;
                })
                .collect(Collectors.toList()));
        res.setTotal(staffPage.getTotal());
        return res;
    }

    @Override
    public Staff getByEmail(String email) {
        return lambdaQuery().eq(Staff::getEmail, email).one();
    }

    @Override
    public boolean checkEmailUnique(String email, Long id) {
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Staff::getEmail, email);
        if (id != null) {
            wrapper.ne(Staff::getId, id);
        }
        return count(wrapper) == 0;
    }

    @Override
    public boolean hasManager(Long hotelId, Long excludeId) {
        if (hotelId == null) {
            return false;
        }
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Staff::getHotelId, hotelId)
               .eq(Staff::getRoleType, Constants.ROLE_MANAGER);
        if (excludeId != null) {
            wrapper.ne(Staff::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    @Override
    public java.util.List<Staff> listEnabledByHotel(Long hotelId) {
        if (hotelId == null) {
            return java.util.Collections.emptyList();
        }
        return lambdaQuery()
                .eq(Staff::getHotelId, hotelId)
                .eq(Staff::getStatus, Constants.STATUS_ENABLE)
                .ne(Staff::getRoleType, Constants.ROLE_ADMIN)
                .list();
    }
}
