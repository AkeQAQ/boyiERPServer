package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryStock;
import com.boyi.mapper.RepositoryStockMapper;
import com.boyi.service.RepositoryStockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 库存表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-09-02
 */
@Service
public class RepositoryStockServiceImpl extends ServiceImpl<RepositoryStockMapper, RepositoryStock> implements RepositoryStockService {

    @Autowired
    RepositoryStockMapper repositoryStockMapper;

    @Override
    public void addNumBySupplierIdAndMaterialId(String supplierId, String materialId, Double num) {

        RepositoryStock stock = this.getBySupplierIdAndMaterialId(supplierId,materialId);
        if(stock == null){
            stock = new RepositoryStock();
            stock.setSupplierId(supplierId);
            stock.setMaterialId(materialId);
            stock.setNum(num);
            stock.setUpdated(LocalDateTime.now());
            this.save(stock);
        }else{
            UpdateWrapper<RepositoryStock> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq(DBConstant.TABLE_REPOSITORY_STOCK.SUPPLIER_ID_FIELDNAME,supplierId)
                    .eq(DBConstant.TABLE_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,materialId)
                    .setSql(" num = num +"+num)
                    .set(DBConstant.TABLE_REPOSITORY_STOCK.UPDATED_FIELDNAME,LocalDateTime.now());
            this.update(updateWrapper);
        }

    }

    @Override
    public void subNumBySupplierIdAndMaterialId(String supplierId, String materialId, Double num)throws Exception {

        RepositoryStock stock = this.getBySupplierIdAndMaterialId(supplierId,materialId);

        if(stock == null){
            throw new Exception("该供应商:"+supplierId+"，该物料："+materialId+"不存在，不能减库存!");
        }
        UpdateWrapper<RepositoryStock> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(DBConstant.TABLE_REPOSITORY_STOCK.SUPPLIER_ID_FIELDNAME,supplierId)
                .eq(DBConstant.TABLE_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,materialId)
                .setSql(" num = num -"+num)
                .set(DBConstant.TABLE_REPOSITORY_STOCK.UPDATED_FIELDNAME,LocalDateTime.now());
        this.update(updateWrapper);
    }

    @Override
    public RepositoryStock getBySupplierIdAndMaterialId(String supplierId, String materialId) {
        return this.getOne(new QueryWrapper<RepositoryStock>()
                .eq(DBConstant.TABLE_REPOSITORY_STOCK.SUPPLIER_ID_FIELDNAME, supplierId)
                .eq(DBConstant.TABLE_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME, materialId));
    }

    @Override
    public void removeByMaterialId(String[] ids) {
        this.remove(new QueryWrapper<RepositoryStock>()
                .in(DBConstant.TABLE_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,ids));
    }

    @Override
    public void removeBySupplierId(String[] ids) {
        this.remove(new QueryWrapper<RepositoryStock>()
                .in(DBConstant.TABLE_REPOSITORY_STOCK.SUPPLIER_ID_FIELDNAME,ids));
    }

    @Override
    public Page<RepositoryStock> pageBySearch(Page page, String queryField, String searchField, String searchStr) {
        return repositoryStockMapper.page(page
                ,new QueryWrapper<RepositoryStock>()
                        .like(StrUtil.isNotBlank(searchStr) && StrUtil.isNotBlank(searchField),queryField,searchStr));
    }
}
