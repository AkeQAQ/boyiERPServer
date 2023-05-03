package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.OrderBuyorderDocumentDetail;
import com.boyi.entity.OrderProductOrder;
import com.boyi.entity.ProduceProductConstituentDetail;
import com.boyi.entity.RepositoryReturnMaterialDetail;
import com.boyi.mapper.ProduceProductConstituentDetailMapper;
import com.boyi.service.ProduceProductConstituentDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-03-19
 */
@Service
public class ProduceProductConstituentDetailServiceImpl extends ServiceImpl<ProduceProductConstituentDetailMapper, ProduceProductConstituentDetail> implements ProduceProductConstituentDetailService {

    @Autowired
    private ProduceProductConstituentDetailMapper produceProductConstituentDetailMapper;

    @Override
    public boolean delByDocumentIds(Long[] ids) {
        return this.remove(new QueryWrapper<ProduceProductConstituentDetail>()
                .in(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT_DETIAL.CONSTITUENT_ID_FIELDNAME, ids));
    }

    @Override
    public List<ProduceProductConstituentDetail> listByForeignId(Long id) {
        return this.list(new QueryWrapper<ProduceProductConstituentDetail>()
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT_DETIAL.CONSTITUENT_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT_DETIAL.ID_FIELDNAME)
        );
    }

    @Override
    public boolean removeByDocId(Long id) {
        return this.remove(
                new QueryWrapper<ProduceProductConstituentDetail>()
                        .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT_DETIAL.CONSTITUENT_ID_FIELDNAME, id));

    }

    @Override
    public int countByMaterialId(String[] ids) {
        return this.count(new QueryWrapper<ProduceProductConstituentDetail>()
                .in(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT_DETIAL.MATERIAL_ID_FIELDNAME, ids));
    }

    @Override
    public List<OrderProductOrder> listByNumBrand(String productNum, String productBrand) {
        return produceProductConstituentDetailMapper.listByNumBrand(productNum,productBrand);
    }

    @Override
    public List<ProduceProductConstituentDetail> listByForeignIdAnd1101MaterialId(Long id) {
        return this.list(new QueryWrapper<ProduceProductConstituentDetail>()
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT_DETIAL.CONSTITUENT_ID_FIELDNAME,id)
                .likeRight(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT_DETIAL.MATERIAL_ID_FIELDNAME,"11.01"));
    }

    @Override
    public List<OrderProductOrder> listByMBomId(Long materialBomId) {
        return produceProductConstituentDetailMapper.listByMBomId(materialBomId);
    }
}
