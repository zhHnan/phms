package com.phms.common.constant;

/**
 * 系统常量类
 *
 * @author PHMS
 */
public class Constants {

    private Constants() {}

    // ==================== 用户类型 ====================
    /**
     * C端用户
     */
    public static final int USER_TYPE_CLIENT = 1;

    /**
     * B端员工
     */
    public static final int USER_TYPE_STAFF = 2;

    // ==================== 员工角色类型 ====================
    /**
     * 普通员工
     */
    public static final int ROLE_STAFF = 1;

    /**
     * 店长
     */
    public static final int ROLE_MANAGER = 2;

    /**
     * 平台超管
     */
    public static final int ROLE_ADMIN = 9;

    // ==================== 状态常量 ====================
    /**
     * 正常/启用
     */
    public static final int STATUS_ENABLE = 1;

    /**
     * 禁用/停业
     */
    public static final int STATUS_DISABLE = 0;

    // ==================== 房间状态 ====================
    /**
     * 空闲
     */
    public static final int ROOM_STATUS_FREE = 0;

    /**
     * 已预订
     */
    public static final int ROOM_STATUS_RESERVED = 1;

    /**
     * 入住中
     */
    public static final int ROOM_STATUS_OCCUPIED = 2;

    /**
     * 待清洁
     */
    public static final int ROOM_STATUS_CLEANING = 3;

    /**
     * 维修
     */
    public static final int ROOM_STATUS_MAINTENANCE = 4;

    // ==================== 订单状态 ====================
    /**
     * 待支付
     */
    public static final int ORDER_STATUS_PENDING = 0;

    /**
     * 待入住
     */
    public static final int ORDER_STATUS_PAID = 1;

    /**
     * 入住中
     */
    public static final int ORDER_STATUS_CHECKED_IN = 2;

    /**
     * 已完成
     */
    public static final int ORDER_STATUS_COMPLETED = 3;

    /**
     * 已取消
     */
    public static final int ORDER_STATUS_CANCELLED = 4;

    // ==================== 宠物类型 ====================
    /**
     * 猫
     */
    public static final int PET_TYPE_CAT = 1;

    /**
     * 狗
     */
    public static final int PET_TYPE_DOG = 2;

    /**
     * 异宠
     */
    public static final int PET_TYPE_OTHER = 3;

    // ==================== 护理类型 ====================
    /**
     * 喂食
     */
    public static final int CARE_TYPE_FEED = 1;

    /**
     * 遛弯
     */
    public static final int CARE_TYPE_WALK = 2;

    /**
     * 清洁
     */
    public static final int CARE_TYPE_CLEAN = 3;

    /**
     * 体检
     */
    public static final int CARE_TYPE_CHECKUP = 4;

    // ==================== 登录方式 ====================
    /**
     * 手机号验证码
     */
    public static final int LOGIN_WAY_PHONE = 1;

    /**
     * 邮箱密码
     */
    public static final int LOGIN_WAY_EMAIL = 2;

    /**
     * 第三方登录
     */
    public static final int LOGIN_WAY_THIRD = 3;

    // ==================== 验证码类型 ====================
    /**
     * 登录验证码
     */
    public static final int CODE_TYPE_LOGIN = 1;

    /**
     * 注册验证码
     */
    public static final int CODE_TYPE_REGISTER = 2;

    // ==================== Redis Key 前缀 ====================
    /**
     * 验证码前缀
     */
    public static final String REDIS_CODE_PREFIX = "phms:code:";

    /**
     * 验证码有效期（分钟）
     */
    public static final int CODE_EXPIRE_MINUTES = 5;
}
