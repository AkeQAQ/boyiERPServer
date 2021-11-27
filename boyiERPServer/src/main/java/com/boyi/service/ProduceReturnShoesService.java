package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ProduceReturnShoes;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2021-11-26
 */
public interface ProduceReturnShoesService extends IService<ProduceReturnShoes> {
    Page<ProduceReturnShoes> pageBySearch(Page page, String searchUserName);

    Page<ProduceReturnShoes> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<String> searchStatusList, Map<String, String> queryMap);
}
