package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;

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
     * 获取供应商 分页全部数据
     */
    @GetMapping("/listByGroupId")
    @PreAuthorize("hasAuthority('baseData:supplier:list')")
    public ResponseResult listByGroupId(String searchStr) {
        Page<BaseSupplier> pageData = null;
        if(searchStr.equals("全部")){
            pageData = baseSupplierService.page(getPage(),new QueryWrapper<BaseSupplier>());
        }else {
            pageData = baseSupplierService.page(getPage(),new QueryWrapper<BaseSupplier>().eq("group_id",searchStr));
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
            else if (searchField.equals("groupId")) {
                queryField = "group_id";
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
    public ResponseResult save(Principal principal, @Validated @RequestBody BaseSupplier BaseSupplier) {
        LocalDateTime now = LocalDateTime.now();
        BaseSupplier.setCreated(now);
        BaseSupplier.setUpdated(now);
        BaseSupplier.setCreateduser(principal.getName());
        BaseSupplier.setUpdateuser(principal.getName());

        BaseSupplier.setId(BaseSupplier.getGroupId()+"."+BaseSupplier.getSubId());

        try {
            baseSupplierService.save(BaseSupplier);
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
    public ResponseResult update(Principal principal, @Validated @RequestBody BaseSupplier BaseSupplier) {
        BaseSupplier.setUpdated(LocalDateTime.now());
        BaseSupplier.setUpdateuser(principal.getName());
        try {
            baseSupplierService.updateById(BaseSupplier);
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

        baseSupplierService.removeByIds(Arrays.asList(ids));

        return ResponseResult.succ("删除成功");
    }
}
