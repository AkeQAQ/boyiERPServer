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

    private String materialId;
    private String materialName;
    private LocalDate date;
    private String docName;
    private String docNum;
    private String unit;
    private String specs;
    private Long status;
    private Integer typeOrder;

    private Double startNum; // 期初数量
    private Double addNum;
    private Double subNum;
    private Double afterNum; // 结存数量


}
