package com.boyi.mapper;

import com.boyi.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2021-08-15
 */
@Repository

public interface SysUserMapper extends BaseMapper<SysUser> {

    List<SysUser> listByMenuId(Long menuId);
}
