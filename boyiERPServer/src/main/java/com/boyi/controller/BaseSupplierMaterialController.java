package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import com.boyi.service.RepositoryBuyinDocumentService;
import com.boyi.service.impl.BaseSupplierMaterialServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

/**
 * <p>
 * 报价-物料报价表 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-24
 */
@Slf4j
@RestController
@RequestMapping("/baseData/supplierMaterial")
public class BaseSupplierMaterialController extends BaseController {

    /**
     * 查询报价
     */
    @GetMapping("/queryPriceByMaterialId")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:list')")
    public ResponseResult queryPriceByMaterialId(String materialId,String dosage) {
        List<BaseSupplierMaterial> list = baseSupplierMaterialService.listByMaterialId(materialId);
        for (BaseSupplierMaterial bsm : list){
            if(bsm.getPrice()!=null){
                bsm.setComment(BigDecimalUtil.mul(dosage,bsm.getPrice()+"").toString());
            }
        }
        return ResponseResult.succ(list);
    }

    /**
     * 查询报价
     */
    @GetMapping("/queryByValidPrice")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:queryByValidPrice')")
    public ResponseResult queryByValidPrice(String supplierId,String materialId,String date) {
        if(date == null || StringUtils.isBlank(date) || materialId == null || StringUtils.isBlank(materialId)){
            return ResponseResult.succ(null);
        }
        LocalDate d = LocalDate.parse(date);
        BaseSupplierMaterial one = baseSupplierMaterialService.getSuccessPrice(supplierId,materialId,d);
        return ResponseResult.succ(one == null ? null : one.getPrice());
    }

