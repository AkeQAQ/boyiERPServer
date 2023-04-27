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
 * @since 2022-03-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceProductConstituentDetail extends BaseEntity {

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
    private String canShowPrint;
    private String content;


    @TableField(exist = false)  // 字段数据库忽略
    private String materialName;

    @TableField(exist = false)  // 字段数据库忽略
    private String unit;

    @TableField(exist = false)  // 字段数据库忽略
    private String specs;

    @TableField(exist = false)  // 字段数据库忽略
    private Boolean canChange;

    private String supplierId;
    private String specialContent1;
    private String specialContent2;

    @TableField(exist = false)  // 字段数据库忽略
    private String supplierName;


}
