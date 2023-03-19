package com.boyi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
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
 * @since 2023-02-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceSupplierTaxSupplement extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String supplierId;

    @TableField(exist = false)
    private String supplierName;

    /**
     * 开票单位
     */
    private String company;

    /**
     * 开票日期
     */
    private LocalDate documentDate;

    private String createdUser;

    private String updateUser;

    /**
     * 发票号
     */
    private String documentNum;

    /**
     * 图片
     */
    private String picUrl;

    /**
     * 税金额
     */
    private BigDecimal taxSupplementAmount;

    /**
     * 税点
     */
    private BigDecimal taxPoint;

    /**
     * 开票金额
     */
    private BigDecimal documentAmount;

    /**
     * 1:未付、0：已付
     */
    @TableField(exist = false)
    private Integer payStatus;

    @TableField(exist = false)
    private LocalDate payDate;
    private Integer status;


    @TableField(exist = false)
    private Double totalAmount;

    @TableField(exist = false)
    private Double payedAmount;

    @TableField(exist = false)
    private Double documentTotalAmount;


    @TableField(exist = false)
    private Double totalDocumentPayedAmount;

    @TableField(exist = false)
    private Double lostAmount;


    @TableField(exist = false)
    private Double payAmount;

}
