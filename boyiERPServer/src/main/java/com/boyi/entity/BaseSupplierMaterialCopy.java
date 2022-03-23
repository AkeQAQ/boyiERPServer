package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * <p>
 * 供应商-物料报价表
 * </p>
 *
 * @author sunke
 * @since 2021-08-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseSupplierMaterialCopy extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField(exist = false)  // 字段数据库忽略
    private String supplierName;
    @TableField(exist = false)
    private String unit;
    @TableField(exist = false)
    private String specs;
    @TableField(exist = false)
    private String materialName;

    private Integer status;

    /**
     * 供应商ID
     */
    private String supplierId;

    /**
     * 物料ID
     */
    private String materialId;

    /**
     * 单价
     */
    private Double price;

    /**
     * 生效日期（包含）
     */
    private LocalDate startDate;

    /**
     * 失效日期（包含）默认100年之后，有同供应商，同物料第二条记录，需要修改该字段
     */
    private LocalDate endDate;

    /**
     * 备注
     */
    private String comment;


    /**
     * 创建人
     */
    @TableField("created_user")
    private String createdUser;

    /**
     * 修改人
     */
    @TableField("update_user")
    private String updateUser;

}
