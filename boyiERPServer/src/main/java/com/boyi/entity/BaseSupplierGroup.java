package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 基础模块-供应商分组表
 * </p>
 *
 * @author sunke
 * @since 2021-08-22
 */
@Data
public class BaseSupplierGroup {

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
     * 分组前缀编码
     */
    private String code;

    private Integer autoSubId;

}
