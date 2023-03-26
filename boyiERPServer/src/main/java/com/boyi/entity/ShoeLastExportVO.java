package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2021-08-21
 */
@Data
public class ShoeLastExportVO {

    private static final long serialVersionUID = 1L;

    private LocalDateTime created;
    private LocalDateTime updated;

    @ExcelAttribute(sort = 0)
    private String id;

    @ExcelAttribute(sort = 1)
    private String name;

    @ExcelAttribute(sort = 8)
    private Integer status;


    @TableField(exist = false)
    @ExcelAttribute(sort = 2)
    private String shoeLast;


    @ExcelAttribute(sort = 3)
    @TableField(exist = false)
    private String shoeLastTotalNum;

    @ExcelAttribute(sort = 4)
    @TableField(exist = false)
    private String shoeLastTotalAmount;

    @ExcelAttribute(sort = 5)
    @TableField(exist = false)
    private String orderNumber;

    @ExcelAttribute(sort = 6)
    @TableField(exist = false)
    private String avgPrice;

    @TableField(exist = false)
    @ExcelAttribute(sort = 7)
    private LocalDateTime shoeLastTime;


}
