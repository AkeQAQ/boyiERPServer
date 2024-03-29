package com.boyi.entity;

import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 仓库模块-采购入库单-详情内容
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Data
public class ExternalAccountRepositoryBuyinDocumentDetail {

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
     * 采购入库换算数量
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
     *  订单号
     */
    private String orderSeq;

    /**
     * 价目日期
     */
    private LocalDate priceDate;

    /**
     *  采购订单下推进行入库的的主键ID
     */
    private Long orderId;

    /**
     *  采购订单详情下推进行入库的的主键ID
     */
    private Long orderDetailId;

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


}
