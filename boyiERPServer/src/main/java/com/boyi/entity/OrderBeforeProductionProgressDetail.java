package com.boyi.entity;

import java.time.LocalDateTime;

import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2022-08-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderBeforeProductionProgressDetail extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 外键
     */
    private Long foreignId;

    /**
     * 10:确认订单，20:客户要求，30:确认鞋
     */
    private Integer typeId;


    /**
     * 创建人
     */
    private String createdUser;

    /**
     * 最后修改人
     */
    private String updatedUser;

    /**
     * 0：进度再当前，1：进度不在当前
     */
    private Integer isCurrent;

    /**
     * 描述内容
     */
    private String content;


}
