package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseMaterial;
import com.boyi.entity.SysUser;
import com.boyi.entity.SysUserRole;
import com.boyi.service.BaseMaterialService;
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
@RequestMapping("/baseData/material")
public class BaseMaterialController extends BaseController {


    /**
     * 获取物料 分页全部数据
     */
    @GetMapping("/listByGroupId")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult listByGroupId(String searchStr) {
        Page<BaseMaterial> pageData = null;
        if(searchStr.equals("全部")){
            pageData = baseMaterialService.page(getPage(),new QueryWrapper<BaseMaterial>());
        }else {
            pageData = baseMaterialService.page(getPage(),new QueryWrapper<BaseMaterial>().eq("group_id",searchStr));
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 获取物料 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult list(String searchStr, String searchField) {
        Page<BaseMaterial> pageData = null;
        if (searchField == "") {
            log.info("未选择搜索字段，全查询");
            pageData = baseMaterialService.page(getPage());
        } else {
            String queryField = "";
            if (searchField.equals("groupId")) {
                queryField = "group_id";
            } else if (searchField.equals("id")) {
                queryField = "id";
            } else if (searchField.equals("name")) {
                queryField = "name";
            } else {
                return ResponseResult.fail("搜索字段不存在");
            }
            log.info("搜索字段:{},查询内容:{}", searchField, searchStr);
            pageData = baseMaterialService.page(getPage(), new QueryWrapper<BaseMaterial>()
                    .like(StrUtil.isNotBlank(searchStr), queryField, searchStr));
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 查询物料
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult queryById(String id) {
        BaseMaterial baseMaterial = baseMaterialService.getById(id);
        return ResponseResult.succ(baseMaterial);
    }

    /**
     * 新增物料
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:material:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody BaseMaterial baseMaterial) {
        LocalDateTime now = LocalDateTime.now();
        baseMaterial.setCreated(now);
        baseMaterial.setUpdated(now);
        baseMaterial.setCreateduser(principal.getName());
        baseMaterial.setUpdateuser(principal.getName());

        baseMaterialService.save(baseMaterial);
        return ResponseResult.succ("新增成功");
    }


    /**
     * 修改物料
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('baseData:material:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody BaseMaterial baseMaterial) {
        baseMaterial.setUpdated(LocalDateTime.now());
        baseMaterial.setUpdateuser(principal.getName());
        try {
            baseMaterialService.updateById(baseMaterial);
            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            return ResponseResult.fail("名称重复!");
        }
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('baseData:material:del')")
    public ResponseResult delete(@RequestBody String[] ids) {

        baseMaterialService.removeByIds(Arrays.asList(ids));

        return ResponseResult.succ("删除成功");
    }
}
