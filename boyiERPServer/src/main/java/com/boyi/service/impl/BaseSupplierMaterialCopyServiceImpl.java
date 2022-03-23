package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.BaseSupplierMaterialCopy;
import com.boyi.mapper.BaseSupplierMaterialCopyMapper;
import com.boyi.mapper.BaseSupplierMaterialMapper;
import com.boyi.service.BaseSupplierMaterialCopyService;
import com.boyi.service.BaseSupplierMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 供应商-物料报价表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-24
 */
@Service
public class BaseSupplierMaterialCopyServiceImpl extends ServiceImpl<BaseSupplierMaterialCopyMapper, BaseSupplierMaterialCopy> implements BaseSupplierMaterialCopyService {

    @Autowired
    BaseSupplierMaterialCopyMapper baseSupplierMaterialCopyMapper;

    public Page<BaseSupplierMaterialCopy> innerQuery(Page page, QueryWrapper<BaseSupplierMaterialCopy> eq) {
        return baseSupplierMaterialCopyMapper.page(page,eq);
    }

    public BaseSupplierMaterialCopy one(QueryWrapper<BaseSupplierMaterialCopy> eq) {
        return baseSupplierMaterialCopyMapper.one(eq);
    }

    public List<BaseSupplierMaterialCopy> myList(QueryWrapper<BaseSupplierMaterialCopy> eq) {
        return baseSupplierMaterialCopyMapper.list(eq);
    }

    @Override
    public int isRigionExcludeSelf(BaseSupplierMaterialCopy baseSupplierMaterial) {
        return baseSupplierMaterialCopyMapper.isRigionExcludeSelf(baseSupplierMaterial);
    }

    @Override
    public BaseSupplierMaterialCopy getSuccessPrice(String supplierId, String materialId, LocalDate buyInDate) {
        return this.getOne(new QueryWrapper<BaseSupplierMaterialCopy>()
                .eq(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.SUPPLIER_ID_FIELDNAME, supplierId)
                .eq(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.MATERIAL_ID_FIELDNAME, materialId)
                .le(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.START_DATE_FIELDNAME,buyInDate)
                .ge(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.END_DATE_FIELDNAME, buyInDate)
                .eq(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.STATUS_FIELDNAME, DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.STATUS_FIELDVALUE_0));
    }

    @Override
    public int countSuccessByMaterialId(String id) {
        return this.count(new QueryWrapper<BaseSupplierMaterialCopy>()
                .eq(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.MATERIAL_ID_FIELDNAME, id)
                .eq(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.STATUS_FIELDNAME,DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.STATUS_FIELDVALUE_0)
        );
    }

    @Override
    public int countByMaterialId(String[] ids) {
        return this.count(new QueryWrapper<BaseSupplierMaterialCopy>()
                .in(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.MATERIAL_ID_FIELDNAME, ids));
    }

    @Override
    public int countSuccessBySupplierId(String id) {
        return this.count(new QueryWrapper<BaseSupplierMaterialCopy>()
                .eq(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.SUPPLIER_ID_FIELDNAME, id)
                .eq(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.STATUS_FIELDNAME,DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.STATUS_FIELDVALUE_0)
        );
    }

    @Override
    public int countBySupplierId(String[] ids) {
        return this.count(new QueryWrapper<BaseSupplierMaterialCopy>()
                .in(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.SUPPLIER_ID_FIELDNAME, ids));
    }

    @Override
    public Page<BaseSupplierMaterialCopy> innerQueryBySearch(Page page, String queryField, String searchField, String searchStr) {
        return this.innerQuery(page
                ,new QueryWrapper<BaseSupplierMaterialCopy>()
                        .like(StrUtil.isNotBlank(searchStr) && StrUtil.isNotBlank(searchField),queryField,searchStr));
    }

    @Override
    public Page<BaseSupplierMaterialCopy> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<BaseSupplierMaterialCopy> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }

        return this.innerQuery(page,queryWrapper
                .like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                        && StrUtil.isNotBlank(searchField),queryField,searchStr)
                .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.STATUS_FIELDNAME,searchStatus)
                        .orderByDesc(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.CREATED_FIELDNAME,DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.END_DATE_FIELDNAME)
                );
    }

    @Override
    public int isRigion(BaseSupplierMaterialCopy baseSupplierMaterial) {
        return baseSupplierMaterialCopyMapper.isRigion(baseSupplierMaterial);
    }

}
