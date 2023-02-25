package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.FinanceSupplierPayshoes;
import com.boyi.entity.ProduceProductConstituent;
import com.boyi.entity.ProduceProductConstituentDetail;
import com.boyi.mapper.FinanceSupplierPayshoesMapper;
import com.boyi.service.FinanceSupplierPayshoesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-02-24
 */
@Service
public class FinanceSupplierPayshoesServiceImpl extends ServiceImpl<FinanceSupplierPayshoesMapper, FinanceSupplierPayshoes> implements FinanceSupplierPayshoesService {

    @Autowired
    public FinanceSupplierPayshoesMapper financeSupplierPayshoesMapper;

    @Override
    public Page<FinanceSupplierPayshoes> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, List<Long> takeStatusList, List<Long> payTypeStatusList, Map<String, String> otherSearch,String searchStartDate,String searchEndDate) {
        QueryWrapper<FinanceSupplierPayshoes> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDNAME,searchStatus)
                        .in(takeStatusList != null && takeStatusList.size() > 0, DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.TAKE_STATUS_FIELDNAME,takeStatusList)
                        .in(payTypeStatusList != null && payTypeStatusList.size() > 0, DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES_DETAILS.PAY_TYPE_FIELDNAME,payTypeStatusList)
                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.PAY_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.PAY_DATE_FIELDNAME,searchEndDate)

                        .orderByDesc(DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.ID_FIELDNAME)

        );
    }

    @Override
    public Page<FinanceSupplierPayshoes> innerQuery(Page page, QueryWrapper<FinanceSupplierPayshoes> eq) {
        return financeSupplierPayshoesMapper.page(page,eq);

    }

    @Override
    public void updateNullWithField(FinanceSupplierPayshoes ppc, String picName) {
        this.update(new UpdateWrapper<FinanceSupplierPayshoes>()
                .set(picName,null)
                .eq(DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.ID_FIELDNAME,ppc.getId()));
    }

}
