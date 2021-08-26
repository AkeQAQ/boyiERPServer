package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * <p>
 * 基础模块-物料分组表
 * </p>
 *
 * @author sunke
 * @since 2021-08-20
 */
@Data
public class BaseMaterialGroup  {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private LocalDateTime created;
    private LocalDateTime updated;

    /**
     * 分组的上级ID
     */
    private Long parentId;

    /**
     * 分组的名称
     */
    private String name;

    /**
     * 物料分组前缀编码
     */
    private String code;

    /**
     *  分组下的子增长ID
     */
    private Integer autoSubId;

}
