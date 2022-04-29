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
    Page<OrderProductOrder> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, List<Long> searchStatus2, Map<String,String> otherSearch);


    void updatePrepared(Long orderId, Integer preparedFieldvalue1);

    List<OrderProductOrder> getByNumBrand(String productNum, String productBrand);


    List<OrderProductOrder> listBatchMaterialsByOrderIds(List<Long> orderIds);

    List<OrderProductOrder> listProductNumBrand(List<Long> orderIds);

    List<OrderProductOrder> listByMonthAndDay(String md);
}
