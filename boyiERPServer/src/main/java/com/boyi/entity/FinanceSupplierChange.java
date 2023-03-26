package com.boyi.entity;

import java.time.LocalDate;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2023-02-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceSupplierChange extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 调整日期
     */
    @ExcelAttribute(sort = 1)
    private LocalDate changeDate;

    /**
     * 供应商
     */
    private String supplierId;

    private String createdUser;

    private String updateUser;

    @ExcelAttribute(sort = 3)
    private Integer status;

    @ExcelAttribute(sort = 2)
    @TableField(exist = false)
    private String supplierName;

    @TableField(exist = false)
    @ExcelAttribute(sort = 4)
    private String materialId;

    @ExcelAttribute(sort = 5)
    @TableField(exist = false)
    private String materialName;

    @ExcelAttribute(sort = 6)
    @TableField(exist = false)
    private String unit;

    @ExcelAttribute(sort = 7)
    @TableField(exist = false)
    private String num;

    @ExcelAttribute(sort = 8)
    @TableField(exist = false)
    private String changePrice;

    @ExcelAttribute(sort = 9)
    @TableField(exist = false)
    private String changeAmount;

    @ExcelAttribute(sort = 10)
    @TableField(exist = false)
    private String comment;

    @TableField(exist = false)
    private List<FinanceSupplierChangeDetails> rowList;

    @TableField(exist = false)
    private Double totalAmount;


}
