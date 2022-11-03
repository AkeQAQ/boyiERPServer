package com.boyi.service;

import com.boyi.entity.ProduceBatchDelay;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-11-02
 */
public interface ProduceBatchDelayService extends IService<ProduceBatchDelay> {

    void updateNullByField(String dateFieldname, Long id);

    List<ProduceBatchDelay> listByBatchId(Long id);
}
