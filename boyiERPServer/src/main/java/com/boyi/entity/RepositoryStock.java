package com.boyi.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 库存表
 * </p>
 *
 * @author sunke
 * @since 2021-09-02
 */
@Data
public class RepositoryStock {

    private static final long serialVersionUID = 1L;

    /**
     * 供应商id
     */
    private String supplierId;

    /**
     * 物料ID
     */
    private String materialId;

    /**
     * 库存数量
     */
    private Double num;

    private LocalDateTime updated;


}
