package com.boyi.controller;


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
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 仓库模块-采购退料单据表 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Slf4j
@RestController
@RequestMapping("/repository/buyOut")
public class RepositoryBuyoutDocumentController extends BaseController {

    @Value("${poi.repositoryBuyOutDemoPath}")
    private String poiDemoPath;

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('repository:buyOut:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        boolean flag = repositoryBuyoutDocumentService.removeByIds(Arrays.asList(ids));

        log.info("删除采购退料表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("采购退料删除失败");
        }

        boolean flagDetail = repositoryBuyoutDocumentDetailService.delByDocumentIds(ids);
        log.info("删除采购退料表详情信息,document_id:{},是否成功：{}",ids,flagDetail?"成功":"失败");

        if(!flagDetail){
            return ResponseResult.fail("采购退料详情表没有删除成功!");
        }
        return ResponseResult.succ("删除成功");
    }

    /**
     * 查询退料
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('repository:buyOut:list')")
    public ResponseResult queryById(Long id) {
        RepositoryBuyoutDocument repositoryBuyoutDocument = repositoryBuyoutDocumentService.getById(id);

        List<RepositoryBuyoutDocumentDetail> details = repositoryBuyoutDocumentDetailService.listByDocumentId(id);

        BaseSupplier supplier = baseSupplierService.getById(repositoryBuyoutDocument.getSupplierId());

        Double totalNum = 0D;
        Double totalAmount = 0D;

        for (RepositoryBuyoutDocumentDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());

            // 查询对应的价目记录
            BaseSupplierMaterial one = baseSupplierMaterialService.getSuccessPrice(supplier.getId(),material.getId(),repositoryBuyoutDocument.getBuyOutDate());

            if(one != null){
                detail.setPrice(one.getPrice());
                double amount = detail.getPrice() * detail.getNum();
                detail.setAmount(new BigDecimal(amount).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue());
                totalAmount += amount;
            }
            totalNum += detail.getNum();
        }


        repositoryBuyoutDocument.setTotalNum( totalNum);
        repositoryBuyoutDocument.setTotalAmount(new BigDecimal(totalAmount).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue());

        repositoryBuyoutDocument.setSupplierName(supplier.getName());

        repositoryBuyoutDocument.setSupplierName(supplier.getName());
        repositoryBuyoutDocument.setRowList(details);

        return ResponseResult.succ(repositoryBuyoutDocument);
    }

    /**
     * 修改退料
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('repository:buyOut:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody RepositoryBuyoutDocument repositoryBuyoutDocument) {

        if(repositoryBuyoutDocument.getRowList() ==null || repositoryBuyoutDocument.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        repositoryBuyoutDocument.setUpdated(LocalDateTime.now());
        repositoryBuyoutDocument.setUpdatedUser(principal.getName());

        try {

            //1. 先删除老的，再插入新的
            boolean flag = repositoryBuyoutDocumentDetailService.removeByDocId(repositoryBuyoutDocument.getId());
            if(flag){
                repositoryBuyoutDocumentService.updateById(repositoryBuyoutDocument);

                for (RepositoryBuyoutDocumentDetail item : repositoryBuyoutDocument.getRowList()){
                    item.setId(null);
                    item.setDocumentId(repositoryBuyoutDocument.getId());
                    item.setSupplierId(repositoryBuyoutDocument.getSupplierId());
                    item.setPriceDate(repositoryBuyoutDocument.getPriceDate());
                }

                repositoryBuyoutDocumentDetailService.saveBatch(repositoryBuyoutDocument.getRowList());
                log.info("采购退料模块-更新内容:{}",repositoryBuyoutDocument);
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
     * 新增退料
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('repository:buyOut:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody RepositoryBuyoutDocument repositoryBuyoutDocument) {
        LocalDateTime now = LocalDateTime.now();
        repositoryBuyoutDocument.setCreated(now);
        repositoryBuyoutDocument.setUpdated(now);
        repositoryBuyoutDocument.setCreatedUser(principal.getName());
        repositoryBuyoutDocument.setUpdatedUser(principal.getName());
        repositoryBuyoutDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_1);

        try {
            repositoryBuyoutDocumentService.save(repositoryBuyoutDocument);

            for (RepositoryBuyoutDocumentDetail item : repositoryBuyoutDocument.getRowList()){
                item.setDocumentId(repositoryBuyoutDocument.getId());
                item.setSupplierId(repositoryBuyoutDocument.getSupplierId());
                item.setPriceDate(repositoryBuyoutDocument.getPriceDate());
            }

            repositoryBuyoutDocumentDetailService.saveBatch(repositoryBuyoutDocument.getRowList());

            return ResponseResult.succ("新增成功");
        } catch (DuplicateKeyException e) {
            log.error("采购退料单，插入异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    /**
     * 获取采购退料 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('repository:buyOut:export')")
    public void export(HttpServletResponse response, String searchStr, String searchField, String searchStartDate, String searchEndDate) {
        Page<RepositoryBuyoutDocument> pageData = null;
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
        pageData = repositoryBuyoutDocumentService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate);

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(RepositoryBuyoutDocument.class,1,0).export(response,fis,pageData.getRecords(),"报表.xlsx",DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    /**
     * 获取采购退料 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('repository:buyOut:list')")
    public ResponseResult list(String searchStr, String searchField, String searchStartDate, String searchEndDate) {
        Page<RepositoryBuyoutDocument> pageData = null;
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
        pageData = repositoryBuyoutDocumentService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate);
        return ResponseResult.succ(pageData);
    }


    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('repository:buyOut:valid')")
    public ResponseResult statusPass(Principal principal,Long id) throws Exception{

        RepositoryBuyoutDocument repositoryBuyoutDocument = new RepositoryBuyoutDocument();
        repositoryBuyoutDocument.setUpdated(LocalDateTime.now());
        repositoryBuyoutDocument.setUpdatedUser(principal.getName());
        repositoryBuyoutDocument.setId(id);
        repositoryBuyoutDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_0);


        // 采购退料审核通过之后，要把数量更新

        // 1. 根据单据ID 获取该单据的全部详情信息，
        List<RepositoryBuyoutDocumentDetail> details = repositoryBuyoutDocumentDetailService.listByDocumentId(id);

        String supplierId = details.get(0).getSupplierId();
        // 2. 得到一个物料，需要减少的数量
        Map<String, Double> subMap = new HashMap<>();// 一个物料，需要减少的数目
        for (RepositoryBuyoutDocumentDetail detail : details) {
            Double materialNum = subMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            subMap.put(detail.getMaterialId(),materialNum+detail.getNum());
        }
        // 3. 减少之后的该供应商，该物料的审核通过完成的入库数目 >= 该供应商，该物料 审核通过的退料数目

        for (Map.Entry<String,Double> entry : subMap.entrySet()) {
            String materialId = entry.getKey();
            Double needSubNum = entry.getValue();// 该单据该物料，需要反审核进行出库的数目

            // 查询该供应商，该物料 审核通过的，总入库数目.
            Double pushCount = repositoryBuyinDocumentService.countBySupplierIdAndMaterialId(supplierId,materialId);
            pushCount  = pushCount==null?0L:pushCount;
            // 假如反审核通过之后的，剩下的该供应商，该物料的入库数目

            // 查询该供应商，该物料 审核完成的退料数目
            Double returnCount = repositoryBuyoutDocumentService.countBySupplierIdAndMaterialId(supplierId,materialId);
            returnCount  = returnCount==null?0L:returnCount;
            double calNum = returnCount + needSubNum;


            if(pushCount < calNum){
                throw new Exception("该供应商:"+supplierId+",该物料:" +materialId+
                        " 入库审核通过的数目:"+pushCount+ " < (退料审核通过的数目:"+returnCount+" + 退料数目:"+needSubNum+")="+calNum );
            }
        }

        // 4. 减少库存
        repositoryStockService.subNumByMaterialId(subMap);

        repositoryBuyoutDocumentService.updateById(repositoryBuyoutDocument);
        log.info("仓库模块-审核通过内容:{}",repositoryBuyoutDocument);
        return ResponseResult.succ("审核通过");
    }


    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('repository:buyOut:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {


        RepositoryBuyoutDocument repositoryBuyoutDocument = new RepositoryBuyoutDocument();
        repositoryBuyoutDocument.setUpdated(LocalDateTime.now());
        repositoryBuyoutDocument.setUpdatedUser(principal.getName());
        repositoryBuyoutDocument.setId(id);
        repositoryBuyoutDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_1);
        repositoryBuyoutDocumentService.updateById(repositoryBuyoutDocument);
        log.info("仓库模块-反审核通过内容:{}",repositoryBuyoutDocument);

        // 采购退料反审核之后，要把数量更新

        // 1. 根据单据ID 获取该单据的全部详情信息，
        List<RepositoryBuyoutDocumentDetail> details = repositoryBuyoutDocumentDetailService.listByDocumentId(id);
        // 2. 遍历更新 一个物料对应的库存数量
        for (RepositoryBuyoutDocumentDetail detail: details){
            repositoryStockService.addNumByMaterialId(detail.getMaterialId(),detail.getNum());
        }

        return ResponseResult.succ("反审核成功");
    }

}
