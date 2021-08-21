package com.boyi.service;

import com.boyi.entity.BaseUnit;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 基础模块-计量单位管理 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-19
 */
public interface BaseUnitService extends IService<BaseUnit> {

    List<BaseUnit> listValid();
}
