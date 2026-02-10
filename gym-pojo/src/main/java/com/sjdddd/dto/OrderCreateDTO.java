package com.sjdddd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * @Author: System
 * @Description: 管理员创建订单的数据传输对象
 * @DateTime: 2026/02/10
 **/
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会员ID
     */
    @NotNull(message = "会员ID不能为空")
    @Positive(message = "会员ID必须为正数")
    private Long userId;

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    @Positive(message = "课程ID必须为正数")
    private Long courseId;
}
