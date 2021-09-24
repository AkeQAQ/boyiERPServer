package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class RepositoryCheck extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Integer status;

    /**
     * 盘点日期
     */
    private LocalDate checkDate;

    /**
     * 盘点人名
     */
    private String checkUser;

    private String createdUser;

    private String updatedUser;


    /**
     *  明细信息 用于接收前端数据
     */
    @TableField(exist = false)  // 字段数据库忽略
    private List<RepositoryCheckDetail> rowList;


    // 用于多表查询的额外字段

    @TableField(exist = false)  // 字段数据库忽略
    private String materialId;

    @TableField(exist = false)  // 字段数据库忽略
    private String materialName;

    @TableField(exist = false)  // 字段数据库忽略
    private String unit;

    @TableField(exist = false)  // 字段数据库忽略
    private String specs;

    @TableField(exist = false)  // 字段数据库忽略
    private Double checkNum;

    @TableField(exist = false)  // 字段数据库忽略
    private Double changeNum;

    @TableField(exist = false)  // 字段数据库忽略
    private Double stockNum;



}
