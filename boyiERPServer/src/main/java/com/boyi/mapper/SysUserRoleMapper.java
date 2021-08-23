package com.boyi.mapper;

import com.boyi.entity.SysUserRole;
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
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    List<Long> getNavMenuIds(Long userId);

}
