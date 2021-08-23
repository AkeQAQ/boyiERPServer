package com.boyi.service;

import com.boyi.entity.SysRoleMenu;
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
public interface SysRoleMenuService extends IService<SysRoleMenu> {
    List<Long> getRoleMenusIds(Long id);

}
