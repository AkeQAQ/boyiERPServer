package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2022-03-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceProductConstituent extends BaseEntity {

    private static final long serialVersionUID = 1L;

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

    private String createdUser;

    private String updatedUser;

    private Integer status;

    private String videoUrl;

    @TableField(exist = false)
    private Integer caiduanForeignPriceStatus;


    /**
     *  明细信息 用于接收前端数据
     */
    @TableField(exist = false)  // 字段数据库忽略
    private List<ProduceProductConstituentDetail> rowList;



}
