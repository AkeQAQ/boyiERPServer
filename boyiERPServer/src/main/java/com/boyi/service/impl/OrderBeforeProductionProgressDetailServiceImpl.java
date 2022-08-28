package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.OrderBeforeProductionProgressDetail;
import com.boyi.mapper.OrderBeforeProductionProgressDetailMapper;
import com.boyi.service.OrderBeforeProductionProgressDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-08-27
 */
@Service
public class OrderBeforeProductionProgressDetailServiceImpl extends ServiceImpl<OrderBeforeProductionProgressDetailMapper, OrderBeforeProductionProgressDetail> implements OrderBeforeProductionProgressDetailService {

    @Override
    public List<OrderBeforeProductionProgressDetail> listByForeignId(Long id) {
        return this.list(new QueryWrapper<OrderBeforeProductionProgressDetail>()
                .eq(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.FOREIGN_ID_FIELDNAME,id)
                .orderByAsc(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.TYPE_ID_FIELDNAME));
    }

    @Override
    public void updateOtherIsCurrent(Long foreignId, Long id, Integer progressFieldvalue1) {
        this.update(new UpdateWrapper<OrderBeforeProductionProgressDetail>()
                .eq(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.FOREIGN_ID_FIELDNAME,foreignId)
                .ne(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.ID_FIELDNAME,id)
                .set(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.IS_CURRENT_FIELDNAME,progressFieldvalue1));
    }
}
