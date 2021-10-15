package com.boyi.entity;

import com.boyi.common.utils.ExcelAttribute;
import lombok.Data;

@Data
public class PickMaterialExcelVO {
    @ExcelAttribute(sort = 0)
    private String materialId;
    @ExcelAttribute(sort = 1)
    private Double num;
}
