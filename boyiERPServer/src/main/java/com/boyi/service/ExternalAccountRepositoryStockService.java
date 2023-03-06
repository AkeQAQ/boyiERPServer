package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ExternalAccountRepositoryStock;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.RepositoryReturnMaterialDetail;
import com.boyi.entity.ExternalAccountRepositoryStock;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 库存表 服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
public interface ExternalAccountRepositoryStockService extends IService<ExternalAccountRepositoryStock> {

    void addNumByMaterialIdFromMap(Map<String, Double> needAddMap)throws Exception;

    void addNumByMaterialId(String materialId, Double num);


    void subNumByMaterialIdNum(String materialId, Double num);

    void subNumByMaterialId(Map<String, Double> details) throws Exception;

    ExternalAccountRepositoryStock getByMaterialId(String materialId);

    void removeByMaterialId(String[] ids);

    Page<ExternalAccountRepositoryStock> pageBySearch(Page page, String queryField, String searchField, String searchStr);

    public void validStockNum(Map<String, Double> subMap)throws Exception;
    public void validStockNumWithErrorMsg(Map<String, Double> subMap,List<Map<String,String>> strList);

    List<ExternalAccountRepositoryStock> listByMaterialIds(List<String> ids);

    void updateNum(String materialId, Double checkNum);

    List<ExternalAccountRepositoryStock> listStockNumLTZero();
}
