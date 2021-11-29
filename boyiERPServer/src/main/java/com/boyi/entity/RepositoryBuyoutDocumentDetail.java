package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDate;

/**
 * <p>
 * 仓库模块-采购退料单-详情内容
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Data
public class RepositoryBuyoutDocumentDetail {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 物料id
     */
    private String materialId;

    /**
     * 采购退料单的外键
     */
    private Long documentId;

    /**
     * 采购退料数量
     */
    private Double num;

    /**
     * 采购退料换算数量
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
     *  价目日期
     */
    private LocalDate priceDate;

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
