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
 * @since 2022-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HisProduceBatchDelay extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String materialId;

    private String materialName;

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
