package com.phms.service;

import com.phms.dto.LoginDTO;
import com.phms.dto.SendCodeDTO;
import com.phms.dto.StaffLoginDTO;
import com.phms.vo.LoginVO;
import com.phms.vo.StaffLoginVO;

/**
 * 认证 Service
 *
 * @author PHMS
 */
public interface AuthService {

    /**
     * 发送验证码
     *
     * @param dto 发送验证码DTO
     * @return 验证码（仅测试环境）
     */
    String sendCode(SendCodeDTO dto);

    /**
     * C端用户登录
     *
     * @param dto     登录DTO
     * @param ip      登录IP
     * @param device  设备信息
     * @return 登录结果
     */
    LoginVO userLogin(LoginDTO dto, String ip, String device);

    /**
     * B端员工登录
     *
     * @param dto     登录DTO
     * @param ip      登录IP
     * @param device  设备信息
     * @return 登录结果
     */
    StaffLoginVO staffLogin(StaffLoginDTO dto, String ip, String device);

    /**
     * 登出
     */
    void logout();
}
