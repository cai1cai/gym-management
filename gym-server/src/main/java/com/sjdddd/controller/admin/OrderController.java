package com.sjdddd.controller.admin;

import com.sjdddd.annotation.OperationLog;
import com.sjdddd.dto.CoachRevenueDTO;
import com.sjdddd.dto.OrderCreateDTO;
import com.sjdddd.dto.OrderListDTO;
import com.sjdddd.entity.Coach;
import com.sjdddd.result.PageResult;
import com.sjdddd.result.Result;
import com.sjdddd.service.CoachService;
import com.sjdddd.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: 沈佳栋
 * @Description: 订单管理控制器
 * @DateTime: 2023/11/25 23:10
 **/

@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CoachService coachService;

    @GetMapping("/list")
    @OperationLog(operDesc = "查询订单列表")
    public Result<PageResult> orderList(
            Integer pageNum,
            Integer pageSize
    ) {
        PageResult pageResult = orderService.list(pageNum, pageSize);
        return Result.success(pageResult);
    }

    @GetMapping("/search")
    @OperationLog(operDesc = "搜索订单列表")
    public Result<PageResult> searchCourseList(
            Integer pageNum,
            Integer pageSize,
            @RequestParam(required = false) String courseName
    ) {
        PageResult pageResult = orderService.searchList(pageNum, pageSize, courseName);
        return Result.success(pageResult);
    }

    @GetMapping("/coach/revenue")
    @OperationLog(operDesc = "查询教练收益统计")
    public Result<List<CoachRevenueDTO>> getCoachRevenue(
            @RequestParam @Positive(message = "教练ID必须为正数") Long coachId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        log.info("查询教练收益统计, coachId:{}, startDate:{}, endDate:{}", coachId, startDate, endDate);

        // 校验教练是否存在
        Coach coach = coachService.getById(coachId);
        if (coach == null) {
            return Result.error("教练不存在");
        }

        List<CoachRevenueDTO> revenueList = orderService.getCoachRevenue(coachId, startDate, endDate);
        return Result.success(revenueList);
    }

    @GetMapping("/coach/totalRevenue")
    @OperationLog(operDesc = "查询教练总收益")
    public Result<BigDecimal> getCoachTotalRevenue(
            @RequestParam @Positive(message = "教练ID必须为正数") Long coachId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        log.info("查询教练总收益, coachId:{}, startDate:{}, endDate:{}", coachId, startDate, endDate);

        // 校验教练是否存在
        Coach coach = coachService.getById(coachId);
        if (coach == null) {
            return Result.error("教练不存在");
        }

        BigDecimal totalRevenue = orderService.getCoachTotalRevenue(coachId, startDate, endDate);
        return Result.success(totalRevenue);
    }

    @PostMapping("/create")
    @OperationLog(operDesc = "管理员替会员创建订单")
    public Result<Long> createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        log.info("管理员替会员创建订单: {}", orderCreateDTO);
        Long bookingId = orderService.createOrder(orderCreateDTO);
        return Result.success(bookingId);
    }
}
