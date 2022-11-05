package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ProduceBatch;
import com.boyi.mapper.ProduceBatchMapper;
import com.boyi.service.ProduceBatchService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-04-29
 */
@Service
public class ProduceBatchServiceImpl extends ServiceImpl<ProduceBatchMapper, ProduceBatch> implements ProduceBatchService {

    @Autowired
    private ProduceBatchMapper produceBatchMapper;

    public Page<ProduceBatch> innerQuery(Page page, QueryWrapper<ProduceBatch> eq) {
        return produceBatchMapper.page(page,eq);
    }

    @Override
    public Page<ProduceBatch> complementInnerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, Map<String, String> otherSearch) {
        QueryWrapper<ProduceBatch> queryWrapper = new QueryWrapper<>();

        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);

            if(key.equals("batch_id")){
                queryWrapper.likeRight(StrUtil.isNotBlank(val) && !val.equals("null")
                        && StrUtil.isNotBlank(key),key,val);
            }else{
                queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                        && StrUtil.isNotBlank(key),key,val);
            }

        }
        if(queryField.equals("batch_id")){
            queryWrapper.
                    likeRight(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                            && StrUtil.isNotBlank(searchField),queryField,searchStr);
        }else{
            queryWrapper.
                    like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                            && StrUtil.isNotBlank(searchField),queryField,searchStr);
        }

        return this.innerQuery(page,
                queryWrapper
                        .orderByDesc(DBConstant.TABLE_PRODUCE_BATCH.ORDER_NUM_FIELDNAME,DBConstant.TABLE_PRODUCE_BATCH.ID_FIELDNAME)

        );
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        this.update(new UpdateWrapper<ProduceBatch>().set(DBConstant.TABLE_PRODUCE_BATCH.STATUS_FIELDNAME,status)
                .eq(DBConstant.TABLE_PRODUCE_BATCH.ID_FIELDNAME,id));
    }

    @Override
    public ProduceBatch getByBatchId(String batchId) {
        return this.getOne(new QueryWrapper<ProduceBatch>()
                .eq(DBConstant.TABLE_PRODUCE_BATCH.BATCH_ID_FIELDNAME,batchId));
    }

    @Override
    public ProduceBatch getByPassedBatchId(String batchId) {
        return this.getOne(new QueryWrapper<ProduceBatch>()
                .eq(DBConstant.TABLE_PRODUCE_BATCH.BATCH_ID_FIELDNAME,batchId)
                .eq(DBConstant.TABLE_PRODUCE_BATCH.STATUS_FIELDNAME,
                        DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_0));
    }

    @Override
    public ProduceBatch getByOrderNum(String orderNum) {
        return this.getOne(new QueryWrapper<ProduceBatch>()
                .eq(DBConstant.TABLE_PRODUCE_BATCH.ORDER_NUM_FIELDNAME,orderNum));
    }

    @Override
    public List<ProduceBatch> listByMonthAndDay(String md) {
        return this.list(new QueryWrapper<ProduceBatch>()
                .likeRight(DBConstant.TABLE_PRODUCE_BATCH.BATCH_ID_FIELDNAME,md)
                .lt(DBConstant.TABLE_PRODUCE_BATCH.CREATED_FIELDNAME, LocalDate.now().plusDays(-300)));
    }

    @Override
    public List<ProduceBatch> listByOrderNum(String orderNum) {
        return this.list(new QueryWrapper<ProduceBatch>()
                .eq(DBConstant.TABLE_PRODUCE_BATCH.ORDER_NUM_FIELDNAME,orderNum));
    }

    @Override
    public Long sumByBatchIdPre(String preBatchId) {
        return this.produceBatchMapper.sumByBatchIdPre(preBatchId);
    }

    @Override
    public List<ProduceBatch> listByLikeRightBatchId(String batchId) {
        return this.list(new QueryWrapper<ProduceBatch>().likeRight(DBConstant.TABLE_PRODUCE_BATCH.BATCH_ID_FIELDNAME,batchId));
    }

    @Override
    public List<ProduceBatch> listByOutDate( String searchQueryOutDateStr) {
        return this.produceBatchMapper.listByOutDate(searchQueryOutDateStr);
    }

    @Override
    public List<ProduceBatch> listByMaterialName(String name) {
        return this.produceBatchMapper.listByMaterialName(name);
    }

    @Override
    public List<ProduceBatch> listByOutDateIsNull() {
        return this.produceBatchMapper.listByOutDateIsNull();
    }

    @Override
    public List<ProduceBatch> listByMaterialNameIsNull() {
        return this.produceBatchMapper.listByMaterialNameIsNull();
    }

    @Override
    public List<ProduceBatch> listDelay() {
        return this.produceBatchMapper.listDelay();
    }
}
