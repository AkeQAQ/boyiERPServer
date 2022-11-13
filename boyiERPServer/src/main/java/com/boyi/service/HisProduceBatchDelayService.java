package com.boyi.service;

import com.boyi.entity.HisProduceBatchDelay;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-11-11
 */
public interface HisProduceBatchDelayService extends IService<HisProduceBatchDelay> {

    List<HisProduceBatchDelay> listByColtIds(Long[] ids);
}
