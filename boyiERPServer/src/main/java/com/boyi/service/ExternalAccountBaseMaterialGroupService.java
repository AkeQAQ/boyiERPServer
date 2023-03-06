package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseMaterialGroup;
import com.boyi.entity.BaseMaterialSameGroup;
import com.boyi.entity.ExternalAccountBaseMaterialGroup;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 基础模块-物料分组表 服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
public interface ExternalAccountBaseMaterialGroupService extends IService<ExternalAccountBaseMaterialGroup> {

    public List<ExternalAccountBaseMaterialGroup> getListByParentId(Long id);

    ExternalAccountBaseMaterialGroup getByCode(String groupCode);
}
