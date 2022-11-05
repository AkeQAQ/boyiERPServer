package com.boyi.service;

import com.boyi.entity.CostOfLabourType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-10-26
 */
public interface CostOfLabourTypeService extends IService<CostOfLabourType> {

    public List<CostOfLabourType> listByName(String name);
}
