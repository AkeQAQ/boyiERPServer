package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.entity.*;
import com.boyi.mapper.ExternalAccountRepositoryBuyinDocumentMapper;
import com.boyi.mapper.ExternalAccountRepositoryBuyinDocumentMapper;
import com.boyi.service.ExternalAccountRepositoryBuyinDocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 仓库模块-采购入库单据表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Service
public class ExternalAccountRepositoryBuyinDocumentServiceImpl extends ServiceImpl<ExternalAccountRepositoryBuyinDocumentMapper, ExternalAccountRepositoryBuyinDocument> implements ExternalAccountRepositoryBuyinDocumentService {
    @Autowired
    ExternalAccountRepositoryBuyinDocumentMapper repositoryBuyinDocumentMapper;
    public Page<ExternalAccountRepositoryBuyinDocument> innerQuery(Page page, QueryWrapper<ExternalAccountRepositoryBuyinDocument> eq) {
        return repositoryBuyinDocumentMapper.page(page,eq);
    }

    @Override
    public ExternalAccountRepositoryBuyinDocument one(QueryWrapper<ExternalAccountRepositoryBuyinDocument> id) {
        return repositoryBuyinDocumentMapper.one(id);
    }

    @Override
    public Integer getSupplierMaterialPassBetweenDate(ExternalAccountBaseSupplierMaterial baseSupplierMaterial){
        return repositoryBuyinDocumentMapper.getSupplierMaterialPassBetweenDate(baseSupplierMaterial);
    }

    @Override
    public int countSupplierOneDocNumExcludSelf(String supplierDocumentNum, String supplierId, Long id) {
        return this.count(new QueryWrapper<ExternalAccountRepositoryBuyinDocument>().
                eq(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_DOCUMENT_NUM_FIELDNAME, supplierDocumentNum)
                .eq(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_ID_FIELDNAME,supplierId)
                .ne(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.ID_FIELDNAME,id)
        );
    }

    @Override
    public int countSupplierOneDocNum(String supplierDocumentNum, String supplierId) {
        return this.count(new QueryWrapper<ExternalAccountRepositoryBuyinDocument>().
                eq(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_DOCUMENT_NUM_FIELDNAME, supplierDocumentNum)
                .eq(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_ID_FIELDNAME,supplierId)
        );
    }

