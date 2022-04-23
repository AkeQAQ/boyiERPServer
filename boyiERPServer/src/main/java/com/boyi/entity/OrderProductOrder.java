package com.boyi.entity;

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
 * @since 2022-03-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderProductOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ExcelAttribute(sort = 0)
    /**
     * 我们的订单号
     */
    private String orderNum;

    /**
     * 客户货号
     */
    @ExcelAttribute(sort = 3)
    private String customerNum;

    /**
     * 公司货号
     */
    @ExcelAttribute(sort = 4)
    private String productNum;

    /**
     * 产品品牌
     */
    @ExcelAttribute(sort = 5)
    private String productBrand;

    /**
     * 产品颜色
     */
    @ExcelAttribute(sort = 6)
    private String productColor;

    /**
     * 订单数目
     */
    @ExcelAttribute(sort = 16)
    private Integer orderNumber;

    /**
     * 品牌区域
     */
    @ExcelAttribute(sort = 17)
    private String productRegion;

    /**
     * 0：订单
1: 回单
     */
    @ExcelAttribute(sort = 18)
    private Integer orderType;

    /**
     * 备注
     */
    @ExcelAttribute(sort = 19)
    private String comment;

    private Integer status;

    private String createdUser;

    private String updatedUser;

    private Integer prepared;


    /**
     *  批量备料多余字段
     *
     */
    @TableField(exist = false)
    private String materialId;
    @TableField(exist = false)
    private String materialName;
    @TableField(exist = false)
    private String materialUnit;
    @TableField(exist = false)
    private String dosage;
    @TableField(exist = false)
    private String calNum;
    @TableField(exist = false)
    private String preparedNum;
    @TableField(exist = false)
    private Long orderId;

}
