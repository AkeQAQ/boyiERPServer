package com.boyi.service;

import com.boyi.entity.ProduceBatchZcProgress;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-04-02
 */
public interface ProduceBatchZcProgressService extends IService<ProduceBatchZcProgress> {

    List<ProduceBatchZcProgress> listByBatchId(Long id);
}
