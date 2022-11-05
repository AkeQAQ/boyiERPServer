package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.dto.PassWordDto;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import com.boyi.service.SysUserService;
import org.apache.commons.math3.analysis.function.Cos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-10-26
 */
@RestController
@RequestMapping("/costOfLabour/costOfLabourType")
public class CostOfLabourTypeController extends BaseController {


    /**
     * 获取全部的工价类型
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabourType:list')")
    public ResponseResult getSearchAllData(Principal principal) {


        String name = principal.getName();
        List<CostOfLabourType> currentUserOwnerTypes = null;

        if(name.equals("admin")){
            currentUserOwnerTypes = costOfLabourTypeService.list();
        }else{
            currentUserOwnerTypes = costOfLabourTypeService.listByName(name);
        }

        ArrayList<Map<Object,Object>> returnList = new ArrayList<>();
        currentUserOwnerTypes.forEach(obj ->{
            Map<Object, Object> returnMap = MapUtil.builder().put("value",obj.getId()+" : "+obj.getTypeName() ).put("id", obj.getId()).put("name", obj.getTypeName()).map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }


    /**
     * 获取工价类型 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabourType:list')")
    public ResponseResult list() {

        List<CostOfLabourType> result = costOfLabourTypeService.list();


        for (int i = 0; i < result.size(); i++) {

            CostOfLabourType type = result.get(i);
            String roleIds = type.getRoleId();
            if(roleIds==null || roleIds.isEmpty()){
                continue;
            }
            String[] roleId = roleIds.split(",");
            ArrayList<SysRole> roleArrayList = new ArrayList<>();
            for (String id : roleId){
                SysRole role = sysRoleService.getById(id);
                roleArrayList.add(role);
            }
            type.setRoles(roleArrayList);

        }

        return ResponseResult.succ(result);
    }

    /**
     *  新增工价类型
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabourType:save')")
    public ResponseResult save(@Validated @RequestBody CostOfLabourType costOfLabourType) {
        LocalDateTime now = LocalDateTime.now();
        costOfLabourType.setCreated(now);
        costOfLabourType.setUpdated(now);
        costOfLabourTypeService.save(costOfLabourType);
        return ResponseResult.succ("新增成功");
    }
    /**
     *  查询工价类型
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabourType:list')")
    public ResponseResult queryById(Long id) {
        CostOfLabourType costOfLabourType = costOfLabourTypeService.getById(id);
        return ResponseResult.succ(costOfLabourType);
    }


    /**
     *  修改工价类型
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabourType:update')")
    public ResponseResult update(@Validated @RequestBody CostOfLabourType costOfLabourType) {
        costOfLabourType.setUpdated(LocalDateTime.now());
        try {
            CostOfLabourType old = costOfLabourTypeService.getById(costOfLabourType.getId());
            old.setTypeName(costOfLabourType.getTypeName());
            old.setSeq(costOfLabourType.getSeq());
            costOfLabourTypeService.updateById(old);
            return ResponseResult.succ("编辑成功");
        }catch (DuplicateKeyException e){
            return ResponseResult.fail("工价类型名重复!");
        }
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabourType:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        costOfLabourTypeService.removeByIds(Arrays.asList(ids));

        return ResponseResult.succ("删除成功");
    }

    /**
     * 获取工价类型拥有的全部角色
     */
    @GetMapping("/queryRolesByCostOfTypeName")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabourType:list')")
    public ResponseResult queryRolesByCostOfTypeName(Long id) {
        CostOfLabourType checkObj = costOfLabourTypeService.getById(id);

        String roleId = checkObj.getRoleId();
        ArrayList<Long> returnRolesIds = new ArrayList<>();
        if(roleId==null || roleId.isEmpty()){
            return ResponseResult.succ(returnRolesIds);
        }
        String[] roleIds = roleId.split(",");

        for (String theOneRoleId : roleIds){
            returnRolesIds.add(Long.valueOf(theOneRoleId));
        }

        return ResponseResult.succ(returnRolesIds);
    }

    @Transactional
    @PostMapping("/authority")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabourType:authority')")
    public ResponseResult rolePerm(Long id, @RequestBody Long[] roleIds) {

        CostOfLabourType old = costOfLabourTypeService.getById(id);

        if(roleIds.length ==0) {
            old.setRoleId("");
        }else{
            StringBuilder sb = new StringBuilder();
            for (Long roleId : roleIds){
                sb.append(roleId).append(",");
            }
            if(sb.length()>0){
                old.setRoleId(sb.deleteCharAt(sb.length() - 1).toString());
            }
        }

        old.setUpdated(LocalDateTime.now());
        costOfLabourTypeService.updateById(old);
        return ResponseResult.succ("分配权限成功");
    }
}
