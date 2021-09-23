package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseMaterial;
import com.boyi.entity.BaseSupplier;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.service.RepositoryBuyinDocumentService;
import com.boyi.service.impl.BaseSupplierMaterialServiceImpl;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Autowired
    BaseSupplierMaterialServiceImpl baseSupplierMaterialServiceImpl;

    /**
     * 查询报价
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:list')")
    public ResponseResult queryById(String id) {
        BaseSupplierMaterial baseSupplierMaterial = baseSupplierMaterialServiceImpl.getById(id);
        return ResponseResult.succ(baseSupplierMaterial);
    }

    /**
     * 获取报价 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:list')")
    public ResponseResult list(String searchStr, String searchField) {
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
        }else {

        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = baseSupplierMaterialService.innerQueryBySearch(getPage(),
                queryField,searchField,searchStr);

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

        int count = baseSupplierMaterialService.isRigionExcludeSelf(baseSupplierMaterial);
        if(count > 0){
            return ResponseResult.fail("日期区间冲突，请检查!");
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
            return ResponseResult.fail("该供应商，该物料，该时间区已有"+count+"条审核通过的采购入库记录");
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
