package com.boyi.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
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
import java.util.concurrent.ConcurrentHashMap;

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

    public static final Map<Long,String> locks = new ConcurrentHashMap<>();

    /**
     * 锁单据
     */
    @GetMapping("/lockById")
    @PreAuthorize("hasAuthority('repository:buyOut:list')")
    public ResponseResult lockById(Principal principal,Long id) {
        locks.put(id,principal.getName());
        log.info("锁单据:{}",id);
        return ResponseResult.succ("");
    }
    /**
     * 解锁单据
     */
    @GetMapping("/lockOpenById")
    @PreAuthorize("hasAuthority('repository:buyOut:list')")
    public ResponseResult lockOpenById(Long id) {
        locks.remove(id);
        log.info("解锁单据:{}",id);
        return ResponseResult.succ("");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('repository:buyOut:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {
        String user = locks.get(ids[0]);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        // 入库对应的数量
        List<RepositoryBuyoutDocumentDetail> details = repositoryBuyoutDocumentDetailService.listByDocumentId(ids[0]);
        for (RepositoryBuyoutDocumentDetail detail : details){
            repositoryStockService.addNumByMaterialId(detail.getMaterialId()
                    ,detail.getNum());
        }

        try {


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
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 查询退料
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('repository:buyOut:list')")
    public ResponseResult queryById(Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }

        RepositoryBuyoutDocument repositoryBuyoutDocument = repositoryBuyoutDocumentService.getById(id);

        List<RepositoryBuyoutDocumentDetail> details = repositoryBuyoutDocumentDetailService.listByDocumentId(id);

        BaseSupplier supplier = baseSupplierService.getById(repositoryBuyoutDocument.getSupplierId());

//        Double totalNum = 0D;
//        Double totalAmount = 0D;

        for (RepositoryBuyoutDocumentDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());

            // 查询对应的价目记录
            BaseSupplierMaterial one = baseSupplierMaterialService.getSuccessPrice(supplier.getId(),material.getId(),repositoryBuyoutDocument.getBuyOutDate());

            if(one != null){
                detail.setPrice(one.getPrice());
//                double amount = detail.getPrice() * detail.getNum();
//                detail.setAmount(new BigDecimal(amount).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue());
//                totalAmount += amount;
            }
//            totalNum += detail.getNum();
        }


//        repositoryBuyoutDocument.setTotalNum( totalNum);
//        repositoryBuyoutDocument.setTotalAmount(new BigDecimal(totalAmount).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue());

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
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody RepositoryBuyoutDocument repositoryBuyoutDocument)throws Exception {

        if(repositoryBuyoutDocument.getRowList() ==null || repositoryBuyoutDocument.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        repositoryBuyoutDocument.setUpdated(LocalDateTime.now());
        repositoryBuyoutDocument.setUpdatedUser(principal.getName());
        repositoryBuyoutDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2);

        try {
            boolean validIsClose = validIsClose(repositoryBuyoutDocument.getBuyOutDate());
            if(!validIsClose){
                return ResponseResult.fail("日期请设置在关账日之后.");
            }

            Map<String, Double> needSubMap = new HashMap<>();   // 需要减少库存的内容
            Map<String, Double> needAddMap = new HashMap<>();   // 需要增加库存的内容
            Map<String, Double> notUpdateMap = new HashMap<>();   // 需要增加库存的内容
            // 校验退料数目 (金蝶目前没有判断，因为导入比较麻烦，目前暂时先取消该功能)
            validCompareReturnNum(repositoryBuyoutDocument, needSubMap,needAddMap,notUpdateMap);
            log.info("需要减少的内容:{},需要添加的内容:{},需要修改的内容:{}",needSubMap,needAddMap,notUpdateMap);

            // 校验库存
            repositoryStockService.validStockNum(needSubMap);

            //1. 先删除老的，再插入新的
            boolean flag = repositoryBuyoutDocumentDetailService.removeByDocId(repositoryBuyoutDocument.getId());
            if(flag){
                repositoryBuyoutDocumentService.updateById(repositoryBuyoutDocument);

                for (RepositoryBuyoutDocumentDetail item : repositoryBuyoutDocument.getRowList()){
                    item.setId(null);
                    item.setDocumentId(repositoryBuyoutDocument.getId());
                    item.setSupplierId(repositoryBuyoutDocument.getSupplierId());
                    item.setPriceDate(repositoryBuyoutDocument.getBuyOutDate());
                }

                repositoryBuyoutDocumentDetailService.saveBatch(repositoryBuyoutDocument.getRowList());
                log.info("采购退料模块-更新内容:{}",repositoryBuyoutDocument);
            }else{
                return ResponseResult.fail("操作失败，期间detail删除失败");
            }

            // 4. 减少,添加库存
            repositoryStockService.subNumByMaterialId(needSubMap);
            repositoryStockService.addNumByMaterialIdFromMap(needAddMap);


            return ResponseResult.succ("编辑成功");
        } catch (Exception e) {
            log.error("供应商，更新异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private void validCompareReturnNum(RepositoryBuyoutDocument repositoryBuyoutDocument,
                                       Map<String, Double> needSubMap,
                                       Map<String, Double> needAddMap,
                                       Map<String, Double> notUpdateMap) throws Exception{

        // 判断2. 库存能否修改。
        List<RepositoryBuyoutDocumentDetail> oldDetails = repositoryBuyoutDocumentDetailService.listByDocumentId(repositoryBuyoutDocument.getId());

        // 新的物料数目：
        Map<String, Double> newMap = new HashMap<>();
        for (RepositoryBuyoutDocumentDetail detail : repositoryBuyoutDocument.getRowList()) {
            Double materialNum = newMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            newMap.put(detail.getMaterialId(),materialNum+detail.getNum());
        }

        // 2.  老的物料数目
        Map<String, Double> oldMap = new HashMap<>();
        for (RepositoryBuyoutDocumentDetail detail : oldDetails) {
            Double materialNum = oldMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            oldMap.put(detail.getMaterialId(),materialNum+detail.getNum());
        }
        Set<String> set = new HashSet<>();
        set.addAll(oldMap.keySet());
        set.addAll(newMap.keySet());

        String oldSupplierId = oldDetails.get(0).getSupplierId();
        String newSupplierId = repositoryBuyoutDocument.getSupplierId();

        // 1. 假如供应商没变
        // 1.1 判断物料的数目，能否修改库存
        if(oldSupplierId.equals(newSupplierId)) {

            // 3. 减少之后的该供应商，该物料的入库数目 >= 该供应商，该物料 退料数目
            for (String materialId : set) {
                Double oldNum = oldMap.get(materialId)==null? 0D: oldMap.get(materialId);
                Double newNum = newMap.get(materialId)==null? 0D: newMap.get(materialId);

                // 老的物料里， 数目比 新的物料数目多的,就是要新增库存的，就不需要判断。
                if(oldNum > newNum){
                    needAddMap.put(materialId,oldNum - newNum );//需要新增的数目
                    continue;
                }else if(oldNum < newNum){
                    needSubMap.put(materialId,newNum - oldNum ); // 需要减少的数目
                }else {
                    notUpdateMap.put(materialId,newNum);
                    continue;
                }
              /*  Double pushCount = repositoryBuyinDocumentService.countBySupplierIdAndMaterialId(newSupplierId,materialId);

                // 查询该供应商，该物料退料数目
                Double returnCount = repositoryBuyoutDocumentService.countBySupplierIdAndMaterialId(newSupplierId,materialId);

                double calReturnNum = returnCount + (newNum-oldNum);

                if(pushCount < calReturnNum){
                    throw new Exception("该供应商:"+newSupplierId+",该物料:" +materialId+
                            "(入库数目 :"+pushCount+"将会  < 修改后的退料的数目:"+calReturnNum);

                }*/
            }
        }// 2. 假如供应商变更了
        else {
            // 新供应商新增了退料，需要判断
            for (String materialId : set) {
                Double oldNum = oldMap.get(materialId) == null ? 0D : oldMap.get(materialId);
                Double newNum = newMap.get(materialId) == null ? 0D : newMap.get(materialId);
                // 老的物料里， 数目比 新的物料数目多的,就是要新增库存的，就不需要判断。
                if (oldNum > newNum) {
                    needAddMap.put(materialId, oldNum - newNum);//需要新增的数目
                } else if (oldNum < newNum) {
                    needSubMap.put(materialId, newNum - oldNum); // 需要减少的数目
                } else {
                    notUpdateMap.put(materialId, newNum);
                }

                if (newNum == 0) {
                    continue;
                }
/*
                Double pushCount = repositoryBuyinDocumentService.countBySupplierIdAndMaterialId(newSupplierId, materialId);

                // 查询该供应商，该物料退料数目
                Double returnCount = repositoryBuyoutDocumentService.countBySupplierIdAndMaterialId(newSupplierId, materialId);

                double calReturnNum = returnCount + newNum;

                if (pushCount < calReturnNum) {
                    throw new Exception("更换供应商:" + newSupplierId + ",该物料:" + materialId +
                            "(入库数目 :" + pushCount + "将会  < 修改后的退料的数目:" + calReturnNum);

                }*/
            }
        }

    }

    /**
     * 新增退料
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('repository:buyOut:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody RepositoryBuyoutDocument repositoryBuyoutDocument)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        repositoryBuyoutDocument.setCreated(now);
        repositoryBuyoutDocument.setUpdated(now);
        repositoryBuyoutDocument.setCreatedUser(principal.getName());
        repositoryBuyoutDocument.setUpdatedUser(principal.getName());
        repositoryBuyoutDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_2);

        boolean validIsClose = validIsClose(repositoryBuyoutDocument.getBuyOutDate());
        if(!validIsClose){
            return ResponseResult.fail("日期请设置在关账日之后.");
        }

        String supplierId = repositoryBuyoutDocument.getSupplierId();
        // 2. 得到一个物料，需要减少的数量
        Map<String, Double> subMap = new HashMap<>();// 一个物料，需要减少的数目
        for (RepositoryBuyoutDocumentDetail detail :  repositoryBuyoutDocument.getRowList()) {
            Double materialNum = subMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            subMap.put(detail.getMaterialId(),materialNum+detail.getNum());
        }
        // 3.该供应商，该物料的入库数目 >= 该供应商，该物料 退料数目 (金蝶目前没有判断，因为导入比较麻烦，目前暂时先取消该功能)
/*
        for (Map.Entry<String,Double> entry : subMap.entrySet()) {
            String materialId = entry.getKey();
            Double returnNum = entry.getValue();// 该单据该物料，需要入库的数目

            // 查询该供应商，该物料 总入库数目.
            Double pushCount = repositoryBuyinDocumentService.countBySupplierIdAndMaterialId(supplierId,materialId);

            // 查询该供应商，该物料 退料数目
            Double returnCount = repositoryBuyoutDocumentService.countBySupplierIdAndMaterialId(supplierId,materialId);
            double calNum = returnCount + returnNum;

            if(pushCount < calNum){
                throw new Exception("该供应商:"+supplierId+",该物料:" +materialId+
                        " 入库数目:"+pushCount+ " < (历史退料数目:"+returnCount+" + 当前退料数目:"+returnNum+")="+calNum );
            }
        }*/


        try {

            repositoryBuyoutDocumentService.save(repositoryBuyoutDocument);

            for (RepositoryBuyoutDocumentDetail item : repositoryBuyoutDocument.getRowList()){
                item.setDocumentId(repositoryBuyoutDocument.getId());
                item.setSupplierId(repositoryBuyoutDocument.getSupplierId());
                item.setPriceDate(repositoryBuyoutDocument.getPriceDate());

                item.setPriceDate(repositoryBuyoutDocument.getBuyOutDate());
            }

            repositoryBuyoutDocumentDetailService.saveBatch(repositoryBuyoutDocument.getRowList());

            repositoryStockService.subNumByMaterialId(subMap);

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",repositoryBuyoutDocument.getId());
        } catch (Exception e) {
            log.error("采购退料单，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取采购退料 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('repository:buyOut:export')")
    public void export(HttpServletResponse response, String searchStr, String searchField, String searchStartDate, String searchEndDate,String searchStatus) {
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
        }

        List<Long> searchStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus)){
            String[] split = searchStatus.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }
        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = repositoryBuyoutDocumentService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate,searchStatusList);

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(RepositoryBuyoutDocument.class,1,0).export("id","CGTL",response,fis,pageData.getRecords(),"报表.xlsx",DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    /**
     * 获取采购退料 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('repository:buyOut:list')")
    public ResponseResult list(String searchStr, String searchField, String searchStartDate, String searchEndDate,String searchStatus) {
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
        List<Long> searchStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus)){
            String[] split = searchStatus.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(searchStatusList.size() == 0){
            return ResponseResult.fail("状态不能为空");
        }
        pageData = repositoryBuyoutDocumentService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate,searchStatusList);
        return ResponseResult.succ(pageData);
    }

    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('repository:buyOut:save')")
    public ResponseResult statusSubmit(Principal principal,Long id) {
        RepositoryBuyoutDocument old = repositoryBuyoutDocumentService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_1){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        RepositoryBuyoutDocument repositoryBuyoutDocument = new RepositoryBuyoutDocument();
        repositoryBuyoutDocument.setUpdated(LocalDateTime.now());
        repositoryBuyoutDocument.setUpdatedUser(principal.getName());
        repositoryBuyoutDocument.setId(id);
        repositoryBuyoutDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_2);
        repositoryBuyoutDocumentService.updateById(repositoryBuyoutDocument);
        log.info("仓库模块-撤销内容:{}",repositoryBuyoutDocument);
        return ResponseResult.succ("已撤销");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('repository:buyOut:save')")
    public ResponseResult statusSubReturn(Principal principal,Long id) {
        RepositoryBuyoutDocument old = repositoryBuyoutDocumentService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_2
                &&
                old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_3
        ){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        RepositoryBuyoutDocument repositoryBuyoutDocument = new RepositoryBuyoutDocument();
        repositoryBuyoutDocument.setUpdated(LocalDateTime.now());
        repositoryBuyoutDocument.setUpdatedUser(principal.getName());
        repositoryBuyoutDocument.setId(id);
        repositoryBuyoutDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_1);
        repositoryBuyoutDocumentService.updateById(repositoryBuyoutDocument);
        log.info("仓库模块-撤销内容:{}",repositoryBuyoutDocument);
        return ResponseResult.succ("已撤销");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('repository:buyOut:valid')")
    public ResponseResult statusPass(Principal principal,Long id) throws Exception{
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }

        RepositoryBuyoutDocument old = repositoryBuyoutDocumentService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_2
                && old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_3){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        RepositoryBuyoutDocument repositoryBuyoutDocument = new RepositoryBuyoutDocument();
        repositoryBuyoutDocument.setUpdated(LocalDateTime.now());
        repositoryBuyoutDocument.setUpdatedUser(principal.getName());
        repositoryBuyoutDocument.setId(id);
        repositoryBuyoutDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_0);

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
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }

        RepositoryBuyoutDocument old = repositoryBuyoutDocumentService.getById(id);
        boolean validIsClose = validIsClose(old.getBuyOutDate());
        if(!validIsClose){
            return ResponseResult.fail("日期请设置在关账日之后.");
        }
        if(old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_0){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        RepositoryBuyoutDocument repositoryBuyoutDocument = new RepositoryBuyoutDocument();
        repositoryBuyoutDocument.setUpdated(LocalDateTime.now());
        repositoryBuyoutDocument.setUpdatedUser(principal.getName());
        repositoryBuyoutDocument.setId(id);
        repositoryBuyoutDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_3);
        repositoryBuyoutDocumentService.updateById(repositoryBuyoutDocument);
        log.info("仓库模块-反审核通过内容:{}",repositoryBuyoutDocument);

      /*  // 采购退料反审核之后，要把数量更新

        // 1. 根据单据ID 获取该单据的全部详情信息，
        List<RepositoryBuyoutDocumentDetail> details = repositoryBuyoutDocumentDetailService.listByDocumentId(id);
        // 2. 遍历更新 一个物料对应的库存数量
        for (RepositoryBuyoutDocumentDetail detail: details){
            repositoryStockService.addNumByMaterialId(detail.getMaterialId(),detail.getNum());
        }*/

        return ResponseResult.succ("反审核成功");
    }



}
