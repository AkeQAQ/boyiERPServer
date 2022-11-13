package com.boyi.controller;


import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.CostOfLabourType;
import com.boyi.entity.ProduceBatchDelay;
import com.boyi.entity.ProduceBatchProgress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-11-02
 */
@RestController
@RequestMapping("/produce/batchDelay")
@Slf4j
public class ProduceBatchDelayController extends BaseController {

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('produce:progress:update')")
    public ResponseResult del(@RequestBody Long id) throws Exception{
        try {

            boolean flag = produceBatchDelayService.removeById(id);

            log.info("删除【生产序号延迟表】信息,id:{},是否成功：{}",id,flag?"成功":"失败");
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
    @PostMapping("/updateDelay")
    @PreAuthorize("hasAuthority('produce:progress:update')")
    public ResponseResult updateProgress(Principal principal, @RequestBody List<ProduceBatchDelay> delays) {
        LocalDateTime now = LocalDateTime.now();
        String userName = principal.getName();
        try {
            for(ProduceBatchDelay delay : delays){
                CostOfLabourType type = costOfLabourTypeService.getById(delay.getCostOfLabourTypeId());
                delay.setCostOfLabourTypeName(type.getTypeName());
                //新增
                if(delay.getId()==null){
                    if((delay.getMaterialId()==null || delay.getMaterialId().isEmpty()) && delay.getMaterialName()!=null&&!delay.getMaterialName().isEmpty()){
                        throw new RuntimeException("物料:"+delay.getMaterialName()+"没有选择，请确认");
                    }
                    delay.setCreated(now);
                    delay.setCreatedUser(userName);
                    produceBatchDelayService.save(delay);

                }else{
                    if(delay.getMaterialName()==null||delay.getMaterialName().isEmpty()){
                        delay.setMaterialId(null);
                        delay.setMaterialName(null);
                    }

                    delay.setUpdated(now);
                    delay.setUpdateUser(userName);
                    produceBatchDelayService.updateById(delay);

                    if(delay.getDate()==null){
                        produceBatchDelayService.updateNullByField(
                                DBConstant.TABLE_PRODUCE_BATCH_DELAY.DATE_FIELDNAME
                                ,delay.getId());
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
