package com.boyi.common.vo;

import com.boyi.common.utils.ExcelAttribute;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrderProductCalVO implements Serializable {

    /**
     * 我们的订单号
     */
    private String orderNum;

    /**
     * 客户货号
     */
    private String customerNum;

    /**
     * 公司货号
     */
    private String productNum;

    /**
     * 产品品牌
     */
    private String productBrand;

    /**
     * 产品颜色
     */
    private String productColor;

    /**
     * 订单数目
     */
    private Integer orderNumber;

    /**
     * 品牌区域
     */
    private String productRegion;

    /**
     * 备注
     */
    private String comment;
    private String materialId;
    private String materialName;
    private String dosage;

    private String needNum;

    private String stockNum;

    private String noInNum;// 备料未到全的数值

    private String noPickNum;// 车间未退数量

    private String preparedNum;


    private String inNum;


}
