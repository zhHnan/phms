package com.phms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.User;

/**
 * C端用户 Service
 *
 * @author PHMS
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户列表
     *
     * @param page     分页参数
     * @param phone    手机号（模糊查询）
     * @param nickname 昵称（模糊查询）
     * @param status   状态
     * @return 分页结果
     */
    Page<User> pageList(Page<User> page, String phone, String nickname, Integer status);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    User getByPhone(String phone);

    /**
     * 校验手机号是否唯一
     *
     * @param phone 手机号
     * @param id    排除的用户ID
     * @return true=唯一 false=已存在
     */
    boolean checkPhoneUnique(String phone, Long id);
}
