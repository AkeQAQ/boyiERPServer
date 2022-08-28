package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2022-08-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderBeforeProductionProgress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 工厂货号
     */
    private String productNum;

    /**
     * 品牌
     */
    private String productBrand;

    private String createdUser;

    private String updatedUser;
    private Integer status;

    @TableField(exist = false)  // 字段数据库忽略
    private List<OrderBeforeProductionProgressDetail> details;
    @TableField(exist = false)  // 字段数据库忽略
    private Integer currentIndex;


}
