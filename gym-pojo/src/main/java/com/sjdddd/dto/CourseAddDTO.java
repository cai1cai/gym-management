package com.sjdddd.dto;

import lombok.Data;

import java.math.BigDecimal;
// import java.util.Date; // 暂时不需要时间字段

/**
 * @Author: 沈佳栋
 * @Description: TODO
 * @DateTime: 2023/11/24 19:23
 **/

@Data
public class CourseAddDTO implements java.io.Serializable{

    private String courseName;

    // 暂时不需要教练字段
    // private Long coachId;
    // private String coachRealName;

    private BigDecimal courseFee;

    // 暂时不需要课程时间字段
    /**
     * 课程开始时间
     */
    //@ApiModelProperty(value="课程开始时间")
    // private Date scheduleStart;

    /**
     * 课程结束时间
     */
    //@ApiModelProperty(value="课程结束时间")
    // private Date scheduleEnd;

    /**
     * 课程状态
     */
    //@ApiModelProperty(value="课程状态")
    private String isEnrolled;
}
