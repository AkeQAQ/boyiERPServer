package com.boyi.entity;

import com.boyi.common.utils.ExcelAttribute;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrderBuyOrderImportVO implements Serializable {
    @ExcelAttribute(sort = 17)
    private String supplierId;
    @ExcelAttribute(sort = 1)
    private String buyDate;
    @ExcelAttribute(sort = 2)
    private String docNum;
    @ExcelAttribute(sort = 16)
    private String materialId;
    @ExcelAttribute(sort = 14)
    private String buyNum;
}
