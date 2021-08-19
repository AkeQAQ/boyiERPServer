package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-15
 */
@RestController
@RequestMapping("/sys/role")
public class SysRoleController extends BaseController {

    /**
     * 获取角色全部数据
     */
    @PostMapping("/listValide")
    @PreAuthorize("hasAuthority('sysManage:role:list')")
    public ResponseResult list() {
        List<SysRole> list = sysRoleService.listValid();
        return ResponseResult.succ(list);
    }

    /**
     * 获取用户 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sysManage:role:list')")
    public ResponseResult list(String searchRoleName) {

        Page<SysRole> pageData = sysRoleService.page(getPage(), new QueryWrapper<SysRole>()
                .like(StrUtil.isNotBlank(searchRoleName), "role_name", searchRoleName));

        return ResponseResult.succ(pageData);
    }

    /**
     *  新增用户
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('sysManage:role:save')")
    public ResponseResult save(@Validated @RequestBody SysRole sysRole) {
        LocalDateTime now = LocalDateTime.now();
        sysRole.setCreated(now);
        sysRole.setUpdated(now);
        sysRoleService.save(sysRole);
        return ResponseResult.succ("新增成功");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('sysManage:role:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        sysRoleService.removeByIds(Arrays.asList(ids));
        sysRoleMenuService.remove(new QueryWrapper<SysRoleMenu>().in("role_id", ids));

        return ResponseResult.succ("删除成功");
    }

    /**
     * 获取该角色 的全部菜单数据
     */
    @GetMapping("/queryMenusByRoleId")
    @PreAuthorize("hasAuthority('sysManage:role:list')")
    public ResponseResult queryMenusByRoleId(Long id) {
        List<Long> roleMenusIds = sysRoleService.getRoleMenusIds(id);

        return ResponseResult.succ(roleMenusIds.toArray());
    }

    @Transactional
    @PostMapping("/authority")
    @PreAuthorize("hasAuthority('sysManage:role:authority')")
    public ResponseResult authority(Long roleId, @RequestBody Long[] menuIds) {

        List<SysRoleMenu> roleMenus = new ArrayList<>();

        Arrays.stream(menuIds).forEach(r -> {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(roleId);
            sysRoleMenu.setMenuId(r);

            roleMenus.add(sysRoleMenu);
        });

        sysRoleMenuService.remove(new QueryWrapper<SysRoleMenu>().eq(DBConstant.TABLE_ROLE.ROLE_ID_FIELDNAME, roleId));
        sysRoleMenuService.saveBatch(roleMenus);

        // 删除缓存
        sysUserService.clearUserAuthorityInfoByRoleId(roleId);

        return ResponseResult.succ("修改成功");
    }

    /**
     *  查询角色
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('sysManage:role:list')")
    public ResponseResult queryById(Long id) {
        SysRole sysRole = sysRoleService.getById(id);
        return ResponseResult.succ(sysRole);
    }

    /**
     *  修改角色
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('sysManage:role:update')")
    public ResponseResult update(@Validated @RequestBody SysRole sysRole) {
        sysRole.setUpdated(LocalDateTime.now());
        sysRoleService.updateById(sysRole);
        return ResponseResult.succ("编辑成功");
    }
}
