package com.boyi.entity;

import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2021-08-15
 */
@Data
public class SysRoleMenu  {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long roleId;

    private Long menuId;


}
