package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
public class BaseMaterialSameGroupDetail extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String materialId;

    /**
     * same_group外键
     */
    private Long groupId;

    private String createdUser;

    private String updateUser;

    @TableField(exist = false)
    private String materialName;


}
