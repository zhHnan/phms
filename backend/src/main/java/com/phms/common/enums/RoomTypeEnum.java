package com.phms.common.enums;

import lombok.Getter;

/**
 * 房间类型枚举
 *
 * @author PHMS
 */
@Getter
public enum RoomTypeEnum {
    
    CAT_STANDARD("cat_standard", "猫咪标间"),
    CAT_DELUXE("cat_deluxe", "猫咪豪华间"),
    DOG_STANDARD("dog_standard", "狗狗标间"),
    DOG_DELUXE("dog_deluxe", "狗狗豪华间"),
    VIP_SUITE("vip_suite", "VIP套间");

    private final String code;
    private final String name;

    RoomTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取显示名称
     */
    public static String getNameByCode(String code) {
        if (code == null) {
            return "";
        }
        for (RoomTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type.getName();
            }
        }
        return code;
    }

    /**
     * 根据code获取枚举
     */
    public static RoomTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (RoomTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
