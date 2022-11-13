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
 * @since 2022-11-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseCustomer extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 客户名称
     */
    private String name;

    private String createdUser;

    private String updateUser;


}
