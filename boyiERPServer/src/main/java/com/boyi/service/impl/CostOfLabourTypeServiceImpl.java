package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.CostOfLabourType;
import com.boyi.entity.SysRole;
import com.boyi.entity.SysUser;
import com.boyi.mapper.CostOfLabourTypeMapper;
import com.boyi.service.CostOfLabourTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.service.SysRoleService;
import com.boyi.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-10-26
 */
@Service
public class CostOfLabourTypeServiceImpl extends ServiceImpl<CostOfLabourTypeMapper, CostOfLabourType> implements CostOfLabourTypeService {

    @Autowired
    public SysUserService sysUserService;

    @Autowired
    public SysRoleService sysRoleService;

    @Override
    public List<CostOfLabourType> listByName(String name) {
        List<CostOfLabourType> currentUserOwnerTypes = new ArrayList<>();

        SysUser currentUser = sysUserService.getByUsername(name);
        List<SysRole> sysRoles = sysRoleService.listRolesByUserId(currentUser.getId());
        Set<String> userRoleIds = new HashSet<>();
        for(SysRole role:sysRoles){
            userRoleIds.add(role.getId()+"");
        }


        currentUserOwnerTypes = new ArrayList<>();

        List<CostOfLabourType> allTypeLists = this.list();
        a:for (CostOfLabourType type : allTypeLists){
            String roleId = type.getRoleId();
            if(roleId==null || roleId.isEmpty()){
                continue;
            }
            String[] roles = roleId.split(",");
            b:for (String role : roles){
                if(userRoleIds.contains(role)){
                    currentUserOwnerTypes.add(type);
                    continue a;
                }
            }

        }
        return currentUserOwnerTypes;
    }
}
