package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ProduceProductConstituent;
import com.boyi.entity.RepositoryReturnMaterial;
import com.boyi.mapper.ProduceProductConstituentMapper;
import com.boyi.service.ProduceProductConstituentService;
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
 * @since 2022-03-19
 */
@Service
public class ProduceProductConstituentServiceImpl extends ServiceImpl<ProduceProductConstituentMapper, ProduceProductConstituent> implements ProduceProductConstituentService {

    @Autowired
    ProduceProductConstituentMapper produceProductConstituentMapper;

    @Override
    public Page<ProduceProductConstituent> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<ProduceProductConstituent> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDNAME,searchStatus)

        );
    }

    @Override
    public Page<ProduceProductConstituent> innerQuery(Page page, QueryWrapper<ProduceProductConstituent> eq) {
        return produceProductConstituentMapper.page(page,eq);
    }

    @Override
    public ProduceProductConstituent getByNumBrandColor(String productNum, String productBrand, String productColor) {
        QueryWrapper<ProduceProductConstituent> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.PRODUCT_NUM_FIELDNAME,productNum)
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.PRODUCT_BRAND_FIELDNAME,productBrand)
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.PRODUCT_COLOR_FIELDNAME,productColor);

        return this.getOne(queryWrapper);
    }

    @Override
    public ProduceProductConstituent getValidByNumBrandColor(String productNum, String productBrand, String productColor) {
        QueryWrapper<ProduceProductConstituent> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.PRODUCT_NUM_FIELDNAME,productNum)
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.PRODUCT_BRAND_FIELDNAME,productBrand)
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.PRODUCT_COLOR_FIELDNAME,productColor)
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDNAME,
                        DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_0)
        ;

        return this.getOne(queryWrapper);
    }
}
