package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
        List<SysRoleMenu> roles = sysRoleMenuMapper.selectList(new QueryWrapper<SysRoleMenu>().select("id","menu_id","role_id")
                .eq("role_id",id));

        ArrayList<Long> roleIds = new ArrayList<>();

        roles.forEach( role ->{
            roleIds.add(role.getMenuId());
        });

        return roleIds;
    }
}
