package com.boyi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class FinanceSummaryDetails extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * finance_summary的外键
     */
    private Long summaryId;

    /**
     * 付款时间
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime payDate;

    /**
     * 付款金额
     */
    private BigDecimal payAmount;

    /**
     * 0:对公转账,1:对公承兑,2:对私转账,3:对私承兑
     */
    private Integer payType;

    private String createdUser;

    private String updatedUser;


}
