package com.boyi.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2023-04-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceBatchZcProgress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime sendDate;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime outDate;

    /**
     * produce_batch_id外键
     */
    private Long produceBatchId;

    private String createdUser;

    private String updateUser;

    /**
     * 0：代表接受，1:代表没接受。是否下部门接收
     */
    private Integer isAccept;

    private Long zcGroupId;

    @TableField(exist = false)
    private String zcGroupName;


    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String comment;


}
