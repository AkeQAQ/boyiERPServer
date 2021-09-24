package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class RepositoryCheckDetail {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 关联的盘点单据表外键ID
     */
    private Long documentId;

    /**
     * 盘点的物料ID
     */
    private String materialId;

    /**
     * 盘点数目
     */
    private Double checkNum;

    private Double changeNum;

    private Double stockNum;

    @TableField(exist = false)  // 字段数据库忽略
    private String materialName;
    @TableField(exist = false)  // 字段数据库忽略
    private String unit;
    @TableField(exist = false)  // 字段数据库忽略
    private String specs;

}
