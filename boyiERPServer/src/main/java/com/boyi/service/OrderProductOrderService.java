package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.vo.OrderProductCalVO;
import com.boyi.entity.AnalysisProductOrderVO;
import com.boyi.entity.OrderProductOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.ProduceOrderMaterialProgress;
import com.boyi.entity.RepositoryStock;

import java.util.HashSet;
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
    Page<OrderProductOrder> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, List<Long> searchStatus2, List<Long> searchStatus3,Map<String,String> otherSearch);


    void updatePrepared(Long orderId, Integer preparedFieldvalue1);



    List<OrderProductOrder> listBatchMaterialsByOrderIds(List<Long> orderIds);

    List<OrderProductOrder> listProductNumBrand(List<Long> orderIds);

    List<OrderProductOrder> listByMonthAndDay(String md);

    OrderProductOrder getByOrderNum(String orderNum);

    List<OrderProductOrder> listByOrderNums(Set<String> orderNums);

    List<ProduceOrderMaterialProgress> listByMBomIdAndProgressMaterialId(Long id, String materialId);

    List<AnalysisProductOrderVO> listGroupByProductNum(String searchStartDate, String searchEndDate);

    List<AnalysisProductOrderVO> listByDate(String searchStartDate, String searchEndDate);

    List<AnalysisProductOrderVO> listGroupByProductBrand(String searchStartDate, String searchEndDate);

    List<AnalysisProductOrderVO> listGroupByMostProductNum(String searchStartDate, String searchEndDate);

    List<OrderProductCalVO> calNoProductOrders();


    List<OrderProductOrder> listNoProduct();


    void addOrderNumberByOrderNum(String orderNum, String needAddNum);

    List<RepositoryStock> listNoPickMaterials();

    List<Map<String, Object>> listBySTXMaterial(String searchStartDate, String searchEndDate);

    List<AnalysisProductOrderVO> listGroupByProductBrandAndOrderType(String searchStartDate, String searchEndDate);

    List<OrderProductCalVO> calNoProductOrdersWithMaterialIds(Set<String> materialIds);

    List<RepositoryStock> listNoPickMaterialsWithMaterialIds(Set<String> keySet);

    List<OrderProductOrder> listByOrderNumWithStartAndEnd(Integer minOrderNum, Integer maxOrderNum);

    List<OrderProductOrder> listByOrderNumsWithZCMaterialIds(Long[] pbId);

    List<Map<String, Object>> listByCalMaterial(String startDate, String endDate);

    List<OrderProductOrder> listNoExistProgressOrdersByHasPPC();

    List<OrderProductOrder> listByEndDate(String sevenDateStr, String nowDateStr);

    List<OrderProductOrder> groupByShoeLast();

    List<OrderProductOrder> listByNoMBomByNumBrand(String productNum, String productBrand);

    List<OrderProductOrder> listByMBomId(Long id);
}
