package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.dto.SysNavDto;
import com.boyi.entity.SysMenu;
import com.boyi.entity.SysUser;
import com.boyi.mapper.SysMenuMapper;
import com.boyi.mapper.SysUserMapper;
import com.boyi.mapper.SysUserRoleMapper;
import com.boyi.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-15
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    SysUserServiceImpl sysUserService;


    @Autowired
    SysMenuMapper sysMenuMapper;

    @Autowired
    SysUserRoleMapper sysUserRoleMapper;


    /**
     * 获取当前用户菜单导航
     */
    @Override
    public List<SysNavDto> getCurrentUserNav(Long userId)throws Exception {
        // 获取用户的所有菜单
        List<Long> menuIds = sysUserRoleMapper.getNavMenuIds(userId);
        if(menuIds == null || menuIds.isEmpty()){
            throw new Exception("该用户没有任何权限，请先分配角色");
        }
        List<SysMenu> menus = buildTreeMenu(this.listByIds(menuIds));
        return convert(menus);
    }


    /**
     * 把list转成树形结构的数据
     */
    @Override
    public List<SysMenu> buildTreeMenu(List<SysMenu> menus) {
        List<SysMenu> finalMenus = new ArrayList<>();
        for (SysMenu menu : menus) {
            // 先寻找各自的孩子
            for (SysMenu e : menus) {
                if (e.getParentId() .equals( menu.getId())) {
                    menu.getChildren().add(e);
                }
            }         // 提取出父节点
            if (menu.getParentId() == 0L) {
                finalMenus.add(menu);
            }
        }
        return finalMenus;
    }

    /**
     * menu转menuDto
     */
    public List<SysNavDto> convert(List<SysMenu> menus) {
        List<SysNavDto> menuDtos = new ArrayList<>();
        menus.forEach(m -> {
            SysNavDto dto = new SysNavDto();
            dto.setId(m.getId());
            dto.setTitle(m.getMenuName());
            dto.setIcon(m.getIcon());
            dto.setPath(m.getUrl());
            dto.setRouterName(m.getAuthority());
            dto.setComponent(m.getComponent());
            dto.setStatus(m.getStatus());
            dto.setOrderType(m.getOrderType());
            if (m.getChildren().size() > 0) {
                List<SysNavDto> convert = convert(m.getChildren());
                TreeSet<SysNavDto> sysMenus = new TreeSet<>(new Comparator<SysNavDto>() {
                    @Override
                    public int compare(SysNavDto o1, SysNavDto o2) {
                        if(o1.getOrderType() != null && o2.getOrderType() != null){
                            int compareTo = o1.getOrderType().compareTo(o2.getOrderType());
                            return compareTo == 0 ? -1 : compareTo;
                        }else {
                            return -1;
                        }
                    }
                });
                sysMenus.addAll(convert);
                ArrayList<SysNavDto> sysNavDtos = new ArrayList<>();
                sysNavDtos.addAll(sysMenus);
                dto.setChildren(sysNavDtos);
            }
            menuDtos.add(dto);
        });
        return menuDtos;
    }

    @Override
    public List<SysMenu> listValid() {
        return sysMenuMapper.selectList(new QueryWrapper<SysMenu>().eq(
                DBConstant.TABLE_MENU.STATUS_FIELDNAME, DBConstant.TABLE_MENU.STATUS_FIELDVALUE_0));
    }
}
