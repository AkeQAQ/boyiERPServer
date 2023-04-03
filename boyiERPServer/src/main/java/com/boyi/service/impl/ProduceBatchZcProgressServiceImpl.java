package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ProduceBatchZcProgress;
import com.boyi.mapper.ProduceBatchZcProgressMapper;
import com.boyi.service.ProduceBatchZcProgressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-04-02
 */
@Service
public class ProduceBatchZcProgressServiceImpl extends ServiceImpl<ProduceBatchZcProgressMapper, ProduceBatchZcProgress> implements ProduceBatchZcProgressService {

    @Override
    public List<ProduceBatchZcProgress> listByBatchId(Long id) {
        return this.list(new QueryWrapper<ProduceBatchZcProgress>()
                .eq(DBConstant.TABLE_PRODUCE_BATCH_ZC_PROGRESS.PRODUCE_BATCH_ID_FIELDNAME,id));
    }
}
