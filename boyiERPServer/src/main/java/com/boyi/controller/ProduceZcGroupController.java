package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseSupplier;
import com.boyi.entity.FinanceSummaryFilters;
import com.boyi.entity.ProduceZcGroup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2023-04-03
 */
@RestController
@RequestMapping("/produce/zcGroup")
public class ProduceZcGroupController extends BaseController {


    @PostMapping("/save")
    @PreAuthorize("hasAuthority('produce:zcGroup:list')")
    public ResponseResult save(Principal principal, @Validated @RequestBody List<ProduceZcGroup> zcGroups) {

        produceZcGroupService.saveOrUpdateBatch(zcGroups);
        return ResponseResult.succ("保存成功");
    }

    @PostMapping("/list")
    public ResponseResult list() {
        List<ProduceZcGroup> result = produceZcGroupService.list();
        return ResponseResult.succ(result);
    }

    /**
     * 获取全部
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('produce:zcGroup:list')")
    public ResponseResult getSearchAllData() {
        List<ProduceZcGroup> groups = produceZcGroupService.list();

        ArrayList<Map<Object,Object>> returnList = new ArrayList<>();
        groups.forEach(obj ->{
            Map<Object, Object> returnMap = MapUtil.builder().put("value",obj.getId()+" : "+obj.getGroupName() ).put("id", obj.getId()).put("name", obj.getGroupName()).map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }

}
