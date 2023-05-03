package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ProduceTechnologyBom;
import com.boyi.entity.ProduceTechnologyBom;
import com.boyi.mapper.ProduceTechnologyBomDetailMapper;
import com.boyi.mapper.ProduceTechnologyBomMapper;
import com.boyi.service.ProduceTechnologyBomService;
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
 * @since 2023-04-27
 */
@Service
public class ProduceTechnologyBomServiceImpl extends ServiceImpl<ProduceTechnologyBomMapper, ProduceTechnologyBom> implements ProduceTechnologyBomService {

    @Autowired
    public ProduceTechnologyBomMapper produceTechnologyBomMapper;

    @Override
    public Page<ProduceTechnologyBom> innerQueryByManySearchWithDetailField(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<ProduceTechnologyBom> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return produceTechnologyBomMapper.page2(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM.STATUS_FIELDNAME,searchStatus)
                        .orderByDesc(DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM.CREATED_FIELDNAME)

        );
    }

    @Override
    public Page<ProduceTechnologyBom> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<ProduceTechnologyBom> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM.STATUS_FIELDNAME,searchStatus)
                        .orderByDesc(DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM.CREATED_FIELDNAME)

        );
    }

    private Page<ProduceTechnologyBom> innerQuery(Page page, QueryWrapper<ProduceTechnologyBom> eq) {
        return produceTechnologyBomMapper.page(page,eq);
    }
}
