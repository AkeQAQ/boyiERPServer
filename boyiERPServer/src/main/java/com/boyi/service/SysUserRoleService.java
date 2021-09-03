package com.boyi.service;

import com.boyi.entity.SysUserRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-15
 */
public interface SysUserRoleService extends IService<SysUserRole> {

    List<Long> getUserRolesIds(Long userId);

    void removeByUserIds(Long[] ids);

    void removeByUserId(Long userId);
}
