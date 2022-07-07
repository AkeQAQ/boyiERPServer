package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 物料收发明细
 * </p>
 *
 * @author sunke
 */
@Data
public class RepositoryInOutDetail {

    private static final long serialVersionUID = 1L;

    @ExcelAttribute(sort = 0)
    private String materialId;
    @ExcelAttribute(sort = 1)
    private String materialName;
    @ExcelAttribute(sort = 3)
    private LocalDate date;
    @ExcelAttribute(sort = 4)
    private String docName;
    @ExcelAttribute(sort = 5)
    private String docNum;
    @ExcelAttribute(sort = 6)
    private String unit;
    private String specs;
    @ExcelAttribute(sort = 2)
    private Long status;
    private Integer typeOrder;

    @ExcelAttribute(sort = 7)
    private Double startNum; // 期初数量
    @ExcelAttribute(sort = 8)
    private Double addNum;
    @ExcelAttribute(sort = 9)
    private Double subNum;
    @ExcelAttribute(sort = 10)
    private Double afterNum; // 结存数量


}
