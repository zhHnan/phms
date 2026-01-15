package com.phms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.entity.LoginLog;
import com.phms.mapper.LoginLogMapper;
import com.phms.service.LoginLogService;
import com.phms.vo.LoginLogVO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录日志 Service 实现类
 *
 * @author PHMS
 */
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {

    @Override
    public Page<LoginLog> pageList(Page<LoginLog> page, Integer loginType, Integer loginWay, Integer status) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(loginType != null, LoginLog::getLoginType, loginType)
                .eq(loginWay != null, LoginLog::getLoginWay, loginWay)
                .eq(status != null, LoginLog::getLoginStatus, status)
                .orderByDesc(LoginLog::getCreatedAt);
        return page(page, wrapper);
    }

    @Override
    public Page<LoginLogVO> pageListVO(Page<LoginLogVO> page,
                                      String userName,
                                      String loginIp,
                                      Integer loginType,
                                      Integer loginWay,
                                      Integer status,
                                      LocalDateTime startTime,
                                      LocalDateTime endTime) {
        return baseMapper.selectLoginLogVOPage(page, userName, loginIp, loginType, loginWay, status, startTime, endTime);
    }

    @Async
    @Override
    public void recordLogin(LoginLog loginLog) {
        save(loginLog);
    }
}
