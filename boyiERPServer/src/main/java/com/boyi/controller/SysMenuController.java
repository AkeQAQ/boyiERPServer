package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.dto.SysMenuDto;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.SysMenu;
import com.boyi.entity.SysRoleMenu;
import com.boyi.entity.SysUser;
import com.boyi.service.SysMenuService;
import com.boyi.service.SysRoleMenuService;
import com.boyi.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-15
 */
@Slf4j
@RestController
@RequestMapping("/sys/menu")
public class SysMenuController extends BaseController {

    @Autowired
    SysUserService sysUserService;
    @Autowired
    SysMenuService sysMenuService;
    @Autowired
    SysRoleMenuService sysRoleMenuService;



    /**
     * 获取当前用户的菜单栏以及权限
     * Principal 标识注入的当前用户
     */
    @GetMapping("/navList")
    public ResponseResult nav(Principal principal)throws Exception {
        String username = principal.getName();
        SysUser sysUser = sysUserService.getByUsername(username);   // ROLE_Admin,sys:user:save
        String[] authoritys = StringUtils.tokenizeToStringArray(sysUserService.getUserAuthorityInfo(sysUser), ",");
        Map<Object, Object> returnMap = MapUtil.builder().put("nav", sysMenuService.getCurrentUserNav(sysUser)).put("auth", authoritys).map();
        return ResponseResult.succ(returnMap);
    }

    /**
     * 获取菜单全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('sysManage:menu:list')")
    public ResponseResult list() {
        List<SysMenu> sysMenus = sysMenuService.buildTreeMenu(sysMenuService.list());
        return ResponseResult.succ(sysMenus);
    }

    /**
     * 获取有效的菜单全部数据
     */
    @PostMapping("/listValide")
    @PreAuthorize("hasAuthority('sysManage:menu:list')")
    public ResponseResult listValide() {
        List<SysMenu> sysMenus = sysMenuService.buildTreeMenu(sysMenuService.listValid());
        return ResponseResult.succ(sysMenus);
    }

    /**
     *  新增菜单
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('sysManage:menu:save')")
    public ResponseResult save(Principal principal,@Validated @RequestBody SysMenuDto sysMenuDto) {
        SysMenu sysMenu = changeSysMenuDto2SysMenu(sysMenuDto);
        LocalDateTime now = LocalDateTime.now();
        sysMenu.setCreated(now);
        sysMenu.setUpdated(now);

        sysMenuService.save(sysMenu);
        log.info("操作人:[{}],新增内容:{}",principal.getName(),sysMenu);
        return ResponseResult.succ("新增成功");
    }

    /**
     *  根据id 查询菜单
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('sysManage:menu:list')")
    public ResponseResult queryById(Long id) {
        SysMenu sysmenu = sysMenuService.getById(id);
        return ResponseResult.succ(sysmenu);
    }

    /**
     *  修改菜单
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('sysManage:menu:update')")
    public ResponseResult update(Principal principal,@Validated @RequestBody SysMenuDto sysMenuDto) {
        SysMenu sysMenu = changeSysMenuDto2SysMenu(sysMenuDto);
        sysMenu.setUpdated(LocalDateTime.now());
        sysMenuService.updateById(sysMenu);
        log.info("操作人:[{}],修改菜单,param:{},update content:{}",principal.getName(),sysMenuDto,sysMenu);
        // 清除所有与该菜单相关的权限缓存
        sysUserService.clearUserAuthorityInfoByMenuId(sysMenu.getId());

        //TODO 菜单禁用的话，其他地方也不能使用，status的伪删除作用要发挥出来，明天都全部检查一次

        return ResponseResult.succ("编辑成功");
    }

    /**
     *  根据id 删除菜单
     */
    @GetMapping("/delById")
    @PreAuthorize("hasAuthority('sysManage:menu:del')")
    public ResponseResult delById(Principal principal,Long id) {
        int count = sysMenuService.count(new QueryWrapper<SysMenu>().eq(DBConstant.TABLE_MENU.PARENT_ID_FIELDNAME, id));
        if (count > 0) {
            return ResponseResult.fail("请先删除子菜单");
        }

        // 清除所有与该菜单相关的权限缓存
        sysUserService.clearUserAuthorityInfoByMenuId(id);

        boolean flag = sysMenuService.removeById(id);
        if(flag){
            // 同步删除中间关联表
            sysRoleMenuService.remove(new QueryWrapper<SysRoleMenu>().eq(DBConstant.TABLE_ROLE_MENU.MENU_FIELDNAME, id));
            log.info("操作人:[{}],删除菜单id:{},并且同时删除 roleMenu中间表",principal.getName(), id);
            return ResponseResult.succ("删除成功");
        }else {
            return ResponseResult.fail("删除失败");

        }
    }

    private SysMenu changeSysMenuDto2SysMenu(SysMenuDto sysMenuDto){
        SysMenu sysMenu = new SysMenu();
        sysMenu.setId(sysMenuDto.getId());
        sysMenu.setParentId(sysMenuDto.getParentId());
        sysMenu.setMenuName(sysMenuDto.getMenuName());
        sysMenu.setAuthority(sysMenuDto.getAuthority());
        sysMenu.setIcon(sysMenuDto.getIcon());
        sysMenu.setUrl(sysMenuDto.getUrl());
        sysMenu.setComponent(sysMenuDto.getComponent());
        sysMenu.setOrderType(sysMenuDto.getOrderType());
        sysMenu.setType(sysMenuDto.getType());
        sysMenu.setStatus(sysMenuDto.getStatus());
        return sysMenu;
    }
}
