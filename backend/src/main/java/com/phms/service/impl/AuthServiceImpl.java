package com.phms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.phms.common.constant.Constants;
import com.phms.common.exception.BusinessException;
import com.phms.common.result.ResultCode;
import com.phms.config.StpInterfaceImpl;
import com.phms.dto.LoginDTO;
import com.phms.dto.SendCodeDTO;
import com.phms.dto.StaffLoginDTO;
import com.phms.entity.Hotel;
import com.phms.entity.LoginLog;
import com.phms.entity.Staff;
import com.phms.entity.User;
import com.phms.service.*;
import com.phms.vo.LoginVO;
import com.phms.vo.StaffLoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证 Service 实现类
 *
 * @author PHMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final VerificationCodeService verificationCodeService;
    private final UserService userService;
    private final StaffService staffService;
    private final HotelService hotelService;
    private final LoginLogService loginLogService;
    private final StpInterfaceImpl stpInterface;

    @Override
    public String sendCode(SendCodeDTO dto) {
        return verificationCodeService.sendCode(dto.getPhone(), dto.getType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO userLogin(LoginDTO dto, String ip, String device) {
        // 验证验证码
        boolean verified = verificationCodeService.verifyCode(dto.getPhone(), dto.getCode(), Constants.CODE_TYPE_LOGIN);
        
        // 记录登录日志
        LoginLog loginLog = new LoginLog();
        loginLog.setLoginType(Constants.USER_TYPE_CLIENT);
        loginLog.setLoginWay(Constants.LOGIN_WAY_PHONE);
        loginLog.setLoginIp(ip);
        loginLog.setDeviceInfo(device);
        
        if (!verified) {
            loginLog.setLoginStatus(0);
            loginLog.setFailReason("验证码错误或已过期");
            loginLogService.recordLogin(loginLog);
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }

        // 查询用户，不存在则自动注册
        User user = userService.getByPhone(dto.getPhone());
        if (user == null) {
            user = new User();
            user.setPhone(dto.getPhone());
            user.setNickname("用户" + RandomUtil.randomNumbers(6));
            user.setBalance(BigDecimal.ZERO);
            user.setStatus(Constants.STATUS_ENABLE);
            userService.save(user);
        }

        // 检查用户状态
        if (user.getStatus() != Constants.STATUS_ENABLE) {
            loginLog.setUserId(user.getId());
            loginLog.setLoginStatus(0);
            loginLog.setFailReason("账号已被禁用");
            loginLogService.recordLogin(loginLog);
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);

        // 登录
        StpUtil.login(user.getId());
        StpUtil.getSession().set("userType", Constants.USER_TYPE_CLIENT);
        StpUtil.getSession().set("name", user.getNickname());

        // 记录登录成功日志
        loginLog.setUserId(user.getId());
        loginLog.setLoginStatus(1);
        loginLogService.recordLogin(loginLog);

        // 构建返回结果
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setToken(StpUtil.getTokenValue());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StaffLoginVO staffLogin(StaffLoginDTO dto, String ip, String device) {
        // 记录登录日志
        LoginLog loginLog = new LoginLog();
        loginLog.setLoginType(Constants.USER_TYPE_STAFF);
        loginLog.setLoginWay(Constants.LOGIN_WAY_EMAIL);
        loginLog.setLoginIp(ip);
        loginLog.setDeviceInfo(device);

        // 查询员工
        Staff staff = staffService.getByEmail(dto.getEmail());
        if (staff == null) {
            loginLog.setLoginStatus(0);
            loginLog.setFailReason("账号不存在");
            loginLogService.recordLogin(loginLog);
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }

        // 验证密码
        if (!BCrypt.checkpw(dto.getPassword(), staff.getPassword())) {
            loginLog.setStaffId(staff.getId());
            loginLog.setLoginStatus(0);
            loginLog.setFailReason("密码不匹配");
            loginLogService.recordLogin(loginLog);
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }

        // 检查状态
        if (staff.getStatus() != Constants.STATUS_ENABLE) {
            loginLog.setStaffId(staff.getId());
            loginLog.setLoginStatus(0);
            loginLog.setFailReason("账号已被禁用");
            loginLogService.recordLogin(loginLog);
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 更新最后登录时间
        staff.setLastLoginTime(LocalDateTime.now());
        staffService.updateById(staff);

        // 登录
        StpUtil.login(staff.getId());
        StpUtil.getSession().set("userType", Constants.USER_TYPE_STAFF);
        StpUtil.getSession().set("roleType", staff.getRoleType());
        StpUtil.getSession().set("hotelId", staff.getHotelId());
        StpUtil.getSession().set("name", staff.getRealName());

        // 获取权限列表
        List<String> permissions = stpInterface.getPermissionList(staff.getId(), "");

        // 记录登录成功日志
        loginLog.setStaffId(staff.getId());
        loginLog.setLoginStatus(1);
        loginLogService.recordLogin(loginLog);

        // 构建返回结果
        StaffLoginVO vo = new StaffLoginVO();
        vo.setStaffId(staff.getId());
        vo.setEmail(staff.getEmail());
        vo.setRealName(staff.getRealName());
        vo.setRoleType(staff.getRoleType());
        vo.setHotelId(staff.getHotelId());
        vo.setPermissions(permissions);
        vo.setToken(StpUtil.getTokenValue());

        // 获取门店名称
        if (staff.getHotelId() != null) {
            Hotel hotel = hotelService.getById(staff.getHotelId());
            if (hotel != null) {
                vo.setHotelName(hotel.getName());
            }
        }

        return vo;
    }

    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }
}
