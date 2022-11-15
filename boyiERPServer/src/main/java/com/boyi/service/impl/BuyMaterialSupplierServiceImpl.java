package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.BuyMaterialSupplier;
import com.boyi.entity.ProduceReturnShoes;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.mapper.BuyMaterialSupplierMapper;
import com.boyi.mapper.RepositoryBuyinDocumentMapper;
import com.boyi.service.BuyMaterialSupplierService;
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

    @Override
    public BuyMaterialSupplier isExist(String supplierId, String supplierMaterialId) {
        List<BuyMaterialSupplier> list = this.list(new QueryWrapper<BuyMaterialSupplier>().eq(DBConstant.TABLE_BUY_MATERIAL_SUPPLIER.SUPPLIER_ID_FIELDNAME, supplierId)
                .eq(DBConstant.TABLE_BUY_MATERIAL_SUPPLIER.SUPPLIER_MATERIAL_ID_FIELDNAME, supplierMaterialId));
        if(list==null || list.size() ==0){
            return null;
        }else {
            return list.get(0);
        }
    }

    @Override
    public BuyMaterialSupplier isExistNotSelf(String supplierId, String supplierMaterialId,Long id) {
        List<BuyMaterialSupplier> list = this.list(new QueryWrapper<BuyMaterialSupplier>().eq(DBConstant.TABLE_BUY_MATERIAL_SUPPLIER.SUPPLIER_ID_FIELDNAME, supplierId)
                .eq(DBConstant.TABLE_BUY_MATERIAL_SUPPLIER.SUPPLIER_MATERIAL_ID_FIELDNAME, supplierMaterialId)
                .ne(DBConstant.TABLE_BUY_MATERIAL_SUPPLIER.ID_FIELDNAME,id));
        if(list==null || list.size() ==0){
            return null;
        }else {
            return list.get(0);
        }
    }

    @Override
    public List<BuyMaterialSupplier> listByInnerMaterialId(String materialId) {

        return this.list(new QueryWrapper<BuyMaterialSupplier>()
                .eq(DBConstant.TABLE_BUY_MATERIAL_SUPPLIER.INNER_MATERIAL_ID_FIELDNAME,materialId));
    }

    @Autowired
    BuyMaterialSupplierMapper buyMaterialSupplierMapper;
    public Page<BuyMaterialSupplier> innerQuery(Page page, QueryWrapper<BuyMaterialSupplier> eq) {
        return buyMaterialSupplierMapper.page(page,eq);
    }
}
