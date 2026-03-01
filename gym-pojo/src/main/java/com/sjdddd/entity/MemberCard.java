package com.sjdddd.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* @Author: 沈佳栋
* @Description: TODO
* @DateTime: 2023/11/20 16:32
**/
/**
    * 会员表
    */
//@ApiModel(description="会员表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberCard {
    /**
    * 会员卡号
    */
    //@ApiModelProperty(value="会员卡号")
    private Long memberCardId;

    /**
    * 用户id
    */
    //@ApiModelProperty(value="用户id")
    private Long userId;

    /**
    * 头像地址
    */
    //@ApiModelProperty(value="头像地址")
    private String avatar;

    /**
    * 会员卡类型，0 拓客卡，1 活动促销卡，2 正常成交卡
    */
    //@ApiModelProperty(value="会员卡类型，0 拓客卡，1 活动促销卡，2 正常成交卡")
    private String memberCardType;

    /**
    * 拓客卡剩余次数（仅拓客卡使用）
    */
    //@ApiModelProperty(value="拓客卡剩余次数")
    private Integer remainingCount;

    /**
    * 卡片金额（活动促销卡和正常成交卡使用）
    */
    //@ApiModelProperty(value="卡片金额")
    private BigDecimal cardAmount;

    /**
    * 创建时间
    */
    //@ApiModelProperty(value="创建时间")
    private LocalDateTime createTime;

    /**
    * 更新时间
    */
    //@ApiModelProperty(value="更新时间")
    private LocalDateTime updateTime;

    /**
     * 用户姓名
     */
    private String userRealName;

    /**
     * 性别
     */
    private String sex;

    /**
     * 手机号
     */
    private String userPhone;
}
