package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2022-10-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CostOfLabourType extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 工价类别名称
     */
    private String typeName;

    /**
     * 角色IDs
     */
    private String roleId;


    @TableField(exist = false)
    private List<SysRole> roles;


}
