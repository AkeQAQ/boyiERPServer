package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ProduceProductConstituentDetail;
import com.boyi.entity.RepositoryReturnMaterialDetail;
import com.boyi.mapper.ProduceProductConstituentDetailMapper;
import com.boyi.service.ProduceProductConstituentDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
}