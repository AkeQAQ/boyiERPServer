package com.boyi.entity;

import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2022-04-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HisOrderProductOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 我们的订单号
     */
    private String orderNum;

    /**
     * 客户货号
     */
    private String customerNum;

    /**
     * 公司货号
     */
    private String productNum;

    /**
     * 产品品牌
     */
    private String productBrand;

    /**
     * 产品颜色
     */
    private String productColor;

    /**
     * 品牌区域
     */
    private String productRegion;

    /**
     * 0：订单
1: 回单
     */
    private Integer orderType;

    /**
     * 备注
     */
    private String comment;

    private String createdUser;

    private String updatedUser;

    /**
     * 订单数量
     */
    private Integer orderNumber;

    /**
     * 备料完成状态
     */
    private Integer prepared;


}
