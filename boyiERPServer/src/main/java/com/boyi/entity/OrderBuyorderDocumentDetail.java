package com.boyi.entity;

import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * <p>
 * 订单模块-采购订单-详情内容
 * </p>
 *
 * @author sunke
 * @since 2021-09-04
 */
@Data
public class OrderBuyorderDocumentDetail  {

    private static final long serialVersionUID = 1L;


    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 物料id
     */
    private String materialId;

    /**
     * 采购入库单的外键
     */
    private Long documentId;

    /**
     * 采购入库数量
     */
    private Double num;

    /**
     * 采购入库库存数量
     */
    private Double radioNum;

    /**
     *  备注信息
     */
    private String comment;

    /**
     *  供应商ID
     */
    private String supplierId;

    /**
     *  状态
     */
    private Integer status;



    @TableField(exist = false)  // 字段数据库忽略
    private String materialName;
    @TableField(exist = false)  // 字段数据库忽略
    private String unit;
    @TableField(exist = false)  // 字段数据库忽略
    private String bigUnit;
    @TableField(exist = false)  // 字段数据库忽略
    private Integer unitRadio;
    @TableField(exist = false)  // 字段数据库忽略
    private String specs;
    @TableField(exist = false)  // 字段数据库忽略
    private Double price;

    @TableField(exist = false)  // 字段数据库忽略
    private Double amount;

    @TableField(exist = false)  // 字段数据库忽略
    private String supplierName;

    /**
     * 交货日期
     */
    private LocalDate doneDate;

    /**
     * 订单号
     */
    private String orderSeq;

    /**
     * 采购日期
     */
    private LocalDate orderDate;

}
