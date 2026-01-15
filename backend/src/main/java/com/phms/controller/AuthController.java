package com.phms.controller;

import cn.hutool.core.util.StrUtil;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.phms.common.result.Result;
import com.phms.common.constant.Constants;
import com.phms.common.exception.BusinessException;
import com.phms.common.result.ResultCode;
import com.phms.dto.LoginDTO;
import com.phms.dto.SendCodeDTO;
import com.phms.dto.StaffLoginDTO;
import com.phms.dto.UserProfileUpdateDTO;
import com.phms.entity.User;
import com.phms.service.AuthService;
import com.phms.service.UserService;
import com.phms.vo.LoginVO;
import com.phms.vo.StaffLoginVO;
import com.phms.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author PHMS
 */
@Tag(name = "认证管理", description = "登录、登出、验证码相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "发送验证码", description = "发送手机验证码，用于C端用户登录")
    @PostMapping("user/send-code")
    public Result<String> sendCode(@Valid @RequestBody SendCodeDTO dto) {
        String code = authService.sendCode(dto);
        return Result.success("验证码发送成功", code);
    }

    @Operation(summary = "C端用户登录", description = "使用手机号+验证码登录")
    @PostMapping("/user/login")
    public Result<LoginVO> userLogin(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        String ip = getIpAddress(request);
        String device = request.getHeader("User-Agent");
        LoginVO vo = authService.userLogin(dto, ip, device);
        return Result.success(vo);
    }

    @Operation(summary = "B端员工登录", description = "使用邮箱+密码登录")
    @PostMapping("/staff/login")
    public Result<StaffLoginVO> staffLogin(@Valid @RequestBody StaffLoginDTO dto, HttpServletRequest request) {
        String ip = getIpAddress(request);
        String device = request.getHeader("User-Agent");
        StaffLoginVO vo = authService.staffLogin(dto, ip, device);
        return Result.success(vo);
    }

    @Operation(summary = "登出", description = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success("登出成功", null);
    }

    @Operation(summary = "获取当前登录用户信息（C端）")
    @GetMapping("/user/info")
    @SaCheckLogin
    public Result<UserInfoVO> userInfo() {
        assertClientUser();
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        return Result.success(vo);
    }

    @Operation(summary = "更新当前登录用户资料（C端）", description = "支持修改昵称/头像")
    @PutMapping("/user/profile")
    @SaCheckLogin
    public Result<Void> updateUserProfile(@Valid @RequestBody UserProfileUpdateDTO dto) {
        assertClientUser();

        boolean nicknameProvided = dto.getNickname() != null && StrUtil.isNotBlank(dto.getNickname());
        boolean avatarProvided = dto.getAvatar() != null && StrUtil.isNotBlank(dto.getAvatar());
        if (!nicknameProvided && !avatarProvided) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请填写要修改的内容");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (nicknameProvided) {
            String nickname = dto.getNickname().trim();
            user.setNickname(nickname);
            StpUtil.getSession().set("name", nickname);
        }
        if (avatarProvided) {
            user.setAvatar(dto.getAvatar().trim());
        }

        userService.updateById(user);
        return Result.success();
    }

    private void assertClientUser() {
        Object userTypeObj = StpUtil.getSession().get("userType");
        Integer userType = userTypeObj == null ? null : Integer.valueOf(String.valueOf(userTypeObj));
        if (userType == null || !userType.equals(Constants.USER_TYPE_CLIENT)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
