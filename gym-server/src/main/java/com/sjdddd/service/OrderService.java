package com.sjdddd.service;

import com.sjdddd.dto.CoachRevenueDTO;
import com.sjdddd.dto.OrderCreateDTO;
import com.sjdddd.dto.OrderListDTO;
import com.sjdddd.result.PageResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: 沈佳栋
 * @Description: TODO
 * @DateTime: 2023/11/25 23:11
 **/
public interface OrderService {

    PageResult list(Integer pageNum, Integer pageSize);

    PageResult searchList(Integer pageNum, Integer pageSize, String courseName);

    PageResult listMemberBills(Integer pageNum, Integer pageSize, Object userId);

    /**
     * 查询教练收益统计
     * @param coachId 教练ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 教练收益列表
     */
    List<CoachRevenueDTO> getCoachRevenue(Long coachId, String startDate, String endDate);

    /**
     * 查询教练总收益
     * @param coachId 教练ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总收益
     */
    BigDecimal getCoachTotalRevenue(Long coachId, String startDate, String endDate);

    /**
     * 管理员替会员创建订单
     * @param orderCreateDTO 订单创建DTO
     * @return 预订记录ID
     */
    Long createOrder(OrderCreateDTO orderCreateDTO);
}
