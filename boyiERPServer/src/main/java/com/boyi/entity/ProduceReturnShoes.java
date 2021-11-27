package com.boyi.entity;

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
    @ExcelAttribute(sort = 2)
    private String packageNo;
    @ExcelAttribute(sort = 3)
    private String userArtNo;
    @ExcelAttribute(sort = 4)
    private String size;
    @ExcelAttribute(sort = 5)
    private String num;
    @ExcelAttribute(sort = 6)
    private String userRequest;
    @ExcelAttribute(sort = 7)
    private String dealSituation;
    @ExcelAttribute(sort = 8)
    private String backPackage;

    @ExcelAttribute(sort = 0)
    private LocalDate returnDate;


}
