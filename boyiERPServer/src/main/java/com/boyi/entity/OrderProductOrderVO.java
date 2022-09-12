package com.boyi.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2022-09-04
 */
@Data
public class OrderProductOrderVO {

    private static final long serialVersionUID = 1L;
    /**
     * 合并来源订单
     */
    private String orders;

    /**
     * 合并目标订单
     */
    private String toMergeOrder;

}
