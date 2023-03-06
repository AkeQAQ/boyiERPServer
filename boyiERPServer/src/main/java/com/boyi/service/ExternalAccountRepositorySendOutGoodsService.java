package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ExternalAccountRepositorySendOutGoods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.ExternalAccountRepositorySendOutGoods;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-05
 */
public interface ExternalAccountRepositorySendOutGoodsService extends IService<ExternalAccountRepositorySendOutGoods> {

    Page<ExternalAccountRepositorySendOutGoods> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatusList, Map<String, String> queryMap, String searchStartDate, String searchEndDate);
    Page<ExternalAccountRepositorySendOutGoods> innerQuery(Page page, QueryWrapper<ExternalAccountRepositorySendOutGoods> like);

    List<ExternalAccountRepositorySendOutGoods> countLTByCloseDate(LocalDate closeDate);
}
