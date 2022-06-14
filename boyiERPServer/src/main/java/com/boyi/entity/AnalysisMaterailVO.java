package com.boyi.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2022-06-09
 */
@Data
public class AnalysisMaterailVO {

    private static final long serialVersionUID = 1L;

    /**
     * 合计数目
     */
    private String sum;

    private String supplierName;

    private String materialName;

    private String productNumBrandMaterial;//品牌-款式-物料

    private String squareFoot;// 盈利、亏损的平方英尺


}
