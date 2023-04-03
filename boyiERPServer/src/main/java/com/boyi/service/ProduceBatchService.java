package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ProduceBatch;
import com.baomidou.mybatisplus.extension.service.IService;

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
 * @since 2022-04-29
 */
public interface ProduceBatchService extends IService<ProduceBatch> {

    Page<ProduceBatch> complementInnerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, Map<String, String> queryMap);

    void updateStatus(Long id, Integer batchStatusFieldvalue1);

    ProduceBatch getByBatchId(String batchId);

    ProduceBatch getByPassedBatchId(String batchId);

    List<ProduceBatch> getByOrderNum(String orderNum);

    List<ProduceBatch> listByMonthAndDay(String md);

    List<ProduceBatch> listByOrderNum(String orderNum);

    Long sumByBatchIdPre(String pre);

    List<ProduceBatch> listByLikeRightBatchId(String batchId);

    List<ProduceBatch> listByOutDate( String searchQueryOutDateStr);

    List<ProduceBatch> listByMaterialName(String searchQueryOutDateStr);

    List<ProduceBatch> listByOutDateIsNull();

    List<ProduceBatch> listByMaterialNameIsNull();

    List<ProduceBatch> listDelay();

    List<ProduceBatch> listByOutDateDataDate(String outDate, String dataDate);

    List<ProduceBatch> listByOutDateIsNullWithDataDate(String dataDate);

    Double sumByBatchIdPres(Set<String> batchIdPres);

    List<ProduceBatch> listByWithZCDataDate(String date);
}
