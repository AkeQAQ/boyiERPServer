package com.boyi.entity;

import java.time.LocalDate;
import java.util.List;

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
 * @since 2023-02-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceSupplierChange extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 调整日期
     */
    private LocalDate changeDate;

    /**
     * 供应商
     */
    private String supplierId;

    private String createdUser;

    private String updateUser;

    private Integer status;

    @TableField(exist = false)
    private String supplierName;

    @TableField(exist = false)
    private String materialId;
    @TableField(exist = false)
    private String materialName;

    @TableField(exist = false)
    private String unit;

    @TableField(exist = false)
    private String num;

    @TableField(exist = false)
    private String changePrice;

    @TableField(exist = false)
    private String changeAmount;

    @TableField(exist = false)
    private String comment;

    @TableField(exist = false)
    private List<FinanceSupplierChangeDetails> rowList;

    @TableField(exist = false)
    private Double totalAmount;


}
