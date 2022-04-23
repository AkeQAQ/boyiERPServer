package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.BaseSupplierMaterialCopy;
import com.boyi.entity.RepositoryBuyinDocument;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 仓库模块-采购入库单据表 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
public interface RepositoryBuyinDocumentService extends IService<RepositoryBuyinDocument> {

    Page<RepositoryBuyinDocument> innerQuery(Page page, QueryWrapper<RepositoryBuyinDocument> like);
    // 蜘蛛王核算用户，特殊SQL
    Page<RepositoryBuyinDocument> innerQueryZZW(Page page, QueryWrapper<RepositoryBuyinDocument> like);

    RepositoryBuyinDocument one(QueryWrapper<RepositoryBuyinDocument> id);

    Integer getSupplierMaterialPassBetweenDate(BaseSupplierMaterial baseSupplierMaterial);
    // 蜘蛛王核算用户，特殊SQL
    Integer getSupplierMaterialCopyPassBetweenDate(BaseSupplierMaterialCopy baseSupplierMaterial);


    // 该单据编号 该供应商，不包括本ID的条数
    int countSupplierOneDocNumExcludSelf(String supplierDocumentNum, String supplierId, Long id);
    // 该单据编号 该供应商
    int countSupplierOneDocNum(String supplierDocumentNum, String supplierId);

    Page<RepositoryBuyinDocument> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate,List<Long> searchStatus);
    Page<RepositoryBuyinDocument> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String,String> otherSearch);
    // 蜘蛛王核算用户，特殊SQL
    Page<RepositoryBuyinDocument> innerQueryZZWByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String,String> otherSearch);

    int countBySupplierId(String ids[]);

    RepositoryBuyinDocument getByOrderId(Long id);

    Double countBySupplierIdAndMaterialId(String supplierId, String materialId);

    List<RepositoryBuyinDocument> countLTByCloseDate(LocalDate closeDate);

    List<RepositoryBuyinDocument> getListFromOrderBetweenDate(LocalDate startDate, LocalDate endDate);

    Double getAllPageTotalAmount(String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatusList, Map<String, String> queryMap);
    // 蜘蛛王核算用户，特殊SQL
    Double getAllPageTotalAmountZZW(String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatusList, Map<String, String> queryMap);

    RepositoryBuyinDocument getNetInFromOrderBetweenDate(LocalDate startD, LocalDate endD,String materialId);
}
