package com.boyi.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseDepartment;
import com.boyi.entity.BaseMaterial;
import com.boyi.entity.RepositoryCheck;
import com.boyi.entity.RepositoryCheckDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/repository/check")
public class RepositoryCheckController extends BaseController {

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('repository:check:del')")
    public ResponseResult delete(@RequestBody Long[] ids)throws Exception {

        boolean flag = repositoryCheckService.removeByIds(Arrays.asList(ids));

        log.info("删除盘点表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("盘点删除失败");
        }

        boolean flagDetail = repositoryCheckDetailService.delByDocumentIds(ids);
        log.info("删除盘点表详情信息,document_id:{},是否成功：{}",ids,flagDetail?"成功":"失败");

        if(!flagDetail){
            return ResponseResult.fail("盘点详情表没有删除成功!");
        }
        return ResponseResult.succ("删除成功");
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('repository:check:list')")
    public ResponseResult queryById(Long id) {
        RepositoryCheck repositoryCheck = repositoryCheckService.getById(id);

        List<RepositoryCheckDetail> details = repositoryCheckDetailService.listByDocumentId(id);

        for (RepositoryCheckDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());
        }

        repositoryCheck.setRowList(details);
        return ResponseResult.succ(repositoryCheck);
    }

    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('repository:check:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody RepositoryCheck repositoryCheck)throws Exception {

        if(repositoryCheck.getRowList() ==null || repositoryCheck.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        repositoryCheck.setUpdated(LocalDateTime.now());
        repositoryCheck.setUpdatedUser(principal.getName());

        try {

            //1. 先删除老的，再插入新的
            boolean flag = repositoryCheckDetailService.removeByDocId(repositoryCheck.getId());
            if(flag){
                repositoryCheckService.updateById(repositoryCheck);

                for (RepositoryCheckDetail item : repositoryCheck.getRowList()){
                    item.setId(null);
                    item.setDocumentId(repositoryCheck.getId());
                }

                repositoryCheckDetailService.saveBatch(repositoryCheck.getRowList());

                log.info("盘点模块-更新内容:{}",repositoryCheck);
            }else{
                return ResponseResult.fail("操作失败，期间detail删除失败");
            }

            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("供应商，更新异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('repository:check:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody RepositoryCheck repositoryCheck)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        repositoryCheck.setCreated(now);
        repositoryCheck.setUpdated(now);
        repositoryCheck.setCreatedUser(principal.getName());
        repositoryCheck.setUpdatedUser(principal.getName());
        repositoryCheck.setStatus(DBConstant.TABLE_REPOSITORY_CHECK.STATUS_FIELDVALUE_1);
//        double theNum = repositoryCheck.getCheckNum() - repositoryCheck.getStockNum(); // 盘点数目 - 账存数目，> 0 则盘赢，加库存. < 0 则盘亏，减库存数目
//        repositoryCheck.setChangeNum(theNum);
        try {


            repositoryCheckService.save(repositoryCheck);

            for (RepositoryCheckDetail item : repositoryCheck.getRowList()){
                item.setDocumentId(repositoryCheck.getId());
            }

            repositoryCheckDetailService.saveBatch(repositoryCheck.getRowList());

            return ResponseResult.succ("新增成功");
        } catch (DuplicateKeyException e) {
            log.error("盘点单，插入异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }


    /**
     * 获取盘点 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('repository:check:list')")
    public ResponseResult list(String searchStr, String searchField, String searchStartDate, String searchEndDate) {
        Page<RepositoryCheck> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
             if (searchField.equals("materialName")) {
                queryField = "material_name";

            }else if (searchField.equals("id")) {
                queryField = "id";

            } else {
                return ResponseResult.fail("搜索字段不存在");
            }
        }else {

        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = repositoryCheckService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate);
        return ResponseResult.succ(pageData);
    }


    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('repository:check:valid')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        RepositoryCheck repositoryCheck = new RepositoryCheck();
        repositoryCheck.setUpdated(LocalDateTime.now());
        repositoryCheck.setUpdatedUser(principal.getName());
        repositoryCheck.setId(id);
        repositoryCheck.setStatus(DBConstant.TABLE_REPOSITORY_CHECK.STATUS_FIELDVALUE_0);
        repositoryCheckService.updateById(repositoryCheck);

        // 把库存设置成盘点数目
        List<RepositoryCheckDetail> details = repositoryCheckDetailService.listByDocumentId(id);
        for (RepositoryCheckDetail item : details) {
            repositoryStockService.updateNum(item.getMaterialId(),item.getCheckNum());
        }

        log.info("仓库模块-盘点模块-审核通过内容:{}",repositoryCheck);

        return ResponseResult.succ("审核通过");
    }


    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('repository:check:returnValid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {

        RepositoryCheck repositoryCheck = new RepositoryCheck();
        repositoryCheck.setUpdated(LocalDateTime.now());
        repositoryCheck.setUpdatedUser(principal.getName());
        repositoryCheck.setId(id);
        repositoryCheck.setStatus(DBConstant.TABLE_REPOSITORY_CHECK.STATUS_FIELDVALUE_1);
        repositoryCheckService.updateById(repositoryCheck);
        log.info("仓库模块-反审核通过内容:{}",repositoryCheck);


        return ResponseResult.succ("反审核成功");
    }

}
