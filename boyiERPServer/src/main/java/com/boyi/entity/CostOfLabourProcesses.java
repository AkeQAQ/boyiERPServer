package com.boyi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

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
 * @since 2022-10-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CostOfLabourProcesses extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * cost_of_labour_type的外键ID
     */
    private Integer costOfLabourTypeId;

    /**
     * 工序名称
     */
    private String processesName;

    /**
     * 保底价格/双
     */
    private BigDecimal lowPrice;

    /**
     * 片数价格/双
     */
    private BigDecimal piecesPrice;

    /**
     * 起始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 备注
     */
    private String comment;

    /**
     * 创建人
     */
    private String createdUser;

    /**
     * 最后修改人
     */
    private String updateUser;

    /**
     * 状态
     */
    private Integer status;

    @TableField(exist = false)
    private String costOfLabourTypeName;


}
