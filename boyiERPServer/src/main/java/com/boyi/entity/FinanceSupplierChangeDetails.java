package com.boyi.entity;

import java.math.BigDecimal;

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
 * @since 2023-02-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceSupplierChangeDetails extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * change表外键
     */
    private Long changeId;

    /**
     * 物料ID外键
     */
    private String materialId;

    /**
     * 数量
     */
    private BigDecimal num;

    /**
     * 调整单价
     */
    private BigDecimal changePrice;

    /**
     * 调整金额
     */
    private BigDecimal changeAmount;

    /**
     * 备注
     */
    private String comment;

    @TableField(exist = false)
    private String materialName;

    @TableField(exist = false)
    private String unit;




}
