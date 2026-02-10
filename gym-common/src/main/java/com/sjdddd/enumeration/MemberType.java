package com.sjdddd.enumeration;

/**
 * @Author: guess-you
 * @Description: 会员类型枚举
 * @DateTime: 2026/02/10
 **/
public enum MemberType {
    NORMAL("1", "普通会员", 0),
    TRIAL("2", "体验会员", 3);

    private String code;
    private String description;
    private int defaultFreeQuota;

    MemberType(String code, String description, int defaultFreeQuota) {
        this.code = code;
        this.description = description;
        this.defaultFreeQuota = defaultFreeQuota;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getDefaultFreeQuota() {
        return defaultFreeQuota;
    }

    /**
     * 根据code获取枚举
     */
    public static MemberType getByCode(String code) {
        for (MemberType memberType : values()) {
            if (memberType.code.equals(code)) {
                return memberType;
            }
        }
        return NORMAL;
    }

    /**
     * 判断是否为体验会员
     */
    public boolean isTrialMember() {
        return this == TRIAL;
    }
}
