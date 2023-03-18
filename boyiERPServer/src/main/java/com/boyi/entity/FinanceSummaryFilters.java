package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2023-03-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceSummaryFilters extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String supplierId;

    @TableField(exist = false)
    private String supplierName;

    private String createdUser;

    private String updatedUser;


}
