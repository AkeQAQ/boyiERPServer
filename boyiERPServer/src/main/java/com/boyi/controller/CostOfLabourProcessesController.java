package com.boyi.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.CostOfLabourProcesses;
import com.boyi.service.CostOfLabourProcessesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-10-27
 */
@RestController
@RequestMapping("/costOfLabour/processes")
@Slf4j
public class CostOfLabourProcessesController extends BaseController {

    /**
     * 查询工序
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('costOfLabour:processes:list')")
    public ResponseResult queryById(String id) {
        CostOfLabourProcesses obj = costOfLabourProcessesService.getById(id);
        return ResponseResult.succ(obj);
    }

    /**
     * 获取工序 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('costOfLabour:processes:list')")
    public ResponseResult list(String searchField, String searchStatus,@RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;

        long start = System.currentTimeMillis();
        Page<CostOfLabourProcesses> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("costOfLabourTypeName")) {
                queryField = "cost_of_labour_type_name";
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
                if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(oneField)) {
                    if (oneField.equals("costOfLabourTypeName")) {
                        theQueryField = "cost_of_labour_type_name";
                    }
                    else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        List<Long> searchStatusList = new ArrayList<Long>();
        if(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(searchStatus)){
            String[] split = searchStatus.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(searchStatusList.size() == 0){
            return ResponseResult.fail("状态不能为空");
        }
        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = costOfLabourProcessesService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,queryMap);

        long end = System.currentTimeMillis();
        log.info("查询list耗时:{}ms",(end-start));
        return ResponseResult.succ(pageData);
    }

    /**
     * 新增工序
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('costOfLabour:processes:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody CostOfLabourProcesses costOfLabourProcesses) {
        if(costOfLabourProcesses.getEndDate() != null && costOfLabourProcesses.getEndDate().isBefore(costOfLabourProcesses.getStartDate()) ){
            return ResponseResult.fail("生效日期不能大于失效日期!");
        }
        if(costOfLabourProcesses.getEndDate() == null){
            LocalDate date = LocalDate.of(2100, 01, 01);
            costOfLabourProcesses.setEndDate(date);
        }

        // 查询表中是否已经有该数据，有的话，新增的起始日期要求> 老数据的结束日期
        int count = costOfLabourProcessesService.isRigion(costOfLabourProcesses);

        if(count > 0){
            return ResponseResult.fail("日期区间冲突，请检查!");
        }

        LocalDateTime now = LocalDateTime.now();
        costOfLabourProcesses.setCreated(now);
        costOfLabourProcesses.setUpdated(now);
        costOfLabourProcesses.setCreatedUser(principal.getName());
        costOfLabourProcesses.setUpdateUser(principal.getName());

        if(costOfLabourProcesses.getEndDate() == null){
            costOfLabourProcesses.setEndDate(LocalDate.of(2100,01,01));
        }
        costOfLabourProcesses.setStatus(1);
        try {
            costOfLabourProcessesService.save(costOfLabourProcesses);

            return ResponseResult.succ("新增成功");
        } catch (DuplicateKeyException e) {
            log.error("工序，插入异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }


    /**
     * 修改工序
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('costOfLabour:processes:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody CostOfLabourProcesses costOfLabourProcesses) {
        if(costOfLabourProcesses.getEndDate().isBefore(costOfLabourProcesses.getStartDate()) ){
            return ResponseResult.fail("生效日期不能大于失效日期!");
        }
        // 查询表中是否已经有同工序时间区间冲突
        if(costOfLabourProcesses.getEndDate() == null){
            LocalDate date = LocalDate.of(2100, 01, 01);
            costOfLabourProcesses.setEndDate(date);
        }
        int count = costOfLabourProcessesService.isRigionExcludeSelf(costOfLabourProcesses);
        if(count > 0){
            return ResponseResult.fail("日期区间冲突，请检查!");
        }

        //TODO
        // 假如状态是0的编辑，则是修改失效日期。查看是否存在，改为新的日期区间之后，是否有审核通过的工价价目变成空
        /*if(costOfLabourProcesses.getStatus() == 0 && costOfLabourProcesses.getId()!=0){
            CostOfLabourProcesses old = costOfLabourProcessesService.getById(costOfLabourProcesses.getId());
            Integer oldCount= repositoryBuyinDocumentService.getSupplierMaterialPassBetweenDate(old);
            Integer newCount= repositoryBuyinDocumentService.getSupplierMaterialPassBetweenDate(baseSupplierMaterial);
            if(oldCount != newCount){
                return ResponseResult.fail("该供应商:"+old.getSupplierId()+"，该物料:"+old.getMaterialId()+"，调整时间区将会导致"+(oldCount-newCount)+"条审核通过的采购入库记录，价格变成空");
            }
        }*/

