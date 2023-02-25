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
 * @since 2023-02-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceSupplierPayshoesDetails extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 赔鞋主表外键
     */
    private Long payShoesId;

    /**
     * 客户货号
     */
    private String customerNum;

    /**
     * 赔鞋数量
     */
    private BigDecimal payNumber;

    /**
     * 赔鞋金额
     */
    private BigDecimal payAmount;

    /**
     * 赔鞋类型。0：大货，1：残鞋
     */
    private Integer payType;

    private String createdUser;

    private String updateUser;


}
