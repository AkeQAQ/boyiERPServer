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
 * @since 2022-11-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceBatchDelay extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String materialId;

    private String materialName;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    /**
     * 外键
     */
    private Long produceBatchId;


    private String createdUser;
    private String updateUser;

    private Long costOfLabourTypeId;

    private String costOfLabourTypeName;



}
