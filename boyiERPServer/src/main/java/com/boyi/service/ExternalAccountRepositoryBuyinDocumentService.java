package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 仓库模块-采购入库单据表 服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
public interface ExternalAccountRepositoryBuyinDocumentService extends IService<ExternalAccountRepositoryBuyinDocument> {

    Page<ExternalAccountRepositoryBuyinDocument> innerQuery(Page page, QueryWrapper<ExternalAccountRepositoryBuyinDocument> like);

    ExternalAccountRepositoryBuyinDocument one(QueryWrapper<ExternalAccountRepositoryBuyinDocument> id);

    Integer getSupplierMaterialPassBetweenDate(ExternalAccountBaseSupplierMaterial baseSupplierMaterial);

    // 该单据编号 该供应商，不包括本ID的条数
    int countSupplierOneDocNumExcludSelf(String supplierDocumentNum, String supplierId, Long id);
    // 该单据编号 该供应商
    int countSupplierOneDocNum(String supplierDocumentNum, String supplierId);

    Page<ExternalAccountRepositoryBuyinDocument> innerQueryBySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus);
    Page<ExternalAccountRepositoryBuyinDocument> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String,String> otherSearch);

    int countBySupplierId(String ids[]);

    ExternalAccountRepositoryBuyinDocument getByOrderId(Long id);

    Double countBySupplierIdAndMaterialId(String supplierId, String materialId);

    List<ExternalAccountRepositoryBuyinDocument> countLTByCloseDate(LocalDate closeDate);

    List<ExternalAccountRepositoryBuyinDocument> getListFromOrderBetweenDate(LocalDate startDate, LocalDate endDate);

    Double getAllPageTotalAmount(String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatusList, Map<String, String> queryMap);

    ExternalAccountRepositoryBuyinDocument getNetInFromOrderBetweenDate(LocalDate startD, LocalDate endD,String materialId);

    List<AnalysisMaterailVO> listSupplierAmountPercent(String searchStartDate, String searchEndDate);

    List<AnalysisMaterailVO> listSupplierAmountPercentBySupType(String searchStartDate, String searchEndDate, String searchField);

    List<AnalysisMaterailVO> listMaterialAmountPercent(String searchStartDate, String searchEndDate);

    List<AnalysisMaterailVO> listMaterialAmountPercentByMaterialType(String searchStartDate, String searchEndDate, String searchField);

    List<ExternalAccountRepositoryBuyinDocument> listGTEndDate(String endDate);
}
