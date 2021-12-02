package com.boyi.entity;

import java.time.LocalDate;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 仓库模块-领料模块
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RepositoryPickMaterial extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ExcelAttribute(sort = 2)
    private Integer status;

    /**
     * 领料日期
     */
    @ExcelAttribute(sort = 1)
    private LocalDate pickDate;

    /**
     * 领料部门ID
     */
    private Long departmentId;

    /**
     * 领料人名
     */
    private String pickUser;

    private String createdUser;

    private String updatedUser;

    private Integer produceDocNum; // 生产计划单号

    @ExcelAttribute(sort = 4)
    private String comment; // 备注


    /**
     *  明细信息 用于接收前端数据
     */
    @TableField(exist = false)  // 字段数据库忽略
    private List<RepositoryPickMaterialDetail> rowList;


    // 用于多表查询的额外字段

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 5)
    private String materialId;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 6)
    private String materialName;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 8)
    private String unit;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 7)
    private String specs;

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 9)
    private Double num;

    @TableField(exist = false)  // 字段数据库忽略
    private Double totalNum; // 该单据总数量

    @TableField(exist = false)  // 字段数据库忽略
    @ExcelAttribute(sort = 3)
    private String departmentName; // 部门名称


}
