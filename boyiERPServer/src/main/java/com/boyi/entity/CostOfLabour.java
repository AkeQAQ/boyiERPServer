package com.boyi.entity;

import java.time.LocalDate;

import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2022-10-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CostOfLabour extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 组成结构外键
     */
    private Long produceProductConstituentId;

    /**
     * 工价类型外键
     */
    private Long costOfLabourTypeId;

    /**
     * 工价日期
     */
    private LocalDate priceDate;

    /**
     * 备注
     */
    private String comment;

    private String createdUser;

    private String updateUser;


}
