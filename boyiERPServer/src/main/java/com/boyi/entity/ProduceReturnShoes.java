package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2021-11-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceReturnShoes extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ExcelAttribute(sort = 1)
    private String userName;
    @ExcelAttribute(sort = 3)
    private String packageNo;
    @ExcelAttribute(sort = 4)
    private String userArtNo;
    @ExcelAttribute(sort = 5)
    private String size;
    @ExcelAttribute(sort = 6)
    private String num;
    @ExcelAttribute(sort = 7)
    private String userRequest;
    @ExcelAttribute(sort = 8)
    private String dealSituation;
    @ExcelAttribute(sort = 9)
    private String backPackage;

    @ExcelAttribute(sort = 0)
    private LocalDate returnDate;

    @ExcelAttribute(sort = 2)
    private String region;
    private Long departmentId;

    @TableField(exist = false)
    private String departmentName;





}
