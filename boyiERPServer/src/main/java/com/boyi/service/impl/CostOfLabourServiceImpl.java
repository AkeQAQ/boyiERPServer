package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.CostOfLabour;
import com.boyi.entity.RepositoryReturnMaterial;
import com.boyi.mapper.CostOfLabourMapper;
import com.boyi.mapper.RepositoryReturnMaterialMapper;
import com.boyi.service.CostOfLabourService;
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
 * @since 2022-10-27
 */
@Service
public class CostOfLabourServiceImpl extends ServiceImpl<CostOfLabourMapper, CostOfLabour> implements CostOfLabourService {

    @Autowired
    CostOfLabourMapper costOfLabourMapper;
    public Page<CostOfLabour> innerQuery(Page page, QueryWrapper<CostOfLabour> eq) {
        return costOfLabourMapper.page(page,eq);
    }

    @Override
    public Page<CostOfLabour> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<CostOfLabour> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate)&&!searchStartDate.equals("null"), DBConstant.TABLE_COST_OF_LABOUR.PRICE_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&&!searchEndDate.equals("null"),DBConstant.TABLE_COST_OF_LABOUR.PRICE_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDNAME,searchStatus)

        );
    }
}
