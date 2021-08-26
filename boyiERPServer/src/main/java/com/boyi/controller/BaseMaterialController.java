package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import com.boyi.service.BaseMaterialService;
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
@RequestMapping("/baseData/material")
public class BaseMaterialController extends BaseController {



    /**
     * 用于增量表格搜索输入建议框的数据
     */
    @PostMapping("/loadTableSearchMaterialDetailAll")
    @PreAuthorize("hasAuthority('baseData:unit:list')")
    public ResponseResult loadTableSearchMaterialDetailAll() {
        List<BaseMaterial> baseSuppliers = baseMaterialService.list(new QueryWrapper<BaseMaterial>().eq("status", 0));

        ArrayList<Map<Object,Object>> returnList = new ArrayList<>();
        baseSuppliers.forEach(obj ->{
            Map<Object, Object> returnMap = MapUtil.builder().put(
                    "value",obj.getId()+" : "+obj.getName() )
                    .put("id", obj.getId())
                    .put("obj", obj)
                    .map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }


    /**
     * 获取有效数据
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('baseData:unit:list')")
    public ResponseResult getSearchAllData() {
        List<BaseMaterial> baseSuppliers = baseMaterialService.list(new QueryWrapper<BaseMaterial>().eq("status", 0));

        ArrayList<Map<Object,Object>> returnList = new ArrayList<>();
        baseSuppliers.forEach(obj ->{
            Map<Object, Object> returnMap = MapUtil.builder().put("value",obj.getId()+" : "+obj.getName() ).put("id", obj.getId()).put("name", obj.getName()).map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }


    /**
     * 获取物料 分页全部数据
     */
    @GetMapping("/listByGroupCode")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult listByGroupCode(String searchStr) {
        Page<BaseMaterial> pageData = null;
        if(searchStr.equals("全部")){
            pageData = baseMaterialService.page(getPage(),new QueryWrapper<BaseMaterial>());
        }else {
            pageData = baseMaterialService.page(getPage(),new QueryWrapper<BaseMaterial>().eq("group_code",searchStr));
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
        baseMaterial.setCreatedUser(principal.getName());
        baseMaterial.setUpdateUser(principal.getName());

        BaseMaterialGroup group = baseMaterialGroupService.getOne(new QueryWrapper<BaseMaterialGroup>().eq("code", baseMaterial.getGroupCode()));

        baseMaterial.setSubId(group.getAutoSubId());

        baseMaterial.setId(group.getCode()+"."+group.getAutoSubId());

        try {
            // 先自增该分组的ID
            group.setAutoSubId(group.getAutoSubId()+1);
            baseMaterialGroupService.updateById(group);

            // 再保存
            baseMaterialService.save(baseMaterial);

            return ResponseResult.succ("新增成功");
        } catch (DuplicateKeyException e) {
            log.error("物料，插入异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }


    /**
     * 修改物料
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('baseData:material:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody BaseMaterial baseMaterial) {
        baseMaterial.setUpdated(LocalDateTime.now());
        baseMaterial.setUpdateUser(principal.getName());
        try {
            baseMaterialService.updateById(baseMaterial);
            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("物料，更新异常",e);
            return ResponseResult.fail("唯一编码重复!");
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
