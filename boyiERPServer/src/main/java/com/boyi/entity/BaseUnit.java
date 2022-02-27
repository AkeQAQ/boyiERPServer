package com.boyi.entity;

import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 基础模块-计量单位管理
 * </p>
 *
 * @author sunke
 * @since 2021-08-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseUnit extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 单位唯一编码
     */
    private String code;

    /**
     * 计量单位名称
     */
    private String name;

    private int priority;


}
