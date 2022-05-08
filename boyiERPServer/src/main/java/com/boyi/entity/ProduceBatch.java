package com.boyi.entity;

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
 * @since 2022-04-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceBatch extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 投产序号
     */
    @ExcelAttribute(sort = 1)
    private Integer batchId;

    private String createdUser;

    private String updatedUser;

    /**
     * 订单序号
     */
    @ExcelAttribute(sort = 0)
    private String orderNum;

    private Integer status;

    private String size34;
    private String size35;
    private String size36;
    @ExcelAttribute(sort = 7)
    private String size37;
    @ExcelAttribute(sort = 8)
    private String size38;
    @ExcelAttribute(sort = 9)
    private String size39;
    @ExcelAttribute(sort = 10)
    private String size40;
    @ExcelAttribute(sort = 11)
    private String size41;
    @ExcelAttribute(sort = 12)
    private String size42;
    @ExcelAttribute(sort = 13)
    private String size43;
    @ExcelAttribute(sort = 14)
    private String size44;
    private String size45;
    private String size46;
    private String size47;

}
