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
 * @since 2022-10-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceBatchProgress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String supplierId;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String supplierName;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String materialId;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String materialName;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime sendForeignProductDate;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime backForeignProductDate;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
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
     *  0：代表接受，1:代表没接受。是否下部门接收
     */
    private Integer isAccept;

    @TableField(exist = false)
    private Integer seq;

    @TableField(exist = false)
    private String batchIdStr;

}
