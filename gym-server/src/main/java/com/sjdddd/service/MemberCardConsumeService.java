package com.sjdddd.service;

import com.sjdddd.entity.MemberCard;
import com.sjdddd.entity.Course;

import java.math.BigDecimal;

/**
 * @Author: 沈佳栋
 * @Description: 会员卡消费服务
 * @DateTime: 2026/3/1 16:00
 **/
public interface MemberCardConsumeService {

    /**
     * 计算会员卡消费后的费用
     * @param memberCard 会员卡信息
     * @param course 项目信息
     * @return 消费后的实际费用
     */
    BigDecimal calculateConsumeFee(MemberCard memberCard, Course course);

    /**
     * 检查会员卡是否可以消费指定项目
     * @param memberCard 会员卡信息
     * @param course 项目信息
     * @return 是否可以消费
     */
    boolean canConsume(MemberCard memberCard, Course course);

    /**
     * 执行会员卡消费
     * @param memberCard 会员卡信息
     * @param course 项目信息
     * @return 消费是否成功
     */
    boolean consume(MemberCard memberCard, Course course);

    /**
     * 获取会员卡折扣率
     * @param memberCardType 会员卡类型
     * @return 折扣率（0.8表示八折，0.6表示六折）
     */
    BigDecimal getDiscountRate(String memberCardType);
}
