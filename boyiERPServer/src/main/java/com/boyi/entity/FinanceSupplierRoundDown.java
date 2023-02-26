package com.boyi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
public class FinanceSupplierRoundDown extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String supplierId;

    /**
     * 抹零金额
     */
    private BigDecimal roundDownAmount;

    private LocalDate roundDownDate;

    private String createdUser;

    private String updateUser;

    private Integer status;

    @TableField(exist = false)
    private String supplierName;



}
