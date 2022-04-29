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


}
