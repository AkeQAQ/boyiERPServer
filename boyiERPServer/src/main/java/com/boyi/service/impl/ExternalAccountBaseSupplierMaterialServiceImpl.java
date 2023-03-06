package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ExternalAccountBaseSupplierMaterial;
import com.boyi.entity.ExternalAccountBaseSupplierMaterial;
import com.boyi.mapper.ExternalAccountBaseSupplierMaterialMapper;
import com.boyi.mapper.ExternalAccountBaseSupplierMaterialMapper;
import com.boyi.service.ExternalAccountBaseSupplierMaterialService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
 * @since 2023-03-04
 */
@Service
public class ExternalAccountBaseSupplierMaterialServiceImpl extends ServiceImpl<ExternalAccountBaseSupplierMaterialMapper, ExternalAccountBaseSupplierMaterial> implements ExternalAccountBaseSupplierMaterialService {

    @Autowired
    ExternalAccountBaseSupplierMaterialMapper baseSupplierMaterialMapper;

    public Page<ExternalAccountBaseSupplierMaterial> innerQuery(Page page, QueryWrapper<ExternalAccountBaseSupplierMaterial> eq) {
        return baseSupplierMaterialMapper.page(page,eq);
    }

    public ExternalAccountBaseSupplierMaterial one(QueryWrapper<ExternalAccountBaseSupplierMaterial> eq) {
        return baseSupplierMaterialMapper.one(eq);
    }

    public List<ExternalAccountBaseSupplierMaterial> myList(QueryWrapper<ExternalAccountBaseSupplierMaterial> eq) {
        return baseSupplierMaterialMapper.list(eq);
    }

    @Override
    public int isRigionExcludeSelf(ExternalAccountBaseSupplierMaterial baseSupplierMaterial) {
        return baseSupplierMaterialMapper.isRigionExcludeSelf(baseSupplierMaterial);
    }

    @Override
    public ExternalAccountBaseSupplierMaterial getSuccessPrice(String supplierId, String materialId, LocalDate buyInDate) {
        return this.getOne(new QueryWrapper<ExternalAccountBaseSupplierMaterial>()
                .eq(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.SUPPLIER_ID_FIELDNAME, supplierId)
                .eq(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.MATERIAL_ID_FIELDNAME, materialId)
                .le(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.START_DATE_FIELDNAME,buyInDate)
                .ge(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.END_DATE_FIELDNAME, buyInDate)
                .eq(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.STATUS_FIELDNAME, DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.STATUS_FIELDVALUE_0));
    }

    @Override
    public int countSuccessByMaterialId(String id) {
        return this.count(new QueryWrapper<ExternalAccountBaseSupplierMaterial>()
                .eq(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.MATERIAL_ID_FIELDNAME, id)
                .eq(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.STATUS_FIELDNAME,DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.STATUS_FIELDVALUE_0)
        );
    }

    @Override
    public int countByMaterialId(String[] ids) {
        return this.count(new QueryWrapper<ExternalAccountBaseSupplierMaterial>()
                .in(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.MATERIAL_ID_FIELDNAME, ids));
    }

    @Override
    public int countSuccessBySupplierId(String id) {
        return this.count(new QueryWrapper<ExternalAccountBaseSupplierMaterial>()
                .eq(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.SUPPLIER_ID_FIELDNAME, id)
                .eq(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.STATUS_FIELDNAME,DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.STATUS_FIELDVALUE_0)
        );
    }

    @Override
    public int countBySupplierId(String[] ids) {
        return this.count(new QueryWrapper<ExternalAccountBaseSupplierMaterial>()
                .in(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.SUPPLIER_ID_FIELDNAME, ids));
    }

    @Override
    public Page<ExternalAccountBaseSupplierMaterial> innerQueryBySearch(Page page, String queryField, String searchField, String searchStr) {
        return this.innerQuery(page
                ,new QueryWrapper<ExternalAccountBaseSupplierMaterial>()
                        .like(StrUtil.isNotBlank(searchStr) && StrUtil.isNotBlank(searchField),queryField,searchStr));
    }

    @Override
    public Page<ExternalAccountBaseSupplierMaterial> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<ExternalAccountBaseSupplierMaterial> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }

        return this.innerQuery(page,queryWrapper
                .like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                        && StrUtil.isNotBlank(searchField),queryField,searchStr)
                .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.STATUS_FIELDNAME,searchStatus)
                .orderByDesc(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.CREATED_FIELDNAME,DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.END_DATE_FIELDNAME)
        );
    }

    @Override
    public int isRigion(ExternalAccountBaseSupplierMaterial baseSupplierMaterial) {
        return baseSupplierMaterialMapper.isRigion(baseSupplierMaterial);
    }

    @Override
    public List<ExternalAccountBaseSupplierMaterial> listByMaterialId(String materialId) {
        return baseSupplierMaterialMapper.list(new QueryWrapper<ExternalAccountBaseSupplierMaterial>()
                .eq(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.MATERIAL_ID_FIELDNAME,materialId));
    }

    @Override
    public List<ExternalAccountBaseSupplierMaterial> listByMaterialIdWithSuccessDate(String innerMaterialId, LocalDate buyInDate) {
        return this.list(new QueryWrapper<ExternalAccountBaseSupplierMaterial>()
                .eq(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.MATERIAL_ID_FIELDNAME, innerMaterialId)
                .le(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.START_DATE_FIELDNAME,buyInDate)
                .ge(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.END_DATE_FIELDNAME, buyInDate)
                .eq(DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.STATUS_FIELDNAME, DBConstant.TABLE_EA_BASE_SUPPLIER_MATERIAL.STATUS_FIELDVALUE_0));
    }
}
