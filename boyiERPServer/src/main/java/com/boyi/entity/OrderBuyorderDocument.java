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
 * 订单模块-采购订单单据表
 * </p>
 *
 * @author sunke
 * @since 2021-09-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderBuyorderDocument extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Integer status;

    /**
     * 供应商ID外键
     */
    private String supplierId;

    /**
     * 采购日期
     */
    @ExcelAttribute(sort = 1)
    private LocalDate orderDate;

    private String createdUser;

    private String updatedUser;


    /**
     *  明细信息 用于接收前端数据
     */
    @TableField(exist = false)  // 字段数据库忽略
    private List<OrderBuyorderDocumentDetail> rowList;


    // 用于多表查询的额外字段
    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 2)
    private String supplierName;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 4)
    private String materialId;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 5)
    private String materialName;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 6)
    private String unit;

    @ExcelAttribute(sort = 9)
    @TableField(exist = false)  // 字段数据库忽略
    private Double price;

    @ExcelAttribute(sort = 7)
    @TableField(exist = false)  // 字段数据库忽略
    private Double num;

    @ExcelAttribute(sort = 10)
    @TableField(exist = false)  // 字段数据库忽略
    private Double amount; // =price*num

    @TableField(exist = false)  // 字段数据库忽略
    private Double totalNum; // 该单据总数量

    @TableField(exist = false)  // 字段数据库忽略
    private Double totalAmount; // 该单据总金额

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 8)
    private LocalDate doneDate; // 该单据总金额

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 11)
    private String orderSeq; //

    @TableField(exist = false)  // 字段数据库忽略
    private Long detailId; // 详情ID

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 3)
    private Integer detailStatus; // 详情的状态

}