        costOfLabourProcesses.setUpdated(LocalDateTime.now());
        costOfLabourProcesses.setUpdateUser(principal.getName());
        try {
            costOfLabourProcessesService.updateById(costOfLabourProcesses);
            log.info("工序模块-更新内容:{}",costOfLabourProcesses);
            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("工序，更新异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('costOfLabour:processes:del')")
    public ResponseResult delete(@RequestBody String[] ids) {

        costOfLabourProcessesService.removeByIds(Arrays.asList(ids));

        log.info("工序模块-删除id:{}",ids);
        return ResponseResult.succ("删除成功");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('costOfLabour:processes:valid')")
    @Transactional
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<CostOfLabourProcesses> lists = new ArrayList<>();
        for (Long id : ids){
            //TODO// 1. 工序价目审核，先查询是否有工价表审核完成的引用，有则不能修改
            /*BaseSupplierMaterial one = baseSupplierMaterialService.getById(id);
            Integer count= repositoryBuyinDocumentService.getSupplierMaterialPassBetweenDate(one);
            if(count > 0){
                return ResponseResult.fail("ID:"+one.getId()+"已有"+count+"条审核通过的采购入库记录");
            }
            if(one.getStatus() == 0){
                return ResponseResult.fail("ID:"+id+"的状态已是审核通过，无法再次审核");
            }*/

            CostOfLabourProcesses costOfLabourProcesses = new CostOfLabourProcesses();
            costOfLabourProcesses.setUpdated(LocalDateTime.now());
            costOfLabourProcesses.setUpdateUser(principal.getName());
            costOfLabourProcesses.setId(id);
            costOfLabourProcesses.setStatus(DBConstant.TABLE_COST_OF_LABOUR_PROCESSES.STATUS_FIELDVALUE_0);
            lists.add(costOfLabourProcesses);

        }
        costOfLabourProcessesService.updateBatchById(lists);
        return ResponseResult.succ("审核通过");
    }


    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('costOfLabour:processes:valid')")
    public ResponseResult statusPass(Principal principal,Long id) {

        //TODO 1. 采购价目审核，先查询是否有采购入库审核完成的引用，有则不能修改
        /*BaseSupplierMaterial one = baseSupplierMaterialService.getById(id);
        Integer count= repositoryBuyinDocumentService.getSupplierMaterialPassBetweenDate(one);
        if(count > 0){
            return ResponseResult.fail("该供应商:"+one.getSupplierId()+"，该物料:"+one.getMaterialId()+"，该时间区已有"+count+"条审核通过的采购入库记录");
        }
        if(one.getStatus() != 1){
            return ResponseResult.fail("ID:"+id+"状态不对，审核失败");
        }*/

        CostOfLabourProcesses costOfLabourProcesses = new CostOfLabourProcesses();
        costOfLabourProcesses.setUpdated(LocalDateTime.now());
        costOfLabourProcesses.setUpdateUser(principal.getName());
        costOfLabourProcesses.setId(id);
        costOfLabourProcesses.setStatus(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.STATUS_FIELDVALUE_0);
        costOfLabourProcessesService.updateById(costOfLabourProcesses);
        log.info("工序模块-审核通过内容:{}",costOfLabourProcesses);

        return ResponseResult.succ("审核通过");
    }


    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('costOfLabour:processes:valid')")
    public ResponseResult statusReturn(Principal principal,Long id) {

        //TODO 1. 采购价目反审核，先查询是否有采购入库审核完成的引用，有则不能修改
        /*BaseSupplierMaterial one = baseSupplierMaterialService.getById(id);
        Integer count= repositoryBuyinDocumentService.getSupplierMaterialPassBetweenDate(one);
        if(count > 0){
            return ResponseResult.fail("该供应商，该物料，该时间区已有"+count+"条审核通过的采购入库记录");
        }*/
        CostOfLabourProcesses costOfLabourProcesses = new CostOfLabourProcesses();
        costOfLabourProcesses.setUpdated(LocalDateTime.now());
        costOfLabourProcesses.setUpdateUser(principal.getName());
        costOfLabourProcesses.setId(id);
        costOfLabourProcesses.setStatus(1);
        costOfLabourProcessesService.updateById(costOfLabourProcesses);
        log.info("工序模块-反审核通过内容:{}",costOfLabourProcesses);

        return ResponseResult.succ("反审核成功");
    }

}
