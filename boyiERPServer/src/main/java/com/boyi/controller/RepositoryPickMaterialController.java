package com.boyi.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 仓库模块-领料模块 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
@Slf4j
@RestController
@RequestMapping("/repository/pickMaterial")
public class RepositoryPickMaterialController extends BaseController {

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('repository:pickMaterial:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        boolean flag = repositoryPickMaterialService.removeByIds(Arrays.asList(ids));

        log.info("删除领料表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("领料删除失败");
        }

        boolean flagDetail = repositoryPickMaterialDetailService.delByDocumentIds(ids);
        log.info("删除领料表详情信息,document_id:{},是否成功：{}",ids,flagDetail?"成功":"失败");

        if(!flagDetail){
            return ResponseResult.fail("领料详情表没有删除成功!");
        }
        return ResponseResult.succ("删除成功");
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('repository:pickMaterial:list')")
    public ResponseResult queryById(Long id) {
        RepositoryPickMaterial repositoryPickMaterial = repositoryPickMaterialService.getById(id);

        List<RepositoryPickMaterialDetail> details = repositoryPickMaterialDetailService.listByDocumentId(id);

        BaseDepartment department = baseDepartmentService.getById(repositoryPickMaterial.getDepartmentId());

        Double totalNum = 0D;
        Double totalAmount = 0D;

        for (RepositoryPickMaterialDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());

            totalNum += detail.getNum();
        }


        repositoryPickMaterial.setTotalNum( totalNum);

        repositoryPickMaterial.setDepartmentName(department.getName());

        repositoryPickMaterial.setRowList(details);
        return ResponseResult.succ(repositoryPickMaterial);
    }



    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('repository:pickMaterial:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody RepositoryPickMaterial repositoryPickMaterial) {

        if(repositoryPickMaterial.getRowList() ==null || repositoryPickMaterial.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        repositoryPickMaterial.setUpdated(LocalDateTime.now());
        repositoryPickMaterial.setUpdatedUser(principal.getName());

        try {

            //1. 先删除老的，再插入新的
            boolean flag = repositoryPickMaterialDetailService.removeByDocId(repositoryPickMaterial.getId());
            if(flag){
                repositoryPickMaterialService.updateById(repositoryPickMaterial);

                for (RepositoryPickMaterialDetail item : repositoryPickMaterial.getRowList()){
                    item.setId(null);
                    item.setDocumentId(repositoryPickMaterial.getId());
                }

                repositoryPickMaterialDetailService.saveBatch(repositoryPickMaterial.getRowList());
                log.info("领料模块-更新内容:{}",repositoryPickMaterial);
            }else{
                return ResponseResult.fail("操作失败，期间detail删除失败");
            }

            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("供应商，更新异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }


    /**
     * 新增入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('repository:pickMaterial:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody RepositoryPickMaterial repositoryPickMaterial) {
        LocalDateTime now = LocalDateTime.now();
        repositoryPickMaterial.setCreated(now);
        repositoryPickMaterial.setUpdated(now);
        repositoryPickMaterial.setCreatedUser(principal.getName());
        repositoryPickMaterial.setUpdatedUser(principal.getName());
        repositoryPickMaterial.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_1);
        try {

            repositoryPickMaterialService.save(repositoryPickMaterial);

            for (RepositoryPickMaterialDetail item : repositoryPickMaterial.getRowList()){
                item.setDocumentId(repositoryPickMaterial.getId());
            }

            repositoryPickMaterialDetailService.saveBatch(repositoryPickMaterial.getRowList());

            return ResponseResult.succ("新增成功");
        } catch (DuplicateKeyException e) {
            log.error("领料单，插入异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    /**
     * 获取领料 分页导出
     *//*
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('repository:pickMaterial:export')")
    public void export(HttpServletResponse response, String searchStr, String searchField, String searchStartDate, String searchEndDate) {
        Page<RepositoryPickMaterial> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            }
            else if (searchField.equals("materialName")) {
                queryField = "material_name";

            }else if (searchField.equals("id")) {
                queryField = "id";

            } else {
            }
        }else {

        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = repositoryPickMaterialService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate);

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(RepositoryPickMaterial.class,1,0).export(response,fis,pageData.getRecords(),"报表.xlsx",DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }*/

    /**
     * 获取领料 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('repository:pickMaterial:list')")
    public ResponseResult list(String searchStr, String searchField, String searchStartDate, String searchEndDate) {
        Page<RepositoryPickMaterial> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("departmentName")) {
                queryField = "department_name";
            }
            else if (searchField.equals("materialName")) {
                queryField = "material_name";

            }else if (searchField.equals("id")) {
                queryField = "id";

            } else {
                return ResponseResult.fail("搜索字段不存在");
            }
        }else {

        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = repositoryPickMaterialService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate);
        return ResponseResult.succ(pageData);
    }


    /**
     * 审核通过
     */
    /*@GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('repository:pickMaterial:valid')")
    public ResponseResult statusPass(Principal principal,Long id) {

        RepositoryPickMaterial repositoryPickMaterial = new RepositoryPickMaterial();
        repositoryPickMaterial.setUpdated(LocalDateTime.now());
        repositoryPickMaterial.setUpdatedUser(principal.getName());
        repositoryPickMaterial.setId(id);
        repositoryPickMaterial.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_0);
        repositoryPickMaterialService.updateById(repositoryPickMaterial);
        log.info("仓库模块-审核通过内容:{}",repositoryPickMaterial);

        // 领料审核通过之后，要把数量更新

        // 1. 根据单据ID 获取该单据的全部详情信息，
        List<RepositoryPickMaterialDetail> details = repositoryPickMaterialDetailService.listByDocumentId(id);
        // 2. 遍历更新 一个供应商，一个物料对应的库存数量
        for (RepositoryPickMaterialDetail detail : details){
            repositoryStockService.addNumBySupplierIdAndMaterialId(detail.getSupplierId()
                    ,detail.getMaterialId()
                    ,detail.getNum());
        }

        return ResponseResult.succ("审核通过");
    }
*/

    /**
     * 反审核
     */
  /*  @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('repository:pickMaterial:valid')")
    public ResponseResult statusReturn(Principal principal,Long id) {


        RepositoryPickMaterial repositoryPickMaterial = new RepositoryPickMaterial();
        repositoryPickMaterial.setUpdated(LocalDateTime.now());
        repositoryPickMaterial.setUpdatedUser(principal.getName());
        repositoryPickMaterial.setId(id);
        repositoryPickMaterial.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_1);
        repositoryPickMaterialService.updateById(repositoryPickMaterial);
        log.info("仓库模块-反审核通过内容:{}",repositoryPickMaterial);

        // 领料反审核之后，要把数量更新

        // 1. 根据单据ID 获取该单据的全部详情信息，
        List<RepositoryPickMaterialDetail> details = repositoryPickMaterialDetailService.listByDocumentId(id);
        // 2. 遍历更新 一个供应商，一个物料对应的库存数量
        for (RepositoryPickMaterialDetail detail : details){
            try {
                repositoryStockService.subNumBySupplierIdAndMaterialId(detail.getSupplierId()
                        ,detail.getMaterialId()
                        ,detail.getNum());
            } catch (Exception e) {
                log.error("数据异常",e);
            }
        }

        return ResponseResult.succ("反审核成功");
    }*/

}
