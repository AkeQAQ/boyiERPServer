package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-21
 */
@Slf4j
@RestController
@RequestMapping("/baseData/supplier")
public class BaseSupplierController extends BaseController {

    /**
     * 获取全部
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('baseData:unit:list')")
    public ResponseResult getSearchAllData() {
        List<BaseSupplier> baseSuppliers = baseSupplierService.list();

        ArrayList<Map<Object,Object>> returnList = new ArrayList<>();
        baseSuppliers.forEach(obj ->{
            Map<Object, Object> returnMap = MapUtil.builder().put("value",obj.getId()+" : "+obj.getName() ).put("id", obj.getId()).put("name", obj.getName()).map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }

    /**
     * 获取供应商 分页全部数据
     */
    @GetMapping("/listByGroupCode")
    @PreAuthorize("hasAuthority('baseData:supplier:list')")
    public ResponseResult listByGroupCode(String searchStr) {
        Page<BaseSupplier> pageData = null;
        if(searchStr.equals("全部")){
            pageData = baseSupplierService.page(getPage(),new QueryWrapper<BaseSupplier>());
        }else {
            pageData = baseSupplierService.page(getPage(),new QueryWrapper<BaseSupplier>().eq("group_code",searchStr));
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 获取供应商 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('baseData:supplier:list')")
    public ResponseResult list(String searchStr, String searchField) {
        Page<BaseSupplier> pageData = null;
        if (searchField == "") {
            log.info("未选择搜索字段，全查询");
            pageData = baseSupplierService.page(getPage());
        } else {
            String queryField = "";
            if (searchField.equals("id")) {
                queryField = "id";
            }
            else if (searchField.equals("groupCode")) {
                queryField = "group_code";
            } else if (searchField.equals("subId")) {
                queryField = "sub_id";
            } else if (searchField.equals("name")) {
                queryField = "name";
            } else {
                return ResponseResult.fail("搜索字段不存在");
            }
            log.info("搜索字段:{},查询内容:{}", searchField, searchStr);
            pageData = baseSupplierService.page(getPage(), new QueryWrapper<BaseSupplier>()
                    .like(StrUtil.isNotBlank(searchStr), queryField, searchStr));
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 查询供应商
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('baseData:supplier:list')")
    public ResponseResult queryById(String id) {
        BaseSupplier BaseSupplier = baseSupplierService.getById(id);
        return ResponseResult.succ(BaseSupplier);
    }

    /**
     * 新增供应商
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:supplier:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody BaseSupplier baseSupplier) {
        LocalDateTime now = LocalDateTime.now();
        baseSupplier.setCreated(now);
        baseSupplier.setUpdated(now);
        baseSupplier.setCreatedUser(principal.getName());
        baseSupplier.setUpdateUser(principal.getName());


        BaseSupplierGroup group = baseSupplierGroupService.getOne(new QueryWrapper<BaseSupplierGroup>().eq("code", baseSupplier.getGroupCode()));

        baseSupplier.setSubId(group.getAutoSubId());

        baseSupplier.setId(baseSupplier.getGroupCode()+"."+baseSupplier.getSubId());

        try {
            // 先自增该分组的ID
            group.setAutoSubId(group.getAutoSubId()+1);
            baseSupplierGroupService.updateById(group);

            baseSupplierService.save(baseSupplier);
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
    @PreAuthorize("hasAuthority('baseData:supplier:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody BaseSupplier baseSupplier) {
        baseSupplier.setUpdated(LocalDateTime.now());
        baseSupplier.setUpdateUser(principal.getName());
        try {

            // 1. 查询以前的信息
            BaseSupplier oldOne = baseSupplierService.getById(baseSupplier.getId());
            if(!oldOne.getName().equals(baseSupplier.getName()) ||
                    !oldOne.getMobile().equals(baseSupplier.getMobile())||
                    !oldOne.getAddress().equals(baseSupplier.getAddress())){

                // 2. 先查询是否有被价目表审核完成的引用，有则不能修改，
                int count = baseSupplierMaterialService.count(new QueryWrapper<BaseSupplierMaterial>()
                        .eq(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.SUPPLIER_ID_FIELDNAME, baseSupplier.getId())
                                .eq(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.STATUS_FIELDNAME,DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.STATUS_FIELDVALUE_0)
                        );
                if(count>0){
                    log.info("供应商ID[{}]不能修改，存在{}个 审核完成的 采购价目记录",baseSupplier.getId(),count);
                    return ResponseResult.fail("供应商ID["+baseSupplier.getId()+"]不能修改，存在"+count+"个 审核完成的 采购价目记录");
                }

                baseSupplierService.updateById(baseSupplier);
                log.info("供应商ID[{}]更新成功，old{},new:{}.",baseSupplier.getId(),oldOne,baseSupplier);
            }else {
                return ResponseResult.fail("没有信息更改!");
            }
            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("供应商，更新异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('baseData:supplier:del')")
    public ResponseResult delete(@RequestBody String[] ids) {

        int count = repositoryBuyinDocumentService.count(new QueryWrapper<RepositoryBuyinDocument>()
                .in("supplier_id", ids));

        if(count > 0){
            return ResponseResult.fail("请先删除"+count+"条对应入库记录!");
        }

        int count2 = baseSupplierMaterialService.count(new QueryWrapper<BaseSupplierMaterial>()
                .in("supplier_id", ids));

        if(count2 > 0){
            return ResponseResult.fail("请先删除"+count2+"条对应价目记录!");
        }

        baseSupplierService.removeByIds(Arrays.asList(ids));

        return ResponseResult.succ("删除成功");
    }
}
