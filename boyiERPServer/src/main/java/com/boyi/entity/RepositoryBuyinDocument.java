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
 * 仓库模块-采购入库单据表
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RepositoryBuyinDocument extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ExcelAttribute(sort = 5)
    private Integer status;

    /**
     * 供应商ID外键
     */
    private String supplierId;

    /**
     * 供应商单据编号
     */
    private String supplierDocumentNum;

    /**
     * 入库日期
     */
    @ExcelAttribute(sort = 1)
    private LocalDate buyInDate;

    private String createdUser;

    @ExcelAttribute(sort = 3)
    private String updatedUser;

    private Integer sourceType;


    /**
     *  明细信息 用于接收前端数据
     */
    @TableField(exist = false)  // 字段数据库忽略
    private List<RepositoryBuyinDocumentDetail> rowList;


    // 用于多表查询的额外字段
    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 2)
    private String supplierName;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 6)
    private String materialId;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 7)
    private String materialName;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 8)
    private String unit;

    @ExcelAttribute(sort = 10)
    @TableField(exist = false)  // 字段数据库忽略
    private Double price;

    @ExcelAttribute(sort = 9)
    @TableField(exist = false)  // 字段数据库忽略
    private Double num;


    @ExcelAttribute(sort = 11)
    @TableField(exist = false)  // 字段数据库忽略
    private Double amount; // =price*num

    @TableField(exist = false)  // 字段数据库忽略
    private Double totalNum; // 该单据总数量

    @TableField(exist = false)  // 字段数据库忽略
    private Double totalAmount; // 该单据总金额

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 13)
    private String orderSeq; // 采购订单的单号

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 12)
    private LocalDate priceDate; // 采购订单详情的采购价

    @TableField(exist = false)  // 字段数据库忽略
    private Long orderId; // 采购订单的ID


}
