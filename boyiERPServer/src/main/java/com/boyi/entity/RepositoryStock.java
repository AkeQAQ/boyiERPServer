package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

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
    private String materialId;

    /**
     * 库存数量
     */
    private Double num;

    private LocalDateTime updated;

    @TableField(exist = false)  // 字段数据库忽略
    private String materialName;

    @TableField(exist = false)  // 字段数据库忽略
    private String unit;

    @TableField(exist = false)  // 字段数据库忽略
    private String specs;

}
