package com.boyi.entity;

import java.math.BigDecimal;
import java.util.List;

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
 * @since 2023-03-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceSummary extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 对账月份
     */
    private String summaryDate;

    private String supplierId;
    @TableField(exist = false)
    private String supplierName;

    /**
     * 净入库（采购入库-采购退料）
     */
    private BigDecimal buyNetInAmount;

    /**
     * 采购入库
     */
    private BigDecimal buyInAmount;

    /**
     * 采购退料
     */
    private BigDecimal buyOutAmount;

    /**
     * 赔鞋金额
     */
    private BigDecimal payShoesAmount;

    /**
     * 罚款金额
     */
    private BigDecimal fineAmount;

    /**
     * 检测费金额
     */
    private BigDecimal testAmount;

    /**
     * 补税点金额
     */
    private BigDecimal taxSupplement;

    /**
     * 扣税点金额
     */
    private BigDecimal taxDeduction;

    /**
     * 抹零
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal roundDown;

    /**
     * 应付金额
     */
    private BigDecimal needPayAmount;

    /**
     * 扣（补）款小计
     */
    @TableField(exist = false)
    private BigDecimal otherTotalAmount;

    private String createdUser;

    private String updatedUser;

    /**
     * 结账单图片
     */
    private String picUrl;

    private Integer status;

    /**
     * 调整金额
     */
    private BigDecimal changeAmount;

    @TableField(exist = false)
    private BigDecimal remainingAmount;

    @TableField(exist = false)
    private BigDecimal payedAmount;


    @TableField(exist = false)
    private List<FinanceSummaryDetails> rowList;

}
