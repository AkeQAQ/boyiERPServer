package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.OrderProductOrder;
import com.boyi.entity.ProduceBatch;
import com.boyi.entity.ProduceOrderMaterialProgress;
import com.boyi.mapper.ProduceBatchMapper;
import com.boyi.service.ProduceBatchService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

    @Override
    public Page<ProduceBatch> complementInnerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, Map<String, String> otherSearch) {
        QueryWrapper<ProduceBatch> queryWrapper = new QueryWrapper<>();

        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.page(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .orderByDesc(DBConstant.TABLE_PRODUCE_BATCH.CREATED_FIELDNAME)

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
}
