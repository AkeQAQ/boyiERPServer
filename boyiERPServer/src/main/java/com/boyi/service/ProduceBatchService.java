package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ProduceBatch;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.ProduceOrderMaterialProgress;

import java.util.List;
import java.util.Map;

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

    ProduceBatch getByOrderNum(String orderNum);

    List<ProduceBatch> listByMonthAndDay(String md);

    List<ProduceBatch> listByOrderNum(String orderNum);
}
