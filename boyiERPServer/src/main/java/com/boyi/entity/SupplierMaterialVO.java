package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * <p>
 * 供应商-物料报价表
 * </p>
 *
 * @author sunke
 * @since 2021-08-24
 */
@Data
public class SupplierMaterialVO  {

    private static final long serialVersionUID = 1L;
    private String materialInnerId;
    private String materialInnerName;
    private String supplierId;
    private String supplierName;

    private String materialOutId;
    private String materialOutName;
    
    private String unit;
    
    private String specs;

    private Double price;

    private String num;

    private String productNumBrand;




}
