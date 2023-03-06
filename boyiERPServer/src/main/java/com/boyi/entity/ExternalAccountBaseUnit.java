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
 * @since 2023-03-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalAccountBaseUnit extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 单位唯一编码
     */
    private String code;

    private String name;

    /**
     * 优先级,默认-1
     */
    private Integer priority;


}
