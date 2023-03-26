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
public class FinanceSupplierTest extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String supplierId;

    @ExcelAttribute(sort = 2)
    @TableField(exist = false)
    private String supplierName;


    /**
     * 检测费金额
     */
    @ExcelAttribute(sort = 3)

    private BigDecimal testAmount;

    @ExcelAttribute(sort = 1)
    private LocalDate testDate;

    private String createdUser;

    private String updateUser;

    @ExcelAttribute(sort = 4)
    private String documentNum;

    @ExcelAttribute(sort = 5)
    private Integer status;


    @TableField(exist = false)
    private Double totalAmount;


}
