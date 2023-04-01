package com.boyi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 库存表
 * </p>
 *
 * @author sunke
 * @since 2023-04-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RepositoryStockLost extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private LocalDate createdDate;

    /**
     * 物料ID
     */
    private String materialId;

    /**
     * 库存数量
     */
    private Double num;

    private BigDecimal needNum;

    private BigDecimal noPickNum;

    /**
     * 当前废库存数量
     */
    private BigDecimal lostNum;

    /**
     * 最近单价
     */
    private BigDecimal latestPrice;


}
