package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.OrderBeforeProductionProgress;
import com.boyi.entity.ProduceReturnShoes;
import com.boyi.mapper.OrderBeforeProductionProgressMapper;
import com.boyi.service.OrderBeforeProductionProgressService;
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
 * @since 2022-08-27
 */
@Service
public class OrderBeforeProductionProgressServiceImpl extends ServiceImpl<OrderBeforeProductionProgressMapper, OrderBeforeProductionProgress> implements OrderBeforeProductionProgressService {

    @Autowired
    private OrderBeforeProductionProgressMapper orderBeforeProductionProgressMapper;

    @Override
    public Page<OrderBeforeProductionProgress> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<String> searchStatusList, Map<String, String> otherSearch) {
        QueryWrapper<OrderBeforeProductionProgress> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.page(page,
                queryWrapper
                        .like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"), DBConstant.TABLE_ORDER_BEFORE_PRODUCT_PROGRESS.CREATED_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_ORDER_BEFORE_PRODUCT_PROGRESS.CREATED_FIELDNAME,searchEndDate)
                        .in(searchStatusList != null && searchStatusList.size() > 0,DBConstant.TABLE_ORDER_BEFORE_PRODUCT_PROGRESS.STATUS_FIELDNAME,searchStatusList)
                        .orderByDesc(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_PROGRESS.CREATED_FIELDNAME)
        );
    }
}
