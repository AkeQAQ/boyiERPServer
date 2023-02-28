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
 * @since 2023-02-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceSupplierTaxDeduction extends BaseEntity {

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
     * 税额扣款
     */
    private BigDecimal deductionAmount;

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
    private Integer payStatus;

    /**
     * 付款状态
     */
    private LocalDate payDate;

    /**
     * 含税未开票金额
     */
    private BigDecimal taxCalAmount;

    private Integer status;

}
