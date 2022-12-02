package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import lombok.Data;

import java.time.LocalDate;
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
     * 物料ID
     */
    @ExcelAttribute(sort = 0)
    private String materialId;

    /**
     * 库存数量
     */
    @ExcelAttribute(sort = 2)
    private Double num;

    private LocalDateTime updated;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 1)
    private String materialName;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 3)
    private String unit;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 4)
    private String specs;

    @ExcelAttribute(sort = 5)
    @TableField(exist = false)  // 字段数据库忽略
    private LocalDate latestPriceDate;

    @ExcelAttribute(sort = 6)
    @TableField(exist = false)  // 字段数据库忽略
    private LocalDate latestPickDate;


    @ExcelAttribute(sort = 7)
    @TableField(exist = false)  // 字段数据库忽略
    private String needNum;

    @ExcelAttribute(sort = 8)
    @TableField(exist = false)  // 字段数据库忽略
    private String noPickNum;


    @ExcelAttribute(sort = 9)
    @TableField(exist = false)  // 字段数据库忽略
    private String noInNum;


}
