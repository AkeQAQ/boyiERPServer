package com.boyi.common.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RealDosageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderNum;
    private String productNum;
    private String productBrand;
    private Integer orderNumber;
    private Integer batchId;
    private String materialId;
    private String materialName;
    private String num;
    private String returnNum;
    private String realDosage;
    private String avgDosage;


}
