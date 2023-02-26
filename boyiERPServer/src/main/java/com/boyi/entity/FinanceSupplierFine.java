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
public class FinanceSupplierFine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String supplierId;

    /**
     * 罚款金额
     */
    private BigDecimal fineAmount;

    private LocalDate fineDate;

    private String createdUser;

    private String updateUser;

    private String documentNum;

    private String picUrl;

    private String fineReason;


    @TableField(exist = false)
    private String supplierName;

    private Integer status;



}
