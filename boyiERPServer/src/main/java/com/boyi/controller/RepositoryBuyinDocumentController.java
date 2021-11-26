package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    public static final Map<Long,String> locks = new ConcurrentHashMap<Long,String>();


    /**
     * 一键采购订单领料
     */
    @GetMapping("/batchPick")
    @PreAuthorize("hasAuthority('repository:buyIn:save')")
    @Transactional
    public ResponseResult batchPick(Principal principal,String startDateStr,String endDateStr,String pickDateStr)throws Exception {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate = LocalDate.parse(startDateStr, dateTimeFormatter);
        LocalDate endDate = LocalDate.parse(endDateStr, dateTimeFormatter);
        LocalDate pickDate = LocalDate.parse(pickDateStr, dateTimeFormatter);

        // 0. 校验关账日
        boolean validIsClose = validIsClose(pickDate);
        if(!validIsClose){
            return ResponseResult.fail("日期请设置在关账日之后.");
        }

        // 1. 根据时间区间，找到采购入库单据中的，是采购入库的 全部信息。
        List<RepositoryBuyinDocument> lists = repositoryBuyinDocumentService.getListFromOrderBetweenDate(startDate,endDate);
        if(lists.size() ==0){
            return ResponseResult.fail("不存在符合条件的记录.");
        }
        // 2. 判断是否有存在审核未通过的，存在则取消
        List<Map<String,String>> strList = new ArrayList<Map<String,String>>();

        HashSet<Long> errorIds = new HashSet<>();
        Map<String, Double> materialMap = new HashMap<>();
        for (RepositoryBuyinDocument obj: lists){
            if(obj.getStatus() != 0){
                if(!errorIds.contains(obj.getId())){
                    errorIds.add(obj.getId());
                    HashMap<String, String> map = new HashMap<>();
                    map.put("content","存在未审核通过的单据。单据编号:"+"["+obj.getId()+"]");
                    strList.add(map);

                }
            }
            String materialId = obj.getMaterialId();
            if(!materialId.startsWith("01.03") && !materialId.startsWith("05.04") && !materialId.startsWith("06.01")
                    && !materialId.startsWith("05.02")&& !materialId.startsWith("05.03")){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","存在物料编码不能归类部门。单据编号:"+"["+obj.getId()+"]"+"，物料编码:"+materialId);
                strList.add(map);
            }
            Double oldNum = materialMap.get(materialId);
            if(oldNum == null || oldNum == 0D){
                oldNum = 0D;
            }
            materialMap.put(materialId,oldNum+obj.getNum());
        }
        if(strList.size() > 0){
            return ResponseResult.succ(200,"存在未审核通过的",strList);
        }

        // 3. 去重物料ID，求和入库数量

        // 4. 判断物料 剩余库存，是否够领，出现负库存则取消
        repositoryStockService.validStockNumWithErrorMsg(materialMap,strList);
        if(strList.size() > 0){
            return ResponseResult.succ(200,"存在负库存！",strList);
        }
        // 5. 根据物料编码 领料部门规则 进行分类，一个部门生成一个领料单据
        /***
         * 01.03, 05.04, 06.01 裁断
         * 05.02,     复底线
         * 05.03     夹包线
         * 05.04     整理线
         */
        RepositoryPickMaterial caiduan = new RepositoryPickMaterial();
        RepositoryPickMaterial fudixian = new RepositoryPickMaterial();
        RepositoryPickMaterial jiabaoxian = new RepositoryPickMaterial();
        RepositoryPickMaterial zhenglixian = new RepositoryPickMaterial();

        boolean caiduanSave = false;
        boolean fudiSave = false;
        boolean jiabaoSave = false;
        boolean zhenglixianSave = false;

        LocalDateTime now = LocalDateTime.now();

        for(Map.Entry<String,Double> entry : materialMap.entrySet()){
            String materialId = entry.getKey();
            Double num = entry.getValue();
            if(materialId.startsWith("01.03") || materialId.startsWith("06.01")){
                // 该部门的领料单，要先插入一条主记录，记下ID
                addDetail(3L,caiduanSave,caiduan,pickDate,now, principal.getName(),num,materialId);
                caiduanSave = true;

            }else if(materialId.startsWith("05.02")){
                addDetail(7L,fudiSave,fudixian,pickDate,now, principal.getName(),num,materialId);
                fudiSave = true;
            }else if(materialId.startsWith("05.03")){
                addDetail(5L,jiabaoSave,jiabaoxian,pickDate,now, principal.getName(),num,materialId);
                jiabaoSave = true;
            }else if(materialId.startsWith("05.04")){
                addDetail(8L,zhenglixianSave,zhenglixian,pickDate,now, principal.getName(),num,materialId);
                zhenglixianSave = true;
            }
        }

        // 插入详情
        if(caiduan.getRowList()!=null && caiduan.getRowList().size() > 0){
            repositoryPickMaterialDetailService.saveBatch(caiduan.getRowList());
        }
        if(fudixian.getRowList()!=null && fudixian.getRowList().size() > 0){
            repositoryPickMaterialDetailService.saveBatch(fudixian.getRowList());
        }if(jiabaoxian.getRowList()!=null && jiabaoxian.getRowList().size() > 0){
            repositoryPickMaterialDetailService.saveBatch(jiabaoxian.getRowList());
        }if(zhenglixian.getRowList()!=null && zhenglixian.getRowList().size() > 0){
            repositoryPickMaterialDetailService.saveBatch(zhenglixian.getRowList());
        }
        // 减少库存
        repositoryStockService.subNumByMaterialId(materialMap);

        return ResponseResult.succ("采购订单一键领料完成");
    }
    private void addDetail(Long departmentId,boolean saveFlag,RepositoryPickMaterial pickMaterial,LocalDate pickDate,LocalDateTime now,
                           String userName,Double num,String materialId){
        if(!saveFlag){
            pickMaterial.setDepartmentId(departmentId); //裁断部门ID 3，这里写死
            pickMaterial.setPickDate(pickDate);
            pickMaterial.setCreated(now);
            pickMaterial.setUpdated(now);
            pickMaterial.setCreatedUser(userName);
            pickMaterial.setUpdatedUser(userName);
            pickMaterial.setStatus(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.STATUS_FIELDVALUE_1);
            repositoryPickMaterialService.save(pickMaterial);
        }
        List<RepositoryPickMaterialDetail> rowList = pickMaterial.getRowList();
        if(rowList == null){
            rowList = new ArrayList<>();
            pickMaterial.setRowList(rowList);
        }
        RepositoryPickMaterialDetail detail = new RepositoryPickMaterialDetail();
        detail.setDocumentId(pickMaterial.getId());
        detail.setNum(num);
        detail.setMaterialId(materialId);
        rowList.add(detail);
    }

    /**
     * 锁单据
     */
    @GetMapping("/lockById")
    @PreAuthorize("hasAuthority('repository:buyIn:list')")
    public ResponseResult lockById(Principal principal,Long id) {
        locks.put(id,principal.getName());
        log.info("锁单据:{}",id);
        return ResponseResult.succ("");
    }
    /**
     * 解锁单据
     */
    @GetMapping("/lockOpenById")
    @PreAuthorize("hasAuthority('repository:buyIn:list')")
    public ResponseResult lockOpenById(Long id) {
        locks.remove(id);
        log.info("解锁单据:{}",id);
        return ResponseResult.succ("");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('repository:buyIn:del')")
    public ResponseResult delete(@RequestBody Long[] ids)throws Exception {
        String user = locks.get(ids[0]);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }

        List<RepositoryBuyinDocumentDetail> details = repositoryBuyinDocumentDetailService.listByDocumentId(ids[0]);
        // 1. 根据单据ID 获取该单据的全部详情信息，
        String supplierId = details.get(0).getSupplierId();
        // 2. 得到一个物料，需要减少的数量
        Map<String, Double> subMap = new HashMap<>();// 一个物料，需要减少的数目
        for (RepositoryBuyinDocumentDetail detail : details) {
            Double materialNum = subMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            subMap.put(detail.getMaterialId(),BigDecimalUtil.add(materialNum,detail.getNum()).doubleValue());
        }
        // 3. 减少之后的该供应商，该物料的入库数目 >= 该供应商，该物料 退料数目 (金蝶目前没有判断，因为导入比较麻烦，目前暂时先取消该功能)
/*

        for (Map.Entry<String,Double> entry : subMap.entrySet()) {
            String materialId = entry.getKey();
            Double needSubNum = entry.getValue();// 该单据该物料，进行出库的数目

            // 查询该供应商，该物料 总入库数目.
            Double pushCount = repositoryBuyinDocumentService.countBySupplierIdAndMaterialId(supplierId,materialId);

            // 剩下的该供应商，该物料的入库数目
            double calNum = pushCount - needSubNum;

            // 查询该供应商，该物料退料数目
            Double returnCount = repositoryBuyoutDocumentService.countBySupplierIdAndMaterialId(supplierId,materialId);

            if(calNum < returnCount){
                throw new Exception("该供应商:"+supplierId+",该物料:" +materialId+
                        "(总入库数目:"+pushCount+" - 出库数目:"+needSubNum+")="+calNum+" < 退料的数目:"+returnCount);
            }
        }
*/

        // 校验库存
        repositoryStockService.validStockNum(subMap);

        try {


        // 4. 减少库存
        repositoryStockService.subNumByMaterialId(subMap);

        boolean flag = repositoryBuyinDocumentService.removeByIds(Arrays.asList(ids));

        log.info("删除采购入库表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("采购入库删除失败");
        }


        boolean flagDetail = repositoryBuyinDocumentDetailService.delByDocumentIds(ids);
        log.info("删除采购入库表详情信息,document_id:{},是否成功：{}",ids,flagDetail?"成功":"失败");

        // 假如是采购订单类型的，还需要更新采购订单的状态
        if(details.get(0).getOrderDetailId() != null){
            List<Long> orderDetailIds = new ArrayList<>();// 删除了的orderDetailID集合
            for (RepositoryBuyinDocumentDetail item : details) {
                orderDetailIds.add(item.getOrderDetailId());
            }
            orderBuyorderDocumentDetailService.statusNotSuccess(orderDetailIds);
        }

        if(!flagDetail){
            return ResponseResult.fail("采购入库详情表没有删除成功!");
        }
        return ResponseResult.succ("删除成功");
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('repository:buyIn:list')")
    public ResponseResult queryById(Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        RepositoryBuyinDocument repositoryBuyinDocument = repositoryBuyinDocumentService.getById(id);

        List<RepositoryBuyinDocumentDetail> details = repositoryBuyinDocumentDetailService.listByDocumentId(id);

        BaseSupplier supplier = baseSupplierService.getById(repositoryBuyinDocument.getSupplierId());


//        Double totalNum = 0D;
//        Double totalAmount = 0D;

        for (RepositoryBuyinDocumentDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());

            // 查询对应的价目记录
            BaseSupplierMaterial one = baseSupplierMaterialService.getSuccessPrice(supplier.getId(),material.getId(),repositoryBuyinDocument.getBuyInDate());

            if(one != null){
                detail.setPrice(one.getPrice());
//                double amount = detail.getPrice() * detail.getNum();
//                detail.setAmount(new BigDecimal(amount).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue());
//                totalAmount += amount;
            }

//            totalNum += detail.getNum();
        }

//        repositoryBuyinDocument.setTotalNum( totalNum);
//        repositoryBuyinDocument.setTotalAmount(new BigDecimal(totalAmount).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue());


        repositoryBuyinDocument.setSupplierName(supplier.getName());

        repositoryBuyinDocument.setRowList(details);
        return ResponseResult.succ(repositoryBuyinDocument);
    }

    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('repository:buyIn:update')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody RepositoryBuyinDocument repositoryBuyinDocument)throws Exception {

        if(repositoryBuyinDocument.getRowList() ==null || repositoryBuyinDocument.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2);
        try {

            boolean validIsClose = validIsClose(repositoryBuyinDocument.getBuyInDate());
            if(!validIsClose){
                return ResponseResult.fail("日期请设置在关账日之后.");
            }

            // 分2种情况，采购订单来源的，和采购入库来源的

            // 采购入库来源的处理:
            if(repositoryBuyinDocument.getSourceType() == DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SOURCE_TYPE_FIELDVALUE_0){

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
                repositoryStockService.validStockNum(needSubMap);

                //2. 先删除老的，再插入新的
                repositoryBuyinDocumentDetailService.removeByDocId(repositoryBuyinDocument.getId());
                repositoryBuyinDocumentService.updateById(repositoryBuyinDocument);

                for (RepositoryBuyinDocumentDetail item : repositoryBuyinDocument.getRowList()){
                    item.setId(null);
                    item.setDocumentId(repositoryBuyinDocument.getId());
                    item.setSupplierId(repositoryBuyinDocument.getSupplierId());
                    item.setPriceDate(repositoryBuyinDocument.getBuyInDate());
                }

                repositoryBuyinDocumentDetailService.saveBatch(repositoryBuyinDocument.getRowList());

                // 4. 减少,添加库存
                repositoryStockService.subNumByMaterialId(needSubMap);
                repositoryStockService.addNumByMaterialIdFromMap(needAddMap);

                log.info("采购入库模块-更新内容:{}",repositoryBuyinDocument);
            }else{
                // 假如是采购订单来源的，需要删除对应记录

                Set<Long> detailIds = new HashSet<>();// 现在的ID集合
                for (RepositoryBuyinDocumentDetail item : repositoryBuyinDocument.getRowList()) {
                    detailIds.add(item.getId());
                }
                List<RepositoryBuyinDocumentDetail> olds = repositoryBuyinDocumentDetailService.listByDocumentId(repositoryBuyinDocument.getRowList().get(0).getDocumentId());

                List<Long> needSubIds = new ArrayList<>();// 删除了的ID集合
                List<Long> orderDetailIds = new ArrayList<>();// 删除了的orderDetailID集合
                for (RepositoryBuyinDocumentDetail item : olds) {
                    if(!detailIds.contains(item.getId())){
                        needSubIds.add(item.getId());
                        orderDetailIds.add(item.getOrderDetailId());
                    }
                }
                repositoryBuyinDocumentService.updateById(repositoryBuyinDocument);
                if(needSubIds.size() != 0){
                    // (金蝶目前没有判断，因为导入比较麻烦，目前暂时先取消该功能)
/*
                    // 校验删除之后的退料数目
                    List<OrderBuyorderDocumentDetail> details = orderBuyorderDocumentDetailService.listByIds(orderDetailIds);
                    // 2. 得到一个物料，需要减少的数量
                    Map<String, Double> subMap = new HashMap<>();// 一个供应商_一个物料，需要减少的数目
                    for (OrderBuyorderDocumentDetail detail : details) {
                        String key = detail.getSupplierId() + "_" + detail.getMaterialId();
                        Double materialNum = subMap.get(key);
                        if(materialNum == null){
                            materialNum= 0D;
                        }
                        subMap.put(key,BigDecimalUtil.add(materialNum,detail.getNum()).doubleValue());
                    }
                    // 3. 减少之后的该供应商，该物料的入库数目 >= 该供应商，该物料 退料数目

                    for (Map.Entry<String,Double> entry : subMap.entrySet()) {
                        String[] arr = entry.getKey().split("_");
                        String supplierId = arr[0];
                        String materialId = arr[1];

                        Double needSubNum = entry.getValue();// 该单据该物料，进行出库的数目

                        // 查询该供应商，该物料 总入库数目.
                        Double pushCount = repositoryBuyinDocumentService.countBySupplierIdAndMaterialId(supplierId,materialId);

                        // 剩下的该供应商，该物料的入库数目
                        double calNum = pushCount - needSubNum;

                        // 查询该供应商，该物料退料数目
                        Double returnCount = repositoryBuyoutDocumentService.countBySupplierIdAndMaterialId(supplierId,materialId);
                        returnCount  = returnCount==null?0L:returnCount;

                        if(calNum < returnCount){
                            throw new Exception("该供应商:"+supplierId+",该物料:" +materialId+
                                    "(总入库数目:"+pushCount+" - 出库数目:"+needSubNum+")="+calNum+" < 退料的数目:"+returnCount);
                        }
                    }*/
                    // 减少库存
                    List<RepositoryBuyinDocumentDetail> details = repositoryBuyinDocumentDetailService.listByIds(needSubIds);
                    for (RepositoryBuyinDocumentDetail detail :details){
                        repositoryStockService.subNumByMaterialIdNum(detail.getMaterialId()
                                ,detail.getNum());
                    }
                    // 1. 根据document_id，删除现有ID之外的数据
                    repositoryBuyinDocumentDetailService.removeByDocIdAndInIds(repositoryBuyinDocument.getId(),needSubIds);
                    // 2. 修改，采购订单，该详情的状态
                    orderBuyorderDocumentDetailService.statusNotSuccess(orderDetailIds);
                    log.info("[采购入库]-[更新],更新订单detail 的id:{},状态改未下推",orderDetailIds);

                }

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
    private void validCompareReturnNum(RepositoryBuyinDocument repositoryBuyinDocument,
                                        Map<String, Double> needSubMap,
                                        Map<String, Double> needAddMap,
                                        Map<String, Double> notUpdateMap
                                                            )throws Exception {

        List<RepositoryBuyinDocumentDetail> oldDetails = repositoryBuyinDocumentDetailService.listByDocumentId(repositoryBuyinDocument.getId());

        // 新的物料数目：
        Map<String, Double> newMap = new HashMap<>();
        for (RepositoryBuyinDocumentDetail detail : repositoryBuyinDocument.getRowList()) {
            Double materialNum = newMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            newMap.put(detail.getMaterialId(),BigDecimalUtil.add(materialNum,detail.getNum()).doubleValue());
        }

        // 2.  老的物料数目
        Map<String, Double> oldMap = new HashMap<>();
        for (RepositoryBuyinDocumentDetail detail : oldDetails) {
            Double materialNum = oldMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            oldMap.put(detail.getMaterialId(),BigDecimalUtil.add(materialNum,detail.getNum()).doubleValue());
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
                    ;
                    needAddMap.put(materialId,BigDecimalUtil.sub(newNum,oldNum).doubleValue());//需要新增的数目
                    continue;
                }else if(oldNum > newNum){
                    needSubMap.put(materialId,BigDecimalUtil.sub(oldNum,newNum).doubleValue()); // 需要减少的数目
                }else {
                    notUpdateMap.put(materialId,newNum);
                    continue;
                }

                // 查询历史该供应商，该物料 总入库数目. (金蝶目前没有判断，因为导入比较麻烦，目前暂时先取消该功能)
                /*Double pushCount = repositoryBuyinDocumentService.countBySupplierIdAndMaterialId(newSupplierId,materialId);

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

                /*Double pushCount = repositoryBuyinDocumentService.countBySupplierIdAndMaterialId(oldSupplierId,materialId);

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
    private ResponseResult validExistSupplierDocNumExcludeSelf(RepositoryBuyinDocument repositoryBuyinDocument) {
        int exitCount = repositoryBuyinDocumentService.countSupplierOneDocNumExcludSelf(
                repositoryBuyinDocument.getSupplierDocumentNum(),
                repositoryBuyinDocument.getSupplierId(),
                repositoryBuyinDocument.getId());

        if(exitCount > 0){
            return ResponseResult.fail("该供应商的单据已经存在！请确认信息!");
        }
        return null;
    }
    private ResponseResult validExistSupplierDocNum(RepositoryBuyinDocument repositoryBuyinDocument) {
        // 1. 先查询该供应商，该单号是否已经有记录，有则不能插入
        int exitCount = repositoryBuyinDocumentService.countSupplierOneDocNum(
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
    @PreAuthorize("hasAuthority('repository:buyIn:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody RepositoryBuyinDocument repositoryBuyinDocument) {

        LocalDateTime now = LocalDateTime.now();
        repositoryBuyinDocument.setCreated(now);
        repositoryBuyinDocument.setUpdated(now);
        repositoryBuyinDocument.setCreatedUser(principal.getName());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2);
        repositoryBuyinDocument.setSourceType(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SOURCE_TYPE_FIELDVALUE_0);
        try {
            boolean validIsClose = validIsClose(repositoryBuyinDocument.getBuyInDate());
            if(!validIsClose){
                return ResponseResult.fail("日期请设置在关账日之后.");
            }

            ResponseResult responseResult = validExistSupplierDocNum(repositoryBuyinDocument);
            if(responseResult != null){
                return responseResult;
            }

            repositoryBuyinDocumentService.save(repositoryBuyinDocument);

            for (RepositoryBuyinDocumentDetail item : repositoryBuyinDocument.getRowList()){
                item.setDocumentId(repositoryBuyinDocument.getId());
                item.setSupplierId(repositoryBuyinDocument.getSupplierId());

                // 普通采购入库，priceDate = buyInDate
                item.setPriceDate(repositoryBuyinDocument.getBuyInDate());
            }

            repositoryBuyinDocumentDetailService.saveBatch(repositoryBuyinDocument.getRowList());

            //  遍历更新 ，一个物料对应的库存数量
            for (RepositoryBuyinDocumentDetail detail : repositoryBuyinDocument.getRowList()){
                repositoryStockService.addNumByMaterialId(detail.getMaterialId()
                        ,detail.getNum());
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
    @PreAuthorize("hasAuthority('repository:buyIn:export')")
    public void export(HttpServletResponse response,  String searchField, String searchStartDate, String searchEndDate,String searchStatus,
                       @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
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
                    } else {
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
        pageData = repositoryBuyinDocumentService.innerQueryByManySearch(page,searchField,queryField,searchStr,searchStartDate,searchEndDate,searchStatusList,queryMap);

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(RepositoryBuyinDocument.class,1,0).export("id","CGRK",response,fis,pageData.getRecords(),"报表.xlsx",DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    /**
     * 获取采购入库 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('repository:buyIn:list')")
    public ResponseResult list(String searchField, String searchStartDate, String searchEndDate,
                               String searchStatus,@RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
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
                    } else {
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
        pageData = repositoryBuyinDocumentService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate,searchStatusList,queryMap);
        return ResponseResult.succ(pageData);
    }


    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('repository:buyIn:save')")
    public ResponseResult statusSubmit(Principal principal,Long id) {

        RepositoryBuyinDocument old = repositoryBuyinDocumentService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_1){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        RepositoryBuyinDocument repositoryBuyinDocument = new RepositoryBuyinDocument();
        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setId(id);
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2);
        repositoryBuyinDocumentService.updateById(repositoryBuyinDocument);
        log.info("仓库模块-审核中内容:{}",repositoryBuyinDocument);
        return ResponseResult.succ("已提交");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('repository:buyIn:save')")
    public ResponseResult statusSubReturn(Principal principal,Long id) {
        RepositoryBuyinDocument old = repositoryBuyinDocumentService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2 &&
                old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_3
        ){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        RepositoryBuyinDocument repositoryBuyinDocument = new RepositoryBuyinDocument();
        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setId(id);
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_1);
        repositoryBuyinDocumentService.updateById(repositoryBuyinDocument);
        log.info("仓库模块-撤销内容:{}",repositoryBuyinDocument);
        return ResponseResult.succ("已撤销");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('repository:buyIn:valid')")
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<RepositoryBuyinDocument> lists = new ArrayList<>();

        for (Long id : ids){
            String user = locks.get(id);
            if(StringUtils.isNotBlank(user)){
                return ResponseResult.fail("单据被["+user+"]占用");
            }

            RepositoryBuyinDocument old = repositoryBuyinDocumentService.getById(id);
            if(old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2
                    && old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_3){
                return ResponseResult.fail("单据编号:"+id+"状态不正确，无法审核通过");
            }

            RepositoryBuyinDocument repositoryBuyinDocument = new RepositoryBuyinDocument();
            repositoryBuyinDocument.setUpdated(LocalDateTime.now());
            repositoryBuyinDocument.setUpdatedUser(principal.getName());
            repositoryBuyinDocument.setId(id);
            repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_0);
            lists.add(repositoryBuyinDocument);

        }
        repositoryBuyinDocumentService.updateBatchById(lists);
        return ResponseResult.succ("审核通过");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('repository:buyIn:valid')")
    public ResponseResult statusPass(Principal principal,Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }

        RepositoryBuyinDocument old = repositoryBuyinDocumentService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2
                && old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_3){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        RepositoryBuyinDocument repositoryBuyinDocument = new RepositoryBuyinDocument();
        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setId(id);
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_0);
        repositoryBuyinDocumentService.updateById(repositoryBuyinDocument);
        log.info("仓库模块-审核通过内容:{}",repositoryBuyinDocument);

        return ResponseResult.succ("审核通过");
    }


    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('repository:buyIn:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        RepositoryBuyinDocument old = repositoryBuyinDocumentService.getById(id);
        boolean validIsClose = validIsClose(old.getBuyInDate());
        if(!validIsClose){
            return ResponseResult.fail("日期请设置在关账日之后.");
        }
        if(old.getStatus()!=DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_0){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        RepositoryBuyinDocument repositoryBuyinDocument = new RepositoryBuyinDocument();
        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setId(id);
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_3);

        repositoryBuyinDocumentService.updateById(repositoryBuyinDocument);
        log.info("仓库模块-反审核通过内容:{}",repositoryBuyinDocument);
        return ResponseResult.succ("反审核成功");
    }

}
