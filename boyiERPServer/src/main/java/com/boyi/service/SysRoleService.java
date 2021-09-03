package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.SysRole;
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
public interface SysRoleService extends IService<SysRole> {

    List<SysRole> listRolesByUserId(Long id);


    List<SysRole> listValid();

    Page<SysRole> pageBySearch(Page page, String searchRoleName);
}
