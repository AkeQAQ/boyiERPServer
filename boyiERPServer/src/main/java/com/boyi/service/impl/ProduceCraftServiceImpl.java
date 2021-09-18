package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ProduceCraft;
import com.boyi.entity.ProduceCraft;
import com.boyi.mapper.ProduceCraftMapper;
import com.boyi.service.ProduceCraftService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 新产品成本核算-实际 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-09-09
 */
@Service
public class ProduceCraftServiceImpl extends ServiceImpl<ProduceCraftMapper, ProduceCraft> implements ProduceCraftService {

    @Override
    public ProduceCraft getByCustomerAndCompanyNum(String customer, String companyNum) {
        return this.getOne(new QueryWrapper<ProduceCraft>()
                .eq(DBConstant.TABLE_PRODUCE_CRAFT.COMPANY_NUM_FIELDNAME,companyNum)
                .eq(DBConstant.TABLE_PRODUCE_CRAFT.COSTOMER_FIELDNAME,customer));
    }

    @Override
    public void updateStatusSuccess(String updateUser,Long id) {
        ProduceCraft old = this.getById(id);
        UpdateWrapper<ProduceCraft> update = new UpdateWrapper<>();
        update.set(DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDNAME,
                        DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDVALUE_2)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.REAL_JSON_FIELDNAME,
                        old.getExcelJson())
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_USER_FIELDNAME,updateUser)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_FIELDNAME, LocalDateTime.now())
                .eq(DBConstant.TABLE_PRODUCE_CRAFT.ID_FIELDNAME,id);
        this.update(update);
    }

    @Override
    public void updateStatusReturn(String updateUser,Long id) {
        UpdateWrapper<ProduceCraft> update = new UpdateWrapper<>();
        update.set(DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDNAME,
                        DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDVALUE_1)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.REAL_JSON_FIELDNAME,null)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_USER_FIELDNAME,updateUser)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_FIELDNAME, LocalDateTime.now())
                .eq(DBConstant.TABLE_PRODUCE_CRAFT.ID_FIELDNAME,id);
        this.update(update);
    }

    @Override
    public ProduceCraft getByIdAndStatusSuccess(Long preId) {
        return this.getOne(new QueryWrapper<ProduceCraft>()
                .eq(DBConstant.TABLE_PRODUCE_CRAFT.ID_FIELDNAME,preId)
                .eq(DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDNAME,
                        DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDVALUE_0));
    }

    @Override
    public void updateStatusFinal(String updateUser,Long id) {
        UpdateWrapper<ProduceCraft> update = new UpdateWrapper<>();
        update.set(DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDNAME,
                        DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDVALUE_0)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_USER_FIELDNAME,updateUser)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_FIELDNAME, LocalDateTime.now())
                .eq(DBConstant.TABLE_PRODUCE_CRAFT.ID_FIELDNAME,id);
        this.update(update);
    }

    @Override
    public void updateStatusReturnReal(String updateUser,Long id) {
        UpdateWrapper<ProduceCraft> update = new UpdateWrapper<>();
        update.set(DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDNAME,
                        DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDVALUE_2)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_USER_FIELDNAME,updateUser)
                .set(DBConstant.TABLE_PRODUCE_CRAFT.UPDATED_FIELDNAME, LocalDateTime.now())
                .eq(DBConstant.TABLE_PRODUCE_CRAFT.ID_FIELDNAME,id);
        this.update(update);
    }
}
