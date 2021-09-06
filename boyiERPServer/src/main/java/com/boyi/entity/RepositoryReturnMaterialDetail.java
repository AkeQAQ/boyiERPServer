package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * <p>
 * 仓库模块-退料模块-详情表
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
@Data
public class RepositoryReturnMaterialDetail {

    private static final long serialVersionUID = 1L;


    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 关联的退料单据表外键ID
     */
    private Long documentId;

    /**
     * 退料的物料ID
     */
    private String materialId;

    /**
     * 退料数目
     */
    private Double num;


    /**
     * 退料原因
     */
    private String reason;


    @TableField(exist = false)  // 字段数据库忽略
    private String materialName;
    @TableField(exist = false)  // 字段数据库忽略
    private String unit;
    @TableField(exist = false)  // 字段数据库忽略
    private String specs;

}
