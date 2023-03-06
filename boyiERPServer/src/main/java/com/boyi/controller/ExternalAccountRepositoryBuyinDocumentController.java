package com.boyi.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.EmailUtils;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.common.utils.ThreadUtils;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * <p>
 * 仓库模块-采购入库单据表 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@RestController
@RequestMapping("/externalAccount/repository/buyIn")
@Slf4j
public class ExternalAccountRepositoryBuyinDocumentController extends BaseController {

    @Value("${poi.eaRepositoryBuyInDemoPath}")
    private String poiDemoPath;

    public static final Map<Long,String> locks = new ConcurrentHashMap<Long,String>();


    /**
     *  获取选中的批量打印的数据
     * @param principal
     * @param ids
     * @return
     */
    @PostMapping("/getBatchPrintByIds")
    public ResponseResult getBatchPrintByIds(Principal principal, @RequestBody Long[] ids) {
        ArrayList<ExternalAccountRepositoryBuyinDocument> lists = new ArrayList<>();

        for (Long id : ids){

            ExternalAccountRepositoryBuyinDocument repositoryBuyinDocument = externalAccountRepositoryBuyinDocumentService.getById(id);

            List<ExternalAccountRepositoryBuyinDocumentDetail> details = externalAccountRepositoryBuyinDocumentDetailService.listByDocumentId(id);

            ExternalAccountBaseSupplier supplier = externalAccountBaseSupplierService.getById(repositoryBuyinDocument.getSupplierId());

            double totalNum = 0d;
            double totalAmount = 0.0d;

            for (ExternalAccountRepositoryBuyinDocumentDetail detail : details){
                ExternalAccountBaseMaterial material = externalAccountBaseMaterialService.getById(detail.getMaterialId());
                detail.setMaterialName(material.getName());
                detail.setUnit(material.getUnit());
                detail.setBigUnit(material.getBigUnit());
                detail.setUnitRadio(material.getUnitRadio());
                detail.setSpecs(material.getSpecs());

                // 查询对应的价目记录
                ExternalAccountBaseSupplierMaterial one = externalAccountBaseSupplierMaterialService.getSuccessPrice(supplier.getId(),material.getId(),detail.getPriceDate());


                if(one !=null){
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
            lists.add(repositoryBuyinDocument);
        }
        return ResponseResult.succ(lists);

    }

    /**
     * 锁单据
     */
    @GetMapping("/lockById")
    @PreAuthorize("hasAuthority('externalAccount:repository:buyIn:list')")
    public ResponseResult lockById(Principal principal,Long id) {
        locks.put(id,principal.getName());
        log.info("锁单据:{}",id);
        return ResponseResult.succ("");
    }
    /**
     * 解锁单据
     */
    @GetMapping("/lockOpenById")
    @PreAuthorize("hasAuthority('externalAccount:repository:buyIn:list')")
    public ResponseResult lockOpenById(Long id) {
        locks.remove(id);
        log.info("解锁单据:{}",id);
        return ResponseResult.succ("");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('externalAccount:repository:buyIn:list')")
    public ResponseResult delete(@RequestBody Long[] ids)throws Exception {
        String user = locks.get(ids[0]);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }

        List<ExternalAccountRepositoryBuyinDocumentDetail> details = externalAccountRepositoryBuyinDocumentDetailService.listByDocumentId(ids[0]);
        // 1. 根据单据ID 获取该单据的全部详情信息，
        String supplierId = details.get(0).getSupplierId();
        // 2. 得到一个物料，需要减少的数量
        Map<String, Double> subMap = new HashMap<>();// 一个物料，需要减少的数目
        for (ExternalAccountRepositoryBuyinDocumentDetail detail : details) {
            Double materialNum = subMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            subMap.put(detail.getMaterialId(),BigDecimalUtil.add(materialNum,detail.getRadioNum()).doubleValue());
        }

        // 校验库存
        externalAccountRepositoryStockService.validStockNum(subMap);

        try {


            // 4. 减少库存
            externalAccountRepositoryStockService.subNumByMaterialId(subMap);

            boolean flag = externalAccountRepositoryBuyinDocumentService.removeByIds(Arrays.asList(ids));

            log.info("删除采购入库表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("采购入库删除失败");
            }


            boolean flagDetail = externalAccountRepositoryBuyinDocumentDetailService.delByDocumentIds(ids);
            log.info("删除采购入库表详情信息,document_id:{},是否成功：{}",ids,flagDetail?"成功":"失败");

            if(!flagDetail){
                return ResponseResult.fail("采购入库详情表没有删除成功!");
            }
            return ResponseResult.succ("删除成功");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('externalAccount:repository:buyIn:list')")
    public ResponseResult queryById(Principal principal,Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        ExternalAccountRepositoryBuyinDocument repositoryBuyinDocument = externalAccountRepositoryBuyinDocumentService.getById(id);

        List<ExternalAccountRepositoryBuyinDocumentDetail> details = externalAccountRepositoryBuyinDocumentDetailService.listByDocumentId(id);

        ExternalAccountBaseSupplier supplier = externalAccountBaseSupplierService.getById(repositoryBuyinDocument.getSupplierId());

        for (ExternalAccountRepositoryBuyinDocumentDetail detail : details){
            ExternalAccountBaseMaterial material = externalAccountBaseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setBigUnit(material.getBigUnit());
            detail.setUnitRadio(material.getUnitRadio());
            detail.setSpecs(material.getSpecs());

            ExternalAccountBaseSupplierMaterial one = null;
            // 查询对应的价目记录
            one = externalAccountBaseSupplierMaterialService.getSuccessPrice(supplier.getId(),material.getId(),detail.getPriceDate());
            if(one!=null){
                detail.setPrice(one.getPrice());
            }

        }

        repositoryBuyinDocument.setSupplierName(supplier.getName());

        repositoryBuyinDocument.setRowList(details);
        return ResponseResult.succ(repositoryBuyinDocument);
    }

    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('externalAccount:repository:buyIn:list')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody ExternalAccountRepositoryBuyinDocument repositoryBuyinDocument)throws Exception {

        if(repositoryBuyinDocument.getRowList() ==null || repositoryBuyinDocument.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2);
        try {

            // 采购入库来源的处理:
            if(repositoryBuyinDocument.getSourceType() == DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.SOURCE_TYPE_FIELDVALUE_0){

                // 校验单据
                ResponseResult result = validExistSupplierDocNumExcludeSelf(repositoryBuyinDocument);
                if(result != null){
                    return result;
                }

                Map<String, Double> needSubMap = new HashMap<>(); // 需要减少的入库
                Map<String, Double> needAddMap = new HashMap<>(); //  需要增加的入库
                Map<String, Double> notUpdateMap = new HashMap<>();  // 不需要变更的入库

                // 校验退料数目
                validCompareReturnNum(repositoryBuyinDocument, needSubMap,needAddMap,notUpdateMap);
                log.info("需要减少的内容:{},需要添加的内容:{},需要修改的内容:{}",needSubMap,needAddMap,notUpdateMap);

                // 校验库存能否减少
                externalAccountRepositoryStockService.validStockNum(needSubMap);

                //2. 先删除老的，再插入新的
                externalAccountRepositoryBuyinDocumentDetailService.removeByDocId(repositoryBuyinDocument.getId());
                externalAccountRepositoryBuyinDocumentService.updateById(repositoryBuyinDocument);

                for (ExternalAccountRepositoryBuyinDocumentDetail item : repositoryBuyinDocument.getRowList()){
                    item.setId(null);
                    item.setDocumentId(repositoryBuyinDocument.getId());
                    item.setSupplierId(repositoryBuyinDocument.getSupplierId());
                    item.setPriceDate(repositoryBuyinDocument.getBuyInDate());
                    item.setRadioNum(item.getNum() * item.getUnitRadio());
                }

                externalAccountRepositoryBuyinDocumentDetailService.saveBatch(repositoryBuyinDocument.getRowList());

                // 4. 减少,添加库存
                externalAccountRepositoryStockService.subNumByMaterialId(needSubMap);
                externalAccountRepositoryStockService.addNumByMaterialIdFromMap(needAddMap);

                log.info("采购入库模块-更新内容:{}",repositoryBuyinDocument);
            }

            return ResponseResult.succ("编辑成功");
        } catch (Exception e) {
            log.error("供应商，更新异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     *  和采购退料进行校验
     * @param repositoryBuyinDocument
     * @return
     */
    private void validCompareReturnNum(ExternalAccountRepositoryBuyinDocument repositoryBuyinDocument,
                                       Map<String, Double> needSubMap,
                                       Map<String, Double> needAddMap,
                                       Map<String, Double> notUpdateMap
    )throws Exception {

        List<ExternalAccountRepositoryBuyinDocumentDetail> oldDetails = externalAccountRepositoryBuyinDocumentDetailService.listByDocumentId(repositoryBuyinDocument.getId());

        // 新的物料数目：
        Map<String, Double> newMap = new HashMap<>();
        for (ExternalAccountRepositoryBuyinDocumentDetail detail : repositoryBuyinDocument.getRowList()) {
            Double materialNum = newMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            newMap.put(detail.getMaterialId(),BigDecimalUtil.add(materialNum,detail.getNum() * detail.getUnitRadio()).doubleValue());
        }

        // 2.  老的物料数目
        Map<String, Double> oldMap = new HashMap<>();
        for (ExternalAccountRepositoryBuyinDocumentDetail detail : oldDetails) {
            Double materialNum = oldMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            oldMap.put(detail.getMaterialId(),BigDecimalUtil.add(materialNum,detail.getRadioNum()).doubleValue());
        }

        Set<String> set = new HashSet<>();
        set.addAll(oldMap.keySet());
        set.addAll(newMap.keySet());

        String oldSupplierId = oldDetails.get(0).getSupplierId();
        String newSupplierId = repositoryBuyinDocument.getSupplierId();

        // 1. 假如供应商没变
        // 1.1 判断物料的数目，能否修改库存
        if(oldSupplierId.equals(newSupplierId)){

            // 3. 减少之后的该供应商，该物料的入库数目 >= 该供应商，该物料 退料数目
            for (String materialId: set) {
                Double oldNum = oldMap.get(materialId) == null ? 0D:oldMap.get(materialId);
                Double newNum = newMap.get(materialId) == null? 0D: newMap.get(materialId);

                if(oldNum < newNum){
                    needAddMap.put(materialId,BigDecimalUtil.sub(newNum,oldNum).doubleValue());//需要新增的数目
                    continue;
                }else if(oldNum > newNum){
                    needSubMap.put(materialId,BigDecimalUtil.sub(oldNum,newNum).doubleValue()); // 需要减少的数目
                }else {
                    notUpdateMap.put(materialId,newNum);
                    continue;
                }

                // 查询历史该供应商，该物料 总入库数目. (金蝶目前没有判断，因为导入比较麻烦，目前暂时先取消该功能)
                /*Double pushCount = externalAccountRepositoryBuyinDocumentService.countBySupplierIdAndMaterialId(newSupplierId,materialId);

                // 剩下的该供应商，该物料的入库数目
                double calNum = pushCount - (oldNum-newNum); // 剩下的入库数目

                // 查询该供应商，该物料退料数目
                Double returnCount = repositoryBuyoutDocumentService.countBySupplierIdAndMaterialId(newSupplierId,materialId);

                // 判断 该供应商该物料的入库数目和退料数目。
                if(calNum < returnCount){
                    throw new Exception("该供应商:"+newSupplierId+",该物料:" +materialId+
                            "(修改后的入库数目 :"+calNum+"将会  < 退料的数目:"+returnCount);
                }*/
            }
        }
        // 2. 假如供应商变更了
        // 2.1 要减少老供应商的入库数量，判断能否减少
        else {
            // 查询旧该供应商，该物料 总入库数目.
            for (String materialId : set){
                Double oldNum = oldMap.get(materialId)==null? 0D: oldMap.get(materialId);

                Double newNum = newMap.get(materialId)==null? 0D: newMap.get(materialId);

                // 老的物料里， 数目比 新的物料数目少的,就是要新增库存的，就不需要判断。
                if(oldNum < newNum){
                    needAddMap.put(materialId,BigDecimalUtil.sub(newNum,oldNum).doubleValue());//需要新增的数目
                }else if(oldNum > newNum){
                    needSubMap.put(materialId,BigDecimalUtil.sub(oldNum,newNum).doubleValue()); // 需要减少的数目
                }else {
                    notUpdateMap.put(materialId,newNum);
                }
                // 新的入库，老的入库不存在，
                if(oldNum == 0){
                    continue;
                }

                /*Double pushCount = externalAccountRepositoryBuyinDocumentService.countBySupplierIdAndMaterialId(oldSupplierId,materialId);

                double calNum = pushCount - oldNum; // 剩下的入库数目

                // 查询该供应商，该物料退料数目
                Double returnCount = repositoryBuyoutDocumentService.countBySupplierIdAndMaterialId(oldSupplierId,materialId);

                // 和退料比较，判断能否减少
                if(calNum < returnCount){
                    throw new Exception("变更前的供应商:"+oldSupplierId+",该物料:" +materialId+
                            "(变更供应商后的入库数目 :"+calNum+"将会  < 退料的数目:"+returnCount);
                }*/
            }
        }
    }

    // 判断1. 先查询该供应商，该单号是否已经有记录，有则不能插入
    private ResponseResult validExistSupplierDocNumExcludeSelf(ExternalAccountRepositoryBuyinDocument repositoryBuyinDocument) {
        int exitCount = externalAccountRepositoryBuyinDocumentService.countSupplierOneDocNumExcludSelf(
                repositoryBuyinDocument.getSupplierDocumentNum(),
                repositoryBuyinDocument.getSupplierId(),
                repositoryBuyinDocument.getId());

        if(exitCount > 0){
            return ResponseResult.fail("该供应商的单据已经存在！请确认信息!");
        }
        return null;
    }
    private ResponseResult validExistSupplierDocNum(ExternalAccountRepositoryBuyinDocument repositoryBuyinDocument) {
        // 1. 先查询该供应商，该单号是否已经有记录，有则不能插入
        int exitCount = externalAccountRepositoryBuyinDocumentService.countSupplierOneDocNum(
                repositoryBuyinDocument.getSupplierDocumentNum(),
                repositoryBuyinDocument.getSupplierId());
        if(exitCount > 0){
            return ResponseResult.fail("该供应商的单据已经存在！请确认信息!");
        }
        return null;
    }

    /**
     * 新增入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('externalAccount:repository:buyIn:list')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody ExternalAccountRepositoryBuyinDocument repositoryBuyinDocument) {

        LocalDateTime now = LocalDateTime.now();
        repositoryBuyinDocument.setCreated(now);
        repositoryBuyinDocument.setUpdated(now);
        repositoryBuyinDocument.setCreatedUser(principal.getName());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2);
        repositoryBuyinDocument.setSourceType(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.SOURCE_TYPE_FIELDVALUE_0);
        try {

            ResponseResult responseResult = validExistSupplierDocNum(repositoryBuyinDocument);
            if(responseResult != null){
                return responseResult;
            }
            externalAccountRepositoryBuyinDocumentService.save(repositoryBuyinDocument);
            StringBuilder sb = new StringBuilder();

            for (ExternalAccountRepositoryBuyinDocumentDetail item : repositoryBuyinDocument.getRowList()){
                item.setDocumentId(repositoryBuyinDocument.getId());
                item.setSupplierId(repositoryBuyinDocument.getSupplierId());

                // 普通采购入库，priceDate = buyInDate
                item.setPriceDate(repositoryBuyinDocument.getBuyInDate());

                item.setRadioNum(item.getUnitRadio() * item.getNum());// 系数换算

                String materialId = item.getMaterialId();
                if(materialId.startsWith("01.01") || materialId.startsWith("01.02")){
                    sb.append("物料["+item.getMaterialId()+","+item.getMaterialName()+"]单价:"+item.getPrice()+".入库数量:"+item.getRadioNum()).append("<br>");
                }
            }

            externalAccountRepositoryBuyinDocumentDetailService.saveBatch(repositoryBuyinDocument.getRowList());

            //  遍历更新 ，一个物料对应的库存数量
            for (ExternalAccountRepositoryBuyinDocumentDetail detail : repositoryBuyinDocument.getRowList()){
                externalAccountRepositoryStockService.addNumByMaterialId(detail.getMaterialId()
                        ,detail.getRadioNum());

            }

            HashMap<String, Object> returnMap = new HashMap<>();
            returnMap.put("id",repositoryBuyinDocument.getId());
            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",repositoryBuyinDocument.getId());
        } catch (DuplicateKeyException e) {
            log.error("采购入库单，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取采购入库 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('externalAccount:repository:buyIn:list')")
    public void export(HttpServletResponse response, String searchField, String searchStartDate, String searchEndDate, String searchStatus,
                       @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        Page<ExternalAccountRepositoryBuyinDocument> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            }
            else if (searchField.equals("materialName")) {
                queryField = "material_name";

            }
            else if (searchField.equals("supplierDocNum")) {
                queryField = "supplier_document_num";

            }
            else if (searchField.equals("id")) {
                queryField = "id";

            }
        }

        Map<String, String> queryMap = new HashMap<>();
        if(manySearchArr!=null && manySearchArr.size() > 0){
            for (int i = 0; i < manySearchArr.size(); i++) {
                Map<String, String> theOneSearch = manySearchArr.get(i);
                String oneField = theOneSearch.get("selectField");
                String oneStr = theOneSearch.get("searchStr");
                String theQueryField = null;
                if (StringUtils.isNotBlank(oneField)) {
                    if (oneField.equals("supplierName")) {
                        theQueryField = "supplier_name";
                    }
                    else if (oneField.equals("materialName")) {
                        theQueryField = "material_name";

                    }else if (oneField.equals("id")) {
                        theQueryField = "id";
                    }
                    else if (oneField.equals("supplierDocNum")) {
                        theQueryField = "supplier_document_num";

                    }
                    else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        List<Long> searchStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus)){
            String[] split = searchStatus.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        Page page = getPage();
        if(page.getSize()==10 && page.getCurrent() == 1){
            page.setSize(1000000L); // 导出全部的话，简单改就一页很大一个条数
        }
        pageData = externalAccountRepositoryBuyinDocumentService.innerQueryByManySearch(page,searchField,queryField,searchStr,searchStartDate,searchEndDate,searchStatusList,queryMap);



        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(ExternalAccountRepositoryBuyinDocument.class,1,0).export("id","CGRK",response,fis,pageData.getRecords(),"报表.xlsx",DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    /**
     * 获取采购入库 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('externalAccount:repository:buyIn:list')")
    public ResponseResult list(Principal principal,String searchField, String searchStartDate, String searchEndDate,
                               String searchStatus,@RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        Page<ExternalAccountRepositoryBuyinDocument> pageData = null;
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

            }
            else if (searchField.equals("supplierDocNum")) {
                queryField = "supplier_document_num";

            }
            else if (searchField.equals("price")) {
                queryField = "price";

            }
            else if (searchField.equals("orderSeq")) {
                queryField = "order_seq";

            }
            else {
                return ResponseResult.fail("搜索字段不存在");
            }
        }

        Map<String, String> queryMap = new HashMap<>();
        if(manySearchArr!=null && manySearchArr.size() > 0){
            for (int i = 0; i < manySearchArr.size(); i++) {
                Map<String, String> theOneSearch = manySearchArr.get(i);
                String oneField = theOneSearch.get("selectField");
                String oneStr = theOneSearch.get("searchStr");
                String theQueryField = null;
                if (StringUtils.isNotBlank(oneField)) {
                    if (oneField.equals("supplierName")) {
                        theQueryField = "supplier_name";
                    }
                    else if (oneField.equals("materialName")) {
                        theQueryField = "material_name";

                    }else if (oneField.equals("id")) {
                        theQueryField = "id";
                    }
                    else if (oneField.equals("supplierDocNum")) {
                        theQueryField = "supplier_document_num";

                    }
                    else if (oneField.equals("price")) {
                        theQueryField = "price";

                    }
                    else if (oneField.equals("orderSeq")) {
                        theQueryField = "order_seq";

                    }
                    else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
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

        final Map<String, Double> valMap = new HashMap<>();
        final String queryFieldTmp = queryField;
        Future<?> submit = ThreadUtils.executorService.submit(new Runnable() {
            @Override
            public void run() {
                long start2 = System.currentTimeMillis();
                Double allPageTotalAmount = externalAccountRepositoryBuyinDocumentService.getAllPageTotalAmount(searchField, queryFieldTmp, searchStr, searchStartDate, searchEndDate, searchStatusList, queryMap);
                long start3 = System.currentTimeMillis();
                log.info("入库list查询,allPageTotalAmount 耗时:{}",(start3-start2)+"ms");
                double value = new BigDecimal(allPageTotalAmount).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                valMap.put("value",value);

            }
        });
        long start = System.currentTimeMillis();
        pageData = externalAccountRepositoryBuyinDocumentService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate,searchStatusList,queryMap);
        long start2 = System.currentTimeMillis();
        log.info("入库list查询,page 耗时:{}",(start2-start)+"ms");

        try {
            submit.get();
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseResult.succ(ResponseResult.SUCCESS_CODE,valMap.get("value")+"",pageData);

    }


    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('externalAccount:repository:buyIn:list')")
    public ResponseResult statusSubmit(Principal principal,Long id) {

        ExternalAccountRepositoryBuyinDocument old = externalAccountRepositoryBuyinDocumentService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_1){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        ExternalAccountRepositoryBuyinDocument repositoryBuyinDocument = new ExternalAccountRepositoryBuyinDocument();
        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setId(id);
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2);
        externalAccountRepositoryBuyinDocumentService.updateById(repositoryBuyinDocument);
        log.info("仓库模块-审核中内容:{}",repositoryBuyinDocument);
        return ResponseResult.succ("已提交");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('externalAccount:repository:buyIn:list')")
    public ResponseResult statusSubReturn(Principal principal,Long id) {
        ExternalAccountRepositoryBuyinDocument old = externalAccountRepositoryBuyinDocumentService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2 &&
                old.getStatus()!=DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_3
        ){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        ExternalAccountRepositoryBuyinDocument repositoryBuyinDocument = new ExternalAccountRepositoryBuyinDocument();
        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setId(id);
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_1);
        externalAccountRepositoryBuyinDocumentService.updateById(repositoryBuyinDocument);
        log.info("仓库模块-撤销内容:{}",repositoryBuyinDocument);
        return ResponseResult.succ("已撤销");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('externalAccount:repository:buyIn:list')")
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<ExternalAccountRepositoryBuyinDocument> lists = new ArrayList<>();

        for (Long id : ids){
            String user = locks.get(id);
            if(StringUtils.isNotBlank(user)){
                return ResponseResult.fail("单据被["+user+"]占用");
            }

            ExternalAccountRepositoryBuyinDocument old = externalAccountRepositoryBuyinDocumentService.getById(id);
            if(old.getStatus()!=DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2
                    && old.getStatus()!=DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_3){
                return ResponseResult.fail("单据编号:"+id+"状态不正确，无法审核通过");
            }

            ExternalAccountRepositoryBuyinDocument repositoryBuyinDocument = new ExternalAccountRepositoryBuyinDocument();
            repositoryBuyinDocument.setUpdated(LocalDateTime.now());
            repositoryBuyinDocument.setUpdatedUser(principal.getName());
            repositoryBuyinDocument.setId(id);
            repositoryBuyinDocument.setStatus(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_0);
            lists.add(repositoryBuyinDocument);

        }
        externalAccountRepositoryBuyinDocumentService.updateBatchById(lists);
        return ResponseResult.succ("审核通过");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('externalAccount:repository:buyIn:list')")
    public ResponseResult statusPass(Principal principal,Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }

        ExternalAccountRepositoryBuyinDocument old = externalAccountRepositoryBuyinDocumentService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2
                && old.getStatus()!=DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_3){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        ExternalAccountRepositoryBuyinDocument repositoryBuyinDocument = new ExternalAccountRepositoryBuyinDocument();
        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setId(id);
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_0);
        externalAccountRepositoryBuyinDocumentService.updateById(repositoryBuyinDocument);
        log.info("仓库模块-审核通过内容:{}",repositoryBuyinDocument);

        return ResponseResult.succ("审核通过");
    }


    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('externalAccount:repository:buyIn:list')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        ExternalAccountRepositoryBuyinDocument old = externalAccountRepositoryBuyinDocumentService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_0){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        ExternalAccountRepositoryBuyinDocument repositoryBuyinDocument = new ExternalAccountRepositoryBuyinDocument();
        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setId(id);
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_3);

        externalAccountRepositoryBuyinDocumentService.updateById(repositoryBuyinDocument);
        log.info("仓库模块-反审核通过内容:{}",repositoryBuyinDocument);
        return ResponseResult.succ("反审核成功");
    }
}
