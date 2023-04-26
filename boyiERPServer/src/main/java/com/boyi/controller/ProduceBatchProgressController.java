package com.boyi.controller;


import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-10-31
 */
@RestController
@RequestMapping("/produce/batchProgress")
@Slf4j
public class ProduceBatchProgressController extends BaseController {


    @Transactional
    @PostMapping("/accept")
    @PreAuthorize("hasAuthority('produce:progress:update')")
    public ResponseResult accept(Principal principal,String id) throws Exception{
        try {
            if(id==null||id.equals("null")){
                return ResponseResult.fail("id是null，不能接收!");
            }
            ProduceBatchProgress progress = produceBatchProgressService.getById(id);
            // 查询该批次号前缀全部的进度表，修改成已接收
            List<ProduceBatchProgress> progresses = produceBatchProgressService.listByProduceBatchIdByCostOfLabourTypeId(progress.getProduceBatchId(), progress.getCostOfLabourTypeId());
            for(ProduceBatchProgress pro : progresses){
                pro.setUpdated(LocalDateTime.now());
                pro.setUpdateUser(principal.getName());
                pro.setIsAccept(DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.ACCEPT_STATUS_FIELDVALUE_0);
                produceBatchProgressService.updateById(pro);
            }

            return ResponseResult.succ("已被接收");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }


    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('produce:progress:update')")
    public ResponseResult del(@RequestBody Long id) throws Exception{
        try {

            boolean flag = produceBatchProgressService.removeById(id);

            log.info("删除【生成进度表】信息,id:{},是否成功：{}",id,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("删除失败");
            }
            return ResponseResult.succ("删除成功");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    /***
     * @param principal
     * @return
     */
    @PostMapping("/updateProgress")
    @PreAuthorize("hasAuthority('produce:progress:update')")
    @Transactional
    public ResponseResult updateProgress(Principal principal, @RequestBody List<ProduceBatchProgress> progresses) {
        LocalDateTime now = LocalDateTime.now();
        String userName = principal.getName();
        try {
            HashMap<Long, String> batchUniqueId_batchIdStr = new HashMap<>();

            HashMap<String, Integer> batchIdStr_count = new HashMap<>();

            Map<Long, ProduceBatch> batchUniqueId_pb = new HashMap<>();


            // 判断一个批次号只能有一个出库日期
            for(ProduceBatchProgress progress : progresses) {
                String batchIdStr = batchUniqueId_batchIdStr.get(progress.getProduceBatchId());

                if(batchIdStr==null){
                    ProduceBatch pb = produceBatchService.getById(progress.getProduceBatchId());
                    batchUniqueId_pb.put(progress.getProduceBatchId(),pb);

                    batchIdStr = pb.getBatchId().split("-")[0];
                    batchUniqueId_batchIdStr.put(progress.getProduceBatchId(),batchIdStr);
                }

                if(progress.getOutDate()!=null){
                    Integer oldCount = batchIdStr_count.get(batchIdStr)==null?0:batchIdStr_count.get(batchIdStr);
                    batchIdStr_count.put(batchIdStr,oldCount+1);
                }
            }

            for(Map.Entry<String,Integer> entry : batchIdStr_count.entrySet()){
                String batchIdStr = entry.getKey();
                Integer count = entry.getValue();

                if(count !=null && count > 1){
                    return ResponseResult.fail("批次号:"+batchIdStr+",有"+count +"个出库日期，不允许。");
                }

            }

            for(ProduceBatchProgress progress : progresses){
                if(progress.getCostOfLabourTypeId() ==null || progress.getCostOfLabourTypeName() ==null
                        || progress.getCostOfLabourTypeName().equals("空值")
                       ){
                    continue;
                }

                CostOfLabourType type = costOfLabourTypeService.getById(progress.getCostOfLabourTypeId());
                progress.setCostOfLabourTypeName(type.getTypeName());
                //新增


                if(progress.getId()==null){
                    if((progress.getSupplierId()==null || progress.getSupplierId().isEmpty()) && progress.getSupplierName()!=null&&!progress.getSupplierName().isEmpty()){
                        throw new RuntimeException("供应商:"+progress.getSupplierName()+"没有选择，请确认");
                    }
                    if((progress.getMaterialId()==null || progress.getMaterialId().isEmpty()) && progress.getMaterialName()!=null&&!progress.getMaterialName().isEmpty()){
                        throw new RuntimeException("物料:"+progress.getMaterialName()+"没有选择，请确认");
                    }
                    if(progress.getMaterialId()!=null && type.getSeq()==1 && !progress.getMaterialId().startsWith("11.01")){
                        throw new RuntimeException(type.getTypeName()+",物料:"+progress.getMaterialName()+"，不是11.01分组下的，不能使用");
                    }

                    String batchIdStr = batchUniqueId_batchIdStr.get(progress.getProduceBatchId());

//                    ProduceBatch pb = produceBatchService.getById(progress.getProduceBatchId());
                    if(type.getSeq()> 1 ){
                        // 判断该批次号，前一个部门，有出库时间，并且已被我们接收了的
                        Integer count = produceBatchProgressService.countByBatchIdSeqOutDateAccept(batchIdStr, type.getSeq() - 1);
                        if(count == 0){
                            return ResponseResult.fail("没有前置流程，不能新增");
                        }
                    }

                    progress.setCreated(now);
                    progress.setCreatedUser(userName);
                    produceBatchProgressService.save(progress);

                }else{
                    ProduceBatchProgress old = produceBatchProgressService.getById(progress.getId());
                    if(old.getSupplierId()!=null &&  !old.getSupplierId().isEmpty() && progress.getSupplierName()!=null && !progress.getSupplierName().isEmpty() && old.getSupplierId().equals(progress.getSupplierId())){
                        progress.setSupplierName(old.getSupplierName());
                    }
                    if(old.getMaterialId()!=null &&  !old.getMaterialId().isEmpty() && progress.getMaterialName()!=null && !progress.getMaterialName().isEmpty() && old.getMaterialId().equals(progress.getMaterialId())){
                        progress.setMaterialName(old.getMaterialName());
                    }

                    if(progress.getSupplierName()==null||progress.getSupplierName().isEmpty()){
                        progress.setSupplierId(null);
                    }
                    if(progress.getMaterialName()==null||progress.getMaterialName().isEmpty()){
                        progress.setMaterialId(null);
                    }
                    if(progress.getMaterialId()!=null && type.getSeq()==1 && !progress.getMaterialId().startsWith("11.01")){
                        throw new RuntimeException(type.getTypeName()+",物料:"+progress.getMaterialName()+"，不是11.01分组下的，不能使用");
                    }

                    progress.setUpdated(now);
                    progress.setUpdateUser(userName);
                    produceBatchProgressService.updateById(progress);
                }
                // 假如是裁断部门，则对该品牌、该货号的组成结构进行外加工信息增删改
                if(type.getSeq() == 1 ) {
                    changeProduceProductConstituentMaterial(batchUniqueId_pb.get(progress.getProduceBatchId()).getOrderNum(),progress.getMaterialId(),principal.getName());
                }

            }

        }

        catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }
        return ResponseResult.succ("修改成功");

    }

    private void changeProduceProductConstituentMaterial(String orderNum, String materialId,String name) {
        if(orderNum.isEmpty()|| materialId==null || materialId.isEmpty()){
            return;
        }
        OrderProductOrder opo = orderProductOrderService.getByOrderNum(orderNum);
        if(opo==null){
            return;
        }
        if(opo.getMaterialBomId()==null){
            throw new RuntimeException("没有审核通过的组成结构信息!");
        }
        List<ProduceProductConstituentDetail> details = produceProductConstituentDetailService.listByForeignId(opo.getMaterialBomId());

        // 假如已经存在该物料，则不新增
        for(ProduceProductConstituentDetail detail :details){
            if(detail.getMaterialId().equals(materialId)){
               return;
            }

        }
        ProduceProductConstituentDetail detail2 = new ProduceProductConstituentDetail();
        detail2.setConstituentId(opo.getMaterialBomId());
        detail2.setMaterialId(materialId);
        detail2.setDosage("1");
        detail2.setCanShowPrint("0");
        detail2.setCreatedUser(name);
        produceProductConstituentDetailService.save(detail2);
    }
}