    @Override
    public Page<ExternalAccountRepositoryBuyinDocument> innerQueryBySearch(Page page, String searchField, String queryField,
                                                            String searchStr, String searchStartDate, String searchEndDate
            , List<Long> searchStatus
    ) {
        return this.innerQuery(page,
                new QueryWrapper<ExternalAccountRepositoryBuyinDocument>().
                        like(StrUtil.isNotBlank(searchStr)
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate),DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate),DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDNAME,searchStatus)
        );
    }

    @Override
    public Page<ExternalAccountRepositoryBuyinDocument> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<ExternalAccountRepositoryBuyinDocument> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            if(key.equals("price")){
                String val = otherSearch.get(key);
                if(val.isEmpty()){
                    queryWrapper.isNull("price");
                }else{
                    queryWrapper.eq( !val.equals("null"),key,val);
                }
            }else if(key.equals("supplier_name")){
                String val = otherSearch.get(key);

                queryWrapper.eq(StrUtil.isNotBlank(val) && !val.equals("null")
                        && StrUtil.isNotBlank(key),key,val);
            }

            else{
                String val = otherSearch.get(key);
                queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                        && StrUtil.isNotBlank(key),key,val);
            }

        }
        if(queryField.equals("price")){
            if(searchStr.isEmpty()){
                queryWrapper.isNull("price");
            }else{
                queryWrapper.
                        eq(!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr);
            }

        }else if(queryField.equals("supplier_name")){
            queryWrapper.
                    eq(StrUtil.isNotBlank(searchStr)  &&!searchStr.equals("null")
                            && StrUtil.isNotBlank(searchField),queryField,searchStr);
        }

        else{
            queryWrapper.
                    like(StrUtil.isNotBlank(searchStr)  &&!searchStr.equals("null")
                            && StrUtil.isNotBlank(searchField),queryField,searchStr);
        }
        return this.innerQuery(page,
                queryWrapper

                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDNAME,searchStatus)
        );
    }


    @Override
    public Double getAllPageTotalAmount(String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String, String> otherSearch) {

        QueryWrapper<ExternalAccountRepositoryBuyinDocument> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            if(key.equals("price")){
                String val = otherSearch.get(key);
                if(val.isEmpty()){
                    queryWrapper.isNull("price");
                }else{
                    queryWrapper.eq( !val.equals("null"),key,val);
                }
            }else if(key.equals("supplier_name")){
                String val = otherSearch.get(key);

                queryWrapper.eq(StrUtil.isNotBlank(val) && !val.equals("null")
                        && StrUtil.isNotBlank(key),key,val);
            }

            else{
                String val = otherSearch.get(key);
                queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                        && StrUtil.isNotBlank(key),key,val);
            }

        }

        if(queryField.equals("price")){
            if(searchStr.isEmpty()){
                queryWrapper.isNull("price");
            }else{
                queryWrapper.
                        eq(!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr);
            }

        }else if(queryField.equals("supplier_name")){
            queryWrapper.
                    eq(StrUtil.isNotBlank(searchStr)  &&!searchStr.equals("null")
                            && StrUtil.isNotBlank(searchField),queryField,searchStr);
        }

        else{
            queryWrapper.
                    like(StrUtil.isNotBlank(searchStr)  &&!searchStr.equals("null")
                            && StrUtil.isNotBlank(searchField),queryField,searchStr);
        }
        queryWrapper
                .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchStartDate)
                .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchEndDate)
                .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDNAME,searchStatus);
        List<ExternalAccountRepositoryBuyinDocument> list = this.repositoryBuyinDocumentMapper.list(queryWrapper);
        Double sumAmount = 0.0D;
        for (ExternalAccountRepositoryBuyinDocument one: list
        ) {
            if(one.getAmount()==null || one.getAmount() ==0.0D){
                continue;
            }
            sumAmount = BigDecimalUtil.add(sumAmount,one.getAmount()).doubleValue();
        }
        return sumAmount;
    }




    @Override
    public ExternalAccountRepositoryBuyinDocument getNetInFromOrderBetweenDate(LocalDate startD, LocalDate endD, String materialId) {
        return repositoryBuyinDocumentMapper.getNetInFromOrderBetweenDate(startD,endD,materialId);
    }

    @Override
    public List<AnalysisMaterailVO> listSupplierAmountPercent(String searchStartDate, String searchEndDate) {
        return repositoryBuyinDocumentMapper.listSupplierAmountPercent( searchStartDate,  searchEndDate);
    }

    @Override
    public List<AnalysisMaterailVO> listSupplierAmountPercentBySupType(String searchStartDate, String searchEndDate, String searchField) {
        return repositoryBuyinDocumentMapper.listSupplierAmountPercentBySupType( searchStartDate,  searchEndDate,searchField);
    }

    @Override
    public List<AnalysisMaterailVO> listMaterialAmountPercent(String searchStartDate, String searchEndDate) {
        return repositoryBuyinDocumentMapper.listMaterialAmountPercent( searchStartDate,  searchEndDate);
    }

    @Override
    public List<AnalysisMaterailVO> listMaterialAmountPercentByMaterialType(String searchStartDate, String searchEndDate, String searchField) {
        return repositoryBuyinDocumentMapper.listMaterialAmountPercentByMaterialType( searchStartDate,  searchEndDate,searchField);
    }

    @Override
    public List<ExternalAccountRepositoryBuyinDocument> listGTEndDate(String endDate) {
        return repositoryBuyinDocumentMapper.listGTEndDate(endDate);
    }

    @Override
    public int countBySupplierId(String ids[]) {
        return this.count(new QueryWrapper<ExternalAccountRepositoryBuyinDocument>()
                .in(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_ID_FIELDNAME, ids));
    }

    @Override
    public ExternalAccountRepositoryBuyinDocument getByOrderId(Long id) {
        return this.getOne(new QueryWrapper<ExternalAccountRepositoryBuyinDocument>()
                .eq(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.ORDER_ID_FIELDNAME, id));
    }

    @Override
    public Double countBySupplierIdAndMaterialId(String supplierId, String materialId) {
        Double count = repositoryBuyinDocumentMapper.getSumNumBySupplierIdAndMaterialId(supplierId, materialId);
        count = count == null ? 0D:count;
        return count;
    }

    @Override
    public List<ExternalAccountRepositoryBuyinDocument> countLTByCloseDate(LocalDate closeDate) {
        return this.list(new QueryWrapper<ExternalAccountRepositoryBuyinDocument>()
                .le(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME, closeDate)
                .ne(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDNAME,
                        DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_0));
    }

    @Override
    public List<ExternalAccountRepositoryBuyinDocument> getListFromOrderBetweenDate(LocalDate startDate, LocalDate endDate) {
        return repositoryBuyinDocumentMapper.getListFromOrderBetweenDate(startDate,endDate);
    }
}
