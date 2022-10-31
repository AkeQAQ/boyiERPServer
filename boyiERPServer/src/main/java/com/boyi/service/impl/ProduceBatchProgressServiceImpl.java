package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ProduceBatchProgress;
import com.boyi.mapper.ProduceBatchProgressMapper;
import com.boyi.service.ProduceBatchProgressService;
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
 * @since 2022-10-31
 */
@Service
public class ProduceBatchProgressServiceImpl extends ServiceImpl<ProduceBatchProgressMapper, ProduceBatchProgress> implements ProduceBatchProgressService {

    @Autowired
    public ProduceBatchProgressMapper produceBatchProgressMapper;

    @Override
    public List<ProduceBatchProgress> listByBatchId(Long id) {
        return this.produceBatchProgressMapper.listByBatchId(id);
    }

    @Override
    public List<ProduceBatchProgress> listByProduceBatchId(Long id) {
        return this.list(new QueryWrapper<ProduceBatchProgress>()
                .eq(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.PRODUCE_BATCH_ID_FIELDNAME,id));
    }
}
