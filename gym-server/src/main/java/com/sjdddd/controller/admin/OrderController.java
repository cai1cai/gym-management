package com.sjdddd.controller.admin;

import com.sjdddd.annotation.OperationLog;
import com.sjdddd.dto.CoachRevenueDTO;
import com.sjdddd.dto.OrderListDTO;
import com.sjdddd.result.PageResult;
import com.sjdddd.result.Result;
import com.sjdddd.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: 沈佳栋
 * @Description: TODO
 * @DateTime: 2023/11/25 23:10
 **/

@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

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
            @RequestParam Long coachId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        log.info("查询教练收益统计, coachId:{}, startDate:{}, endDate:{}", coachId, startDate, endDate);
        List<CoachRevenueDTO> revenueList = orderService.getCoachRevenue(coachId, startDate, endDate);
        return Result.success(revenueList);
    }

    @GetMapping("/coach/totalRevenue")
    @OperationLog(operDesc = "查询教练总收益")
    public Result<BigDecimal> getCoachTotalRevenue(
            @RequestParam Long coachId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        log.info("查询教练总收益, coachId:{}, startDate:{}, endDate:{}", coachId, startDate, endDate);
        BigDecimal totalRevenue = orderService.getCoachTotalRevenue(coachId, startDate, endDate);
        return Result.success(totalRevenue);
    }
}
