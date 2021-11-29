package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2021-08-21
 */
@Data
public class BaseMaterial  {

    private static final long serialVersionUID = 1L;

    private LocalDateTime created;
    private LocalDateTime updated;


    private String id;

    /**
     * 子编码
     */
    private String subId;

    /**
     * 物料分组ID
     */
    private String groupCode;

    private String name;

    /**
     * 单位
     */
    private String unit;

    /**
     * 入库大单位
     */
    private String bigUnit;
    /**
     * 换算系数
     */
    private Integer unitRadio;

    /**
     * 创建人
     */
    @TableField("created_user")
    private String createdUser;

    /**
     * 修改人
     */
    @TableField("update_user")
    private String updateUser;

    /**
     * 规格型号
     */
    private String specs;



}
