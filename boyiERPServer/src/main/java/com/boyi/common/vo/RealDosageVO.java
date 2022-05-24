package com.boyi.common.vo;

import com.boyi.common.utils.ExcelAttribute;
import lombok.Data;

import java.io.Serializable;

@Data
public class RealDosageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelAttribute(sort = 0)
    private String orderNum;
    @ExcelAttribute(sort = 1)
    private String productNum;
    @ExcelAttribute(sort = 2)
    private String productBrand;
    @ExcelAttribute(sort = 3)
    private Integer orderNumber;
    @ExcelAttribute(sort = 4)
    private String batchId;
    @ExcelAttribute(sort = 6)
    private String materialId;
    @ExcelAttribute(sort = 7)
    private String materialName;
    @ExcelAttribute(sort = 8)
    private String num;
    @ExcelAttribute(sort = 9)
    private String returnNum;
    @ExcelAttribute(sort = 5)
    private String batchNum;

    @ExcelAttribute(sort = 10)
    private String realDosage;
    @ExcelAttribute(sort = 11)
    private String avgDosage;
    @ExcelAttribute(sort = 12)
    private String planDosage;



}
