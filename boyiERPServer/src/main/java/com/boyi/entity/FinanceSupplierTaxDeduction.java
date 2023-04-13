package com.boyi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
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
 * @since 2023-02-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceSupplierTaxDeduction extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String supplierId;
    @TableField(exist = false)
    @ExcelAttribute(sort = 2)
    private String supplierName;

    /**
     * 开票单位
     */
    @ExcelAttribute(sort = 3)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String company;

    /**
     * 开票日期
     */
    @ExcelAttribute(sort = 1)
    private LocalDate documentDate;

    private String createdUser;

    private String updateUser;

    /**
     * 发票号
     */
    @ExcelAttribute(sort = 4)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String documentNum;

    /**
     * 图片
     */
    private String picUrl;

    /**
     * 税额扣款
     */
    @ExcelAttribute(sort = 8)
    private BigDecimal deductionAmount;

    /**
     * 税点
     */
    @ExcelAttribute(sort = 7)
    private BigDecimal taxPoint;

    /**
     * 开票金额
     */
    @ExcelAttribute(sort = 5)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal documentAmount;

    /**
     * 1:未付、0：已付
     */
    @ExcelAttribute(sort = 10)
    private Integer payStatus;

    /**
     * 付款状态
     */
    private LocalDate payDate;

    /**
     * 含税未开票金额
     */
    @ExcelAttribute(sort = 6)
    private BigDecimal taxCalAmount;

    @ExcelAttribute(sort = 9)
    private Integer status;


    @TableField(exist = false)
    private Double totalAmount;

}
