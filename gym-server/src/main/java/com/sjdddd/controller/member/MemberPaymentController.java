package com.sjdddd.controller.member;

import com.sjdddd.annotation.OperationLog;
import com.sjdddd.constant.JwtClaimsConstant;
import com.sjdddd.entity.MemberCard;
import com.sjdddd.properties.JwtProperties;
import com.sjdddd.result.PageResult;
import com.sjdddd.result.Result;
import com.sjdddd.service.CourseService;
import com.sjdddd.service.MemberService;
import com.sjdddd.service.OrderService;
import com.sjdddd.service.PaymentService;
import com.sjdddd.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * @Author: 沈佳栋
 * @Description: TODO
 * @DateTime: 2023/11/26 15:56
 **/

@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/member")
public class MemberPaymentController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/course/list")
    @OperationLog(operDesc = "会员查询课程列表")
    public Result<PageResult> courseMemberList(
            Integer pageNum,
            Integer pageSize,
            HttpServletRequest request
    ) {

        String token = request.getHeader(jwtProperties.getUserTokenName());

        log.info("token:{}", token);

        Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

        log.info("claims:{}", claims);

        Object userId = claims.get(JwtClaimsConstant.USER_ID);

        log.info("userId:{}", userId);


        PageResult pageResult = courseService.memberList(pageNum, pageSize, userId);

        return Result.success(pageResult);

    }

    // 余额查询
    @GetMapping("/balance")
    @OperationLog(operDesc = "查询会员卡信息")
    public Result<MemberCard> balance(
            HttpServletRequest request
    ) {

        String token = request.getHeader(jwtProperties.getUserTokenName());

        log.info("token:{}", token);

        Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

        log.info("claims:{}", claims);

        Object userId = claims.get(JwtClaimsConstant.USER_ID);

        log.info("userId:{}", userId);

        MemberCard memberCard = memberService.getMemberCard(userId);

        return Result.success(memberCard);

    }


    @PostMapping("/payCourseFee")
    @OperationLog(operDesc = "支付课程费用")
    public Result<?> payCourseFee(
            HttpServletRequest request,
            @RequestParam Long courseId
    ) {

        String token = request.getHeader(jwtProperties.getUserTokenName());

        log.info("token:{}", token);

        Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

        log.info("claims:{}", claims);

        Object userId = claims.get(JwtClaimsConstant.USER_ID);


        // 实现支付逻辑
        // 包括检查用户余额、减少用户余额、创建支付记录等
        boolean success = paymentService.processPayment(userId, courseId);

        if (success) {
            // 支付成功，返回适当的响应
            return Result.success("支付成功");
        } else {
            // 支付失败，返回错误响应
            return Result.error("支付失败");
        }
    }

    @PostMapping("/unenroll")
    @OperationLog(operDesc = "取消报名,退课")
    public Result<?> unenroll(
            HttpServletRequest request,
            @RequestParam Long courseId
    ) {

        String token = request.getHeader(jwtProperties.getUserTokenName());

        log.info("token:{}", token);

        Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

        log.info("claims:{}", claims);

        Object userId = claims.get(JwtClaimsConstant.USER_ID);

        // 执行退课逻辑
        boolean isUnenrolled = courseService.unenrollCourse(userId, courseId);

        if (isUnenrolled) {
            // 执行退款逻辑
            paymentService.refund(userId, courseId);

            return Result.success("课程退订成功");
        } else {
            return Result.error("课程退订失败");
        }
    }

    @GetMapping("/bill/list")
    @OperationLog(operDesc = "会员查询账单列表")
    public Result<PageResult> orderList(
            Integer pageNum,
            Integer pageSize,
            HttpServletRequest request
    ) {
        String token = request.getHeader(jwtProperties.getUserTokenName());

        log.info("token:{}", token);

        Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

        log.info("claims:{}", claims);

        Object userId = claims.get(JwtClaimsConstant.USER_ID);

        PageResult pageResult = orderService.listMemberBills(pageNum, pageSize, userId);

        return Result.success(pageResult);
    }

    /**
     * 获取会员消费记录
     */
    @GetMapping("/consumption/list")
    @OperationLog(operDesc = "获取会员消费记录")
    public Result<PageResult> getMemberConsumption(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam String userId,
            HttpServletRequest request
    ) {
        String token = request.getHeader(jwtProperties.getUserTokenName());
        Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
        Object tokenUserId = claims.get(JwtClaimsConstant.USER_ID);
        
        // 暂时允许所有登录用户查看消费记录
        log.info("查询用户 {} 的消费记录，操作用户: {}", userId, tokenUserId);
        
        PageResult pageResult = orderService.listMemberBills(pageNum, pageSize, userId);
        return Result.success(pageResult);
    }

    /**
     * 搜索会员消费记录
     */
    @GetMapping("/consumption/search")
    @OperationLog(operDesc = "搜索会员消费记录")
    public Result<PageResult> searchMemberConsumption(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam String userId,
            @RequestParam(required = false) String courseName,
            HttpServletRequest request
    ) {
        String token = request.getHeader(jwtProperties.getUserTokenName());
        Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
        Object tokenUserId = claims.get(JwtClaimsConstant.USER_ID);
        
        // 暂时允许所有登录用户查看消费记录
        log.info("查询用户 {} 的消费记录，操作用户: {}", userId, tokenUserId);
        
        PageResult pageResult = orderService.searchList(pageNum, pageSize, courseName, userId);
        return Result.success(pageResult);
    }
}
