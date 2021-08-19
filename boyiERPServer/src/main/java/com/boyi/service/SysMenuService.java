package com.boyi.service;

import com.boyi.common.dto.SysNavDto;
import com.boyi.entity.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.SysUser;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-15
 */
public interface SysMenuService extends IService<SysMenu> {

    List<SysNavDto> getCurrentUserNav(SysUser sysUser) throws Exception;

    List<SysMenu> buildTreeMenu(List<SysMenu> menus);

    List<SysNavDto> convert(List<SysMenu> menus);

    List<SysMenu> listValid();

}
