package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.SysMenu;
import com.boyi.entity.SysRole;
import com.boyi.entity.SysRoleMenu;
import com.boyi.entity.SysUserRole;
import com.boyi.mapper.SysRoleMapper;
import com.boyi.mapper.SysRoleMenuMapper;
import com.boyi.service.SysRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-15
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    SysRoleMapper sysRoleMapper;

    @Override
    public List<SysRole> listRolesByUserId(Long userId) {

        List<SysRole> sysRoles = this.list(new QueryWrapper<SysRole>()
                .inSql("id", "select role_id from sys_user_role where user_id = " + userId));

        return sysRoles;
    }

    @Override
    public List<Long> getRoleMenusIds(Long id) {
        List<SysRoleMenu> roles = sysRoleMenuMapper.selectList(new QueryWrapper<SysRoleMenu>().select("id","menu_id","role_id")
                .eq("role_id",id));

        ArrayList<Long> roleIds = new ArrayList<>();

        roles.forEach( role ->{
            roleIds.add(role.getMenuId());
        });

        return roleIds;
    }

    @Override
    public List<SysRole> listValid() {
        return sysRoleMapper.selectList(new QueryWrapper<SysRole>().eq(
                DBConstant.TABLE_ROLE.STATUS_FIELDNAME, DBConstant.TABLE_ROLE.STATUS_FIELDVALUE_0));
    }
}
