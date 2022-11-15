package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseMaterialSameGroup;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-11-14
 */
public interface BaseMaterialSameGroupService extends IService<BaseMaterialSameGroup> {

    Page<BaseMaterialSameGroup> queryByManySearch(Page page, String searchField, String queryField, String searchStr, Map<String, String> queryMap);

}
