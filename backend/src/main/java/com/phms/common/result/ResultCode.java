package com.phms.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 *
 * @author PHMS
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    FAIL(500, "操作失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限操作"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 用户名或密码错误
     */
    LOGIN_ERROR(1001, "用户名或密码错误"),

    /**
     * 验证码错误
     */
    CAPTCHA_ERROR(1002, "验证码错误或已过期"),

    /**
     * 账号已被禁用
     */
    ACCOUNT_DISABLED(1003, "账号已被禁用"),

    /**
     * 手机号已存在
     */
    PHONE_EXISTS(1004, "手机号已存在"),

    /**
     * 邮箱已存在
     */
    EMAIL_EXISTS(1005, "邮箱已存在"),

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(1006, "用户不存在"),

    /**
     * 房间不可用
     */
    ROOM_UNAVAILABLE(2001, "房间不可用"),

    /**
     * 订单状态异常
     */
    ORDER_STATUS_ERROR(2002, "订单状态异常"),

    /**
     * 订单不存在
     */
    ORDER_NOT_FOUND(2003, "订单不存在"),

    /**
     * 只有超管有权限修改信息
     */
    NO_MANAGER_PERMISSION(3001, "只有超管有权限修改信息");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String message;
}
