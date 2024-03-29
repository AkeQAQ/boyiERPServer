package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.ExcelImportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-04-29
 */
@RestController
@RequestMapping("/produce/batch")
@Slf4j
public class ProduceBatchController extends BaseController {
    @Value("${poi.produceBatchImportDemoPath}")
    private String poiImportDemoPath;



    @PostMapping("/zcProgressList")
    @PreAuthorize("hasAuthority('produce:batch:list')")
    public ResponseResult zcProgressList(Principal principal,@RequestBody Map<String,Object> params) {
        Object searchQueryStartDateStr = params.get("searchQueryStartDateStr");

        List<ProduceBatch> progresses= new ArrayList<>();

        List<ProduceBatch> progressesLists= new ArrayList<>();

        String name = principal.getName();
        progressesLists = this.produceBatchService.listByWithZCDataDate(searchQueryStartDateStr.toString());

        // 将同batchId的去除,并且将-1这种消除
        HashMap<String, Long> batchIdPre_number = new HashMap<>();

        HashMap<String, ProduceBatch> batchIdPre_ownZCProgress = new HashMap<>();

        for(ProduceBatch pb : progressesLists){
            String batchIdPre = pb.getBatchId().split("-")[0];

            if(!batchIdPre_ownZCProgress.containsKey(batchIdPre) && pb.getGroupName()!=null && !pb.getGroupName().isEmpty()){
                batchIdPre_ownZCProgress.put(batchIdPre,pb);
            }

            // 查询该批次号前缀的数量
            Long totalNum = batchIdPre_number.get(batchIdPre);
            if(totalNum==null){
                totalNum = produceBatchService.sumByBatchIdPre(batchIdPre);

                batchIdPre_number.put(batchIdPre,totalNum);

            }else{
                continue;
            }

            pb.setMergeBatchNumber(totalNum+"");
            pb.setBatchId(batchIdPre);
            progresses.add(pb);
        }
        for(ProduceBatch pb : progresses) {
            String batchIdPre = pb.getBatchId().split("-")[0];

            String groupName = pb.getGroupName();
            if(groupName==null || groupName.isEmpty()){
                ProduceBatch ownZCProgress = batchIdPre_ownZCProgress.get(batchIdPre);
                if(ownZCProgress!=null){
                    pb.setGroupName(ownZCProgress.getGroupName());
                    pb.setSendForeignProductDate(ownZCProgress.getSendForeignProductDate());
                    pb.setOutDate(ownZCProgress.getOutDate());
                }
            }
        }


            HashMap<String, Object> returnMap = new HashMap<>();
        returnMap.put("progressData",progresses);


        return ResponseResult.succ(returnMap);

    }


    @PostMapping("/push")
    @PreAuthorize("hasAuthority('produce:batch:push')")
    @Transactional
    public ResponseResult push(Principal principal,@RequestBody Long[] ids) {

        LocalDateTime now = LocalDateTime.now();
        LocalDate nowDate = LocalDate.now();

        String username = principal.getName();

        List<CostOfLabourType> colts = costOfLabourTypeService.listByName(username);
        if(colts.isEmpty()){
            return ResponseResult.fail("没有工价类型权限");
        }
        if(colts.size() > 1){
            return ResponseResult.fail("工价类型权限超过1个，无法筛选所属进度表");
        }
        CostOfLabourType colt = colts.get(0);


        Map<String, Long> batchIdStr_totalNum = new HashMap<>();

        Map<String, List<ProduceBatchProgress>> supplierId_progresses = new HashMap<>();


        for (Long id : ids){
            // 1. 查询所有批次号前缀的总数量.
            ProduceBatch pb = produceBatchService.getById(id);
            String batchIdStr = pb.getBatchId().split("-")[0];
            Long total = produceBatchService.sumByBatchIdPre(batchIdStr);
            batchIdStr_totalNum.put(batchIdStr,total);
            // 2. 按供应商分组
            List<ProduceBatchProgress> progresses = produceBatchProgressService.listByProduceBatchIdByCostOfLabourTypeId(id,colt.getId());
            if(progresses.isEmpty()){
                continue;
            }
            pb.setPush(DBConstant.TABLE_PRODUCE_BATCH.PUSH_FIELDVALUE_0);
            pb.setUpdated(now);
            pb.setUpdatedUser(username);
            produceBatchService.updateById(pb);

            for(ProduceBatchProgress progress : progresses){
                String supplierId = progress.getSupplierId();
                // 没有工艺，只有出库进度信息。
                if(supplierId ==null || supplierId.isEmpty()){
                    continue;
                }
                List<ProduceBatchProgress> theSupplierProgresses = supplierId_progresses.get(supplierId);
                if(theSupplierProgresses==null){
                    theSupplierProgresses = new ArrayList<>();
                    supplierId_progresses.put(supplierId,theSupplierProgresses);
                }

                // 重新设置批次号前缀。
                progress.setBatchIdStr(batchIdStr);
                theSupplierProgresses.add(progress);
            }
        }

        // 3. 生成采购订单表,一个供应商一个订单表
        for(Map.Entry<String,List<ProduceBatchProgress>> entry : supplierId_progresses.entrySet()){
            String supplierId = entry.getKey();
            List<ProduceBatchProgress> progresses = entry.getValue();
            if(progresses==null||progresses.isEmpty()){
                log.warn("出现生产采购订单表，有供应商但是进度表信息是空的情况.{}",entry);
                continue;
            }

            // 生成一个采购订单记录
            OrderBuyorderDocument obd = new OrderBuyorderDocument();
            obd.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.STATUS_FIELDVALUE_1);
            obd.setSupplierId(supplierId);
            obd.setOrderDate(nowDate); // 采购日期默认当天
            obd.setCreated(now);
            obd.setCreatedUser(username);
            orderBuyorderDocumentService.save(obd);

            // 生成采购订单下的子记录
            ArrayList<OrderBuyorderDocumentDetail> obdds = new ArrayList<>();
            for(ProduceBatchProgress progress:progresses){

                BaseMaterial bm = baseMaterialService.getById(progress.getMaterialId());

                OrderBuyorderDocumentDetail obdd = new OrderBuyorderDocumentDetail();
                obdd.setMaterialId(progress.getMaterialId());
                obdd.setDocumentId(obd.getId());
                obdd.setNum(Double.valueOf(batchIdStr_totalNum.get(progress.getBatchIdStr())));
                obdd.setSupplierId(obd.getSupplierId());

                LocalDateTime backForeignProductDate = progress.getBackForeignProductDate();
                if(backForeignProductDate!=null){
                    LocalDate localDate = backForeignProductDate.toLocalDate();
                    obdd.setDoneDate(localDate);// 交货日期根据进度表的返回时间
                }

                obdd.setOrderSeq(progress.getBatchIdStr());

                String orderSeq = obdd.getOrderSeq();
                long count = orderBuyorderDocumentService.countBySupplierIdMaterialIdOrderSeqInOneYear(obd.getSupplierId(),
                        obdd.getMaterialId(),orderSeq);
                if(count >0 ){
                    return ResponseResult.fail("供应商:"+obd.getSupplierId()+",物料:"+bm.getName()+",订单号:"+orderSeq+",已经有在近一年内，有重复记录，不能下推!");
                }

                obdd.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_1);
                obdd.setOrderDate(nowDate);
                obdd.setRadioNum(BigDecimalUtil.mul(obdd.getNum(),bm.getUnitRadio()).doubleValue()  );
                obdds.add(obdd);
            }

