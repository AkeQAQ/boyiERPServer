package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.OrderBeforeProductionProgress;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.ProduceReturnShoes;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-08-27
 */
public interface OrderBeforeProductionProgressService extends IService<OrderBeforeProductionProgress> {

    Page<OrderBeforeProductionProgress> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<String> searchStatusList, Map<String, String> queryMap);
}
