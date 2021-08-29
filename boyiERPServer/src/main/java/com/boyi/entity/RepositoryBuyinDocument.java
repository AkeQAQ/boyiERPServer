package com.boyi.entity;

import java.time.LocalDate;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
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
    private LocalDate buyInDate;

    /**
     *  明细信息 用于接收前端数据
     */
    @TableField(exist = false)  // 字段数据库忽略
    private List<RepositoryBuyinDocumentDetail> rowList;


    // 用于多表查询的额外字段
    @TableField(exist = false)  // 字段数据库忽略
    private String supplierName;
    @TableField(exist = false)  // 字段数据库忽略
    private String materialId;
    @TableField(exist = false)  // 字段数据库忽略
    private String materialName;
    @TableField(exist = false)  // 字段数据库忽略
    private String unit;
    @TableField(exist = false)  // 字段数据库忽略
    private Double price;
    @TableField(exist = false)  // 字段数据库忽略
    private Integer num;


    @TableField(exist = false)  // 字段数据库忽略
    private Double amount; // =price*num


    private String createdUser;
    private String updatedUser;

}
