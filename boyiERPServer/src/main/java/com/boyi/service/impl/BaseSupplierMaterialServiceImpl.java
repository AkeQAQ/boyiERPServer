package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.mapper.BaseSupplierMaterialMapper;
import com.boyi.service.BaseSupplierMaterialService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 供应商-物料报价表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-24
 */
@Service
public class BaseSupplierMaterialServiceImpl extends ServiceImpl<BaseSupplierMaterialMapper, BaseSupplierMaterial> implements BaseSupplierMaterialService {

    @Autowired
    BaseSupplierMaterialMapper baseSupplierMaterialMapper;

    public Page<BaseSupplierMaterial> innerQuery(Page page, QueryWrapper<BaseSupplierMaterial> eq) {
        return baseSupplierMaterialMapper.page(page,eq);
    }

    public BaseSupplierMaterial one(QueryWrapper<BaseSupplierMaterial> eq) {
        return baseSupplierMaterialMapper.one(eq);
    }

    public List<BaseSupplierMaterial> myList(QueryWrapper<BaseSupplierMaterial> eq) {
        return baseSupplierMaterialMapper.list(eq);
    }

    @Override
    public int isRigionExcludeSelf(BaseSupplierMaterial baseSupplierMaterial) {
        return baseSupplierMaterialMapper.isRigionExcludeSelf(baseSupplierMaterial);
    }
}
