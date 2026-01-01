package com.phms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.User;
import com.phms.mapper.UserMapper;
import com.phms.service.UserService;
import org.springframework.stereotype.Service;

/**
 * C端用户 Service 实现类
 *
 * @author PHMS
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Page<User> pageList(Page<User> page, String phone, String nickname, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(phone), User::getPhone, phone)
                .like(StrUtil.isNotBlank(nickname), User::getNickname, nickname)
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreatedAt);
        return page(page, wrapper);
    }

    @Override
    public User getByPhone(String phone) {
        return lambdaQuery().eq(User::getPhone, phone).one();
    }

    @Override
    public boolean checkPhoneUnique(String phone, Long id) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        if (id != null) {
            wrapper.ne(User::getId, id);
        }
        return count(wrapper) == 0;
    }
}
