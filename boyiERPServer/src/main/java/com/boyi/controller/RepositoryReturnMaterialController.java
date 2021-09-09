package com.boyi.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseDepartment;
import com.boyi.entity.BaseMaterial;
import com.boyi.entity.RepositoryReturnMaterial;
import com.boyi.entity.RepositoryReturnMaterialDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 仓库模块-退料模块 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
@Slf4j
@RestController
@RequestMapping("/repository/returnMaterial")
public class RepositoryReturnMaterialController extends BaseController {

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('repository:returnMaterial:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        boolean flag = repositoryReturnMaterialService.removeByIds(Arrays.asList(ids));

        log.info("删除退料表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("退料删除失败");
        }

        boolean flagDetail = repositoryReturnMaterialDetailService.delByDocumentIds(ids);
        log.info("删除退料表详情信息,document_id:{},是否成功：{}",ids,flagDetail?"成功":"失败");

        if(!flagDetail){
            return ResponseResult.fail("退料详情表没有删除成功!");
        }
        return ResponseResult.succ("删除成功");
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('repository:returnMaterial:list')")
    public ResponseResult queryById(Long id) {
        RepositoryReturnMaterial repositoryReturnMaterial = repositoryReturnMaterialService.getById(id);

        List<RepositoryReturnMaterialDetail> details = repositoryReturnMaterialDetailService.listByDocumentId(id);

        BaseDepartment department = baseDepartmentService.getById(repositoryReturnMaterial.getDepartmentId());

        Double totalNum = 0D;
        Double totalAmount = 0D;

        for (RepositoryReturnMaterialDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());

            totalNum += detail.getNum();
        }

        repositoryReturnMaterial.setDepartmentName(department.getName());

        repositoryReturnMaterial.setRowList(details);
        return ResponseResult.succ(repositoryReturnMaterial);
    }



    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('repository:returnMaterial:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody RepositoryReturnMaterial repositoryReturnMaterial) {

        if(repositoryReturnMaterial.getRowList() ==null || repositoryReturnMaterial.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        repositoryReturnMaterial.setUpdated(LocalDateTime.now());
        repositoryReturnMaterial.setUpdatedUser(principal.getName());

        try {

            //1. 先删除老的，再插入新的
            boolean flag = repositoryReturnMaterialDetailService.removeByDocId(repositoryReturnMaterial.getId());
            if(flag){
                repositoryReturnMaterialService.updateById(repositoryReturnMaterial);

                for (RepositoryReturnMaterialDetail item : repositoryReturnMaterial.getRowList()){
                    item.setId(null);
                    item.setDocumentId(repositoryReturnMaterial.getId());
                }

                repositoryReturnMaterialDetailService.saveBatch(repositoryReturnMaterial.getRowList());
                log.info("退料模块-更新内容:{}",repositoryReturnMaterial);
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
    @PreAuthorize("hasAuthority('repository:returnMaterial:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody RepositoryReturnMaterial repositoryReturnMaterial) {
        LocalDateTime now = LocalDateTime.now();
        repositoryReturnMaterial.setCreated(now);
        repositoryReturnMaterial.setUpdated(now);
        repositoryReturnMaterial.setCreatedUser(principal.getName());
        repositoryReturnMaterial.setUpdatedUser(principal.getName());
        repositoryReturnMaterial.setStatus(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_1);
        try {

            repositoryReturnMaterialService.save(repositoryReturnMaterial);

            for (RepositoryReturnMaterialDetail item : repositoryReturnMaterial.getRowList()){
                item.setDocumentId(repositoryReturnMaterial.getId());
            }

            repositoryReturnMaterialDetailService.saveBatch(repositoryReturnMaterial.getRowList());

            return ResponseResult.succ("新增成功");
        } catch (DuplicateKeyException e) {
            log.error("退料单，插入异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    /**
     * 获取退料 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('repository:returnMaterial:list')")
    public ResponseResult list(String searchStr, String searchField, String searchStartDate, String searchEndDate) {
        Page<RepositoryReturnMaterial> pageData = null;
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
        pageData = repositoryReturnMaterialService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate);
        return ResponseResult.succ(pageData);
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('repository:returnMaterial:valid')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {
        // 1. 根据单据ID 获取该单据的全部详情信息，
        List<RepositoryReturnMaterialDetail> details = repositoryReturnMaterialDetailService.listByDocumentId(id);
        HashMap<String, Double> map = new HashMap<>();// 一个物料，需要添加的数目

        // 1. 遍历获取一个物料要添加的数目。
        for (RepositoryReturnMaterialDetail detail : details) {
            Double materialNum = map.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            map.put(detail.getMaterialId(),materialNum+detail.getNum());
        }
        RepositoryReturnMaterial repositoryReturnMaterial1 = repositoryReturnMaterialService.getById(id);

        // 2. 该部门，该物料 退料不能 > 该部门，该物料领料通过的 总和
        for (Map.Entry<String,Double> entry : map.entrySet()) {
            String materialId = entry.getKey();
            Double needAddNum = entry.getValue();// 该单据该物料，需要退料进行入库的数目
            // 查询该部门，该物料 审核通过的，总领料数目.
            Double pickCount = repositoryPickMaterialService.countByDepartmentIdMaterialId(repositoryReturnMaterial1.getDepartmentId(),
                    materialId);
            pickCount = pickCount == null ? 0D : pickCount;
            if(needAddNum > pickCount ){
                throw new Exception("该部门:"+repositoryReturnMaterial1.getDepartmentId()+",该物料:" +materialId+
                        " 退料数目:"+needAddNum+" > 领料审核通过的数目:"+pickCount);
            }

            // 查询该部门，该物料 审核完成的退料数目
            Double returnCount = repositoryReturnMaterialService.countByDepartmentIdMaterialId(repositoryReturnMaterial1.getDepartmentId(),
                    materialId);
            returnCount  = returnCount==null?0L:returnCount;
            if(needAddNum > (pickCount - returnCount)){
                throw new Exception("该部门:"+repositoryReturnMaterial1.getDepartmentId()+",该物料:" +materialId+
                        " 退料数目:"+needAddNum+" > (领料审核通过的数目:"+pickCount +"- 已退料审核完成数目:"+returnCount+")="+(pickCount-returnCount));
            }
        }
        // 3. 遍历更新一个物料对应的库存数量
        for (Map.Entry<String,Double> entry : map.entrySet()) {
            String materialId = entry.getKey();
            Double needAddNum = entry.getValue();// 该单据该物料，需要退料进行入库的数目
            repositoryStockService.addNumByMaterialId(materialId,needAddNum);
        }
        RepositoryReturnMaterial repositoryReturnMaterial = new RepositoryReturnMaterial();
        repositoryReturnMaterial.setUpdated(LocalDateTime.now());
        repositoryReturnMaterial.setUpdatedUser(principal.getName());
        repositoryReturnMaterial.setId(id);
        repositoryReturnMaterial.setStatus(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_0);
        repositoryReturnMaterialService.updateById(repositoryReturnMaterial);
        log.info("仓库模块-退料模块-审核通过内容:{}",repositoryReturnMaterial);

        return ResponseResult.succ("审核通过");
    }


    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('repository:returnMaterial:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {


        List<RepositoryReturnMaterialDetail> details = repositoryReturnMaterialDetailService.listByDocumentId(id);
        // 1. 遍历更新 一个物料对应的库存数量
        repositoryStockService.subNumReturnMaterialId(details);
        RepositoryReturnMaterial repositoryReturnMaterial = new RepositoryReturnMaterial();
        repositoryReturnMaterial.setUpdated(LocalDateTime.now());
        repositoryReturnMaterial.setUpdatedUser(principal.getName());
        repositoryReturnMaterial.setId(id);
        repositoryReturnMaterial.setStatus(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_1);
        repositoryReturnMaterialService.updateById(repositoryReturnMaterial);
        log.info("仓库模块-反审核通过内容:{}",repositoryReturnMaterial);


        return ResponseResult.succ("反审核成功");
    }

}
