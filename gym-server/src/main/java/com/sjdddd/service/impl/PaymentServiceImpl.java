package com.sjdddd.service.impl;

import com.sjdddd.constant.MessageConstant;
import com.sjdddd.entity.Booking;
import com.sjdddd.entity.Course;
import com.sjdddd.entity.MemberCard;
import com.sjdddd.entity.Payment;
import com.sjdddd.exception.BaseException;
import com.sjdddd.mapper.BookingMapper;
import com.sjdddd.mapper.CourseMapper;
import com.sjdddd.mapper.MemberCardMapper;
import com.sjdddd.mapper.PaymentMapper;
import com.sjdddd.service.MemberCardConsumeService;
import com.sjdddd.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: 沈佳栋
 * @Description: TODO
 * @DateTime: 2023/11/26 16:19
 **/

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private MemberCardMapper memberCardMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private MemberCardConsumeService memberCardConsumeService;

    @Override
    @Transactional
    public boolean processPayment(Object userId, Long courseId) {
        log.info("userId:{}, courseId:{}", userId, courseId);

        Course course = courseMapper.selectByPrimaryKey(courseId);
        if (course == null) {
            throw new BaseException("课程不存在");
        }
        
        MemberCard memberCard = memberCardMapper.selectByUserId(userId);
        if (memberCard == null) {
            throw new BaseException("会员卡信息不存在");
        }
        
        // 检查是否可以消费
        boolean canConsume = memberCardConsumeService.canConsume(memberCard, course);
        if (!canConsume) {
            return false;
        }
        
        BigDecimal actualFee = memberCardConsumeService.calculateConsumeFee(memberCard, course);
        
        // 执行会员卡消费
        boolean consumeSuccess = memberCardConsumeService.consume(memberCard, course);
        if (!consumeSuccess) {
            return false;
        }

        // 处理预订
        Booking booking = new Booking();
        booking.setCourseId(courseId);
        booking.setUserId(userId);
        booking.setBookingDate(new java.util.Date());
        booking.setIsEnrolledByCurrentUser("1");
        log.info("booking:{}", booking);
        bookingMapper.insert(booking);

        // 更新课程
        courseMapper.updateCourseStatus(courseId);

        // 记录支付信息
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setAmount(actualFee);
        payment.setBookingId(booking.getBookingId());
        payment.setPaymentType("余额支付");
        payment.setPaymentStatus("1");
        payment.setPaymentDate(new java.util.Date());
        paymentMapper.insert(payment);

        return true;
    }

    @Override
    @Transactional
    public void refund(Object userId, Long courseId) {
        // 检查是否可以退款
        Booking booking = bookingMapper.selectByUserIdAndCourseId(userId, courseId);
        log.info("booking:{}", booking);

        if (booking == null) {
            throw new BaseException(MessageConstant.NOT_EXISTS);
        }

        Course course = courseMapper.selectByPrimaryKey(courseId);
        if (course == null) {
            throw new BaseException("课程不存在");
        }
        
        MemberCard memberCard = memberCardMapper.selectByUserId(userId);
        if (memberCard == null) {
            throw new BaseException("会员卡信息不存在");
        }
        
        // 检查是否可以退款
        boolean canRefund = memberCardConsumeService.canConsume(memberCard, course);
        if (!canRefund) {
            throw new BaseException("该会员卡无法退款此项目");
        }
        
        BigDecimal courseFee = course.getCourseFee();
        BigDecimal actualFee = memberCardConsumeService.calculateConsumeFee(memberCard, course);
        
        // 退款：将费用返还到会员卡
        if ("0".equals(memberCard.getMemberCardType())) {
            // 拓客卡：增加次数
            memberCard.setRemainingCount((memberCard.getRemainingCount() != null ? memberCard.getRemainingCount() : 0) + 1);
        } else {
            // 其他卡：返还金额
            BigDecimal currentAmount = memberCard.getCardAmount() != null ? memberCard.getCardAmount() : BigDecimal.ZERO;
            memberCard.setCardAmount(currentAmount.add(actualFee));
        }
        
        memberCardMapper.updateByPrimaryKeySelective(memberCard);

        // 更新预订状态
        bookingMapper.updateByBookingStatus(userId, courseId);

        // 记录退款信息
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setAmount(courseFee);
        payment.setPaymentType("余额退款");
        payment.setPaymentStatus("2");
        payment.setBookingId(booking.getBookingId()); // 关联原始预订记录
        payment.setPaymentDate(new java.util.Date());
        paymentMapper.insert(payment);
    }

}
