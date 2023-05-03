package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ProduceProductConstituentDetail;
import com.boyi.entity.ProduceTechnologyBomDetail;
import com.boyi.mapper.ProduceTechnologyBomDetailMapper;
import com.boyi.service.ProduceTechnologyBomDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-04-27
 */
@Service
public class ProduceTechnologyBomDetailServiceImpl extends ServiceImpl<ProduceTechnologyBomDetailMapper, ProduceTechnologyBomDetail> implements ProduceTechnologyBomDetailService {

    @Override
    public void delByDocumentIds(Long[] ids) {
         this.remove(new QueryWrapper<ProduceTechnologyBomDetail>()
                .in(DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM_DETIAL.CONSTITUENT_ID_FIELDNAME, ids));
    }

    @Override
    public List<ProduceTechnologyBomDetail> listByForeignId(Long id) {
        return this.list(new QueryWrapper<ProduceTechnologyBomDetail>()
                .eq(DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM_DETIAL.CONSTITUENT_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM_DETIAL.ID_FIELDNAME)
        );
    }

    @Override
    public boolean removeByDocId(Long id) {
        return this.remove(
                new QueryWrapper<ProduceTechnologyBomDetail>()
                        .eq(DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM_DETIAL.CONSTITUENT_ID_FIELDNAME, id));
    }
}
