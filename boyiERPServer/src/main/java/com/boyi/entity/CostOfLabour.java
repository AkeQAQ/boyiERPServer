package com.boyi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
 * @since 2022-10-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CostOfLabour extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 组成结构外键
     */
    private Long produceProductConstituentId;

    /**
     * 工价类型外键
     */
    private Long costOfLabourTypeId;

    /**
     * 工价日期
     */
    private LocalDate priceDate;

    /**
     * 备注
     */
    private String comment;

    private String createdUser;

    private String updateUser;

    private Integer status;

    @TableField(exist = false)
    private List<CostOfLabourDetail> rowList;
    @TableField(exist = false)
    private String productNum;
    @TableField(exist = false)
    private String productBrand;
    @TableField(exist = false)
    private String costOfLabourTypeName;
    @TableField(exist = false)
    private String processesName;

    @TableField(exist = false)
    private String calPiecesPrice;

    @TableField(exist = false)
    private String lowPrice;

    @TableField(exist = false)
    private String realPrice;

    @TableField(exist = false)
    private String reason;

    @TableField(exist = false)
    private String totalPrice;

    @TableField(exist = false)
    private String prePrice;


}
