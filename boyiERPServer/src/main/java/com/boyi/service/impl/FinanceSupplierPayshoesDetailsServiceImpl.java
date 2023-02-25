package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.FinanceSupplierPayshoesDetails;
import com.boyi.entity.ProduceProductConstituentDetail;
import com.boyi.mapper.FinanceSupplierPayshoesDetailsMapper;
import com.boyi.service.FinanceSupplierPayshoesDetailsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-02-24
 */
@Service
public class FinanceSupplierPayshoesDetailsServiceImpl extends ServiceImpl<FinanceSupplierPayshoesDetailsMapper, FinanceSupplierPayshoesDetails> implements FinanceSupplierPayshoesDetailsService {

    @Override
    public void delByDocumentIds(Long[] ids) {
         this.remove(new QueryWrapper<FinanceSupplierPayshoesDetails>()
                .in(DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES_DETAILS.PAY_SHOES_ID_FIELDNAME, ids));
    }

    @Override
    public List<FinanceSupplierPayshoesDetails> listByForeignId(Long id) {
        return this.list(new QueryWrapper<FinanceSupplierPayshoesDetails>()
                .eq(DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES_DETAILS.PAY_SHOES_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES_DETAILS.ID_FIELDNAME)
        );
    }

    @Override
    public boolean removeByDocId(Long id) {
        return this.remove(
                new QueryWrapper<FinanceSupplierPayshoesDetails>()
                        .eq(DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES_DETAILS.PAY_SHOES_ID_FIELDNAME, id));
    }
}
