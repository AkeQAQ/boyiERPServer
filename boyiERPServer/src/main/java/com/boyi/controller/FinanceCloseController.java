package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 仓库关账模块 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2023-03-02
 */
@RestController
@RequestMapping("/finance/close")
public class FinanceCloseController extends BaseController {


    /**
     * 获取部门 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('finance:close:list')")
    public ResponseResult list() {
        List<FinanceClose> list = financeCloseService.list(new QueryWrapper<FinanceClose>()
                .orderByDesc(DBConstant.TABLE_FINANCE_CLOSE.CLOSE_DATE_FIELDNAME));
        return ResponseResult.succ(list);
    }


    @PostMapping("/save")
    @PreAuthorize("hasAuthority('finance:close:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody FinanceClose financeClose) {
        LocalDateTime now = LocalDateTime.now();
        String name = principal.getName();
        financeClose.setUpdatedUser(name);
        financeClose.setCreatedUser(name);
        financeClose.setUpdated(now);
        financeClose.setCreated(now);
        // 判断在该日期之前（包含该日期），存在未审核的单据
        List<FinanceSupplierPayshoes> payshoes = financeSupplierPayshoesService.countLTByCloseDate(financeClose.getCloseDate());
        List<FinanceSupplierChange> changes = financeSupplierChangeService.countLTByCloseDate(financeClose.getCloseDate());
        List<FinanceSupplierFine> fines = financeSupplierFineService.countLTByCloseDate(financeClose.getCloseDate());
        List<FinanceSupplierTest> testes = financeSupplierTestService.countLTByCloseDate(financeClose.getCloseDate());
        List<FinanceSupplierTaxSupplement> taxSupplements = financeSupplierTaxSupplementService.countLTByCloseDate(financeClose.getCloseDate());
        List<FinanceSupplierTaxDeduction> taxDeduction = financeSupplierTaxDeductionService.countLTByCloseDate(financeClose.getCloseDate());

        List<Map<String,String>> strList = new ArrayList<Map<String,String>>();

        if( payshoes.size() > 0  ||  changes.size() > 0 ||  fines.size() > 0 ||  testes.size() > 0
                || taxSupplements.size() > 0 || taxDeduction.size() > 0){

            for (FinanceSupplierPayshoes obj:payshoes){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","存在未审核通过的"+"[供应商赔鞋]["+obj.getId()+"]");
                strList.add(map);
            }
            for (FinanceSupplierChange obj:changes){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","存在未审核通过的"+"[供应商单价调整]["+obj.getId()+"]");
                strList.add(map);
            }
            for (FinanceSupplierFine obj:fines){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","存在未审核通过的"+"[供应商罚款]["+obj.getId()+"]");
                strList.add(map);
            }
            for (FinanceSupplierTest obj:testes){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","存在未审核通过的"+"[供应商检测费]["+obj.getId()+"]");
                strList.add(map);
            }
            for (FinanceSupplierTaxSupplement obj:taxSupplements){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","存在未审核通过的"+"[供应商补税点]["+obj.getId()+"]");
                strList.add(map);
            }
            for (FinanceSupplierTaxDeduction obj:taxDeduction){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","存在未审核通过的"+"[供应商扣税点]["+obj.getId()+"]");
                strList.add(map);
            }


            return ResponseResult.succ(200,"存在未审核通过的单据",strList);
        }

        financeCloseService.save(financeClose);
        return ResponseResult.succ("新增成功");
    }

    @GetMapping("/return")
    @PreAuthorize("hasAuthority('finance:close:return')")
    public ResponseResult returnLast(Principal principal) {
        LocalDateTime now = LocalDateTime.now();
        List<FinanceClose> lists = financeCloseService.list(new QueryWrapper<FinanceClose>().orderByDesc(DBConstant.TABLE_FINANCE_CLOSE.CLOSE_DATE_FIELDNAME));
        if(lists.size() <= 1){
            return ResponseResult.fail("历史<=1条记录，无法回退");
        }
        financeCloseService.removeById(lists.get(0).getId());// 删除最近的一条

        return ResponseResult.succ("回退成功");
    }
}
