package com.boyi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
 * @since 2023-03-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceSummary extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    @ExcelAttribute(sort = 0)
    private String showId;
    /**
     * 对账月份
     */
    @ExcelAttribute(sort = 1)
    private String summaryDate;

    @ExcelAttribute(sort = 2)
    private String supplierId;

    @ExcelAttribute(sort = 3)
    @TableField(exist = false)
    private String supplierName;

    /**
     * 净入库（采购入库-采购退料）
     */
    @ExcelAttribute(sort = 6)
    private BigDecimal buyNetInAmount;

    /**
     * 采购入库
     */
    @ExcelAttribute(sort = 4)
    private BigDecimal buyInAmount;

    /**
     * 采购退料
     */
    @ExcelAttribute(sort = 5)
    private BigDecimal buyOutAmount;

    /**
     * 赔鞋金额
     */
    @ExcelAttribute(sort = 7)
    private BigDecimal payShoesAmount;

    /**
     * 罚款金额
     */
    @ExcelAttribute(sort = 8)
    private BigDecimal fineAmount;

    /**
     * 检测费金额
     */
    @ExcelAttribute(sort = 9)
    private BigDecimal testAmount;

    /**
     * 补税点金额
     */
    @ExcelAttribute(sort = 10)
    private BigDecimal taxSupplement;

    /**
     * 扣税点金额
     */
    @ExcelAttribute(sort = 11)
    private BigDecimal taxDeduction;

    /**
     * 抹零
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ExcelAttribute(sort = 13)
    private BigDecimal roundDown;

    /**
     * 应付金额
     */
    @ExcelAttribute(sort = 14)
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
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String picUrl;

    @ExcelAttribute(sort = 17)
    private Integer status;

    /**
     * 调整金额
     */
    @ExcelAttribute(sort = 12)
    private BigDecimal changeAmount;

    @TableField(exist = false)
    @ExcelAttribute(sort = 16)
    private BigDecimal remainingAmount;

    @TableField(exist = false)
    @ExcelAttribute(sort = 15)
    private BigDecimal payedAmount;


    @TableField(exist = false)
    private List<FinanceSummaryDetails> rowList;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ExcelAttribute(sort = 18)
    private LocalDate settleDate;

}
