package com.boyi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.BaseMaterialGroup;
import com.boyi.entity.BaseSupplier;
import com.boyi.entity.BaseSupplierGroup;

import java.util.List;

/**
 * <p>
 * 基础模块-物料分组表 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-20
 */
public interface BaseSupplierGroupService extends IService<BaseSupplierGroup> {
    public List<BaseSupplierGroup> getListByParentId(Long id);
}
