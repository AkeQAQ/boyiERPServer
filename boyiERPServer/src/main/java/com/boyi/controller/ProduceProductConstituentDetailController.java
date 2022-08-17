package com.boyi.controller;


import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseMaterial;
import com.boyi.entity.ProduceOrderMaterialProgress;
import com.boyi.entity.ProduceProductConstituent;
import com.boyi.entity.ProduceProductConstituentDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
 * @since 2022-03-19
 */
@RestController
@RequestMapping("/produce/productConstituentDetail")
public class ProduceProductConstituentDetailController extends BaseController {

    /**
     * 查询入库
     */
    @GetMapping("/changeShowPrint")
    @PreAuthorize("hasAuthority('produce:productConstituent:list')")
    public ResponseResult changeShowPrint(Principal principal,Long id) {
        ProduceProductConstituentDetail old = this.produceProductConstituentDetailService.getById(id);
        if(old==null){
            return ResponseResult.fail("id:"+id+",不存在记录");
        }
        if(old.getCanShowPrint().equals("0")){
            old.setCanShowPrint("1");
        }else{
            old.setCanShowPrint("0");
        }
        old.setUpdatedUser(principal.getName());
        old.setUpdated(LocalDateTime.now());
        this.produceProductConstituentDetailService.updateById(old);
        return ResponseResult.succ("开关状态修改成功!");
    }
}
