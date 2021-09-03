package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.dto.SysNavDto;
import com.boyi.entity.SysMenu;
import com.boyi.entity.SysUserRole;
import com.boyi.mapper.SysUserRoleMapper;
import com.boyi.service.SysUserRoleService;
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
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    @Autowired
    SysUserRoleMapper sysUserRoleMapper;

    @Override
    public List<Long> getUserRolesIds(Long userId){
        List<SysUserRole> roles = sysUserRoleMapper.selectList(new QueryWrapper<SysUserRole>()
                .select(DBConstant.TABLE_USER_ROLE.ID_FIELDNAME
                        ,DBConstant.TABLE_USER_ROLE.USER_ID_FIELDNAME
                        ,DBConstant.TABLE_USER_ROLE.ROLE_ID_FIELDNAME)
                .eq(DBConstant.TABLE_USER_ROLE.USER_ID_FIELDNAME,userId));

        ArrayList<Long> roleIds = new ArrayList<>();

        roles.forEach( role ->{
            roleIds.add(role.getRoleId());
        });

        return roleIds;
    }

    @Override
    public void removeByUserIds(Long[] ids) {
        this.remove(new QueryWrapper<SysUserRole>().in(DBConstant.TABLE_USER_ROLE.USER_ID_FIELDNAME, ids));
    }

    @Override
    public void removeByUserId(Long userId) {
        this.remove(new QueryWrapper<SysUserRole>().eq(DBConstant.TABLE_USER_ROLE.USER_ID_FIELDNAME, userId));
    }


}
