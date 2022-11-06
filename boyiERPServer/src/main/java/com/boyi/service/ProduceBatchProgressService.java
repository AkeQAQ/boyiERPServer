package com.boyi.service;

import com.boyi.entity.ProduceBatchProgress;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-10-31
 */
public interface ProduceBatchProgressService extends IService<ProduceBatchProgress> {

    List<ProduceBatchProgress> listByBatchId(Long id);

    List<ProduceBatchProgress> listByProduceBatchId(Long id);

    void updateNullByField(String sendForeignProductDateFieldname,Long id);

    List<ProduceBatchProgress> listBySupplierId(String id);
    List<ProduceBatchProgress> listByMaterialId(String id);

    List<ProduceBatchProgress> listByMaterialIds(String[] ids);

    List<ProduceBatchProgress> listBySupplierIds(String[] ids);

    Integer countByBatchIdSeqOutDateAccept(String batchIdStr, int i);

    List<ProduceBatchProgress> listByProduceBatchIdByCostOfLabourTypeId(Long id,Long costOfLabourTypeId);

    Integer countByBatchIdStrAndCostOfLabourTypeIdAndOutDateIsNotNull(String batchIdStr,Long coltId);

}
