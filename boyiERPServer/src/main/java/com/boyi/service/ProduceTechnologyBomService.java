package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ProduceTechnologyBom;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-04-27
 */
public interface ProduceTechnologyBomService extends IService<ProduceTechnologyBom> {

    Page<ProduceTechnologyBom> innerQueryByManySearchWithDetailField(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatusList, Map<String, String> queryMap);

    Page<ProduceTechnologyBom> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatusList, Map<String, String> queryMap);
}
