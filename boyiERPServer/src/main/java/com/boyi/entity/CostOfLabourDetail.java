package com.boyi.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
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
public class CostOfLabourDetail extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 工价表外键
     */
    private Long foreignId;

    /**
     * 工序标外键
     */
    private Long costOfLabourProcessesId;

    /**
     * 片数
     */
    private BigDecimal pieces;

    /**
     * 实际最终价格
     */
    private BigDecimal realPrice;

    private String reason;

    @TableField(exist = false)
    private LocalDateTime created;

    @TableField(exist = false)
    private LocalDateTime updated;

    @TableField(exist = false)
    private String processesName;
    @TableField(exist = false)
    private String lowPrice;
    @TableField(exist = false)
    private String piecesPrice;

    @TableField(exist = false)
    private String calPrice;



}
