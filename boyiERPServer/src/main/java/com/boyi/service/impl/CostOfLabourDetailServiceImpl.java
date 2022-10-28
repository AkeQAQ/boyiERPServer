package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.CostOfLabourDetail;
import com.boyi.entity.CostOfLabourProcesses;
import com.boyi.entity.RepositoryReturnMaterialDetail;
import com.boyi.mapper.CostOfLabourDetailMapper;
import com.boyi.service.CostOfLabourDetailService;
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
 * @since 2022-10-27
 */
@Service
public class CostOfLabourDetailServiceImpl extends ServiceImpl<CostOfLabourDetailMapper, CostOfLabourDetail> implements CostOfLabourDetailService {

    @Autowired
    public CostOfLabourDetailMapper costOfLabourDetailMapper;

    @Override
    public boolean removeByForeignId(Long id) {
        return this.remove(new QueryWrapper<CostOfLabourDetail>().eq(DBConstant.TABLE_COST_OF_LABOUR_DETAIL.FOREIGN_ID_FIELDNAME, id));

    }

    @Override
    public List<CostOfLabourDetail> listByForeignId(Long id) {
        return this.list(new QueryWrapper<CostOfLabourDetail>()
                .eq(DBConstant.TABLE_COST_OF_LABOUR_DETAIL.FOREIGN_ID_FIELDNAME,id));
    }

    @Override
    public boolean removeByForeignIds(Long[] ids) {
        return this.remove(new QueryWrapper<CostOfLabourDetail>()
                .in(DBConstant.TABLE_COST_OF_LABOUR_DETAIL.FOREIGN_ID_FIELDNAME,ids));
    }

    @Override
    public Integer countByProcessesIdBetweenDate(CostOfLabourProcesses one) {
        return this.costOfLabourDetailMapper.countByProcessesIdBetweenDate(one);
    }
}
