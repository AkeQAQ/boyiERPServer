package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.OrderProductpricePre;
import com.boyi.entity.OrderProductpriceReal;
import com.boyi.mapper.OrderProductpricePreMapper;
import com.boyi.mapper.OrderProductpriceRealMapper;
import com.boyi.service.OrderProductpricePreService;
import com.boyi.service.OrderProductpriceRealService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 新产品成本核算-实际 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-09-09
 */
@Service
public class OrderProductpriceRealServiceImpl extends ServiceImpl<OrderProductpriceRealMapper, OrderProductpriceReal> implements OrderProductpriceRealService {

    @Override
    public void updateFilePathByCompanyNumAndCustomer(Integer companyNum, String customer, String storePath) {
        UpdateWrapper<OrderProductpriceReal> update = new UpdateWrapper<>();
        update.set(DBConstant.TABLE_ORDER_PRODUCTPRICEREAL.SAVE_PATH_FIELDNAME,storePath)
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEREAL.COMPANY_NUM_FIELDNAME,companyNum)
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEREAL.COSTOMER_FIELDNAME,customer);
        this.update(update);
    }

    @Override
    public OrderProductpriceReal getByCustomerAndCompanyNum(String customer, Integer companyNum) {
        return this.getOne(new QueryWrapper<OrderProductpriceReal>()
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEREAL.COMPANY_NUM_FIELDNAME,companyNum)
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEREAL.COSTOMER_FIELDNAME,customer));
    }

    @Override
    public void updateStatusSuccess(Long id) {
        UpdateWrapper<OrderProductpriceReal> update = new UpdateWrapper<>();
        update.set(DBConstant.TABLE_ORDER_PRODUCTPRICEREAL.STATUS_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEREAL.STATUS_FIELDVALUE_0)
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEREAL.ID_FIELDNAME,id);
        this.update(update);
    }

    @Override
    public void updateStatusReturn(Long id) {
        UpdateWrapper<OrderProductpriceReal> update = new UpdateWrapper<>();
        update.set(DBConstant.TABLE_ORDER_PRODUCTPRICEREAL.STATUS_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEREAL.STATUS_FIELDVALUE_1)
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEREAL.ID_FIELDNAME,id);
        this.update(update);
    }
}
