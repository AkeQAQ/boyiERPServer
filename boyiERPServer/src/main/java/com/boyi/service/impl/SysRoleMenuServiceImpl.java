package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.SysRoleMenu;
import com.boyi.mapper.SysRoleMenuMapper;
import com.boyi.service.SysRoleMenuService;
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
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {
    @Autowired
    SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public List<Long> getRoleMenusIds(Long id) {
        List<SysRoleMenu> roles = sysRoleMenuMapper.selectList(new QueryWrapper<SysRoleMenu>()
                .select(DBConstant.TABLE_ROLE_MENU.ID_FIELDNAME,DBConstant.TABLE_ROLE_MENU.MENU_ID_FIELDNAME,DBConstant.TABLE_ROLE_MENU.ROLE_ID_FIELDNAME)
                .eq(DBConstant.TABLE_ROLE_MENU.ROLE_ID_FIELDNAME,id));

        ArrayList<Long> roleIds = new ArrayList<>();

        roles.forEach( role ->{
            roleIds.add(role.getMenuId());
        });

        return roleIds;
    }

    @Override
    public void removeByMenuId(Long id) {
        this.remove(new QueryWrapper<SysRoleMenu>().eq(DBConstant.TABLE_ROLE_MENU.MENU_ID_FIELDNAME, id));
    }

    @Override
    public void removeByRoleIds(Long[] ids) {
        this.remove(new QueryWrapper<SysRoleMenu>().in(DBConstant.TABLE_ROLE_MENU.ROLE_ID_FIELDNAME, ids));
    }

    @Override
    public void removeByRoleId(Long roleId) {
        this.remove(new QueryWrapper<SysRoleMenu>().eq(DBConstant.TABLE_ROLE_MENU.ROLE_ID_FIELDNAME, roleId));
    }
}
