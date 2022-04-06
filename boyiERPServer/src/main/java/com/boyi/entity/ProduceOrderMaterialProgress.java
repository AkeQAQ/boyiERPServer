package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2022-03-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceOrderMaterialProgress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 产品订单的ID号，有ID 的是订单报，没ID是补单报
     */
    private Long orderId;

    /**
     * 备料的物料编码
     */
    private String materialId;


    /**
     * 已经报备料的数量
     */
    private String preparedNum;

    private String createdUser;

    private String updatedUser;

    /**
     * 已入库数量
     */
    private String inNum;

    /**
     *  应报物料数目
     */
    private String calNum;

    private Integer progressPercent;
    private String comment;

    @TableField(exist = false)  // 字段数据库忽略
    private String orderNum;

    @TableField(exist = false)  // 字段数据库忽略
    private String productNum;

    @TableField(exist = false)  // 字段数据库忽略
    private String productBrand;

    @TableField(exist = false)  // 字段数据库忽略
    private String productColor;

    @TableField(exist = false)  // 字段数据库忽略
    private String unit;

    @TableField(exist = false)  // 字段数据库忽略
    private Integer status;

    @TableField(exist = false)  // 字段数据库忽略
    private Integer prepared;

    @TableField(exist = false)
    private String addNum;

    @TableField(exist = false) // 批量备料用
    private String addNums;

    @TableField(exist = false)
    private String orderNumber;
    /**
     * 备料的物料名称
     */
    @TableField(exist = false)
    private String materialName;

    /**
     *  批量备料接受参数用
     */
    @TableField(exist = false)
    private List<Map<String,Object>> details;

}
