package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.CostOfLabourProcesses;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-10-27
 */
public interface CostOfLabourProcessesService extends IService<CostOfLabourProcesses> {

    Page<CostOfLabourProcesses> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatusList, Map<String, String> queryMap);

    int isRigion(CostOfLabourProcesses costOfLabourProcesses);

    int isRigionExcludeSelf(CostOfLabourProcesses costOfLabourProcesses);

    List<CostOfLabourProcesses> listByTypeIdAndPriceDate(String labourTypeId, LocalDate priceDate);
}
