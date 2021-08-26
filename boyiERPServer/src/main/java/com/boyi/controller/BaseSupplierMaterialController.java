package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseMaterial;
import com.boyi.entity.BaseSupplier;
import com.boyi.entity.BaseSupplierMaterial;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 供应商-物料报价表 前端控制器
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
     * 查询供应商
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:list')")
    public ResponseResult queryById(String id) {
        BaseSupplierMaterial baseSupplierMaterial = baseSupplierMaterialServiceImpl.one(new QueryWrapper<BaseSupplierMaterial>().eq("id",id));
        return ResponseResult.succ(baseSupplierMaterial);
    }

    /**
     * 获取供应商 分页全部数据
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
        pageData = baseSupplierMaterialService.innerQuery(getPage(),new QueryWrapper<BaseSupplierMaterial>().like(StrUtil.isNotBlank(searchStr) && StrUtil.isNotBlank(searchField),queryField,searchStr));
        return ResponseResult.succ(pageData);
    }

    /**
     * 新增供应商
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody BaseSupplierMaterial baseSupplierMaterial) {
        if(baseSupplierMaterial.getEndDate() != null && baseSupplierMaterial.getEndDate().isBefore(baseSupplierMaterial.getStartDate()) ){
            return ResponseResult.fail("生效日期不能大于失效日期!");
        }
        String materialId = baseSupplierMaterial.getMaterialId();
        String supplierId = baseSupplierMaterial.getSupplierId();
        LocalDate startDate = baseSupplierMaterial.getStartDate();
        // 查询表中是否已经有该数据，有的话，新增的起始日期要求> 老数据的结束日期
        int count = baseSupplierMaterialService.count(new QueryWrapper<BaseSupplierMaterial>().eq("supplier_id", supplierId).eq("material_id", materialId).ge("end_date", startDate).eq("status",0));
        if(count > 0){
            return ResponseResult.fail("该起始日期前仍有有效日期，请检查!");
        }

        LocalDateTime now = LocalDateTime.now();
        baseSupplierMaterial.setCreated(now);
        baseSupplierMaterial.setUpdated(now);
        baseSupplierMaterial.setCreatedUser(principal.getName());
        baseSupplierMaterial.setUpdateUser(principal.getName());

        if(baseSupplierMaterial.getEndDate() == null){
            baseSupplierMaterial.setEndDate(LocalDate.of(2100,01,01));
        }
        baseSupplierMaterial.setStatus(0);
        try {
            baseSupplierMaterialService.save(baseSupplierMaterial);
            return ResponseResult.succ("新增成功");
        } catch (DuplicateKeyException e) {
            log.error("供应商，插入异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }


    /**
     * 修改供应商
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody BaseSupplierMaterial baseSupplierMaterial) {
        if(baseSupplierMaterial.getEndDate().isBefore(baseSupplierMaterial.getStartDate()) ){
            return ResponseResult.fail("生效日期不能大于失效日期!");
        }
        String materialId = baseSupplierMaterial.getMaterialId();
        String supplierId = baseSupplierMaterial.getSupplierId();
        LocalDate startDate = baseSupplierMaterial.getStartDate();
        // 查询表中是否已经有该数据，有的话，新增的起始日期要求> 老数据的结束日期
        int count = baseSupplierMaterialService.count(new QueryWrapper<BaseSupplierMaterial>().eq("supplier_id", supplierId).eq("material_id", materialId).ge("end_date", startDate).eq("status",0).ne("id",baseSupplierMaterial.getId()));
        if(count > 0){
            return ResponseResult.fail("该起始日期前仍有有效日期，请检查!");
        }

        baseSupplierMaterial.setUpdated(LocalDateTime.now());
        baseSupplierMaterial.setUpdateUser(principal.getName());
        try {
            baseSupplierMaterialService.updateById(baseSupplierMaterial);
            log.info("报价模块-更新内容:{}",baseSupplierMaterial);
            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("供应商，更新异常",e);
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
     * 修改供应商
     */
    @GetMapping("/statusStop")
    @PreAuthorize("hasAuthority('baseData:supplierMaterial:del')")
    public ResponseResult statusStop(Principal principal,Long id) {

        BaseSupplierMaterial baseSupplierMaterial = new BaseSupplierMaterial();
        baseSupplierMaterial.setUpdated(LocalDateTime.now());
        baseSupplierMaterial.setUpdateUser(principal.getName());
        baseSupplierMaterial.setId(id);
        baseSupplierMaterial.setStatus(1);
        baseSupplierMaterialService.updateById(baseSupplierMaterial);
        log.info("报价模块-禁用内容:{}",baseSupplierMaterial);
        return ResponseResult.succ("禁用成功");
    }

    public static void main(String[] args) {
        LocalDateTime start = LocalDateTime.of(2021, 8, 25, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2021, 8, 24, 0, 0, 0);
        System.out.println(start.isBefore(end));
        System.out.println(start.isAfter(end));


    }

}
