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
 * @since 2022-05-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HisProduceBatch extends BaseEntity {

    private static final long serialVersionUID = 1L;


    private Long id;

    /**
     * 投产序号
     */
    private String batchId;

    private String createdUser;

    private String updatedUser;

    /**
     * 订单序号
     */
    private String orderNum;

    private String size34;

    private String size35;

    private String size36;

    private String size37;

    private String size38;

    private String size39;

    private String size40;

    private String size41;

    private String size42;

    private String size43;

    private String size44;

    private String size45;

    private String size46;

    private String size47;


}
