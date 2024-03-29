package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.entity.AnalysisMaterailVO;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.BaseSupplierMaterialCopy;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.mapper.RepositoryBuyinDocumentMapper;
import com.boyi.service.RepositoryBuyinDocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
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
 * @since 2021-08-26
 */
@Service
@Slf4j
public class RepositoryBuyinDocumentServiceImpl extends ServiceImpl<RepositoryBuyinDocumentMapper, RepositoryBuyinDocument> implements RepositoryBuyinDocumentService {
    @Autowired
    RepositoryBuyinDocumentMapper repositoryBuyinDocumentMapper;
    public Page<RepositoryBuyinDocument> innerQuery(Page page, QueryWrapper<RepositoryBuyinDocument> eq) {
        return repositoryBuyinDocumentMapper.page(page,eq);
    }
    public Page<RepositoryBuyinDocument> innerQueryZZW(Page page, QueryWrapper<RepositoryBuyinDocument> eq) {
        return repositoryBuyinDocumentMapper.pageZZW(page,eq);
    }

    @Override
    public RepositoryBuyinDocument one(QueryWrapper<RepositoryBuyinDocument> id) {
        return repositoryBuyinDocumentMapper.one(id);
    }

    @Override
    public Integer getSupplierMaterialPassBetweenDate(BaseSupplierMaterial baseSupplierMaterial){
        return repositoryBuyinDocumentMapper.getSupplierMaterialPassBetweenDate(baseSupplierMaterial);
    }

    @Override
    public Integer getSupplierMaterialCopyPassBetweenDate(BaseSupplierMaterialCopy baseSupplierMaterial) {
        return repositoryBuyinDocumentMapper.getSupplierMaterialCopyPassBetweenDate(baseSupplierMaterial);
    }

    @Override
    public int countSupplierOneDocNumExcludSelf(String supplierDocumentNum, String supplierId, Long id) {
        return this.count(new QueryWrapper<RepositoryBuyinDocument>().
                eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_DOCUMENT_NUM_FIELDNAME, supplierDocumentNum)
                .eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_ID_FIELDNAME,supplierId)
                .ne(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.ID_FIELDNAME,id)
        );
    }

    @Override
    public int countSupplierOneDocNum(String supplierDocumentNum, String supplierId) {
        return this.count(new QueryWrapper<RepositoryBuyinDocument>().
                eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_DOCUMENT_NUM_FIELDNAME, supplierDocumentNum)
                .eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_ID_FIELDNAME,supplierId)
        );
    }

    @Override
    public Page<RepositoryBuyinDocument> innerQueryBySearch(Page page,String searchField, String queryField,
                                                            String searchStr, String searchStartDate, String searchEndDate
    ,List<Long> searchStatus
                                                            ) {
        return this.innerQuery(page,
                new QueryWrapper<RepositoryBuyinDocument>().
                        like(StrUtil.isNotBlank(searchStr)
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate),DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate),DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDNAME,searchStatus)
        );
    }

    @Override
    public Page<RepositoryBuyinDocument> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<RepositoryBuyinDocument> queryWrapper = new QueryWrapper<>();
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

                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDNAME,searchStatus)
        );
    }

    @Override
    public Page<RepositoryBuyinDocument> innerQueryZZWByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<RepositoryBuyinDocument> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            if(key.equals("price")){
                String val = otherSearch.get(key);
                if(val.isEmpty()){
                    queryWrapper.isNull("price");
                }else{
                    queryWrapper.eq( !val.equals("null"),key,val);
                }
            }else{
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

        }else{
            queryWrapper.
                    like(StrUtil.isNotBlank(searchStr)  &&!searchStr.equals("null")
                            && StrUtil.isNotBlank(searchField),queryField,searchStr);
        }
        return this.innerQueryZZW(page,
                queryWrapper

                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDNAME,searchStatus)
        );
    }

    @Override
    public Double getAllPageTotalAmount(String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String, String> otherSearch) {

        QueryWrapper<RepositoryBuyinDocument> queryWrapper = new QueryWrapper<>();
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
                .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchStartDate)
                .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchEndDate)
                .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDNAME,searchStatus);
        List<RepositoryBuyinDocument> list = this.repositoryBuyinDocumentMapper.list(queryWrapper);
        Double sumAmount = 0.0D;
        for (RepositoryBuyinDocument one: list
             ) {
            if(one.getAmount()==null || one.getAmount() ==0.0D){
                continue;
            }
            sumAmount = BigDecimalUtil.add(sumAmount,one.getAmount()).doubleValue();
        }
        return sumAmount;
    }



    @Override
    public Double getAllPageTotalAmountZZW(String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<RepositoryBuyinDocument> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            if(key.equals("price")){
                String val = otherSearch.get(key);
                if(val.isEmpty()){
                    queryWrapper.isNull("price");
                }else{
                    queryWrapper.eq( !val.equals("null"),key,val);
                }
            }else{
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

        }else{
            queryWrapper.
                    like(StrUtil.isNotBlank(searchStr)  &&!searchStr.equals("null")
                            && StrUtil.isNotBlank(searchField),queryField,searchStr);
        }
        queryWrapper
                .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchStartDate)
                .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchEndDate)
                .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDNAME,searchStatus);
        List<RepositoryBuyinDocument> list = this.repositoryBuyinDocumentMapper.listZZW(queryWrapper);
        Double sumAmount = 0.0D;
        for (RepositoryBuyinDocument one: list
        ) {
            if(one.getAmount()==null || one.getAmount() ==0.0D){
                continue;
            }
            sumAmount+=one.getAmount();
        }
        return sumAmount;
    }

    @Override
    public RepositoryBuyinDocument getNetInFromOrderBetweenDate(LocalDate startD, LocalDate endD,String materialId) {
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
    public List<RepositoryBuyinDocument> listGTEndDate(String endDate) {
        return repositoryBuyinDocumentMapper.listGTEndDate(endDate);
    }

    @Override
    public List<RepositoryBuyinDocument> getSupplierTotalAmountBetweenDate(LocalDate startDateTime, LocalDate endDateTime) {
        return repositoryBuyinDocumentMapper.getSupplierTotalAmountBetweenDate(startDateTime,endDateTime);
    }

    @Override
    public List<RepositoryBuyinDocument> getMaterialTotalAmountByShoeLast() {
        return repositoryBuyinDocumentMapper.getMaterialTotalAmountByShoeLast();
    }

    @Override
    public int countBySupplierId(String ids[]) {
        return this.count(new QueryWrapper<RepositoryBuyinDocument>()
                .in(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_ID_FIELDNAME, ids));
    }

    @Override
    public RepositoryBuyinDocument getByOrderId(Long id) {
        return this.getOne(new QueryWrapper<RepositoryBuyinDocument>()
                .eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.ORDER_ID_FIELDNAME, id));
    }

    @Override
    public Double countBySupplierIdAndMaterialId(String supplierId, String materialId) {
        Double count = repositoryBuyinDocumentMapper.getSumNumBySupplierIdAndMaterialId(supplierId, materialId);
        count = count == null ? 0D:count;
        return count;
    }

    @Override
    public List<RepositoryBuyinDocument> countLTByCloseDate(LocalDate closeDate) {
        return this.list(new QueryWrapper<RepositoryBuyinDocument>()
                .le(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME, closeDate)
                .ne(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDNAME,
                        DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_0));
    }

    @Override
    public List<RepositoryBuyinDocument> getListFromOrderBetweenDate(LocalDate startDate, LocalDate endDate) {
        return repositoryBuyinDocumentMapper.getListFromOrderBetweenDate(startDate,endDate);
    }

}
