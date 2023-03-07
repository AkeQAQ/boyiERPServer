package com.boyi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    @ExcelAttribute(sort = 4)
    private BigDecimal fineAmount;

    @ExcelAttribute(sort = 1)
    private LocalDate fineDate;

    private String createdUser;

    private String updateUser;

    @ExcelAttribute(sort = 3)
    private String documentNum;

    private String picUrl;
    @ExcelAttribute(sort = 5)
    private String fineReason;


    @TableField(exist = false)
    @ExcelAttribute(sort = 2)
    private String supplierName;
    @ExcelAttribute(sort = 6)
    private Integer status;

    @TableField(exist = false)
    private Double totalAmount;


}
