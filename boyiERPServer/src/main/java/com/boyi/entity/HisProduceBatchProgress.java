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
public class HisProduceBatchProgress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String supplierId;

    private String supplierName;

    private String materialId;

    private String materialName;

    private LocalDateTime sendForeignProductDate;

    private LocalDateTime backForeignProductDate;

    private LocalDateTime outDate;

    /**
     * produce_batch_id外键
     */
    private Long produceBatchId;

    /**
     * 工序类别外键
     */
    private Long costOfLabourTypeId;

    private String costOfLabourTypeName;

    private String createdUser;

    private String updateUser;

    /**
     * 0：代表接受，1:代表没接受。是否下部门接收
     */
    private Integer isAccept;


}
