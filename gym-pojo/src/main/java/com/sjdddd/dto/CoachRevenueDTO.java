package com.sjdddd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: 教练收益统计DTO
 * @Description: 教练收益统计信息
 * @DateTime: 2026/02/10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachRevenueDTO {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程开始时间
     */
    private Date scheduleStart;

    /**
     * 课程结束时间
     */
    private Date scheduleEnd;

    /**
     * 课程费用
     */
    private BigDecimal courseFee;

    /**
     * 预订数量
     */
    private Integer bookingCount;

    /**
     * 总收益（课程费用 × 预订数量）
     */
    private BigDecimal totalRevenue;
}
