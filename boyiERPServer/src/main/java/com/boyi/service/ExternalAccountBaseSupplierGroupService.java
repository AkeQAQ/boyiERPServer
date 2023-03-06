package com.boyi.service;

import com.boyi.entity.ExternalAccountBaseSupplierGroup;
import com.boyi.entity.ExternalAccountBaseSupplierGroup;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 基础模块-供应商分组表 服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
public interface ExternalAccountBaseSupplierGroupService extends IService<ExternalAccountBaseSupplierGroup> {
    public List<ExternalAccountBaseSupplierGroup> getListByParentId(Long id);

    ExternalAccountBaseSupplierGroup getByCode(String groupCode);
}