    /**
     * 查询报价
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:list')")
    public ResponseResult queryById(String id) {
        BaseSupplierMaterial baseSupplierMaterial = baseSupplierMaterialService.getById(id);
        return ResponseResult.succ(baseSupplierMaterial);
    }

    /**
     * 获取报价 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:list')")
    public ResponseResult list(String searchField, String searchStatus,@RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;

        long start = System.currentTimeMillis();
        Page<BaseSupplierMaterial> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            }
            else if (searchField.equals("materialName")) {
                queryField = "material_name";

            } else {
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
                    if (oneField.equals("supplierName")) {
                        theQueryField = "supplier_name";
                    }
                    else if (oneField.equals("materialName")) {
                        theQueryField = "material_name";

                    } else {
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
        pageData = baseSupplierMaterialService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,queryMap);

        long end = System.currentTimeMillis();
        log.info("查询list耗时:{}ms",(end-start));
        return ResponseResult.succ(pageData);
    }

    /**
     * 新增报价
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody BaseSupplierMaterial baseSupplierMaterial) {
        if(baseSupplierMaterial.getEndDate() != null && baseSupplierMaterial.getEndDate().isBefore(baseSupplierMaterial.getStartDate()) ){
            return ResponseResult.fail("生效日期不能大于失效日期!");
        }
        if(baseSupplierMaterial.getEndDate() == null){
            LocalDate date = LocalDate.of(2100, 01, 01);
            baseSupplierMaterial.setEndDate(date);
        }

        // 查询表中是否已经有该数据，有的话，新增的起始日期要求> 老数据的结束日期
        int count = baseSupplierMaterialService.isRigion(baseSupplierMaterial);

        if(count > 0){
            return ResponseResult.fail("日期区间冲突，请检查!");
        }

        LocalDateTime now = LocalDateTime.now();
        baseSupplierMaterial.setCreated(now);
        baseSupplierMaterial.setUpdated(now);
        baseSupplierMaterial.setCreatedUser(principal.getName());
        baseSupplierMaterial.setUpdateUser(principal.getName());

        if(baseSupplierMaterial.getEndDate() == null){
            baseSupplierMaterial.setEndDate(LocalDate.of(2100,01,01));
        }
        baseSupplierMaterial.setStatus(1);
        try {
            baseSupplierMaterialService.save(baseSupplierMaterial);

            return ResponseResult.succ("新增成功");
        } catch (DuplicateKeyException e) {
            log.error("报价，插入异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }


    /**
     * 修改报价
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody BaseSupplierMaterial baseSupplierMaterial) {
        if(baseSupplierMaterial.getEndDate().isBefore(baseSupplierMaterial.getStartDate()) ){
            return ResponseResult.fail("生效日期不能大于失效日期!");
        }
        // 查询表中是否已经有同供应商，同物料的时间区间冲突
        if(baseSupplierMaterial.getEndDate() == null){
            LocalDate date = LocalDate.of(2100, 01, 01);
            baseSupplierMaterial.setEndDate(date);
        }
        int count = baseSupplierMaterialService.isRigionExcludeSelf(baseSupplierMaterial);
        if(count > 0){
            return ResponseResult.fail("日期区间冲突，请检查!");
        }

        // 假如状态是0的编辑，则是修改失效日期。查看是否存在，改为新的日期区间之后，是否有审核通过的单据价目变成空
        if(baseSupplierMaterial.getStatus() == 0 && baseSupplierMaterial.getId()!=0){
            BaseSupplierMaterial old = baseSupplierMaterialService.getById(baseSupplierMaterial.getId());
            Integer oldCount= repositoryBuyinDocumentService.getSupplierMaterialPassBetweenDate(old);
            Integer newCount= repositoryBuyinDocumentService.getSupplierMaterialPassBetweenDate(baseSupplierMaterial);
            if(oldCount != newCount){
                return ResponseResult.fail("该供应商:"+old.getSupplierId()+"，该物料:"+old.getMaterialId()+"，调整时间区将会导致"+(oldCount-newCount)+"条审核通过的采购入库记录，价格变成空");
            }
        }

        baseSupplierMaterial.setUpdated(LocalDateTime.now());
        baseSupplierMaterial.setUpdateUser(principal.getName());
        try {
            baseSupplierMaterialService.updateById(baseSupplierMaterial);
            log.info("报价模块-更新内容:{}",baseSupplierMaterial);
            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("报价，更新异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:del')")
    public ResponseResult delete(@RequestBody String[] ids) {

        baseSupplierMaterialService.removeByIds(Arrays.asList(ids));

        log.info("报价模块-删除id:{}",ids);
        return ResponseResult.succ("删除成功");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:valid')")
    @Transactional
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<BaseSupplierMaterial> lists = new ArrayList<>();
        for (Long id : ids){
            // 1. 采购价目审核，先查询是否有采购入库审核完成的引用，有则不能修改
            BaseSupplierMaterial one = baseSupplierMaterialService.getById(id);
            Integer count= repositoryBuyinDocumentService.getSupplierMaterialPassBetweenDate(one);
            if(count > 0){
                return ResponseResult.fail("ID:"+one.getId()+"已有"+count+"条审核通过的采购入库记录");
            }
            if(one.getStatus() == 0){
                return ResponseResult.fail("ID:"+id+"的状态已是审核通过，无法再次审核");
            }

            BaseSupplierMaterial baseSupplierMaterial = new BaseSupplierMaterial();
            baseSupplierMaterial.setUpdated(LocalDateTime.now());
            baseSupplierMaterial.setUpdateUser(principal.getName());
            baseSupplierMaterial.setId(id);
            baseSupplierMaterial.setStatus(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.STATUS_FIELDVALUE_0);
            lists.add(baseSupplierMaterial);

        }
        baseSupplierMaterialService.updateBatchById(lists);
        return ResponseResult.succ("审核通过");
    }


    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:valid')")
    public ResponseResult statusPass(Principal principal,Long id) {

        // 1. 采购价目审核，先查询是否有采购入库审核完成的引用，有则不能修改
        BaseSupplierMaterial one = baseSupplierMaterialService.getById(id);
        Integer count= repositoryBuyinDocumentService.getSupplierMaterialPassBetweenDate(one);
        if(count > 0){
            return ResponseResult.fail("该供应商:"+one.getSupplierId()+"，该物料:"+one.getMaterialId()+"，该时间区已有"+count+"条审核通过的采购入库记录");
        }
        if(one.getStatus() != 1){
            return ResponseResult.fail("ID:"+id+"状态不对，审核失败");
        }

        BaseSupplierMaterial baseSupplierMaterial = new BaseSupplierMaterial();
        baseSupplierMaterial.setUpdated(LocalDateTime.now());
        baseSupplierMaterial.setUpdateUser(principal.getName());
        baseSupplierMaterial.setId(id);
        baseSupplierMaterial.setStatus(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.STATUS_FIELDVALUE_0);
        baseSupplierMaterialService.updateById(baseSupplierMaterial);
        log.info("报价模块-审核通过内容:{}",baseSupplierMaterial);

        return ResponseResult.succ("审核通过");
    }


    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:valid')")
    public ResponseResult statusReturn(Principal principal,Long id) {

        // 1. 采购价目反审核，先查询是否有采购入库审核完成的引用，有则不能修改
        BaseSupplierMaterial one = baseSupplierMaterialService.getById(id);
        Integer count= repositoryBuyinDocumentService.getSupplierMaterialPassBetweenDate(one);
        if(count > 0){
            return ResponseResult.fail("该供应商，该物料，该时间区已有"+count+"条审核通过的采购入库记录");
        }
        BaseSupplierMaterial baseSupplierMaterial = new BaseSupplierMaterial();
        baseSupplierMaterial.setUpdated(LocalDateTime.now());
        baseSupplierMaterial.setUpdateUser(principal.getName());
        baseSupplierMaterial.setId(id);
        baseSupplierMaterial.setStatus(1);
        baseSupplierMaterialService.updateById(baseSupplierMaterial);
        log.info("报价模块-反审核通过内容:{}",baseSupplierMaterial);

        return ResponseResult.succ("反审核成功");
    }


    public static void main(String[] args) {
        LocalDateTime start = LocalDateTime.of(2021, 8, 25, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2021, 8, 24, 0, 0, 0);
        System.out.println(start.isBefore(end));
        System.out.println(start.isAfter(end));


    }

}
