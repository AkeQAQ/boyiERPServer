package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 库存历史表
 * </p>
 *
 * @author sunke
 */
@Data
public class RepositoryStockHistory {

    private static final long serialVersionUID = 1L;

    /**
     * 物料ID
     */
    private String materialId;

    /**
     * 库存数量
     */
    private Double num;

    private LocalDate date;

}
