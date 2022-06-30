package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.vo.OrderProductCalVO;
import com.boyi.entity.AnalysisProductOrderVO;
import com.boyi.entity.OrderProductOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.ProduceOrderMaterialProgress;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    OrderProductOrder getByOrderNum(String orderNum);

    List<OrderProductOrder> listByOrderNums(Set<String> orderNums);

    List<ProduceOrderMaterialProgress> listByProductNumBrandAndProgressMaterialId(String productNum, String productBrand, String materialId);

    List<AnalysisProductOrderVO> listGroupByProductNum(String searchStartDate, String searchEndDate);

    List<AnalysisProductOrderVO> listByDate(String searchStartDate, String searchEndDate);

    List<AnalysisProductOrderVO> listGroupByProductBrand(String searchStartDate, String searchEndDate);

    List<AnalysisProductOrderVO> listGroupByMostProductNum(String searchStartDate, String searchEndDate);

    List<OrderProductCalVO> calNoProductOrders();


    List<OrderProductOrder> listNoProduct();


    void addOrderNumberByOrderNum(String orderNum, String needAddNum);
}
