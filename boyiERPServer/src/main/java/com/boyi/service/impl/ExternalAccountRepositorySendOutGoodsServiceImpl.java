package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ExternalAccountRepositorySendOutGoods;
import com.boyi.entity.ExternalAccountRepositorySendOutGoods;
import com.boyi.mapper.ExternalAccountRepositorySendOutGoodsMapper;
import com.boyi.mapper.FinanceSupplierChangeMapper;
import com.boyi.service.ExternalAccountRepositorySendOutGoodsService;
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
 * @since 2023-03-05
 */
@Service
public class ExternalAccountRepositorySendOutGoodsServiceImpl extends ServiceImpl<ExternalAccountRepositorySendOutGoodsMapper, ExternalAccountRepositorySendOutGoods> implements ExternalAccountRepositorySendOutGoodsService {

    @Autowired
    public ExternalAccountRepositorySendOutGoodsMapper externalAccountRepositorySendOutGoodsMapper;

    @Override
    public Page<ExternalAccountRepositorySendOutGoods> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch, String searchStartDate, String searchEndDate) {
        QueryWrapper<ExternalAccountRepositorySendOutGoods> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDNAME,searchStatus)
                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.SEND_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.SEND_DATE_FIELDNAME,searchEndDate)

                        .orderByDesc(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.ID_FIELDNAME)

        );
    }

    @Override
    public Page<ExternalAccountRepositorySendOutGoods> innerQuery(Page page, QueryWrapper<ExternalAccountRepositorySendOutGoods> eq) {
        return externalAccountRepositorySendOutGoodsMapper.page(page,eq);

    }

    @Override
    public List<ExternalAccountRepositorySendOutGoods> countLTByCloseDate(LocalDate closeDate) {
        return this.list(new QueryWrapper<ExternalAccountRepositorySendOutGoods>()
                .le(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.SEND_DATE_FIELDNAME, closeDate)
                .ne(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDNAME,
                        DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_0));
    }

}
