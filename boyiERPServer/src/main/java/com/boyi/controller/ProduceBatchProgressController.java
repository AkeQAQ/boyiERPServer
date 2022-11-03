package com.boyi.controller;


import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.ProduceBatch;
import com.boyi.entity.ProduceBatchProgress;
import com.boyi.entity.RepositoryPickMaterial;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public ResponseResult updateProgress(Principal principal, @RequestBody List<ProduceBatchProgress> progresses) {
        LocalDateTime now = LocalDateTime.now();
        String userName = principal.getName();
        try {
            for(ProduceBatchProgress progress : progresses){
                if(progress.getSupplierId() ==null || progress.getSupplierName().isEmpty()
                        || progress.getSupplierName().equals("空值")
                        ||progress.getMaterialId() ==null
                        || progress.getMaterialName().equals("空值")
                        || progress.getMaterialName().isEmpty()){
                    continue;
                }
                //新增
                if(progress.getId()==null){
                    progress.setCreated(now);
                    progress.setCreatedUser(userName);
                    produceBatchProgressService.save(progress);

                }else{
                    progress.setUpdated(now);
                    progress.setUpdateUser(userName);
                    produceBatchProgressService.updateById(progress);

                    if(progress.getSendForeignProductDate()==null){

                        produceBatchProgressService.updateNullByField(
                                DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.SEND_FOREIGN_PRODUCT_DATE_FIELDNAME
                        ,progress.getId());
                    }
                    if(progress.getBackForeignProductDate()==null){

                        produceBatchProgressService.updateNullByField(
                                DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.BACK_FOREIGN_PRODUCT_DATE_FIELDNAME
                                ,progress.getProduceBatchId());
                    }
                    if(progress.getOutDate()==null){

                        produceBatchProgressService.updateNullByField(
                                DBConstant.TABLE_PRODUCE_BATCH_PROGRESS.OUT_DATE_FIELDNAME
                                ,progress.getId());
                    }
                }

            }

        }

        catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }
        return ResponseResult.succ("修改成功");

    }
}
