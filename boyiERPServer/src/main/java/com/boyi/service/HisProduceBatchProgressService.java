package com.boyi.service;

import com.boyi.entity.HisProduceBatchProgress;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.ProduceBatchProgress;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-11-11
 */
public interface HisProduceBatchProgressService extends IService<HisProduceBatchProgress> {
    List<HisProduceBatchProgress> listBySupplierId(String id);
    List<HisProduceBatchProgress> listByMaterialId(String id);

    List<HisProduceBatchProgress> listByMaterialIds(String[] ids);

    List<HisProduceBatchProgress> listBySupplierIds(String[] ids);

    List<HisProduceBatchProgress> listByColtIds(Long[] ids);
}
