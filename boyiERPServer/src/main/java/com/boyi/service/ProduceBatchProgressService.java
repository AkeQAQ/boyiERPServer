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
}
