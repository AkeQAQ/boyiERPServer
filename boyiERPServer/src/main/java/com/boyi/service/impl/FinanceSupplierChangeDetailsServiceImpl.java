package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.FinanceSupplierChangeDetails;
import com.boyi.entity.FinanceSupplierPayshoesDetails;
import com.boyi.mapper.FinanceSupplierChangeDetailsMapper;
import com.boyi.service.FinanceSupplierChangeDetailsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-02-25
 */
@Service
public class FinanceSupplierChangeDetailsServiceImpl extends ServiceImpl<FinanceSupplierChangeDetailsMapper, FinanceSupplierChangeDetails> implements FinanceSupplierChangeDetailsService {

    @Override
    public List<FinanceSupplierChangeDetails> listByForeignId(Long id) {
        return this.list(new QueryWrapper<FinanceSupplierChangeDetails>()
                .eq(DBConstant.TABLE_FINANCE_SUPPLIER_CHANGE_DETAILS.CHANGE_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_FINANCE_SUPPLIER_CHANGE_DETAILS.ID_FIELDNAME)
        );
    }

    @Override
    public boolean removeByDocId(Long id) {
        return this.remove(
                new QueryWrapper<FinanceSupplierChangeDetails>()
                        .eq(DBConstant.TABLE_FINANCE_SUPPLIER_CHANGE_DETAILS.CHANGE_ID_FIELDNAME, id));
    }

    @Override
    public void delByDocumentIds(Long[] ids) {
        this.remove(new QueryWrapper<FinanceSupplierChangeDetails>()
                .in(DBConstant.TABLE_FINANCE_SUPPLIER_CHANGE_DETAILS.CHANGE_ID_FIELDNAME, ids));
    }
}
