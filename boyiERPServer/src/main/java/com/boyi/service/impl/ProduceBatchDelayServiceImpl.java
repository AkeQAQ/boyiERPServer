package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ProduceBatchDelay;
import com.boyi.entity.ProduceBatchProgress;
import com.boyi.mapper.ProduceBatchDelayMapper;
import com.boyi.service.ProduceBatchDelayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-11-02
 */
@Service
public class ProduceBatchDelayServiceImpl extends ServiceImpl<ProduceBatchDelayMapper, ProduceBatchDelay> implements ProduceBatchDelayService {

    @Autowired
    public ProduceBatchDelayMapper produceBatchDelayMapper;

    @Override
    public void updateNullByField(String field, Long id) {
        if(field.equals(DBConstant.TABLE_PRODUCE_BATCH_DELAY.DATE_FIELDNAME)){
            this.produceBatchDelayMapper.updateDateByField(id);
        }
    }

    @Override
    public List<ProduceBatchDelay> listByBatchId(Long id) {
        return this.list(new QueryWrapper<ProduceBatchDelay>()
                .eq(DBConstant.TABLE_PRODUCE_BATCH_DELAY.PRODUCE_BATCH_ID_FIELDNAME,id));
    }

    @Override
    public List<ProduceBatchDelay> listByBatchIds(List<Long> produceBatchIds) {
        return this.list(new QueryWrapper<ProduceBatchDelay>()
                .in(DBConstant.TABLE_PRODUCE_BATCH_DELAY.PRODUCE_BATCH_ID_FIELDNAME,produceBatchIds));
    }

    @Override
    public List<ProduceBatchDelay> listByProduceBatchId(Long id) {
        return this.list(new QueryWrapper<ProduceBatchDelay>()
                .eq(DBConstant.TABLE_PRODUCE_BATCH_DELAY.PRODUCE_BATCH_ID_FIELDNAME,id));
    }
}
