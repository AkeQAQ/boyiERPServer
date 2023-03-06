package com.boyi.entity;

import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 供应商-物料报价表
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalAccountBaseSupplierMaterial extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 1)
    private String supplierName;
    @TableField(exist = false)
    @ExcelAttribute(sort = 4)
    private String unit;
    @TableField(exist = false)
    @ExcelAttribute(sort = 5)
    private String specs;
    @TableField(exist = false)
    @ExcelAttribute(sort = 3)
    private String materialName;

    @ExcelAttribute(sort = 9)
    private Integer status;

    /**
     * 供应商ID
     */
    private String supplierId;

    /**
     * 物料ID
     */
    @ExcelAttribute(sort = 2)
    private String materialId;

    /**
     * 单价
     */
    @ExcelAttribute(sort = 6)
    private Double price;

    /**
     * 生效日期（包含）
     */
    @ExcelAttribute(sort = 7)
    private LocalDate startDate;

    /**
     * 失效日期（包含）默认100年之后，有同供应商，同物料第二条记录，需要修改该字段
     */
    @ExcelAttribute(sort = 8)
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
