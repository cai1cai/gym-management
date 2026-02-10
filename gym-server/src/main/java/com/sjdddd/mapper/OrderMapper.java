package com.sjdddd.mapper;

import com.sjdddd.dto.CoachRevenueDTO;
import com.sjdddd.entity.Bill;
import com.sjdddd.entity.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: 沈佳栋
 * @Description: TODO
 * @DateTime: 2023/11/25 23:53
 **/
@Mapper
public interface OrderMapper {

    List<Order> selectAllOrders();

    List<Order> searchOrders(String courseName);

    Long countTotalOrders();

    Long countSearchedOrders(String courseName);

    List<Bill> selectByUserIdAllOrders(Object userId);

    /**
     * 按教练ID和时间范围统计收益
     * @param coachId 教练ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 教练收益统计列表
     */
    List<CoachRevenueDTO> selectCoachRevenue(Long coachId, String startDate, String endDate);

    /**
     * 统计教练总收益
     * @param coachId 教练ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总收益
     */
    java.math.BigDecimal selectCoachTotalRevenue(Long coachId, String startDate, String endDate);
}
