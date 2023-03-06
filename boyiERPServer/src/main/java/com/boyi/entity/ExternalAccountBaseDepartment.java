package com.boyi.entity;

import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 基础模块-部门管理
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalAccountBaseDepartment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 部门名称。utf8 3字节，最多存储10个中文
     */
    private String name;


}
