package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BuyMaterialSupplier;
import com.boyi.entity.ProduceReturnShoes;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.mapper.BuyMaterialSupplierMapper;
import com.boyi.mapper.RepositoryBuyinDocumentMapper;
import com.boyi.service.BuyMaterialSupplierService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-02-22
 */
@Service
public class BuyMaterialSupplierServiceImpl extends ServiceImpl<BuyMaterialSupplierMapper, BuyMaterialSupplier> implements BuyMaterialSupplierService {

    @Override
    public Page<BuyMaterialSupplier> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, Map<String, String> otherSearch) {
        QueryWrapper<BuyMaterialSupplier> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper
                        .like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr));
    }
    @Autowired
    BuyMaterialSupplierMapper buyMaterialSupplierMapper;
    public Page<BuyMaterialSupplier> innerQuery(Page page, QueryWrapper<BuyMaterialSupplier> eq) {
        return buyMaterialSupplierMapper.page(page,eq);
    }
}
