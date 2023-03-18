package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2023-03-17
 */
@RestController
@RequestMapping("/finance/summaryFilters")
public class FinanceSummaryFiltersController extends BaseController {


    @PostMapping("/save")
    @PreAuthorize("hasAuthority('finance:summaryFilter:list')")
    public ResponseResult save(Principal principal, @Validated @RequestBody List<FinanceSummaryFilters> financeSummaryFilters) {

        financeSummaryFiltersService.remove(new QueryWrapper<FinanceSummaryFilters>()
                .gt("id",0));
        if(financeSummaryFilters==null || financeSummaryFilters.size()==0){
            return ResponseResult.succ("已清空!");
        }else{
            financeSummaryFiltersService.saveBatch(financeSummaryFilters);
        }
        return ResponseResult.succ("保存成功");
    }

    @PostMapping("/list")
    public ResponseResult list() {
        List<FinanceSummaryFilters> result = financeSummaryFiltersService.list();
        for(FinanceSummaryFilters f : result){
            BaseSupplier bs = baseSupplierService.getById(f.getSupplierId());
            f.setSupplierName(bs.getName());
        }
        return ResponseResult.succ(result);
    }
}
