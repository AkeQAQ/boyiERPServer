package com.boyi.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2022-02-22
 */
@Data
public class BuyMaterialSupplier extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 内部物料ID
     */
    private String innerMaterialId;

    @TableField(exist = false)  // 字段数据库忽略
    private String materialName;

    /**
     * 供应商ID
     */
    private String supplierId;

    /**
     * 供应商名称，冗余字段
     */
    private String supplierName;

    /**
     * 供应商的物料编码
     */
    private String supplierMaterialId;

    /**
     * 供应商的物料名称
     */
    private String supplierMaterialName;

    /**
     * 供应商的物料价格
     */
    private Double supplierMaterialPrice;

    /**
     * 创建人
     */
    private String createdUser;

    /**
     * 修改人
     */
    private String updatedUser;

}
