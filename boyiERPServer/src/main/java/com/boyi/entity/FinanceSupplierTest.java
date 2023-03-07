package com.boyi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

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
 * @since 2023-02-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceSupplierTest extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String supplierId;

    @TableField(exist = false)
    private String supplierName;


    /**
     * 检测费金额
     */
    private BigDecimal testAmount;

    private LocalDate testDate;

    private String createdUser;

    private String updateUser;

    private String documentNum;

    private Integer status;


    @TableField(exist = false)
    private Double totalAmount;


}
