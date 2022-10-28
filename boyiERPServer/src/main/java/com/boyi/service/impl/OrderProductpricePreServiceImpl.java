package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.OrderProductpricePre;
import com.boyi.mapper.OrderProductpricePreMapper;
import com.boyi.service.OrderProductpricePreService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 新产品成本核算-报价 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-09-09
 */
@Service
public class OrderProductpricePreServiceImpl extends ServiceImpl<OrderProductpricePreMapper, OrderProductpricePre> implements OrderProductpricePreService {

    @Override
    public void updateFilePathByCompanyNumAndCustomer(String companyNum, String customer, String storePath) {
        UpdateWrapper<OrderProductpricePre> update = new UpdateWrapper<>();
        update.set(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.SAVE_PATH_FIELDNAME,storePath)
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.COMPANY_NUM_FIELDNAME,companyNum)
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.COSTOMER_FIELDNAME,customer);
        this.update(update);
    }

    @Override
    public OrderProductpricePre getByCustomerAndCompanyNum(String customer, String companyNum) {
        return this.getOne(new QueryWrapper<OrderProductpricePre>()
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.COMPANY_NUM_FIELDNAME,companyNum)
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.COSTOMER_FIELDNAME,customer));
    }

    @Override
    public void updateStatusSuccess(String updateUser,Long id) {
        OrderProductpricePre old = this.getById(id);
        UpdateWrapper<OrderProductpricePre> update = new UpdateWrapper<>();
        update.set(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDVALUE_2)
                .set(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.REAL_JSON_FIELDNAME,
                        old.getExcelJson())
                .set(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.UPDATED_USER_FIELDNAME,updateUser)
                .set(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.UPDATED_FIELDNAME, LocalDateTime.now())
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.ID_FIELDNAME,id);
        this.update(update);
    }

    @Override
    public void updateStatusReturn(String updateUser,Long id) {
        UpdateWrapper<OrderProductpricePre> update = new UpdateWrapper<>();
        update.set(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDVALUE_1)
                .set(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.REAL_JSON_FIELDNAME,null)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_USER_FIELDNAME,updateUser)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_FIELDNAME, LocalDateTime.now())
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.ID_FIELDNAME,id);
        this.update(update);
    }

    @Override
    public OrderProductpricePre getByIdAndStatusSuccess(Long preId) {
        return this.getOne(new QueryWrapper<OrderProductpricePre>()
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.ID_FIELDNAME,preId)
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDVALUE_0));
    }

    @Override
    public void updateStatusFinal(String updateUser,Long id) {
        UpdateWrapper<OrderProductpricePre> update = new UpdateWrapper<>();
        update.set(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDVALUE_0)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_USER_FIELDNAME,updateUser)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_FIELDNAME, LocalDateTime.now())
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.ID_FIELDNAME,id);
        this.update(update);
    }

    @Override
    public void updateStatusReturnReal(String updateUser,Long id) {
        UpdateWrapper<OrderProductpricePre> update = new UpdateWrapper<>();
        update.set(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDVALUE_2)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_USER_FIELDNAME,updateUser)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_FIELDNAME, LocalDateTime.now())
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.ID_FIELDNAME,id);
        this.update(update);
    }

    @Override
    public OrderProductpricePre getByCustomerAndCompanyNumSimple(String customer, String companyNum) {
        return this.getOne(new QueryWrapper<OrderProductpricePre>()
                        .select(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.CAIDUAN_PRICE_FIELDNAME,
                                DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.ZHENCHE_PRICE_FIELDNAME,
                                DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.CX_PRICE_FIELDNAME)
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.COMPANY_NUM_FIELDNAME,companyNum)
                .eq(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.COSTOMER_FIELDNAME,customer));
    }
}
