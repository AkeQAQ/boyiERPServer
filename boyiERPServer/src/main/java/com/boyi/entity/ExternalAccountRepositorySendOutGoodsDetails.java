package com.boyi.entity;

import java.math.BigDecimal;

import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2023-03-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalAccountRepositorySendOutGoodsDetails extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * send表外键
     */
    private Long sendId;

    /**
     * 物料ID外键
     */
    private String productNum;

    /**
     * 数量
     */
    private BigDecimal num;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String productName;

    private String unit;


    private String comment;
}
