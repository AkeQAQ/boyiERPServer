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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2023-04-02
 */
@RestController
@RequestMapping("/produce/batchZCProgress")
@Slf4j
public class ProduceBatchZcProgressController extends BaseController {



    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('produce:zcProgress:update')")
    public ResponseResult del(@RequestBody Long id) throws Exception{
        try {

            boolean flag = produceBatchZcProgressService.removeById(id);

            log.info("删除【针车进度表】信息,id:{},是否成功：{}",id,flag?"成功":"失败");
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
    @PreAuthorize("hasAuthority('produce:zcProgress:update')")
    @Transactional
    public ResponseResult updateProgress(Principal principal, @RequestBody List<ProduceBatchZcProgress> progresses) {
        LocalDateTime now = LocalDateTime.now();
        String userName = principal.getName();
        try {
            if(progresses.size()>1){
                throw new RuntimeException("不能有2个进度表");
            }
            /*HashMap<Long, String> batchUniqueId_batchIdStr = new HashMap<>();

            HashMap<String, Integer> batchIdStr_count = new HashMap<>();

            Map<Long, ProduceBatch> batchUniqueId_pb = new HashMap<>();


            // 判断一个批次号只能有一个出库日期
            for(ProduceBatchZcProgress progress : progresses) {
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
            }*/

           /* for(Map.Entry<String,Integer> entry : batchIdStr_count.entrySet()){
                String batchIdStr = entry.getKey();
                Integer count = entry.getValue();

                if(count !=null && count > 1){
                    return ResponseResult.fail("批次号:"+batchIdStr+",有"+count +"个出库日期，不允许。");
                }

            }*/

            for(ProduceBatchZcProgress progress : progresses){
                //新增
                if(progress.getId()==null){

                    progress.setCreated(now);
                    progress.setCreatedUser(userName);
                    produceBatchZcProgressService.save(progress);

                }else{

                    progress.setUpdated(now);
                    progress.setUpdateUser(userName);
                    produceBatchZcProgressService.updateById(progress);
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
