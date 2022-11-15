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
 * @since 2022-11-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseMaterialSameGroup extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 组名
     */
    private String name;

    private String createdUser;

    private String updateUser;

    private Integer status;

    @TableField(exist = false)
    private List<BaseMaterialSameGroupDetail> details;

    @TableField(exist = false)
    private String materialName;

    @TableField(exist = false)
    private String materialId;


}
