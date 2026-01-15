package com.phms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.LoginLog;
import com.phms.vo.LoginLogVO;

import java.time.LocalDateTime;

/**
 * 登录日志 Service
 *
 * @author PHMS
 */
public interface LoginLogService extends IService<LoginLog> {

    /**
     * 分页查询登录日志
     *
     * @param page      分页参数
     * @param loginType 登录类型
     * @param loginWay  登录方式
     * @param status    登录状态
     * @return 分页结果
     */
    Page<LoginLog> pageList(Page<LoginLog> page, Integer loginType, Integer loginWay, Integer status);

    /**
     * 分页查询登录日志（后台展示VO）
     */
    Page<LoginLogVO> pageListVO(Page<LoginLogVO> page,
                               String userName,
                               String loginIp,
                               Integer loginType,
                               Integer loginWay,
                               Integer status,
                               LocalDateTime startTime,
                               LocalDateTime endTime);

    /**
     * 记录登录日志
     *
     * @param loginLog 日志信息
     */
    void recordLogin(LoginLog loginLog);
}
