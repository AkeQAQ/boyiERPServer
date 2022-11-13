package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.HisProduceBatchDelay;
import com.boyi.mapper.HisProduceBatchDelayMapper;
import com.boyi.service.HisProduceBatchDelayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-11-11
 */
@Service
public class HisProduceBatchDelayServiceImpl extends ServiceImpl<HisProduceBatchDelayMapper, HisProduceBatchDelay> implements HisProduceBatchDelayService {

    @Override
    public List<HisProduceBatchDelay> listByColtIds(Long[] ids) {
        return this.list(new QueryWrapper<HisProduceBatchDelay>()
                .in(DBConstant.TABLE_PRODUCE_BATCH_DELAY.COST_OF_LABOUR_TYPE_ID_FIELDNAME,ids));
    }
}
