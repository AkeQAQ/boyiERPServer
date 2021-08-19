package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
        return getOne(new QueryWrapper<SysUser>().eq("user_name", username));
    }

    @Override
    public List<Long> getUserRolesIds(Long userId){
        List<SysUserRole> roles = sysUserRoleMapper.selectList(new QueryWrapper<SysUserRole>().select("id","user_id","role_id")
                .eq("user_id",userId));

        ArrayList<Long> roleIds = new ArrayList<>();

        roles.forEach( role ->{
            roleIds.add(role.getRoleId());
        });

        return roleIds;
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
                    .inSql("id", "select role_id from sys_user_role where user_id = " + userId));

            // 获取角色编码
            if (roles.size() > 0) {
                authority = roles.stream().map(r -> "ROLE_" + r.getCode()).collect(Collectors.joining(","));
            }

            // 获取菜单操作编码
            List<Long> menuIds = sysUserMapper.getNavMenuIds(userId);
            if (menuIds.size() > 0) {
                List<SysMenu> menus = sysMenuMapper.selectBatchIds(menuIds);
                String menuPerms = menus.stream().map(m -> m.getAuthority()).collect(Collectors.joining(","));

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
        List<SysUser> sysUsers = this.list(new QueryWrapper<SysUser>().inSql("id", "select user_id from sys_user_role where role_id = " + roleId));
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
