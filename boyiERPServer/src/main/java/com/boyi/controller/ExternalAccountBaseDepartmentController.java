package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.ExternalAccountBaseDepartment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 基础模块-部门管理 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@RestController
@RequestMapping("/externalAccount/baseData/department")
@Slf4j
public class ExternalAccountBaseDepartmentController extends BaseController {

    /**
     * 获取全部
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('externalAccount:baseData:department:list')")
    public ResponseResult getSearchAllData() {
        List<ExternalAccountBaseDepartment> baseDepartments = externalAccountBaseDepartmentService.list();

        ArrayList<Map<Object,Object>> returnList = new ArrayList<>();
        baseDepartments.forEach(obj ->{
            Map<Object, Object> returnMap = MapUtil.builder().put("value",obj.getId()+" : "+obj.getName() ).put("id", obj.getId()).put("name", obj.getName()).map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }


    /**
     * 获取部门 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('externalAccount:baseData:department:list')")
    public ResponseResult list(String searchName) {
        Page<ExternalAccountBaseDepartment> pageData = externalAccountBaseDepartmentService.pageBySearch(getPage(),searchName);
        return ResponseResult.succ(pageData);
    }


    /**
     *  新增部门
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('externalAccount:baseData:department:list')")
    public ResponseResult save(@Validated @RequestBody ExternalAccountBaseDepartment ExternalAccountBaseDepartment) {
        LocalDateTime now = LocalDateTime.now();
        ExternalAccountBaseDepartment.setCreated(now);
        ExternalAccountBaseDepartment.setUpdated(now);
        externalAccountBaseDepartmentService.save(ExternalAccountBaseDepartment);
        return ResponseResult.succ("新增成功");
    }
    /**
     *  查询部门
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('externalAccount:baseData:department:list')")
    public ResponseResult queryById(Long id) {
        ExternalAccountBaseDepartment ExternalAccountBaseDepartment = externalAccountBaseDepartmentService.getById(id);
        return ResponseResult.succ(ExternalAccountBaseDepartment);
    }

    /**
     *  修改部门
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('externalAccount:baseData:department:list')")
    public ResponseResult update(@Validated @RequestBody ExternalAccountBaseDepartment ExternalAccountBaseDepartment) {
        ExternalAccountBaseDepartment.setUpdated(LocalDateTime.now());
        try {
            externalAccountBaseDepartmentService.updateById(ExternalAccountBaseDepartment);
            return ResponseResult.succ("编辑成功");
        }catch (DuplicateKeyException e){
            return ResponseResult.fail("用户名重复!");
        }
    }

    @PostMapping("/del")
    @PreAuthorize("hasAuthority('externalAccount:baseData:department:list')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        externalAccountBaseDepartmentService.removeByIds(Arrays.asList(ids));

        return ResponseResult.succ("删除成功");
    }
}
