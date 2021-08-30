package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import com.boyi.service.RepositoryBuyinDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 仓库模块-采购入库单据表 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Slf4j
@RestController
@RequestMapping("/repository/buyIn")
public class RepositoryBuyinDocumentController extends BaseController {


    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('repository:buyIn:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        boolean flag = repositoryBuyinDocumentService.removeByIds(Arrays.asList(ids));

        log.info("删除采购入库表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("采购入库删除失败");
        }
        boolean flagDetail = repositoryBuyinDocumentDetailService.remove(new QueryWrapper<RepositoryBuyinDocumentDetail>()
                .in("document_id", ids));
        log.info("删除采购入库表详情信息,document_id:{},是否成功：{}",ids,flagDetail?"成功":"失败");

        if(!flagDetail){
            return ResponseResult.fail("采购入库详情表没有删除成功!");
        }
        return ResponseResult.succ("删除成功");
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('repository:buyIn:list')")
    public ResponseResult queryById(Long id) {
//        RepositoryBuyinDocument repositoryBuyinDocument = repositoryBuyinDocumentService.one(new QueryWrapper<RepositoryBuyinDocument>().eq("id", id));
        RepositoryBuyinDocument repositoryBuyinDocument = repositoryBuyinDocumentService.getById(id);

        List<RepositoryBuyinDocumentDetail> details = repositoryBuyinDocumentDetailService.list(new QueryWrapper<RepositoryBuyinDocumentDetail>().eq("document_id", id));

        BaseSupplier supplier = baseSupplierService.getById(repositoryBuyinDocument.getSupplierId());

        Long totalNum = 0L;
        Double totalAmount = 0D;

        for (RepositoryBuyinDocumentDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());


            // 查询对应的价目记录
            BaseSupplierMaterial one = baseSupplierMaterialService.getOne(new QueryWrapper<BaseSupplierMaterial>()
                    .eq("supplier_id", supplier.getId())
                    .eq("material_id", material.getId())
                    .le("start_date", repositoryBuyinDocument.getBuyInDate())
                    .ge("end_date", repositoryBuyinDocument.getBuyInDate())
                    .eq("status",0));
            if(one != null){
                detail.setPrice(one.getPrice());
                totalAmount += detail.getPrice() * detail.getNum();
            }

            totalNum += detail.getNum();
        }


        repositoryBuyinDocument.setTotalNum( totalNum);
        repositoryBuyinDocument.setTotalAmount(new BigDecimal(totalAmount).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue());

        repositoryBuyinDocument.setSupplierName(supplier.getName());

        repositoryBuyinDocument.setRowList(details);
        return ResponseResult.succ(repositoryBuyinDocument);
    }



    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('repository:buyIn:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody RepositoryBuyinDocument repositoryBuyinDocument) {

        if(repositoryBuyinDocument.getRowList() ==null || repositoryBuyinDocument.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        try {

            // 1. 先查询该供应商，该单号是否已经有记录，有则不能插入
            int exitCount = repositoryBuyinDocumentService.count(new QueryWrapper<RepositoryBuyinDocument>().
                    eq("supplier_document_num", repositoryBuyinDocument.getSupplierDocumentNum())
                    .eq("supplier_id",repositoryBuyinDocument.getSupplierId())
                    .ne("id",repositoryBuyinDocument.getId())
            );
            if(exitCount > 0){
                return ResponseResult.fail("该供应商的单据已经存在！请确认信息!");
            }

            //2. 先删除老的，再插入新的
            boolean flag = repositoryBuyinDocumentDetailService.remove(new QueryWrapper<RepositoryBuyinDocumentDetail>().eq("document_id", repositoryBuyinDocument.getId()));
            if(flag){
                repositoryBuyinDocumentService.updateById(repositoryBuyinDocument);

                for (RepositoryBuyinDocumentDetail item : repositoryBuyinDocument.getRowList()){
                    item.setId(null);
                    item.setDocumentId(repositoryBuyinDocument.getId());
                }

                repositoryBuyinDocumentDetailService.saveBatch(repositoryBuyinDocument.getRowList());
                log.info("采购入库模块-更新内容:{}",repositoryBuyinDocument);
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
    @PreAuthorize("hasAuthority('repository:buyIn:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody RepositoryBuyinDocument repositoryBuyinDocument) {
        LocalDateTime now = LocalDateTime.now();
        repositoryBuyinDocument.setCreated(now);
        repositoryBuyinDocument.setUpdated(now);
        repositoryBuyinDocument.setCreatedUser(principal.getName());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_1);

        try {
            // 1. 先查询该供应商，该单号是否已经有记录，有则不能插入
            int exitCount = repositoryBuyinDocumentService.count(new QueryWrapper<RepositoryBuyinDocument>().
                    eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_DOCUMENT_NUM_FIELDNAME, repositoryBuyinDocument.getSupplierDocumentNum())
                    .eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_ID_FIELDNAME,repositoryBuyinDocument.getSupplierId()));
            if(exitCount > 0){
                return ResponseResult.fail("该供应商的单据已经存在！请确认信息!");
            }
            repositoryBuyinDocumentService.save(repositoryBuyinDocument);



            for (RepositoryBuyinDocumentDetail item : repositoryBuyinDocument.getRowList()){
                item.setDocumentId(repositoryBuyinDocument.getId());
            }

            repositoryBuyinDocumentDetailService.saveBatch(repositoryBuyinDocument.getRowList());
            return ResponseResult.succ("新增成功");
        } catch (DuplicateKeyException e) {
            log.error("采购入库单，插入异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    /**
     * 获取采购入库 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('repository:buyIn:list')")
    public ResponseResult list(String searchStr, String searchField) {
        Page<RepositoryBuyinDocument> pageData = null;
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
        pageData = repositoryBuyinDocumentService.innerQuery(getPage(),
                new QueryWrapper<RepositoryBuyinDocument>().
                        like(StrUtil.isNotBlank(searchStr)
                                && StrUtil.isNotBlank(searchField),queryField,searchStr));
        return ResponseResult.succ(pageData);
    }


    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('repository:buyIn:valid')")
    public ResponseResult statusPass(Principal principal,Long id) {

        RepositoryBuyinDocument repositoryBuyinDocument = new RepositoryBuyinDocument();
        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setId(id);
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_0);
        repositoryBuyinDocumentService.updateById(repositoryBuyinDocument);
        log.info("仓库模块-审核通过内容:{}",repositoryBuyinDocument);

        return ResponseResult.succ("审核通过");
    }


    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('repository:buyIn:valid')")
    public ResponseResult statusReturn(Principal principal,Long id) {


        RepositoryBuyinDocument repositoryBuyinDocument = new RepositoryBuyinDocument();
        repositoryBuyinDocument.setUpdated(LocalDateTime.now());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setId(id);
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_1);
        repositoryBuyinDocumentService.updateById(repositoryBuyinDocument);
        log.info("仓库模块-反审核通过内容:{}",repositoryBuyinDocument);


        return ResponseResult.succ("反审核成功");
    }

}
