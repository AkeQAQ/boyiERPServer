package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseMaterial;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-21
 */
public interface BaseMaterialService extends IService<BaseMaterial> {

    Integer countByGroupCode(String groupCode);

    Page<BaseMaterial> pageByGroupCode(Page page, String searchStr);

    Page<BaseMaterial> pageBySearch(Page page,String queryField, String searchStr);

    List<BaseMaterial> listSame(String name, String unit, String specs, String groupCode);

    List<BaseMaterial> listSameExcludSelf(String name, String unit, String specs, String groupCode, String id);
}
