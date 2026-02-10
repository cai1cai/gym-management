package com.sjdddd.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sjdddd.dto.CoachRevenueDTO;
import com.sjdddd.dto.OrderCreateDTO;
import com.sjdddd.entity.Booking;
import com.sjdddd.entity.Bill;
import com.sjdddd.entity.Course;
import com.sjdddd.entity.Order;
import com.sjdddd.entity.Payment;
import com.sjdddd.exception.BaseException;
import com.sjdddd.mapper.BookingMapper;
import com.sjdddd.mapper.CourseMapper;
import com.sjdddd.mapper.MemberCardMapper;
import com.sjdddd.mapper.OrderMapper;
import com.sjdddd.mapper.PaymentMapper;
import com.sjdddd.result.PageResult;
import com.sjdddd.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Author: 沈佳栋
 * @Description: TODO
 * @DateTime: 2023/11/25 23:11
 **/
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private MemberCardMapper memberCardMapper;

    @Override
    public PageResult list(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<Order> list = orderMapper.selectAllOrders();

        PageInfo page = new PageInfo(list);

        long total = page.getTotal();


        return new PageResult(total, list);

    }

    @Override
    public PageResult listMemberBills(Integer pageNum, Integer pageSize, Object userId) {
        PageHelper.startPage(pageNum, pageSize);

        List<Bill> list = orderMapper.selectByUserIdAllOrders(userId);

        PageInfo page = new PageInfo(list);

        long total = page.getTotal();


        return new PageResult(total, list);

    }

    @Override
    public PageResult searchList(Integer pageNum, Integer pageSize, String courseName) {
        PageHelper.startPage(pageNum, pageSize);

        List<Order> list = orderMapper.searchOrders(courseName);

        PageInfo page = new PageInfo(list);

        long total = page.getTotal();

        return new PageResult(total, list);
    }

    @Override
    public List<CoachRevenueDTO> getCoachRevenue(Long coachId, String startDate, String endDate) {
        log.info("查询教练收益统计, coachId:{}, startDate:{}, endDate:{}", coachId, startDate, endDate);
        return orderMapper.selectCoachRevenue(coachId, startDate, endDate);
    }

    @Override
    public BigDecimal getCoachTotalRevenue(Long coachId, String startDate, String endDate) {
        log.info("查询教练总收益, coachId:{}, startDate:{}, endDate:{}", coachId, startDate, endDate);
        BigDecimal total = orderMapper.selectCoachTotalRevenue(coachId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public Long createOrder(OrderCreateDTO orderCreateDTO) {
        log.info("管理员替会员创建订单: userId={}, courseId={}", orderCreateDTO.getUserId(), orderCreateDTO.getCourseId());

        // 1. 获取课程信息
        Course course = courseMapper.selectByPrimaryKey(orderCreateDTO.getCourseId());
        if (course == null) {
            throw new BaseException("课程不存在");
        }

        // 2. 检查该会员是否已预订此课程
        Booking existingBooking = bookingMapper.selectByUserIdAndCourseId(orderCreateDTO.getUserId(), orderCreateDTO.getCourseId());
        if (existingBooking != null) {
            throw new BaseException("该会员已预订此课程，请勿重复预订");
        }

        // 3. 获取会员余额
        BigDecimal balance = memberCardMapper.selectByUserId(orderCreateDTO.getUserId());

        // 4. 检查余额是否充足
        if (balance.compareTo(course.getCourseFee()) < 0) {
            throw new BaseException("会员余额不足，当前余额: " + balance + "，需要金额: " + course.getCourseFee());
        }

        // 5. 创建预订记录
        Booking booking = new Booking();
        booking.setUserId(orderCreateDTO.getUserId());
        booking.setCourseId(orderCreateDTO.getCourseId());
        booking.setBookingDate(new Date());
        booking.setIsEnrolledByCurrentUser("1");
        bookingMapper.insert(booking);

        // 6. 扣除会员余额
        BigDecimal newBalance = balance.subtract(course.getCourseFee());
        memberCardMapper.updateBalanceByUserId(orderCreateDTO.getUserId(), newBalance);

        // 7. 创建支付记录
        Payment payment = new Payment();
        payment.setUserId(orderCreateDTO.getUserId());
        payment.setBookingId(booking.getBookingId());
        payment.setAmount(course.getCourseFee());
        payment.setPaymentType("管理员代预订");
        payment.setPaymentStatus("1");
        payment.setPaymentDate(new Date());
        paymentMapper.insert(payment);

        log.info("订单创建成功，预订ID: {}，会员余额已扣除: {}", booking.getBookingId(), newBalance);
        return booking.getBookingId();
    }
}
