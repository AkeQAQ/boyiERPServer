package com.boyi.entity;

import java.time.LocalDate;
import java.util.List;

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
 * @since 2023-03-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalAccountRepositorySendOutGoods extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 调整日期
     */
    private LocalDate sendDate;

    /**
     * 客户
     */
    private String customerName;

    private String createdUser;

    private String updateUser;

    private Integer status;

    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productNum;

    @TableField(exist = false)
    private String unit;

    @TableField(exist = false)
    private String num;

    @TableField(exist = false)
    private String price;

    @TableField(exist = false)
    private String amount;

    @TableField(exist = false)
    private String comment;

    @TableField(exist = false)
    private List<ExternalAccountRepositorySendOutGoodsDetails> rowList;

    @ExcelAttribute(sort = 11)
    @TableField(exist = false)  // 字段数据库忽略
    private Double totalAmount;

    @TableField(exist = false)  // 字段数据库忽略
    private Double totalNum; // 该单据总数量
}
