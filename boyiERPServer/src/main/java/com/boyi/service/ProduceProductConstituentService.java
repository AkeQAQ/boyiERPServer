package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.ProduceProductConstituent;
import com.boyi.entity.RepositoryReturnMaterial;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-03-19
 */
public interface ProduceProductConstituentService extends IService<ProduceProductConstituent> {
    Page<ProduceProductConstituent> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr,  List<Long> searchStatus, Map<String,String> otherSearch);
    Page<ProduceProductConstituent> innerQuery(Page page, QueryWrapper<ProduceProductConstituent> like);
    ProduceProductConstituent getValidByNumBrand(String productNum,String productBrand);


    Page<ProduceProductConstituent> innerQueryByManySearchWithDetailField(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatusList, Map<String, String> queryMap);

}
