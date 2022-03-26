package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.OrderProductOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.ProduceProductConstituent;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-03-25
 */
public interface OrderProductOrderService extends IService<OrderProductOrder> {
    Page<OrderProductOrder> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String,String> otherSearch);

}