            orderBuyorderDocumentDetailService.saveBatch(obdds);

        }

        return ResponseResult.succ("下推成功！");
    }

    @PostMapping("/progressList")
    @PreAuthorize("hasAuthority('produce:batch:list')")
    public ResponseResult list(Principal principal,Boolean showSendNoBack,Boolean showHasEndDate,@RequestBody Map<String,Object> params) {
        Object searchQueryOutDateStr = params.get("searchQueryOutDateStr");
        Object searchQueryStartDateStr = params.get("searchQueryStartDateStr");
        String supplierName = params.get("supplierName").toString();

        List<ProduceBatch> progresses= new ArrayList<>();
        List<ProduceBatch> delays = new ArrayList<>();

        List<ProduceBatch> progressesLists= new ArrayList<>();


        String name = principal.getName();
        List<CostOfLabourType> currentUserOwnerTypes = null;
        if(name.equals("admin")){
            currentUserOwnerTypes = costOfLabourTypeService.list();
        }else{
            currentUserOwnerTypes = costOfLabourTypeService.listByName(name);
        }
        HashSet<Integer> ownSeqs = new HashSet<>();
        for(CostOfLabourType type : currentUserOwnerTypes){
            if(type.getSeq()!=null){
                ownSeqs.add(type.getSeq());
            }
        }
        boolean outDateIsNull = searchQueryOutDateStr==null || searchQueryOutDateStr.toString().trim().equals("");
        boolean dataDateIsNull = searchQueryStartDateStr==null || searchQueryStartDateStr.toString().trim().equals("");

        if(!outDateIsNull && dataDateIsNull){
            progressesLists = this.produceBatchService.listByOutDate(searchQueryOutDateStr.toString());
        }else if(outDateIsNull && dataDateIsNull){
            progressesLists = this.produceBatchService.listByOutDateIsNull();
        } else if(!outDateIsNull && !dataDateIsNull){
            progressesLists = this.produceBatchService.listByOutDateDataDate(searchQueryOutDateStr.toString(),searchQueryStartDateStr.toString());
        }else if(outDateIsNull && !dataDateIsNull){
            progressesLists = this.produceBatchService.listByOutDateIsNullWithDataDate(searchQueryStartDateStr.toString());
        }



        // 将同batchId的去除,并且将-1这种消除
        HashMap<String, Long> batchIdPre_number = new HashMap<>();
        for(ProduceBatch pb : progressesLists){

            boolean isCan = false;
            // 只能看此用户或者此用户前1个流程的
            if(ownSeqs.contains(pb.getSeq()) || pb.getSeq() ==null ){
                isCan = true;
            }else{
                a:for(Integer seq : ownSeqs){
                    // 拥有的部门，不是0的顺序，并且前一个部门就是该进度表也可查看
                    if( seq!=0 && seq-1==pb.getSeq()){
                        isCan = true;
                        break a;
                    }
                }
            }
            if(!isCan){
                continue;
            }
            String batchIdPre = pb.getBatchId().split("-")[0];

            // 假如不是该供应商，则过滤
            if(!supplierName.isEmpty()){
                String supplierName1 = pb.getSupplierName();
                if(!supplierName.equals(supplierName1)){
                    continue;
                }
            }
            if(showHasEndDate){
                if(pb.getEndDate()==null || pb.getEndDate().isEmpty()){
                    continue;
                }
            }
            if(showSendNoBack){
                if( !(pb.getOutDate()==null && pb.getSendForeignProductDate()!=null && pb.getBackForeignProductDate()==null )){
                    continue;
                }
            }
            // 查询出库是空的，还要看下该角色，该批次号是否已经有出库日期的记录
            if(outDateIsNull){
                Integer count = this.produceBatchProgressService.countByBatchIdStrAndCostOfLabourTypeIdAndOutDateIsNotNull(batchIdPre,pb.getCostOfLabourTypeId());
                if(count >0){
                    continue;
                }
            }




            // 查询该批次号前缀的数量
            Long totalNum = batchIdPre_number.get(batchIdPre);
            if(totalNum==null){
                 totalNum = produceBatchService.sumByBatchIdPre(batchIdPre);
                batchIdPre_number.put(batchIdPre,totalNum);
            }

            pb.setMergeBatchNumber(totalNum+"");
            pb.setBatchId(batchIdPre);
            progresses.add(pb);
            /*// 查出 同批次号ID的进度表，并且ID不为当前的这个
            List<ProduceBatchProgress> onePbIdProgresses = this.produceBatchProgressService.listByProduceBatchId(pb.getId());
            for(ProduceBatchProgress pomp : onePbIdProgresses){
                if(Objects.equals(pomp.getId(), pb.getProduceBatchProgressId()) || !pb.getCostOfLabourTypeId().equals(pb.getCostOfLabourTypeId())){
                    continue;
                }
                ProduceBatch otherPb = new ProduceBatch();
                otherPb.setOrderNum(pb.getOrderNum());
                otherPb.setBatchId(pb.getBatchId());
                otherPb.setProductNum(pb.getProductNum());
                otherPb.setProductBrand(pb.getProductBrand());
                otherPb.setOrderType(pb.getOrderType());
                otherPb.setEndDate(pb.getEndDate());
//                otherPb.setMergeBatchNumber(pb.getMergeBatchNumber());
                otherPb.setCostOfLabourTypeId(pomp.getCostOfLabourTypeId());
                otherPb.setCostOfLabourTypeName(pomp.getCostOfLabourTypeName());
                otherPb.setSupplierName(pomp.getSupplierName());
                otherPb.setMaterialName(pomp.getMaterialName());
                otherPb.setSendForeignProductDate(pomp.getSendForeignProductDate());
                otherPb.setBackForeignProductDate(pomp.getBackForeignProductDate());
                otherPb.setOutDate(pomp.getOutDate());
                otherPb.setIsAccept(pomp.getIsAccept());
                progresses.add(otherPb);
            }*/
        }


        StringBuilder sb = new StringBuilder();
        String allTotalNum = "0";
        HashSet<String> isAddFlag = new HashSet<>();
        final DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        TreeMap<String, ProduceBatch> orderByEndDateASC = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if(o2==null || o2.isEmpty()){
                    return 1;
                }
                if(o1==null || o1.isEmpty()){
                    return -1;
                }
                if(o1.startsWith("4") || o2.startsWith("4")){
                    return 1;
                }
                LocalDateTime o1Date = LocalDateTime.parse(o1, sdf);
                LocalDateTime o2Date = LocalDateTime.parse(o2, sdf);

                return o1Date
                        .isBefore(o2Date)
                        ? -1 : 1;
            }
        });

        TreeMap<LocalDateTime, ProduceBatch> orderBySendDateASC = new TreeMap<>(new Comparator<LocalDateTime>() {
            @Override
            public int compare(LocalDateTime o1, LocalDateTime o2) {
                if(o2==null ){
                    return 1;
                }
                if(o1==null ){
                    return -1;
                }

                return o1
                        .isBefore(o2)
                        ? -1 : 1;
            }
        });

        for(ProduceBatch pb : progresses){
            if(showHasEndDate){
                orderByEndDateASC.put(pb.getEndDate(),pb);
            }else {
                // 假如是没筛选货期，而是还行了外发未回的。按外发时间排序
                if(showSendNoBack){
                    orderBySendDateASC.put(pb.getSendForeignProductDate(),pb);
                }
            }

            String batchId = pb.getBatchId();
            sb.append(batchId).append(" ");
            if(!isAddFlag.contains(batchId)){
                allTotalNum = BigDecimalUtil.add(allTotalNum,pb.getMergeBatchNumber()).toString();
                isAddFlag.add(batchId);
            }
        }
        log.info("progresses:size {}, orderLists size:{}",progresses.size(),orderByEndDateASC.values().size());


        List<ProduceBatch> delaysLists = this.produceBatchService.listDelay();

        for(ProduceBatch pb : delaysLists){
            String batchIdPre = pb.getBatchId().split("-")[0];
            pb.setBatchId(batchIdPre);
            delays.add(pb);
        }

        HashMap<String, Object> returnMap = new HashMap<>();
        returnMap.put("delayData",delays);
        if(showHasEndDate){
            returnMap.put("progressData",orderByEndDateASC.values());

        }else{
            if(showSendNoBack){
                returnMap.put("progressData",orderBySendDateASC.values());
            }else{
                returnMap.put("progressData",progresses);
            }

        }
        returnMap.put("totalBatchId",sb.toString());
        returnMap.put("allTotalNum",allTotalNum);

        return ResponseResult.succ(returnMap);

    }


    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('produce:batch:valid')")
    @Transactional
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {

        ArrayList<ProduceBatch> lists = new ArrayList<>();

        for (Long id : ids){
            ProduceBatch batch = produceBatchService.getById(id);
            if(!batch.getStatus().equals(DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_1)){
                return ResponseResult.fail("状态不对，已修改，请刷新!");
            }

            ProduceBatch pb = new ProduceBatch();
            pb.setUpdated(LocalDateTime.now());
            pb.setUpdatedUser(principal.getName());
            pb.setId(id);
            pb.setStatus(DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_0);
            lists.add(pb);
        }
        produceBatchService.updateBatchById(lists);

        return ResponseResult.succ("批量审核通过");
    }

    @PostMapping("/statusReturnPassBatch")
    @PreAuthorize("hasAuthority('produce:batch:valid')")
    @Transactional
    public ResponseResult statusReturnPassBatch(Principal principal,@RequestBody Long[] ids) {

        ArrayList<ProduceBatch> lists = new ArrayList<>();

        for (Long id : ids){
            ProduceBatch batch = produceBatchService.getById(id);
            if(!batch.getStatus().equals(DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_0)){
                return ResponseResult.fail("状态不对，已修改，请刷新!");
            }


            List<RepositoryPickMaterial> picks = repositoryPickMaterialService.getSameBatch(null, batch.getBatchId(),null);

            if(picks.size() > 0){
                StringBuilder sb = new StringBuilder();
                for (RepositoryPickMaterial pick : picks){
                    sb.append(pick.getBatchId()).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                return ResponseResult.fail("生产领料已关联生产序号["+sb.toString()+"]");
            }

            // 已关联进度表
            // 查询当前批次号 前缀的batchId，看看是否进度表存在关联记录
            List<ProduceBatch> pbs = produceBatchService.listByLikeRightBatchId(batch.getBatchId().split("-")[0]);
            if(!pbs.isEmpty()){
                for(ProduceBatch pb : pbs){
                    List<ProduceBatchProgress> progresses = produceBatchProgressService.listByProduceBatchId(pb.getId());
                    if(progresses!=null&&progresses.size()>0){
                        return ResponseResult.fail("生产序号："+pb.getBatchId()+"已有裁断车间进度表记录。不能删除");
                    }

                    List<ProduceBatchZcProgress> zcProgress = produceBatchZcProgressService.listByBatchId(pb.getId());
                    if(zcProgress!=null&&zcProgress.size()>0){
                        return ResponseResult.fail("生产序号："+pb.getBatchId()+"已有针车车间进度表记录。不能删除");
                    }
                }
            }

            ProduceBatch pb = new ProduceBatch();
            pb.setUpdated(LocalDateTime.now());
            pb.setUpdatedUser(principal.getName());
            pb.setId(id);
            pb.setStatus(DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_1);
            lists.add(pb);

        }
        produceBatchService.updateBatchById(lists);

        return ResponseResult.succ("批量反审核通过");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('produce:batch:del')")
    public ResponseResult del(@RequestBody Long[] ids) throws Exception{
        try {
            List<ProduceBatch> batches = produceBatchService.listByIds(Arrays.asList(ids));
            ArrayList<String> batchIds = new ArrayList<>();
            for (ProduceBatch batch : batches){
                if(batch.getStatus().equals(DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_0)){
                    return ResponseResult.fail("唯一编码:"+batch.getId()+",已审核通过，不能删除");
                }
                batchIds.add(batch.getBatchId());
            }

            List<RepositoryPickMaterial> picks = repositoryPickMaterialService.listByBatchIds(batchIds);

            if(picks.size() > 0){
                StringBuilder sb = new StringBuilder();
                for (RepositoryPickMaterial pick : picks){
                    sb.append(pick.getBatchId()).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                return ResponseResult.fail("生产领料已关联生产序号["+sb.toString()+"]");
            }

            for(ProduceBatch batch:batches){

                // 已关联进度表
                // 查询当前批次号 前缀的batchId，看看是否进度表存在关联记录
                List<ProduceBatch> pbs = produceBatchService.listByLikeRightBatchId(batch.getBatchId().split("-")[0]);
                if(!pbs.isEmpty()){
                    for(ProduceBatch pb : pbs){
                        List<ProduceBatchProgress> progresses = produceBatchProgressService.listByProduceBatchId(pb.getId());
                        if(progresses!=null&&progresses.size()>0){
                            return ResponseResult.fail("生产序号："+pb.getBatchId()+"已有裁断车间进度表记录。不能删除");
                        }

                        List<ProduceBatchZcProgress> zcProgress = produceBatchZcProgressService.listByBatchId(pb.getId());
                        if(zcProgress!=null&&zcProgress.size()>0){
                            return ResponseResult.fail("生产序号："+pb.getBatchId()+"已有针车车间进度表记录。不能删除");
                        }

                        List<ProduceBatchDelay> delays = produceBatchDelayService.listByProduceBatchId(pb.getId());
                        if(delays!=null&&delays.size()>0){
                            return ResponseResult.fail("生产序号："+pb.getBatchId()+"已有车间欠料进度表记录。不能删除");
                        }

                    }
                }

            }


            boolean flag = produceBatchService.removeByIds(Arrays.asList(ids));

            log.info("删除【生产序号】信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("删除失败");
            }
            return ResponseResult.succ("删除成功");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    @Transactional
    @PostMapping("valid")
    @PreAuthorize("hasAuthority('produce:batch:valid')")
    public ResponseResult complementReValid(Long id) throws Exception{
        try {
            ProduceBatch old = produceBatchService.getById(id);
            if(!old.getStatus().equals(DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_1)){
                return ResponseResult.fail("状态不对，已修改，请刷新!");
            }


            produceBatchService.updateStatus(id,DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_0);
            return ResponseResult.succ("审核通过!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    @Transactional
    @PostMapping("reValid")
    @PreAuthorize("hasAuthority('produce:batch:valid')")
    public ResponseResult complementValid(Long id) throws Exception{
        try {
            ProduceBatch batch = produceBatchService.getById(id);
            if(!batch.getStatus().equals(DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_0)){
                return ResponseResult.fail("状态不对，已修改，请刷新!");
            }


            List<RepositoryPickMaterial> picks = repositoryPickMaterialService.getSameBatch(null, batch.getBatchId(),null);

            if(picks.size() > 0){
                StringBuilder sb = new StringBuilder();
                for (RepositoryPickMaterial pick : picks){
                    sb.append(pick.getBatchId()).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                return ResponseResult.fail("生产领料已关联生产序号["+sb.toString()+"]");
            }

            // 已关联进度表
            // 查询当前批次号 前缀的batchId，看看是否进度表存在关联记录
            List<ProduceBatch> pbs = produceBatchService.listByLikeRightBatchId(batch.getBatchId().split("-")[0]);
            if(!pbs.isEmpty()){
                for(ProduceBatch pb : pbs){
                    List<ProduceBatchProgress> progresses = produceBatchProgressService.listByProduceBatchId(pb.getId());
                    if(progresses!=null&&progresses.size()>0){
                        return ResponseResult.fail("相关联的生产序号："+pb.getBatchId()+"已有裁断车间进度表记录。不能反审核");
                    }

                    List<ProduceBatchZcProgress> zcProgress = produceBatchZcProgressService.listByBatchId(pb.getId());
                    if(zcProgress!=null&&zcProgress.size()>0){
                        return ResponseResult.fail("生产序号："+pb.getBatchId()+"已有针车车间进度表记录。不能删除");
                    }
                }
            }

            produceBatchService.updateStatus(id,DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_1);
            return ResponseResult.succ("反审核通过!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    @PostMapping("/list")
    @PreAuthorize("hasAuthority('produce:batch:list')")
    public ResponseResult list(Principal principal, String searchField, @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<ProduceBatch> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (!searchField.equals("")) {
            if (searchField.equals("batchId")) {
                queryField = "batch_id";
            }else if(searchField.equals("productNum")){
                queryField = "product_num";
            }else if(searchField.equals("productBrand")){
                queryField = "product_brand";
            }
            else if(searchField.equals("orderNum")){
                queryField = "order_num";
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
                    if (oneField.equals("batchId")) {
                        theQueryField = "batch_id";
                    }else if(oneField.equals("productNum")){
                        theQueryField = "product_num";
                    }
                    else if(oneField.equals("productBrand")){
                        theQueryField = "product_brand";
                    }
                    else if(oneField.equals("orderNum")){
                        theQueryField = "order_num";
                    }
                    else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);


        pageData = produceBatchService.complementInnerQueryByManySearch(getPage(),searchField,queryField,searchStr,queryMap);
        // 获取该用户拥有的工价类别
        String name = principal.getName();
        List<CostOfLabourType> currentUserOwnerTypes = null;

        if(name.equals("admin")){
            currentUserOwnerTypes = costOfLabourTypeService.list();
        }else{
            currentUserOwnerTypes = costOfLabourTypeService.listByName(name);
        }
        Set<Long> typeIds = new HashSet<>();
        for(CostOfLabourType type : currentUserOwnerTypes){
            typeIds.add(type.getId());
        }

        for(ProduceBatch pb : pageData.getRecords()){
            String pre = pb.getBatchId().split("-")[0];
            pb.setMergeBatchId(pre);
            Long sum = produceBatchService.sumByBatchIdPre(pre);
            pb.setMergeBatchNumber(sum+"");

            // 查询裁断的进度表
            List<ProduceBatchProgress> progresses = produceBatchProgressService.listByBatchId(pb.getId());
            if(progresses==null || progresses.isEmpty()){
                progresses= new ArrayList<>();
            }
            List<ProduceBatchProgress> ownProgress = new ArrayList<>();

            for(ProduceBatchProgress progress : progresses){

                if(typeIds.contains(progress.getCostOfLabourTypeId())){
                    ownProgress.add(progress);
                }
            }
            pb.setProgresses(ownProgress);

            // 查询针车的进度表
            if(typeIds.contains(2L)){
                List<ProduceBatchZcProgress> zcProgresses = produceBatchZcProgressService.listByBatchId(pb.getId());
                for(ProduceBatchZcProgress progress : zcProgresses){
                    if(progress.getZcGroupId()!=null){
                        ProduceZcGroup group = produceZcGroupService.getById(progress.getZcGroupId());
                        progress.setZcGroupName(group.getGroupName());
                    }

                }
                pb.setZcProgresses(zcProgresses);

            }else{
                pb.setZcProgresses(new ArrayList<>());
            }

            List<ProduceBatchDelay> delays = produceBatchDelayService.listByBatchId(pb.getId());
            if(delays==null || delays.isEmpty()){
                delays= new ArrayList<>();
            }
            List<ProduceBatchDelay> ownDelays = new ArrayList<>();

            for(ProduceBatchDelay delay : delays){
                if(typeIds.contains(delay.getCostOfLabourTypeId())){
                    ownDelays.add(delay);
                }
            }
            pb.setDelays(ownDelays);
        }

        return ResponseResult.succ(pageData);
    }

    /***
     * @param principal
     * @param produceBatch
     * @return
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('produce:batch:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody ProduceBatch produceBatch) {
        LocalDateTime now = LocalDateTime.now();
        try {
                produceBatch.setCreated(now);
                produceBatch.setUpdated(now);
                produceBatch.setCreatedUser(principal.getName());
                produceBatch.setUpdatedUser(principal.getName());
                produceBatch.setStatus(DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_1);
                //判断订单号是否存在
                OrderProductOrder order = orderProductOrderService.getByOrderNum(produceBatch.getOrderNum());
                if(order == null){
                    return ResponseResult.fail("【产品订单】不存在该订单号:"+produceBatch.getOrderNum());
                }
            if(produceBatch.getBatchId()!=null && produceBatch.getBatchId().split("-")[0].length() >=9){
                return ResponseResult.fail("批次号(-)之前的位数不能超过8位!");
            }
            produceBatchService.save(produceBatch);
                // 修改同批次号的创建时间
            List<ProduceBatch> batches = produceBatchService.listByLikeRightBatchId(produceBatch.getBatchId().split("-")[0]);
            for(ProduceBatch pb : batches){
                pb.setCreated(now);
            }
            produceBatchService.updateBatchById(batches);

            return ResponseResult.succ("新增成功");
        }
        catch (DuplicateKeyException e2){
            log.error("报错",e2);
            throw new RuntimeException("生产序号不能重复!");
        }
        catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }/***
     * @param principal
     * @param produceBatch
     * @return
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('produce:batch:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody ProduceBatch produceBatch) {
        LocalDateTime now = LocalDateTime.now();
        try {

            produceBatch.setUpdated(now);
            produceBatch.setUpdatedUser(principal.getName());
            //判断订单号是否存在
            OrderProductOrder order = orderProductOrderService.getByOrderNum(produceBatch.getOrderNum());
            if(order == null){
                return ResponseResult.fail("【产品订单】不存在该订单号:"+produceBatch.getOrderNum());
            }
            if(produceBatch.getBatchId()!=null && produceBatch.getBatchId().split("-")[0].length() >=9){
                return ResponseResult.fail("批次号(-)之前的位数不能超过8位!");
            }
            produceBatchService.updateById(produceBatch);
            return ResponseResult.succ("修改成功");
        }
        catch (DuplicateKeyException e2){
            log.error("报错",e2);
            throw new RuntimeException("生产序号不能重复!");
        }
        catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }


    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('produce:batch:list')")
    public ResponseResult queryById(Long id ) {
        ProduceBatch pb = produceBatchService.getById(id);
        return ResponseResult.succ(pb);
    }

    @PostMapping("/queryCaiDuanPrintByIds")
    @PreAuthorize("hasAuthority('produce:batch:list')")
    public ResponseResult queryCaiDuanPrintByIds(@RequestBody Long[] ids ) {

        try{
            HashMap<String, Object> theMap = new HashMap<>();
            List<HashMap<String, Object>> onePageLists = new ArrayList<>();
            for(Long id : ids){

                HashMap<String, Object> onePage = new HashMap<>();

                ArrayList<HashMap<String, String>> subLists = new ArrayList<>();

                onePageLists.add(onePage);
                onePage.put("subList",subLists);

                ProduceBatch pb = produceBatchService.getById(id);
                // 1. 查询订单信息
                OrderProductOrder order = orderProductOrderService.getByOrderNum(pb.getOrderNum());
                if(pb.getSize40()==null||pb.getSize40().isEmpty()){
                    pb.setSize40("0");
                }
                if(pb.getSize41()==null||pb.getSize41().isEmpty()){
                    pb.setSize41("0");
                }
                if(pb.getSize42()==null||pb.getSize42().isEmpty()){
                    pb.setSize42("0");
                }if(pb.getSize43()==null||pb.getSize43().isEmpty()){
                    pb.setSize43("0");
                }if(pb.getSize44()==null||pb.getSize44().isEmpty()){
                    pb.setSize44("0");
                }if(pb.getSize45()==null||pb.getSize45().isEmpty()){
                    pb.setSize45("0");
                }if(pb.getSize46()==null||pb.getSize46().isEmpty()){
                    pb.setSize46("0");
                }if(pb.getSize47()==null||pb.getSize47().isEmpty()){
                    pb.setSize47("0");
                }if(pb.getSize40()==null||pb.getSize40().isEmpty()){
                    pb.setSize40("0");
                }if(pb.getSize39()==null||pb.getSize39().isEmpty()){
                    pb.setSize39("0");
                }if(pb.getSize38()==null||pb.getSize38().isEmpty()){
                    pb.setSize38("0");
                }if(pb.getSize37()==null||pb.getSize37().isEmpty()){
                    pb.setSize37("0");
                }if(pb.getSize36()==null||pb.getSize36().isEmpty()){
                    pb.setSize36("0");
                }if(pb.getSize35()==null||pb.getSize35().isEmpty()){
                    pb.setSize35("0");
                }if(pb.getSize34()==null||pb.getSize34().isEmpty()){
                    pb.setSize34("0");
                }

                onePage.put("productNum",order.getProductNum());
                onePage.put("productBrand",order.getProductBrand());
                onePage.put("productColor",order.getProductColor());

                onePage.put("batchId",pb.getBatchId());
                onePage.put("customerNum",order.getCustomerNum());
                onePage.put("size34",pb.getSize34());
                onePage.put("size35",pb.getSize35());
                onePage.put("size36",pb.getSize36());
                onePage.put("size37",pb.getSize37());
                onePage.put("size38",pb.getSize38());
                onePage.put("size39",pb.getSize39());
                onePage.put("size40",pb.getSize40());
                onePage.put("size41",pb.getSize41());
                onePage.put("size42",pb.getSize42());
                onePage.put("size43",pb.getSize43());
                onePage.put("size44",pb.getSize44());
                onePage.put("size45",pb.getSize45());
                onePage.put("size46",pb.getSize46());
                onePage.put("size47",pb.getSize47());
                BigDecimal theTotalNum = new BigDecimal(pb.getSize34()).add(new BigDecimal(pb.getSize35())).add(new BigDecimal(pb.getSize36()))
                        .add(new BigDecimal(pb.getSize37())).add(new BigDecimal(pb.getSize38())).add(new BigDecimal(pb.getSize39()))
                        .add(new BigDecimal(pb.getSize40())).add(new BigDecimal(pb.getSize41())).add(new BigDecimal(pb.getSize42()))
                        .add(new BigDecimal(pb.getSize43())).add(new BigDecimal(pb.getSize44())).add(new BigDecimal(pb.getSize45()))
                        .add(new BigDecimal(pb.getSize46())).add(new BigDecimal(pb.getSize47()));
                onePage.put("totalNum",theTotalNum.toString());


                // 2. 查询组成结构
//                List<OrderProductOrder> details = produceProductConstituentDetailService.listByNumBrand(order.getProductNum(), order.getProductBrand());
                List<OrderProductOrder> details = produceProductConstituentDetailService.listByMBomId(order.getMaterialBomId());

                for (OrderProductOrder detail : details){
                    String materialId = detail.getMaterialId();
                    // 筛选皮料，
                    if( (materialId.startsWith("01.") || materialId.startsWith("02.01") || materialId.startsWith("03.01") )
                            && detail.getCanShowPrint().equals("0")){
                        HashMap<String, String> theSub = new HashMap<>();
                        theSub.put("materialId",materialId);
                        theSub.put("materialName",detail.getMaterialName());
                        theSub.put("dosage",detail.getDosage());

                        // 计算用量，
                        theSub.put("needNum",calOneOrderNeedNum(pb,detail.getDosage()));
                        theSub.put("comment",detail.getContent());
                        subLists.add(theSub);
                    }
                }

                theMap.put("rowList",onePageLists);
            }
            return ResponseResult.succ(theMap);
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }


    @PostMapping("/queryZhenChePrintByIds")
    @PreAuthorize("hasAuthority('produce:batch:list')")
    public ResponseResult queryZhenChePrintByIds(@RequestBody Long[] ids ) {

        try{
            HashMap<String, Object> theMap = new HashMap<>();
            List<HashMap<String, Object>> onePageLists = new ArrayList<>();

            // 重复的批次号不打印
            HashSet<String> existBatchIdPre = new HashSet<>();

            for(Long id : ids){
                ProduceBatch pb = produceBatchService.getById(id);
                OrderProductOrder order = orderProductOrderService.getByOrderNum(pb.getOrderNum());

                if(!order.getProductNum().contains("S") && !order.getProductNum().contains("L")
                        && !order.getProductNum().contains("s") && !order.getProductNum().contains("l")){
                    continue;
                }
                // 假如对应的货号是单鞋，不需要打印
                String batchIdPre = pb.getBatchId().split("-")[0];

                if(existBatchIdPre.contains(batchIdPre)){
                    continue;
                }
                existBatchIdPre.add(batchIdPre);

                HashMap<String, Object> onePage = new HashMap<>();

                ArrayList<HashMap<String, String>> subLists = new ArrayList<>();

                onePageLists.add(onePage);
                onePage.put("subList",subLists);



                // 1. 查询订单信息
                if(pb.getSize40()==null||pb.getSize40().isEmpty()){
                    pb.setSize40("0");
                }
                if(pb.getSize41()==null||pb.getSize41().isEmpty()){
                    pb.setSize41("0");
                }
                if(pb.getSize42()==null||pb.getSize42().isEmpty()){
                    pb.setSize42("0");
                }if(pb.getSize43()==null||pb.getSize43().isEmpty()){
                    pb.setSize43("0");
                }if(pb.getSize44()==null||pb.getSize44().isEmpty()){
                    pb.setSize44("0");
                }if(pb.getSize45()==null||pb.getSize45().isEmpty()){
                    pb.setSize45("0");
                }if(pb.getSize46()==null||pb.getSize46().isEmpty()){
                    pb.setSize46("0");
                }if(pb.getSize47()==null||pb.getSize47().isEmpty()){
                    pb.setSize47("0");
                }if(pb.getSize40()==null||pb.getSize40().isEmpty()){
                    pb.setSize40("0");
                }if(pb.getSize39()==null||pb.getSize39().isEmpty()){
                    pb.setSize39("0");
                }if(pb.getSize38()==null||pb.getSize38().isEmpty()){
                    pb.setSize38("0");
                }if(pb.getSize37()==null||pb.getSize37().isEmpty()){
                    pb.setSize37("0");
                }if(pb.getSize36()==null||pb.getSize36().isEmpty()){
                    pb.setSize36("0");
                }if(pb.getSize35()==null||pb.getSize35().isEmpty()){
                    pb.setSize35("0");
                }if(pb.getSize34()==null||pb.getSize34().isEmpty()){
                    pb.setSize34("0");
                }
                List<ProduceBatch> batches = produceBatchService.listByLikeRightBatchId(batchIdPre);
                for(ProduceBatch onePb : batches){
                    // 对当前pb 进行其他同batchId的，进行累加
                    if(onePb.getId().equals(pb.getId())){
                        continue;
                    }
                    if(onePb.getSize40()!=null && !onePb.getSize40().isEmpty()){
                        pb.setSize40(BigDecimalUtil.add(pb.getSize40(),onePb.getSize40()).toString() );
                    }
                    if(onePb.getSize41()!=null && !onePb.getSize41().isEmpty()){
                        pb.setSize41(BigDecimalUtil.add(pb.getSize41(),onePb.getSize41()).toString() );
                    }
                    if(onePb.getSize42()!=null && !onePb.getSize42().isEmpty()){
                        pb.setSize42(BigDecimalUtil.add(pb.getSize42(),onePb.getSize42()).toString() );
                    }
                    if(onePb.getSize43()!=null && !onePb.getSize43().isEmpty()){
                        pb.setSize43(BigDecimalUtil.add(pb.getSize43(),onePb.getSize43()).toString() );
                    }
                    if(onePb.getSize44()!=null && !onePb.getSize44().isEmpty()){
                        pb.setSize44(BigDecimalUtil.add(pb.getSize44(),onePb.getSize44()).toString() );
                    }
                    if(onePb.getSize45()!=null && !onePb.getSize45().isEmpty()){
                        pb.setSize45(BigDecimalUtil.add(pb.getSize45(),onePb.getSize45()).toString() );
                    }
                    if(onePb.getSize46()!=null && !onePb.getSize46().isEmpty()){
                        pb.setSize46(BigDecimalUtil.add(pb.getSize46(),onePb.getSize46()).toString() );
                    }
                    if(onePb.getSize47()!=null && !onePb.getSize47().isEmpty()){
                        pb.setSize47(BigDecimalUtil.add(pb.getSize47(),onePb.getSize47()).toString() );
                    }
                    if(onePb.getSize34()!=null && !onePb.getSize34().isEmpty()){
                        pb.setSize34(BigDecimalUtil.add(pb.getSize34(),onePb.getSize34()).toString() );
                    }
                    if(onePb.getSize35()!=null && !onePb.getSize35().isEmpty()){
                        pb.setSize35(BigDecimalUtil.add(pb.getSize35(),onePb.getSize35()).toString() );
                    }
                    if(onePb.getSize36()!=null && !onePb.getSize36().isEmpty()){
                        pb.setSize36(BigDecimalUtil.add(pb.getSize36(),onePb.getSize36()).toString() );
                    }
                    if(onePb.getSize37()!=null && !onePb.getSize37().isEmpty()){
                        pb.setSize37(BigDecimalUtil.add(pb.getSize37(),onePb.getSize37()).toString() );
                    }
                    if(onePb.getSize38()!=null && !onePb.getSize38().isEmpty()){
                        pb.setSize38(BigDecimalUtil.add(pb.getSize38(),onePb.getSize38()).toString() );
                    }
                    if(onePb.getSize39()!=null && !onePb.getSize39().isEmpty()){
                        pb.setSize39(BigDecimalUtil.add(pb.getSize39(),onePb.getSize39()).toString() );
                    }

                }
                pb.setBatchId(batchIdPre);

                onePage.put("productNum",order.getProductNum());
                onePage.put("productBrand",order.getProductBrand());
                onePage.put("productColor",order.getProductColor());

                onePage.put("batchId",pb.getBatchId());
                onePage.put("customerNum",order.getCustomerNum());
                onePage.put("size34",pb.getSize34());
                onePage.put("size35",pb.getSize35());
                onePage.put("size36",pb.getSize36());
                onePage.put("size37",pb.getSize37());
                onePage.put("size38",pb.getSize38());
                onePage.put("size39",pb.getSize39());
                onePage.put("size40",pb.getSize40());
                onePage.put("size41",pb.getSize41());
                onePage.put("size42",pb.getSize42());
                onePage.put("size43",pb.getSize43());
                onePage.put("size44",pb.getSize44());
                onePage.put("size45",pb.getSize45());
                onePage.put("size46",pb.getSize46());
                onePage.put("size47",pb.getSize47());
                BigDecimal theTotalNum = new BigDecimal(pb.getSize34()).add(new BigDecimal(pb.getSize35())).add(new BigDecimal(pb.getSize36()))
                        .add(new BigDecimal(pb.getSize37())).add(new BigDecimal(pb.getSize38())).add(new BigDecimal(pb.getSize39()))
                        .add(new BigDecimal(pb.getSize40())).add(new BigDecimal(pb.getSize41())).add(new BigDecimal(pb.getSize42()))
                        .add(new BigDecimal(pb.getSize43())).add(new BigDecimal(pb.getSize44())).add(new BigDecimal(pb.getSize45()))
                        .add(new BigDecimal(pb.getSize46())).add(new BigDecimal(pb.getSize47()));
                onePage.put("totalNum",theTotalNum.toString());


                // 2. 查询组成结构
//                List<OrderProductOrder> details = produceProductConstituentDetailService.listByNumBrand(order.getProductNum(), order.getProductBrand());
                List<OrderProductOrder> details = produceProductConstituentDetailService.listByMBomId(order.getMaterialBomId());
                for (OrderProductOrder detail : details){
                    String materialId = detail.getMaterialId();

                    // 筛选物料分组
                    if((materialId.startsWith("04.01")  || materialId.startsWith("04.04") || materialId.startsWith("06.05")) && detail.getCanShowPrint().equals("0")){
                        HashMap<String, String> theSub = new HashMap<>();
                        theSub.put("materialId",materialId);
                        theSub.put("materialName",detail.getMaterialName());
                        theSub.put("dosage",detail.getDosage());
                        theSub.put("materialUnit",detail.getMaterialUnit());

                        // 计算用量，
                        theSub.put("needNum",calOneOrderNeedNum(pb,detail.getDosage()));

                        subLists.add(theSub);
                    }
                }

                theMap.put("rowList",onePageLists);
            }
            return ResponseResult.succ(theMap);
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }


    @GetMapping("/queryZhenChePrintById")
    @PreAuthorize("hasAuthority('produce:batch:list')")
    public ResponseResult queryZhenChePrintById(Long id ) {

        try{
            HashMap<String, Object> theMap = new HashMap<>();

            List<HashMap<String, Object>> onePageLists = new ArrayList<>();

            HashMap<String, Object> onePage = new HashMap<>();

            ArrayList<HashMap<String, String>> subLists = new ArrayList<>();

            onePageLists.add(onePage);
            onePage.put("subList",subLists);



            ProduceBatch pb = produceBatchService.getById(id);

            // 1. 查询订单信息
            OrderProductOrder order = orderProductOrderService.getByOrderNum(pb.getOrderNum());
            if(pb.getSize40()==null||pb.getSize40().isEmpty()){
                pb.setSize40("0");
            }
            if(pb.getSize41()==null||pb.getSize41().isEmpty()){
                pb.setSize41("0");
            }
            if(pb.getSize42()==null||pb.getSize42().isEmpty()){
                pb.setSize42("0");
            }if(pb.getSize43()==null||pb.getSize43().isEmpty()){
                pb.setSize43("0");
            }if(pb.getSize44()==null||pb.getSize44().isEmpty()){
                pb.setSize44("0");
            }if(pb.getSize45()==null||pb.getSize45().isEmpty()){
                pb.setSize45("0");
            }if(pb.getSize46()==null||pb.getSize46().isEmpty()){
                pb.setSize46("0");
            }if(pb.getSize47()==null||pb.getSize47().isEmpty()){
                pb.setSize47("0");
            }if(pb.getSize40()==null||pb.getSize40().isEmpty()){
                pb.setSize40("0");
            }if(pb.getSize39()==null||pb.getSize39().isEmpty()){
                pb.setSize39("0");
            }if(pb.getSize38()==null||pb.getSize38().isEmpty()){
                pb.setSize38("0");
            }if(pb.getSize37()==null||pb.getSize37().isEmpty()){
                pb.setSize37("0");
            }if(pb.getSize36()==null||pb.getSize36().isEmpty()){
                pb.setSize36("0");
            }if(pb.getSize35()==null||pb.getSize35().isEmpty()){
                pb.setSize35("0");
            }if(pb.getSize34()==null||pb.getSize34().isEmpty()){
                pb.setSize34("0");
            }

            List<ProduceBatch> batches = produceBatchService.listByLikeRightBatchId(pb.getBatchId().split("-")[0]);
            for(ProduceBatch onePb : batches){
                // 对当前pb 进行其他同batchId的，进行累加
                if(onePb.getId().equals(pb.getId())){
                    continue;
                }
                if(onePb.getSize40()!=null && !onePb.getSize40().isEmpty()){
                    pb.setSize40(BigDecimalUtil.add(pb.getSize40(),onePb.getSize40()).toString() );
                }
                if(onePb.getSize41()!=null && !onePb.getSize41().isEmpty()){
                    pb.setSize41(BigDecimalUtil.add(pb.getSize41(),onePb.getSize41()).toString() );
                }
                if(onePb.getSize42()!=null && !onePb.getSize42().isEmpty()){
                    pb.setSize42(BigDecimalUtil.add(pb.getSize42(),onePb.getSize42()).toString() );
                }
                if(onePb.getSize43()!=null && !onePb.getSize43().isEmpty()){
                    pb.setSize43(BigDecimalUtil.add(pb.getSize43(),onePb.getSize43()).toString() );
                }
                if(onePb.getSize44()!=null && !onePb.getSize44().isEmpty()){
                    pb.setSize44(BigDecimalUtil.add(pb.getSize44(),onePb.getSize44()).toString() );
                }
                if(onePb.getSize45()!=null && !onePb.getSize45().isEmpty()){
                    pb.setSize45(BigDecimalUtil.add(pb.getSize45(),onePb.getSize45()).toString() );
                }
                if(onePb.getSize46()!=null && !onePb.getSize46().isEmpty()){
                    pb.setSize46(BigDecimalUtil.add(pb.getSize46(),onePb.getSize46()).toString() );
                }
                if(onePb.getSize47()!=null && !onePb.getSize47().isEmpty()){
                    pb.setSize47(BigDecimalUtil.add(pb.getSize47(),onePb.getSize47()).toString() );
                }
                if(onePb.getSize34()!=null && !onePb.getSize34().isEmpty()){
                    pb.setSize34(BigDecimalUtil.add(pb.getSize34(),onePb.getSize34()).toString() );
                }
                if(onePb.getSize35()!=null && !onePb.getSize35().isEmpty()){
                    pb.setSize35(BigDecimalUtil.add(pb.getSize35(),onePb.getSize35()).toString() );
                }
                if(onePb.getSize36()!=null && !onePb.getSize36().isEmpty()){
                    pb.setSize36(BigDecimalUtil.add(pb.getSize36(),onePb.getSize36()).toString() );
                }
                if(onePb.getSize37()!=null && !onePb.getSize37().isEmpty()){
                    pb.setSize37(BigDecimalUtil.add(pb.getSize37(),onePb.getSize37()).toString() );
                }
                if(onePb.getSize38()!=null && !onePb.getSize38().isEmpty()){
                    pb.setSize38(BigDecimalUtil.add(pb.getSize38(),onePb.getSize38()).toString() );
                }
                if(onePb.getSize39()!=null && !onePb.getSize39().isEmpty()){
                    pb.setSize39(BigDecimalUtil.add(pb.getSize39(),onePb.getSize39()).toString() );
                }

            }
            pb.setBatchId(pb.getBatchId().split("-")[0]);

            onePage.put("productNum",order.getProductNum());
            onePage.put("productBrand",order.getProductBrand());
            onePage.put("productColor",order.getProductColor());

            onePage.put("batchId",pb.getBatchId());
            onePage.put("customerNum",order.getCustomerNum());
            onePage.put("size34",pb.getSize34());
            onePage.put("size35",pb.getSize35());
            onePage.put("size36",pb.getSize36());
            onePage.put("size37",pb.getSize37());
            onePage.put("size38",pb.getSize38());
            onePage.put("size39",pb.getSize39());
            onePage.put("size40",pb.getSize40());
            onePage.put("size41",pb.getSize41());
            onePage.put("size42",pb.getSize42());
            onePage.put("size43",pb.getSize43());
            onePage.put("size44",pb.getSize44());
            onePage.put("size45",pb.getSize45());
            onePage.put("size46",pb.getSize46());
            onePage.put("size47",pb.getSize47());
            BigDecimal theTotalNum = new BigDecimal(pb.getSize34()).add(new BigDecimal(pb.getSize35())).add(new BigDecimal(pb.getSize36()))
                    .add(new BigDecimal(pb.getSize37())).add(new BigDecimal(pb.getSize38())).add(new BigDecimal(pb.getSize39()))
                    .add(new BigDecimal(pb.getSize40())).add(new BigDecimal(pb.getSize41())).add(new BigDecimal(pb.getSize42()))
                    .add(new BigDecimal(pb.getSize43())).add(new BigDecimal(pb.getSize44())).add(new BigDecimal(pb.getSize45()))
                    .add(new BigDecimal(pb.getSize46())).add(new BigDecimal(pb.getSize47()));
            onePage.put("totalNum",theTotalNum.toString());


            // 2. 查询组成结构
//            List<OrderProductOrder> details = produceProductConstituentDetailService.listByNumBrand(order.getProductNum(), order.getProductBrand());
            List<OrderProductOrder> details = produceProductConstituentDetailService.listByMBomId(order.getMaterialBomId());

            for (OrderProductOrder detail : details){
                String materialId = detail.getMaterialId();
                // 筛选物料分组
                if((materialId.startsWith("04.01") || materialId.startsWith("04.04")  || materialId.startsWith("06.05")) && detail.getCanShowPrint().equals("0")){
                    HashMap<String, String> theSub = new HashMap<>();
                    theSub.put("materialId",materialId);
                    theSub.put("materialName",detail.getMaterialName());
                    theSub.put("dosage",detail.getDosage());
                    theSub.put("materialUnit",detail.getMaterialUnit());
                    // 计算用量，
                    theSub.put("needNum",calOneOrderNeedNum(pb,detail.getDosage()));

                    subLists.add(theSub);
                }
            }

            theMap.put("rowList",onePageLists);
            return ResponseResult.succ(theMap);
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }

    @GetMapping("/queryCaiDuanPrintById")
    @PreAuthorize("hasAuthority('produce:batch:list')")
    public ResponseResult queryCaiDuanPrintById(Long id ) {

        try{
            HashMap<String, Object> theMap = new HashMap<>();

            List<HashMap<String, Object>> onePageLists = new ArrayList<>();

            HashMap<String, Object> onePage = new HashMap<>();

            ArrayList<HashMap<String, String>> subLists = new ArrayList<>();

            onePageLists.add(onePage);
            onePage.put("subList",subLists);



            ProduceBatch pb = produceBatchService.getById(id);
            // 1. 查询订单信息
            OrderProductOrder order = orderProductOrderService.getByOrderNum(pb.getOrderNum());
            if(pb.getSize40()==null||pb.getSize40().isEmpty()){
                pb.setSize40("0");
            }
            if(pb.getSize41()==null||pb.getSize41().isEmpty()){
                pb.setSize41("0");
            }
            if(pb.getSize42()==null||pb.getSize42().isEmpty()){
                pb.setSize42("0");
            }if(pb.getSize43()==null||pb.getSize43().isEmpty()){
                pb.setSize43("0");
            }if(pb.getSize44()==null||pb.getSize44().isEmpty()){
                pb.setSize44("0");
            }if(pb.getSize45()==null||pb.getSize45().isEmpty()){
                pb.setSize45("0");
            }if(pb.getSize46()==null||pb.getSize46().isEmpty()){
                pb.setSize46("0");
            }if(pb.getSize47()==null||pb.getSize47().isEmpty()){
                pb.setSize47("0");
            }if(pb.getSize40()==null||pb.getSize40().isEmpty()){
                pb.setSize40("0");
            }if(pb.getSize39()==null||pb.getSize39().isEmpty()){
                pb.setSize39("0");
            }if(pb.getSize38()==null||pb.getSize38().isEmpty()){
                pb.setSize38("0");
            }if(pb.getSize37()==null||pb.getSize37().isEmpty()){
                pb.setSize37("0");
            }if(pb.getSize36()==null||pb.getSize36().isEmpty()){
                pb.setSize36("0");
            }if(pb.getSize35()==null||pb.getSize35().isEmpty()){
                pb.setSize35("0");
            }if(pb.getSize34()==null||pb.getSize34().isEmpty()){
                pb.setSize34("0");
            }

            onePage.put("productNum",order.getProductNum());
            onePage.put("productBrand",order.getProductBrand());
            onePage.put("productColor",order.getProductColor());

            onePage.put("batchId",pb.getBatchId());
            onePage.put("customerNum",order.getCustomerNum());
            onePage.put("size34",pb.getSize34());
            onePage.put("size35",pb.getSize35());
            onePage.put("size36",pb.getSize36());
            onePage.put("size37",pb.getSize37());
            onePage.put("size38",pb.getSize38());
            onePage.put("size39",pb.getSize39());
            onePage.put("size40",pb.getSize40());
            onePage.put("size41",pb.getSize41());
            onePage.put("size42",pb.getSize42());
            onePage.put("size43",pb.getSize43());
            onePage.put("size44",pb.getSize44());
            onePage.put("size45",pb.getSize45());
            onePage.put("size46",pb.getSize46());
            onePage.put("size47",pb.getSize47());
            BigDecimal theTotalNum = new BigDecimal(pb.getSize34()).add(new BigDecimal(pb.getSize35())).add(new BigDecimal(pb.getSize36()))
                    .add(new BigDecimal(pb.getSize37())).add(new BigDecimal(pb.getSize38())).add(new BigDecimal(pb.getSize39()))
                    .add(new BigDecimal(pb.getSize40())).add(new BigDecimal(pb.getSize41())).add(new BigDecimal(pb.getSize42()))
                    .add(new BigDecimal(pb.getSize43())).add(new BigDecimal(pb.getSize44())).add(new BigDecimal(pb.getSize45()))
                    .add(new BigDecimal(pb.getSize46())).add(new BigDecimal(pb.getSize47()));
            onePage.put("totalNum",theTotalNum.toString());


            // 2. 查询组成结构
//            List<OrderProductOrder> details = produceProductConstituentDetailService.listByNumBrand(order.getProductNum(), order.getProductBrand());
            List<OrderProductOrder> details = produceProductConstituentDetailService.listByMBomId(order.getMaterialBomId());

            for (OrderProductOrder detail : details){
                String materialId = detail.getMaterialId();
                // 筛选皮料，
                if((materialId.startsWith("01.") || materialId.startsWith("02.01") || materialId.startsWith("03.01") ) && detail.getCanShowPrint().equals("0")){
                    HashMap<String, String> theSub = new HashMap<>();
                    theSub.put("materialId",materialId);
                    theSub.put("materialName",detail.getMaterialName());
                    theSub.put("dosage",detail.getDosage());

                    // 计算用量，
                    theSub.put("needNum",calOneOrderNeedNum(pb,detail.getDosage()));
                    theSub.put("comment",detail.getContent());

                    subLists.add(theSub);
                }
            }

            theMap.put("rowList",onePageLists);
            return ResponseResult.succ(theMap);
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }

    private String calOneOrderNeedNum(ProduceBatch pb, String dosage) {
        String theRadio = "0";
        BigDecimal bd41 = BigDecimalUtil.mul(
                        BigDecimalUtil.add(dosage,
                                BigDecimalUtil.mul(theRadio,"1").toString()
                        ).toString()
                ,pb.getSize41());
        BigDecimal bd42 = BigDecimalUtil.mul(
                BigDecimalUtil.add(dosage,
                        BigDecimalUtil.mul(theRadio,"2").toString()
                ).toString()
                ,pb.getSize42());

        BigDecimal bd43 = BigDecimalUtil.mul(
                BigDecimalUtil.add(dosage,
                        BigDecimalUtil.mul(theRadio,"3").toString()
                ).toString()
                ,pb.getSize43() );

        BigDecimal bd44 = BigDecimalUtil.mul(
                BigDecimalUtil.add(dosage,
                        BigDecimalUtil.mul(theRadio,"4").toString()
                ).toString()
                ,pb.getSize44() );

        BigDecimal bd45 = BigDecimalUtil.mul(
                BigDecimalUtil.add(dosage,
                        BigDecimalUtil.mul(theRadio,"5").toString()
                ).toString()
                ,pb.getSize45() );

        BigDecimal bd46 = BigDecimalUtil.mul(
                BigDecimalUtil.add(dosage,
                        BigDecimalUtil.mul(theRadio,"6").toString()
                ).toString()
                ,pb.getSize46() );

        BigDecimal bd47 = BigDecimalUtil.mul(
                BigDecimalUtil.add(dosage,
                        BigDecimalUtil.mul(theRadio,"7").toString()
                ).toString()
                ,pb.getSize47() );


        BigDecimal bd39 = BigDecimalUtil.mul(
                BigDecimalUtil.sub(dosage,
                        BigDecimalUtil.mul(theRadio,"1").toString()
                ).toString()
                ,pb.getSize39() );

        BigDecimal bd38 = BigDecimalUtil.mul(
                BigDecimalUtil.sub(dosage,
                        BigDecimalUtil.mul(theRadio,"2").toString()
                ).toString()
                ,pb.getSize38() );
        BigDecimal bd37 = BigDecimalUtil.mul(
                BigDecimalUtil.sub(dosage,
                        BigDecimalUtil.mul(theRadio,"3").toString()
                ).toString()
                ,pb.getSize37() );
        BigDecimal bd36 = BigDecimalUtil.mul(
                BigDecimalUtil.sub(dosage,
                        BigDecimalUtil.mul(theRadio,"4").toString()
                ).toString()
                ,pb.getSize36() );
        BigDecimal bd35 = BigDecimalUtil.mul(
                BigDecimalUtil.sub(dosage,
                        BigDecimalUtil.mul(theRadio,"5").toString()
                ).toString()
                ,pb.getSize35() );
        BigDecimal bd34 = BigDecimalUtil.mul(
                BigDecimalUtil.sub(dosage,
                        BigDecimalUtil.mul(theRadio,"6").toString()
                ).toString()
                ,pb.getSize34());

        BigDecimal bd40 = BigDecimalUtil.mul(dosage,pb.getSize40());

        return bd34.add(bd35).add(bd36).add(bd37).add(bd38).add(bd39).add(bd40).add(bd41)
                .add(bd42).add(bd43).add(bd44).add(bd45).add(bd46).add(bd47).toString();
    }

    /**
     *  只允许数字和-出现
     * @return
     */
    public  Boolean isMatchesBatchIdRegEx(String str) {
        Pattern pattern = Pattern.compile("\\d+-?\\d+");
        return pattern.matcher(str).matches();

    }
    /**
     * 上传
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('produce:batch:import')")
    public ResponseResult upload(Principal principal, MultipartFile[] files) {
        MultipartFile file = files[0];

        log.info("上传内容: files:{}",file);
        ExcelImportUtil<ProduceBatch> utils = new ExcelImportUtil<ProduceBatch>(ProduceBatch.class);
        List<ProduceBatch> batches = new ArrayList<ProduceBatch>();
        try (InputStream fis = file.getInputStream();){
            List<ProduceBatch> excelBatch = utils.readExcel(fis, 1, 0,-1,null);
            for (ProduceBatch batch : excelBatch){
                if(batch.getBatchId()!=null && !batch.getBatchId().isEmpty()){
                    if(this.isMatchesBatchIdRegEx(batch.getBatchId())){
                        batches.add(batch);
                    }else{
                        return ResponseResult.fail("批次号ID："+batch.getBatchId()+"不是数字和(0或1个'-')字符的组合");
                    }
                }
                if(batch.getBatchId()!=null && batch.getBatchId().split("-")[0].length() >=9){
                    return ResponseResult.fail("批次号(-)之前的位数不能超过8位!");
                }

            }
            log.info("解析的excel数据:{}",batches);


            if(batches == null || batches.size() == 0){
                return ResponseResult.fail("解析内容未空");
            }
            ArrayList<Map<String,String>> errorMsgs = new ArrayList<>();
            ArrayList<String> ids = new ArrayList<>();
            HashSet<String> orderNums = new HashSet<>();

            LocalDateTime now = LocalDateTime.now();

            // excel 的批次号数据
            HashMap<String, List<ProduceBatch>> excelOrderNum_pb = new HashMap<>();


            for (ProduceBatch batch: batches){
                batch.setCreated(now);
                batch.setUpdated(now);
                batch.setCreatedUser(principal.getName());
                batch.setUpdatedUser(principal.getName());
                batch.setStatus(DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_0);

                ids.add(batch.getBatchId());
                orderNums.add(batch.getOrderNum());

                List<ProduceBatch> orderNum_pbs = excelOrderNum_pb.get(batch.getOrderNum());
                if(orderNum_pbs==null){
                    orderNum_pbs = new ArrayList<ProduceBatch>();
                    excelOrderNum_pb.put(batch.getOrderNum(),orderNum_pbs);
                }
                orderNum_pbs.add(batch);
            }

            List<ProduceBatch> exist = produceBatchService.list(new QueryWrapper<ProduceBatch>().in(DBConstant.TABLE_PRODUCE_BATCH.BATCH_ID_FIELDNAME, ids));
            if(exist != null && !exist.isEmpty()){
                for (ProduceBatch existOne:exist){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content","生产序号："+existOne.getBatchId()+"已存在");
                    errorMsgs.add(errorMsg);
                }
                return ResponseResult.succ(errorMsgs);
            }

            List<OrderProductOrder> orders = orderProductOrderService.listByOrderNums(orderNums);
            if(orders==null || orders.size()==0){
                return ResponseResult.fail("没查到任何订单号");
            }else{
                Set<String> dbOrderNums = new HashSet<>();
                StringBuilder sb = new StringBuilder();

                for (OrderProductOrder order : orders){
                    dbOrderNums.add(order.getOrderNum());
                    if(order.getOrderType().equals(2)){
                        sb.append("订单号:").append(order.getOrderNum()).append("是取消类型！");
                    }

                    // 1. 校验系统的订单和生产序号的工厂货号、品牌是否一致
                    List<ProduceBatch> pbs = excelOrderNum_pb.get(order.getOrderNum());
                    ProduceBatch pb = pbs.get(0);
                    String batchIdPre = pb.getBatchId().split("-")[0];

                    if(!pb.getProductNum().equals(order.getProductNum()) ||
                    !pb.getProductBrand().equals(order.getProductBrand())){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content","订单号："+order.getOrderNum()+",进度表货号:"+pb.getProductNum()
                        +"，品牌:"+pb.getProductBrand()+"。与订单的货号:"+order.getProductNum()+",品牌:"+order.getProductBrand()
                        +"。存在不一致!!!请注意！");
                        errorMsgs.add(errorMsg);
                    }

                    // 2. 校验订单和生产序号的数量是否一致。(目前逻辑已修改：一个订单可能分多次投)

                    // 2.1 先查询db中，该生产序号开头的全部批次号，db的数量
                    /*List<ProduceBatch> dbBatches = produceBatchService.listByLikeRightBatchId(batchIdPre);
                    Double dbNum = 0D;
                    for(ProduceBatch dbPb:dbBatches){
                        Double oneDbPbTotalNum = dbPbTotalNum(dbPb);
                        dbNum+=oneDbPbTotalNum;
                    }

                    // 2.2. 再查询excel中，该生产序号开头的全部批次号，excel的数量。和DB求和，跟订单表的总数量进行对比
                    Double excelNum = 0D;
                    for(ProduceBatch excelPb : pbs){
                        Double oneDbPbTotalNum = dbPbTotalNum(excelPb);
                        excelNum+=oneDbPbTotalNum;

                    }
                    Integer produceBatchTotalNumber = BigDecimalUtil.add(dbNum, excelNum).intValue();
                    if(!produceBatchTotalNumber.equals(order.getOrderNumber()) ){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content","订单号："+order.getOrderNum()+",订单数量:"+order.getOrderNumber()
                                +"  != 批次号:"+batchIdPre+"。数据库已存在数量:"+dbNum+",新导入生管进度表数量:"+excelNum
                                +",求和："+produceBatchTotalNumber+"...请注意!!!");
                        errorMsgs.add(errorMsg);

                    }*/

                }
                for (String uploadOrderNum : orderNums){
                    if(!dbOrderNums.contains(uploadOrderNum)){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content","订单号："+uploadOrderNum+"不存在");
                        errorMsgs.add(errorMsg);
                    }
                }


                if(sb.toString().length()>0){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content",sb.toString());
                    errorMsgs.add(errorMsg);
                }

                if(errorMsgs.size() > 0){
                    return ResponseResult.succ(errorMsgs);
                }
            }
            produceBatchService.saveBatch(batches);

            for(ProduceBatch produceBatch : batches){
                // 修改同批次号的创建时间
                List<ProduceBatch> oldBatches = produceBatchService.listByLikeRightBatchId(produceBatch.getBatchId().split("-")[0]);
                for(ProduceBatch pb : oldBatches){
                    pb.setCreated(now);
                }
                produceBatchService.updateBatchById(oldBatches);
            }
        }
        catch (Exception e) {
            if( e instanceof  DuplicateKeyException){
                return ResponseResult.fail("生产序号重复！");
            }
            log.error("发生错误:",e);
            throw new RuntimeException(e.getMessage());
        }

        return ResponseResult.succ("上传成功");
    }

    @PostMapping("/down")
    @PreAuthorize("hasAuthority('produce:batch:save')")
    public ResponseResult down(HttpServletResponse response, Long id)throws Exception {
        response.setContentType("application/octet-stream");
        response.setHeader("content-disposition", "attachment;filename=" + new String("生产序号导入模板".getBytes("ISO8859-1")));
        response.setHeader("filename","生产序号导入模板" );

        FileInputStream fis = new FileInputStream(new File(poiImportDemoPath));
        FileCopyUtils.copy(fis,response.getOutputStream());
        return ResponseResult.succ("下载成功");
    }

    public static Double dbPbTotalNum(ProduceBatch pb){
        if(pb.getSize40()==null||pb.getSize40().isEmpty()){
            pb.setSize40("0");
        }
        if(pb.getSize41()==null||pb.getSize41().isEmpty()){
            pb.setSize41("0");
        }
        if(pb.getSize42()==null||pb.getSize42().isEmpty()){
            pb.setSize42("0");
        }if(pb.getSize43()==null||pb.getSize43().isEmpty()){
            pb.setSize43("0");
        }if(pb.getSize44()==null||pb.getSize44().isEmpty()){
            pb.setSize44("0");
        }if(pb.getSize45()==null||pb.getSize45().isEmpty()){
            pb.setSize45("0");
        }if(pb.getSize46()==null||pb.getSize46().isEmpty()){
            pb.setSize46("0");
        }if(pb.getSize47()==null||pb.getSize47().isEmpty()){
            pb.setSize47("0");
        }if(pb.getSize39()==null||pb.getSize39().isEmpty()){
            pb.setSize39("0");
        }if(pb.getSize38()==null||pb.getSize38().isEmpty()){
            pb.setSize38("0");
        }if(pb.getSize37()==null||pb.getSize37().isEmpty()){
            pb.setSize37("0");
        }if(pb.getSize36()==null||pb.getSize36().isEmpty()){
            pb.setSize36("0");
        }if(pb.getSize35()==null||pb.getSize35().isEmpty()){
            pb.setSize35("0");
        }if(pb.getSize34()==null||pb.getSize34().isEmpty()){
            pb.setSize34("0");
        }
        BigDecimal theTotalNum = new BigDecimal(pb.getSize34()).add(new BigDecimal(pb.getSize35())).add(new BigDecimal(pb.getSize36()))
                .add(new BigDecimal(pb.getSize37())).add(new BigDecimal(pb.getSize38())).add(new BigDecimal(pb.getSize39()))
                .add(new BigDecimal(pb.getSize40())).add(new BigDecimal(pb.getSize41())).add(new BigDecimal(pb.getSize42()))
                .add(new BigDecimal(pb.getSize43())).add(new BigDecimal(pb.getSize44())).add(new BigDecimal(pb.getSize45()))
                .add(new BigDecimal(pb.getSize46())).add(new BigDecimal(pb.getSize47()));
        return theTotalNum.doubleValue();
    }

    public static void main(String[] args) {
        String str1 = "100.1";
        String str2 = "100-10";
        String str3 = "100- 1";
        Pattern pattern = Pattern.compile("\\d+-?\\d+");
        System.out.println(pattern.matcher(str1).matches());
        System.out.println(pattern.matcher(str2).matches());
        System.out.println(pattern.matcher(str3).matches());

        LocalDate now = LocalDate.now().plusDays(-300);

        System.out.println(now);

    }

}
