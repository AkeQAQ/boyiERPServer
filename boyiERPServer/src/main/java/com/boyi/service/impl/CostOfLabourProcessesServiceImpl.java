package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.CostOfLabourProcesses;
import com.boyi.mapper.BaseSupplierMaterialMapper;
import com.boyi.mapper.CostOfLabourProcessesMapper;
import com.boyi.service.CostOfLabourProcessesService;
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
 * @since 2022-10-27
 */
@Service
public class CostOfLabourProcessesServiceImpl extends ServiceImpl<CostOfLabourProcessesMapper, CostOfLabourProcesses> implements CostOfLabourProcessesService {

    @Autowired
    CostOfLabourProcessesMapper costOfLabourProcessesMapper;

    public Page<CostOfLabourProcesses> innerQuery(Page page, QueryWrapper<CostOfLabourProcesses> eq) {
        return costOfLabourProcessesMapper.page(page,eq);
    }

    @Override
    public Page<CostOfLabourProcesses> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<CostOfLabourProcesses> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }

        return this.innerQuery(page,queryWrapper
                .like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                        && StrUtil.isNotBlank(searchField),queryField,searchStr)
                .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_COST_OF_LABOUR_PROCESSES.STATUS_FIELDNAME,searchStatus)
                .orderByDesc(DBConstant.TABLE_COST_OF_LABOUR_PROCESSES.CREATED_FIELDNAME,DBConstant.TABLE_COST_OF_LABOUR_PROCESSES.END_DATE_FIELDNAME)
        );
    }

    @Override
    public int isRigion(CostOfLabourProcesses costOfLabourProcesses) {
        return this.costOfLabourProcessesMapper.isRigion(costOfLabourProcesses);
    }

    @Override
    public int isRigionExcludeSelf(CostOfLabourProcesses costOfLabourProcesses) {
        return this.costOfLabourProcessesMapper.isRigionExcludeSelf(costOfLabourProcesses);
    }
}
