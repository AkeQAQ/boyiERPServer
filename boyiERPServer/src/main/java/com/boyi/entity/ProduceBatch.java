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
 * @since 2022-04-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceBatch extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 投产序号
     */
    private Integer batchId;

    private String createdUser;

    private String updatedUser;

    /**
     * 订单序号
     */
    private String orderNum;

    private Integer status;

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
