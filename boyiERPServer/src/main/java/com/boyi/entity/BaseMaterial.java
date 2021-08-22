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

    private Integer status;
    private String id;

    /**
     * 子编码
     */
    private String subId;


    /**
     * 物料分组ID
     */
    private String groupId;

    private String name;

    /**
     * 单位
     */
    private String unit;

    /**
     * 创建人
     */
    @TableField("createdUser")
    private String createduser;

    /**
     * 修改人
     */
    @TableField("updateUser")
    private String updateuser;

    /**
     * 规格型号
     */
    private String specs;

    /**
     * 图片路径
     */
    private String picUrl;


}
