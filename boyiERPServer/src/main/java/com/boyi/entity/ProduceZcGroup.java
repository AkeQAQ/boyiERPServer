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
 * @since 2023-04-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceZcGroup extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String groupName;

    private String createdUser;

    private String updateUser;


}
