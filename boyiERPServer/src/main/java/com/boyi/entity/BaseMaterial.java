package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
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

    @ExcelAttribute(sort = 0)
    private String id;

    /**
     * 子编码
     */
    private String subId;

    /**
     * 物料分组ID
     */
    @ExcelAttribute(sort = 2)
    private String groupCode;

    @ExcelAttribute(sort = 1)
    private String name;

    /**
     * 单位
     */
    @ExcelAttribute(sort = 3)
    private String unit;

    /**
     * 入库大单位
     */
    @ExcelAttribute(sort = 5)
    private String bigUnit;
    /**
     * 换算系数
     */
    @ExcelAttribute(sort = 6)
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
     * 修改人
     */
    @TableField("status")
    private Integer status;

    /**
     * 规格型号
     */
    @ExcelAttribute(sort = 4)
    private String specs;

    /**
     * 低预警线
     */
    @ExcelAttribute(sort = 7)
    private Double lowWarningLine;

    private String videoUrl;


}
