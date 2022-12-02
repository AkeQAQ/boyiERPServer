package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.vo.OrderProductCalVO;
import com.boyi.entity.ProduceOrderMaterialProgress;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-03-26
 */
@Repository
public interface ProduceOrderMaterialProgressService extends IService<ProduceOrderMaterialProgress> {

    Page<ProduceOrderMaterialProgress> complementInnerQueryByManySearch(Page page, String searchField, String queryField, String searchStr,  Map<String,String> otherSearch);

    List<ProduceOrderMaterialProgress> listByOrderId(Long orderId);
    List<ProduceOrderMaterialProgress> listByOrderIds(Set<Long> orderIds);

    ProduceOrderMaterialProgress getByOrderIdAndMaterialId(Long orderId, String materialId);

    boolean isPreparedByOrderId(Long orderId);

    Page<ProduceOrderMaterialProgress> innerQuery(Page page, QueryWrapper<ProduceOrderMaterialProgress> like);

    Page<ProduceOrderMaterialProgress> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, List<Long> searchStatus2, Map<String,String> otherSearch,String searchNoPropread);


    List<ProduceOrderMaterialProgress> listByMaterialIdCreatedAscNotOver(String materialId);

    void updateInNum(Long id, String afterNum);

    // 查询进度表的该物料信息，从创建时间从远到近，报备数量>已入库数量的。

    void addInNum(Double xiaodanNum, String materialId);

    void subInNum(Double subNum, String materialId);

    List<ProduceOrderMaterialProgress> listByMaterialIdCreatedDescHasInNum(String materialId);

    List<ProduceOrderMaterialProgress> listByOrderIdsAndMaterialId(Long[] orderIds, String materialId);

    void updateStatus(Long id, Integer complementStatusFieldvalue0);

    int countByMaterialId(String materialId);

    int countByMaterialIdAndPreparedNumGtInNum(String materialId);

    List<OrderProductCalVO> listNoInNums();

    List<ProduceOrderMaterialProgress> groupByMaterialIds();

    Double groupByMaterialIdAndBetweenDateAndOrderIdIsNull(String id, String searchStartDate, String searchEndDate);

    List<OrderProductCalVO> listNoInNumsWithMaterialIds(Set<String> keySet);
}
