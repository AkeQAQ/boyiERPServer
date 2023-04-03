package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import com.boyi.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2022-04-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceBatch extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 投产序号
     */
    @ExcelAttribute(sort = 1)
    private String batchId;

    private String createdUser;

    private String updatedUser;

    /**
     * 订单序号
     */
    @ExcelAttribute(sort = 0)
    private String orderNum;

    private Integer status;

    private String size34;
    private String size35;
    private String size36;
    @ExcelAttribute(sort = 7)
    private String size37;
    @ExcelAttribute(sort = 8)
    private String size38;
    @ExcelAttribute(sort = 9)
    private String size39;
    @ExcelAttribute(sort = 10)
    private String size40;
    @ExcelAttribute(sort = 11)
    private String size41;
    @ExcelAttribute(sort = 12)
    private String size42;
    @ExcelAttribute(sort = 13)
    private String size43;
    @ExcelAttribute(sort = 14)
    private String size44;
    private String size45;
    private String size46;
    private String size47;
    private Integer push;

    @ExcelAttribute(sort = 3)
    @TableField(exist = false)
    private String productNum;

    @ExcelAttribute(sort = 4)
    @TableField(exist = false)
    private String productBrand;

    @TableField(exist = false)
    private Integer orderType;


    @TableField(exist = false)
    private String endDate;

    @TableField(exist = false)
    private String mergeBatchId;

    @TableField(exist = false)
    private String mergeBatchNumber;

    @TableField(exist = false)
    private List<ProduceBatchProgress> progresses;

    @TableField(exist = false)
    private List<ProduceBatchDelay> delays;

    @TableField(exist = false)
    private String costOfLabourTypeName;

    @TableField(exist = false)
    private Long costOfLabourTypeId;


    @TableField(exist = false)
    private LocalDateTime outDate;

    @TableField(exist = false)
    private String materialName;


    @TableField(exist = false)
    private String supplierName;


    @TableField(exist = false)
    private LocalDateTime sendForeignProductDate;


    @TableField(exist = false)
    private LocalDateTime backForeignProductDate;

    @TableField(exist = false)
    private Integer isAccept;

    @TableField(exist = false)
    private Integer seq;

    @TableField(exist = false)
    private String zcPickId;


    @TableField(exist = false)
    private Long produceBatchProgressId;


    @TableField(exist = false)
    private List<ProduceBatchZcProgress> zcProgresses;


    @TableField(exist = false)
    private String groupName;
}
