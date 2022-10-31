package com.boyi.entity;

import java.time.LocalDateTime;

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
 * @since 2022-10-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceBatchProgress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String supplierId;

    private String supplierName;

    private String materialId;

    private String materialName;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendForeignProductDate;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime backForeignProductDate;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
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


}
