package com.boyi.entity;

import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2022-04-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HisProduceOrderMaterialProgress extends BaseEntity {

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
     * 应报备用料
     */
    private String calNum;

    /**
     * 进度，0-100
     */
    private Integer progressPercent;

    /**
     * 备注
     */
    private String comment;

    /**
     * 补数备料状态
     */
    private Integer complementStatus;


}
