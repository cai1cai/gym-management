package com.sjdddd.service.impl;

import com.sjdddd.entity.MemberCard;
import com.sjdddd.entity.Course;
import com.sjdddd.mapper.MemberCardMapper;
import com.sjdddd.service.MemberCardConsumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @Author: 沈佳栋
 * @Description: 会员卡消费服务实现
 * @DateTime: 2026/3/1 16:00
 **/
@Service
public class MemberCardConsumeServiceImpl implements MemberCardConsumeService {

    @Autowired
    private MemberCardMapper memberCardMapper;

    @Override
    public BigDecimal calculateConsumeFee(MemberCard memberCard, Course course) {
        if (memberCard == null || course == null) {
            return course != null ? course.getCourseFee() : BigDecimal.ZERO; // 如果没有会员卡或项目为空，返回原价或0
        }

        String cardType = memberCard.getMemberCardType();
        BigDecimal originalFee = course.getCourseFee();

        if ("0".equals(cardType)) {
            // 拓客卡：只能使用面部项目和身体项目中的肩颈项目，按次消费，不享受折扣
            if (canConsume(memberCard, course)) {
                return BigDecimal.ZERO; // 拓客卡不扣费，只扣次数
            } else {
                return originalFee; // 不能消费的项目返回原价
            }
        } else if ("1".equals(cardType)) {
            // 活动促销卡：享受八折优惠
            return originalFee.multiply(getDiscountRate("1"));
        } else if ("2".equals(cardType)) {
            // 正常成交卡：享受六折优惠
            return originalFee.multiply(getDiscountRate("2"));
        }

        return originalFee; // 默认返回原价
    }

    @Override
    public boolean canConsume(MemberCard memberCard, Course course) {
        if (memberCard == null || course == null) {
            return false;
        }

        String cardType = memberCard.getMemberCardType();

        if ("0".equals(cardType)) {
            // 拓客卡：只能使用面部项目（courseType=1）和身体项目中的肩颈项目
            // 这里假设肩颈项目的courseName包含"肩颈"关键字，实际项目中可能需要专门的字段
            Integer courseType = course.getCourseType();
            String courseName = course.getCourseName();
            
            // 面部项目
            if (courseType != null && courseType == 1) {
                // 检查剩余次数
                return memberCard.getRemainingCount() != null && memberCard.getRemainingCount() > 0;
            }
            
            // 身体项目中的肩颈项目
            if (courseType != null && courseType == 0 && courseName != null && courseName.contains("肩颈")) {
                // 检查剩余次数
                return memberCard.getRemainingCount() != null && memberCard.getRemainingCount() > 0;
            }
            
            return false; // 其他项目不能使用拓客卡
        } else if ("1".equals(cardType) || "2".equals(cardType)) {
            // 活动促销卡和正常成交卡：检查卡内金额是否足够
            BigDecimal originalFee = course.getCourseFee();
            BigDecimal discountedFee = calculateConsumeFee(memberCard, course);
            BigDecimal cardAmount = memberCard.getCardAmount();
            
            return cardAmount != null && cardAmount.compareTo(discountedFee) >= 0;
        }

        return false;
    }

    @Override
    public boolean consume(MemberCard memberCard, Course course) {
        if (!canConsume(memberCard, course)) {
            return false;
        }

        String cardType = memberCard.getMemberCardType();

        if ("0".equals(cardType)) {
            // 拓客卡：减少次数
            Integer remainingCount = memberCard.getRemainingCount();
            if (remainingCount != null && remainingCount > 0) {
                memberCard.setRemainingCount(remainingCount - 1);
                memberCardMapper.updateByPrimaryKeySelective(memberCard);
                return true;
            }
        } else if ("1".equals(cardType) || "2".equals(cardType)) {
            // 活动促销卡和正常成交卡：扣除金额
            BigDecimal discountedFee = calculateConsumeFee(memberCard, course);
            BigDecimal cardAmount = memberCard.getCardAmount();
            
            if (cardAmount != null && cardAmount.compareTo(discountedFee) >= 0) {
                memberCard.setCardAmount(cardAmount.subtract(discountedFee));
                memberCardMapper.updateByPrimaryKeySelective(memberCard);
                return true;
            }
        }

        return false;
    }

    @Override
    public BigDecimal getDiscountRate(String memberCardType) {
        if ("1".equals(memberCardType)) {
            return new BigDecimal("0.8"); // 活动促销卡八折
        } else if ("2".equals(memberCardType)) {
            return new BigDecimal("0.6"); // 正常成交卡六折
        }
        return BigDecimal.ONE; // 拓客卡或其他类型无折扣
    }
}
