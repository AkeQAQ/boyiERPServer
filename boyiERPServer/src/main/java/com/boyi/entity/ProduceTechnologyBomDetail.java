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
 * @since 2023-04-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceTechnologyBomDetail extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 组成的物料ID
     */
    private String materialId;

    /**
     * 一双的用量
     */
    private String dosage;

    /**
     * 外键ID
     */
    private Long constituentId;

    private String createdUser;

    private String updatedUser;

    /**
     * 0:能显示，1：不能显示
     */
    private String canShowPrint;

    /**
     * 备注说明
     */
    private String content;

    private String supplierId;

    private String specialContent1;

    private String specialContent2;


    @TableField(exist = false)  // 字段数据库忽略
    private String materialName;

    @TableField(exist = false)  // 字段数据库忽略
    private String unit;

    @TableField(exist = false)  // 字段数据库忽略
    private String specs;

    @TableField(exist = false)  // 字段数据库忽略
    private Boolean canChange;

    @TableField(exist = false)  // 字段数据库忽略
    private String supplierName;

}
