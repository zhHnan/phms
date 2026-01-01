package com.phms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.entity.VerificationCode;

/**
 * 验证码 Service
 *
 * @author PHMS
 */
public interface VerificationCodeService extends IService<VerificationCode> {

    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @param type  类型：1=登录 2=注册
     * @return 验证码（仅测试环境返回）
     */
    String sendCode(String phone, Integer type);

    /**
     * 验证验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @param type  类型
     * @return true=验证成功 false=验证失败
     */
    boolean verifyCode(String phone, String code, Integer type);
}
