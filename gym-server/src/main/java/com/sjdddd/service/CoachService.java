package com.sjdddd.service;

import com.sjdddd.dto.CoachAddDTO;
import com.sjdddd.dto.CoachEditDTO;
import com.sjdddd.entity.Coach;
import com.sjdddd.result.PageResult;

/**
 * @Author: 沈佳栋
 * @Description: 教练服务接口
 * @DateTime: 2023/11/23 22:23
 **/
public interface CoachService {

    PageResult list(Integer pageNum, Integer pageSize);

    Coach add(CoachAddDTO coachAddDTO);

    Coach edit(CoachEditDTO coachEditDTO);

    Coach delete(Long coachId);

    PageResult searchList(Integer pageNum, Integer pageSize, String coachRealName);

    /**
     * 根据ID获取教练信息
     * @param coachId 教练ID
     * @return 教练信息，不存在返回null
     */
    Coach getById(Long coachId);
}
