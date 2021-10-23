package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.dto.PassWordDto;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseDepartment;
import com.boyi.entity.BaseSupplier;
import com.boyi.service.BaseDepartmentService;
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
 * 基础模块-部门管理 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-22
 */
@RestController
@RequestMapping("/baseData/department")
public class BaseDepartmentController extends BaseController {

    /**
     * 获取全部
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('baseData:department:list')")
    public ResponseResult getSearchAllData() {
        List<BaseDepartment> baseDepartments = baseDepartmentService.list();

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
    @PreAuthorize("hasAuthority('baseData:department:list')")
    public ResponseResult list(String searchName) {
        Page<BaseDepartment> pageData = baseDepartmentService.pageBySearch(getPage(),searchName);
        return ResponseResult.succ(pageData);
    }


    /**
     *  新增部门
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:department:save')")
    public ResponseResult save(@Validated @RequestBody BaseDepartment BaseDepartment) {
        LocalDateTime now = LocalDateTime.now();
        BaseDepartment.setCreated(now);
        BaseDepartment.setUpdated(now);
        baseDepartmentService.save(BaseDepartment);
        return ResponseResult.succ("新增成功");
    }
    /**
     *  查询部门
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('baseData:department:list')")
    public ResponseResult queryById(Long id) {
        BaseDepartment BaseDepartment = baseDepartmentService.getById(id);
        return ResponseResult.succ(BaseDepartment);
    }

    /**
     *  修改部门
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('baseData:department:update')")
    public ResponseResult update(@Validated @RequestBody BaseDepartment BaseDepartment) {
        BaseDepartment.setUpdated(LocalDateTime.now());
        try {
            baseDepartmentService.updateById(BaseDepartment);
            return ResponseResult.succ("编辑成功");
        }catch (DuplicateKeyException e){
            return ResponseResult.fail("用户名重复!");
        }
    }

    @PostMapping("/del")
    @PreAuthorize("hasAuthority('baseData:department:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        baseDepartmentService.removeByIds(Arrays.asList(ids));

        return ResponseResult.succ("删除成功");
    }
}
