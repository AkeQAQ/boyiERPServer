package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.HisProduceBatchProgress;
import com.boyi.entity.ProduceBatchProgress;
import com.boyi.mapper.HisProduceBatchProgressMapper;
import com.boyi.service.HisProduceBatchProgressService;
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
public class HisProduceBatchProgressServiceImpl extends ServiceImpl<HisProduceBatchProgressMapper, HisProduceBatchProgress> implements HisProduceBatchProgressService {

    @Override
    public List<HisProduceBatchProgress> listBySupplierId(String id) {

        return this.list(new QueryWrapper<HisProduceBatchProgress>()
                .eq(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.SUPPLIER_ID_FIELDNAME,id));
    }

    @Override
    public List<HisProduceBatchProgress> listByMaterialId(String id) {
        return this.list(new QueryWrapper<HisProduceBatchProgress>()
                .eq(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.MATERIAL_ID_FIELDNAME,id));
    }

    @Override
    public List<HisProduceBatchProgress> listByMaterialIds(String[] ids) {
        return this.list(new QueryWrapper<HisProduceBatchProgress>()
                .in(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.MATERIAL_ID_FIELDNAME,ids));
    }

    @Override
    public List<HisProduceBatchProgress> listBySupplierIds(String[] ids) {
        return this.list(new QueryWrapper<HisProduceBatchProgress>()
                .in(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.SUPPLIER_ID_FIELDNAME,ids));
    }

    @Override
    public List<HisProduceBatchProgress> listByColtIds(Long[] ids) {
        return this.list(new QueryWrapper<HisProduceBatchProgress>()
                .in(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.COST_OF_LABOUR_TYPE_ID_FIELDNAME,ids));
    }
}
