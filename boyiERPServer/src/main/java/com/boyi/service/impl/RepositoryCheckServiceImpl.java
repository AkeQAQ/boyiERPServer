package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.RepositoryCheck;
import com.boyi.mapper.RepositoryCheckMapper;
import com.boyi.service.RepositoryCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepositoryCheckServiceImpl extends ServiceImpl<RepositoryCheckMapper, RepositoryCheck> implements RepositoryCheckService {
    @Autowired
    RepositoryCheckMapper repositoryCheckMapper;
    public Page<RepositoryCheck> innerQuery(Page page, QueryWrapper<RepositoryCheck> eq) {
        return repositoryCheckMapper.page(page,eq);
    }

    @Override
    public RepositoryCheck one(QueryWrapper<RepositoryCheck> id) {
        return repositoryCheckMapper.one(id);
    }

    @Override
    public Page<RepositoryCheck> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate) {
        return this.innerQuery(page,
                new QueryWrapper<RepositoryCheck>().
                        like(StrUtil.isNotBlank(searchStr)
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate),DBConstant.TABLE_REPOSITORY_CHECK.CHECK_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate),DBConstant.TABLE_REPOSITORY_CHECK.CHECK_DATE_FIELDNAME,searchEndDate));
    }

}
