package com.boyi.service;

import com.boyi.entity.BaseMaterialGroup;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 基础模块-物料分组表 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-20
 */
public interface BaseMaterialGroupService extends IService<BaseMaterialGroup> {

    public List<BaseMaterialGroup> getListByParentId(Long id);
}
