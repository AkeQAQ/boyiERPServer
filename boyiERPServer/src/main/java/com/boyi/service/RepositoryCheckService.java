package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.RepositoryCheck;
import com.boyi.entity.RepositoryCheck;

import java.util.List;

public interface RepositoryCheckService extends IService<RepositoryCheck> {

    Page<RepositoryCheck> innerQuery(Page page, QueryWrapper<RepositoryCheck> like);

    RepositoryCheck one(QueryWrapper<RepositoryCheck> id);

    Page<RepositoryCheck> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate);

    List<RepositoryCheck> listGtEndDate(String endDate);
}
