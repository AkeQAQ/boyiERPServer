package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.SysMenu;
import com.boyi.entity.SysRole;
import com.boyi.entity.SysUser;
import com.boyi.entity.SysUserRole;
import com.boyi.mapper.SysMenuMapper;
import com.boyi.mapper.SysRoleMapper;
import com.boyi.mapper.SysUserMapper;
import com.boyi.mapper.SysUserRoleMapper;
import com.boyi.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-15
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    SysUserMapper sysUserMapper;

    @Autowired
    SysRoleMapper sysRoleMapper;

    @Autowired
    SysMenuMapper sysMenuMapper;

    @Autowired
    SysUserRoleMapper sysUserRoleMapper;

    Map<String, String> UserName_Own_Roles_Auth = new HashMap<>(); // 用户对应的角色，菜单权限的 内存缓存

    @Override
    public SysUser getByUsername(String username) {

        return getOne(new QueryWrapper<SysUser>().eq(DBConstant.TABLE_USER.USER_NAME_FIELDNAME, username)
                .eq(DBConstant.TABLE_USER.STATUS_FIELDNAME,DBConstant.TABLE_USER.STATUS_FIELDVALUE_0));
    }


    @Override
    public String getUserAuthorityInfo(SysUser sysUser) {
//        SysUser sysUser = sysUserMapper.selectById(userId);
        Long userId = sysUser.getId();
        //  ROLE_admin,ROLE_normal,sys:user:list,....
        String authority = "";

        if (UserName_Own_Roles_Auth.containsKey("GrantedAuthority:" + sysUser.getUserName())) {
//        if (redisUtil.hasKey("GrantedAuthority:" + sysUser.getUserName())) {

//            authority = (String) redisUtil.get("GrantedAuthority:" + sysUser.getUserName());
            authority = (String) UserName_Own_Roles_Auth.get("GrantedAuthority:" + sysUser.getUserName());
        } else {
            List<SysRole> roles = sysRoleMapper.selectList(new QueryWrapper<SysRole>()
                    .inSql(DBConstant.TABLE_USER_ROLE.ID_FIELDNAME,
                            "select role_id from sys_user_role where user_id = " + userId));

            // 获取角色编码
            if (roles.size() > 0) {
                authority = roles.stream().map(r -> "ROLE_" + r.getCode()).collect(Collectors.joining(","));
            }

            // 获取菜单操作编码
            List<Long> menuIds = sysUserRoleMapper.getNavMenuIds(userId);
            if (menuIds.size() > 0) {
                List<SysMenu> menus = sysMenuMapper.selectBatchIds(menuIds);
                String menuPerms = "";
                for (SysMenu m : menus) {
                    if(m.getStatus() == 0){
                        menuPerms= menuPerms.concat(m.getAuthority()).concat(",");
                    }
                }

                authority = authority.concat(",");
                authority = authority.concat(menuPerms);
            }

//            redisUtil.set("GrantedAuthority:" + sysUser.getUsername(), authority, 60 * 60);
            UserName_Own_Roles_Auth.put("GrantedAuthority:" + sysUser.getUserName(), authority);
        }

        return authority;
    }

    // 删除某个用户的权限信息
    @Override
    public void clearUserAuthorityInfo(String username) {
        UserName_Own_Roles_Auth.remove("GrantedAuthority:" + username);
    }// 删除所有与该角色关联的用户的权限信息

    @Override
    public void clearUserAuthorityInfoByRoleId(Long roleId) {
        List<SysUser> sysUsers = this.list(new QueryWrapper<SysUser>().inSql(
                DBConstant.TABLE_USER_ROLE.ID_FIELDNAME,
                "select user_id from sys_user_role where role_id = " + roleId));
        sysUsers.forEach(u -> {
            this.clearUserAuthorityInfo(u.getUserName());
        });
    }// 删除所有与该菜单关联的所有用户的权限信息

    @Override
    public void clearUserAuthorityInfoByMenuId(Long menuId) {
        List<SysUser> sysUsers = sysUserMapper.listByMenuId(menuId);
        sysUsers.forEach(u -> {
            this.clearUserAuthorityInfo(u.getUserName());
        });
    }

    @Override
    public Page<SysUser> pageBySearch(Page page, String searchUserName) {
        return this.page(page, new QueryWrapper<SysUser>()
                .like(StrUtil.isNotBlank(searchUserName), DBConstant.TABLE_USER.USER_NAME_FIELDNAME, searchUserName));
    }

    public static void main(String[] args) {
        List<SysRole> roles = new ArrayList<>();
        SysRole sysRole = new SysRole();
        sysRole.setCode("normal");
        SysRole sysRole2 = new SysRole();
        sysRole2.setCode("admin");

        roles.add(sysRole);
        roles.add(sysRole2);
        String roleCodes = roles.stream().map(r -> "ROLE_" + r.getCode()).collect(Collectors.joining(","));
        String authority = roleCodes.concat(",");

        System.out.println(roleCodes);
        System.out.println(authority);

    }
}
