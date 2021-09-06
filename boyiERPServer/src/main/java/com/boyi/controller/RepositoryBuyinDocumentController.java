package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 仓库模块-采购入库单据表 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Slf4j
@RestController
@RequestMapping("/repository/buyIn")
public class RepositoryBuyinDocumentController extends BaseController {

    @Value("${poi.repositoryBuyInDemoPath}")
    private String poiDemoPath;

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('repository:buyIn:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        boolean flag = repositoryBuyinDocumentService.removeByIds(Arrays.asList(ids));

        log.info("删除采购入库表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("采购入库删除失败");
        }

        boolean flagDetail = repositoryBuyinDocumentDetailService.delByDocumentIds(ids);
        log.info("删除采购入库表详情信息,document_id:{},是否成功：{}",ids,flagDetail?"成功":"失败");

        if(!flagDetail){
            return ResponseResult.fail("采购入库详情表没有删除成功!");
        }
        return ResponseResult.succ("删除成功");
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('repository:buyIn:list')")
    public ResponseResult queryById(Long id) {
        RepositoryBuyinDocument repositoryBuyinDocument = repositoryBuyinDocumentService.getById(id);

        List<RepositoryBuyinDocumentDetail> details = repositoryBuyinDocumentDetailService.listByDocumentId(id);

        BaseSupplier supplier = baseSupplierService.getById(repositoryBuyinDocument.getSupplierId());

        Double totalNum = 0D;
        Double totalAmount = 0D;

        for (RepositoryBuyinDocumentDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());


            // 查询对应的价目记录
            BaseSupplierMaterial one = baseSupplierMaterialService.getSuccessPrice(supplier.getId(),material.getId(),repositoryBuyinDocument.getBuyInDate());

            if(one != null){
                detail.setPrice(one.getPrice());
                double amount = detail.getPrice() * detail.getNum();
                detail.setAmount(new BigDecimal(amount).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue());
                totalAmount += amount;
            }

            totalNum += detail.getNum();
        }


        repositoryBuyinDocument.setTotalNum( totalNum);
        repositoryBuyinDocument.setTotalAmount(new BigDecimal(totalAmount).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue());

        repositoryBuyinDocument.setSupplierName(supplier.getName());

        repositoryBuyinDocument.setRowList(details);
        return ResponseResult.succ(repositoryBuyinDocument);
    }



    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('repository:buyIn:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody RepositoryBuyinDocument repositoryBuyinDocument) {

        if(repositoryBuyinDocument.getRowList() ==null || repositoryBuyinDocument.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setPriceDate(repositoryBuyinDocument.getBuyInDate());
        // 普通采购入库，priceDate = buyInDate
        if(repositoryBuyinDocument.getPriceDate()==null){
            repositoryBuyinDocument.setPriceDate(repositoryBuyinDocument.getBuyInDate());
        }

        try {

            // 1. 先查询该供应商，该单号是否已经有记录，有则不能插入
            int exitCount = repositoryBuyinDocumentService.countSupplierOneDocNumExcludSelf(
                    repositoryBuyinDocument.getSupplierDocumentNum(),
                    repositoryBuyinDocument.getSupplierId(),
                    repositoryBuyinDocument.getId());

            if(exitCount > 0){
                return ResponseResult.fail("该供应商的单据已经存在！请确认信息!");
            }

            //2. 先删除老的，再插入新的
            boolean flag = repositoryBuyinDocumentDetailService.removeByDocId(repositoryBuyinDocument.getId());
            if(flag){
                repositoryBuyinDocumentService.updateById(repositoryBuyinDocument);

                for (RepositoryBuyinDocumentDetail item : repositoryBuyinDocument.getRowList()){
                    item.setId(null);
                    item.setDocumentId(repositoryBuyinDocument.getId());
                    item.setSupplierId(repositoryBuyinDocument.getSupplierId());
                }

                repositoryBuyinDocumentDetailService.saveBatch(repositoryBuyinDocument.getRowList());
                log.info("采购入库模块-更新内容:{}",repositoryBuyinDocument);
            }else{
                return ResponseResult.fail("操作失败，期间detail删除失败");
            }

            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("供应商，更新异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }


    /**
     * 新增入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('repository:buyIn:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody RepositoryBuyinDocument repositoryBuyinDocument) {
        LocalDateTime now = LocalDateTime.now();
        repositoryBuyinDocument.setCreated(now);
        repositoryBuyinDocument.setUpdated(now);
        repositoryBuyinDocument.setCreatedUser(principal.getName());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_1);
        // 普通采购入库，priceDate = buyInDate
        if(repositoryBuyinDocument.getPriceDate()==null){
            repositoryBuyinDocument.setPriceDate(repositoryBuyinDocument.getBuyInDate());
        }
        try {
            // 1. 先查询该供应商，该单号是否已经有记录，有则不能插入
            int exitCount = repositoryBuyinDocumentService.countSupplierOneDocNum(
                    repositoryBuyinDocument.getSupplierDocumentNum(),
                    repositoryBuyinDocument.getSupplierId());
            if(exitCount > 0){
                return ResponseResult.fail("该供应商的单据已经存在！请确认信息!");
            }

            repositoryBuyinDocumentService.save(repositoryBuyinDocument);

            for (RepositoryBuyinDocumentDetail item : repositoryBuyinDocument.getRowList()){
                item.setDocumentId(repositoryBuyinDocument.getId());
                item.setSupplierId(repositoryBuyinDocument.getSupplierId());
            }

            repositoryBuyinDocumentDetailService.saveBatch(repositoryBuyinDocument.getRowList());

            return ResponseResult.succ("新增成功");
        } catch (DuplicateKeyException e) {
            log.error("采购入库单，插入异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    /**
     * 获取采购入库 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('repository:buyIn:export')")
    public void export(HttpServletResponse response, String searchStr, String searchField, String searchStartDate, String searchEndDate) {
        Page<RepositoryBuyinDocument> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            }
            else if (searchField.equals("materialName")) {
                queryField = "material_name";

            }else if (searchField.equals("id")) {
                queryField = "id";

            } else {
            }
        }else {

        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = repositoryBuyinDocumentService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate);

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(RepositoryBuyinDocument.class,1,0).export(response,fis,pageData.getRecords(),"报表.xlsx",DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    /**
     * 获取采购入库 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('repository:buyIn:list')")
    public ResponseResult list(String searchStr, String searchField, String searchStartDate, String searchEndDate) {
        Page<RepositoryBuyinDocument> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            }
            else if (searchField.equals("materialName")) {
                queryField = "material_name";

            }else if (searchField.equals("id")) {
                queryField = "id";

            } else {
                return ResponseResult.fail("搜索字段不存在");
            }
        }else {

        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = repositoryBuyinDocumentService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate);
        return ResponseResult.succ(pageData);
    }


    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('repository:buyIn:valid')")
    public ResponseResult statusPass(Principal principal,Long id) {

        RepositoryBuyinDocument repositoryBuyinDocument = new RepositoryBuyinDocument();
        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setId(id);
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_0);
        repositoryBuyinDocumentService.updateById(repositoryBuyinDocument);
        log.info("仓库模块-审核通过内容:{}",repositoryBuyinDocument);

        // 采购入库审核通过之后，要把数量更新

        // 1. 根据单据ID 获取该单据的全部详情信息，
        List<RepositoryBuyinDocumentDetail> details = repositoryBuyinDocumentDetailService.listByDocumentId(id);
        // 2. 遍历更新 ，一个物料对应的库存数量
        for (RepositoryBuyinDocumentDetail detail : details){
            repositoryStockService.addNumBySupplierIdAndMaterialId(detail.getMaterialId()
                    ,detail.getNum());
        }

        return ResponseResult.succ("审核通过");
    }


    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('repository:buyIn:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {


        RepositoryBuyinDocument repositoryBuyinDocument = new RepositoryBuyinDocument();
        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setId(id);
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_1);
        repositoryBuyinDocumentService.updateById(repositoryBuyinDocument);
        log.info("仓库模块-反审核通过内容:{}",repositoryBuyinDocument);

        // 采购入库反审核之后，要把数量更新

        // 1. 根据单据ID 获取该单据的全部详情信息，
        List<RepositoryBuyinDocumentDetail> details = repositoryBuyinDocumentDetailService.listByDocumentId(id);
        // 2. 遍历更新 一个供应商，一个物料对应的库存数量
        repositoryStockService.subNumByMaterialId(details);

        return ResponseResult.succ("反审核成功");
    }

}
