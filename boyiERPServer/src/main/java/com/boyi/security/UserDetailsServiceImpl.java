package com.boyi.security;

import com.boyi.common.constant.DBConstant;
import com.boyi.entity.SysUser;
import com.boyi.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.List;

/**
 * 因为security在认证用户身份的时候会调用UserDetailsService.loadUserByUsername()方法,获取用户信息
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserService.getByUsername(username);
        log.info("获取用户:{},的信息", sysUser);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户名或密码不正确!");
        }else if(sysUser.getStatus() == DBConstant.TABLE_USER.STATUS_FIELDVALUE_1){
            // 用户是禁止状态的话，不允许登陆(目前还没实现，禁止还是提示的 用户名和密码错误)
            log.info("用户:{} 禁止，无法登陆", sysUser);
            throw new UsernameNotFoundException("该用户已被禁止，无法登陆.");
        }
        return new AccountUser(sysUser.getId(), sysUser.getUserName(), sysUser.getPassword(), getUserAuthority(sysUser));
    }

    public List<GrantedAuthority> getUserAuthority(SysUser sysUser) {
        // 通过内置的工具类，把权限字符串封装成GrantedAuthority列表
        return AuthorityUtils.commaSeparatedStringToAuthorityList(sysUserService.getUserAuthorityInfo(sysUser));
    }
}