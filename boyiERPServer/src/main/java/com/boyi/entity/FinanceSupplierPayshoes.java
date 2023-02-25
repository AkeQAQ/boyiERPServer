package com.boyi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.boyi.common.utils.BigDecimalUtil;
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
 * @since 2023-02-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceSupplierPayshoes  extends BaseEntity{

    private static final long serialVersionUID = 1L;

    /**
     * 单据编号
     */
    private String documentNum;
    /**
     * 供应商ID
     */
    private String supplierId;

    /**
     * 罚款日期
     */
    private LocalDate payDate;

    /**
     * 状态。0：已拿, 1: 未拿
     */
    private Integer takeStatus;

    /**
     * 单据图片
     */
    private String picUrl;

    private String createdUser;

    private String updateUser;

    /**
     * 0：已审核、1:暂存、2：审核中、3：重新审核
     */
    private Integer status;


    @TableField(exist = false)
    private String supplierName;

    @TableField(exist = false)
    private String customerNum;
    @TableField(exist = false)
    private BigDecimal payAmount;
    @TableField(exist = false)
    private BigDecimal payNumber;
    @TableField(exist = false)
    private Integer payType;

    @TableField(exist = false)
    private List<FinanceSupplierPayshoesDetails> rowList;

}
