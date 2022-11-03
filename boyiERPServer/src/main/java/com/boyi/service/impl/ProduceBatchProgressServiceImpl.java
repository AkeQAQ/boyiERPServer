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

    @Override
    public void updateNullByField(String field,Long id) {
        if(field.equals(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.SEND_FOREIGN_PRODUCT_DATE_FIELDNAME)){
            this.produceBatchProgressMapper.updateSendDateByField(id);
        }else if(field.equals(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.BACK_FOREIGN_PRODUCT_DATE_FIELDNAME)){
            this.produceBatchProgressMapper.updateBackDateByField(id);
        }else if(field.equals(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.OUT_DATE_FIELDNAME)){
            this.produceBatchProgressMapper.updateOutDateByField(id);
        }
    }

    @Override
    public List<ProduceBatchProgress> listBySupplierId(String id) {

        return this.list(new QueryWrapper<ProduceBatchProgress>()
                .eq(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.SUPPLIER_ID_FIELDNAME,id));
    }

    @Override
    public List<ProduceBatchProgress> listByMaterialId(String id) {
        return this.list(new QueryWrapper<ProduceBatchProgress>()
                .eq(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.MATERIAL_ID_FIELDNAME,id));
    }

    @Override
    public List<ProduceBatchProgress> listByMaterialIds(String[] id) {
        return this.list(new QueryWrapper<ProduceBatchProgress>()
                .in(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.MATERIAL_ID_FIELDNAME,id));
    }

    @Override
    public List<ProduceBatchProgress> listBySupplierIds(String[] ids) {

        return this.list(new QueryWrapper<ProduceBatchProgress>()
                .in(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.SUPPLIER_ID_FIELDNAME,ids));
    }
}
