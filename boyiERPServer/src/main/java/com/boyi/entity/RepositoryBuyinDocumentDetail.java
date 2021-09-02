package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 仓库模块-采购入库单-详情内容
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Data
public class RepositoryBuyinDocumentDetail {

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
    private Integer num;

    /**
     *  备注信息
     */
    private String comment;

    @TableField(exist = false)  // 字段数据库忽略
    private String materialName;
    @TableField(exist = false)  // 字段数据库忽略
    private String unit;
    @TableField(exist = false)  // 字段数据库忽略
    private String specs;
    @TableField(exist = false)  // 字段数据库忽略
    private Double price;

    @TableField(exist = false)  // 字段数据库忽略
    private Double amount;

}
