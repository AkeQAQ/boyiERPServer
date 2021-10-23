package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-15
 */
public interface SysUserService extends IService<SysUser> {

    SysUser getByUsername(String username);

    String getUserAuthorityInfo(SysUser userId);

    // 删除某个用户的权限信息
    void clearUserAuthorityInfo(String username);

    // 删除所有与该角色关联的用户的权限信息
    void clearUserAuthorityInfoByRoleId(Long roleId);

    // 删除所有与该菜单关联的所有用户的权限信息
    void clearUserAuthorityInfoByMenuId(Long menuId);

    Page<SysUser> pageBySearch(Page page, String searchUserName);
}
