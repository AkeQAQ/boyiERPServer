package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
 * @since 2023-04-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceTechnologyBom extends BaseEntity {

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

    /**
     * 后帮定位高度
     */
    private String shoeHeight;

    /**
     * 针距
     */
    private String shoeNeedleDistance;

    /**
     * 斗底
     */
    private String shoeDoudi;

    /**
     * 拉帮
     */
    private String shoeLabang;

    /**
     * 锁头
     */
    private String shoeSuotou;

    /**
     * 包边
     */
    private String shoeBaobian;

    /**
     * 车布边
     */
    private String shoeChebubian;

    /**
     * 烫松紧
     */
    private String shoeTangsongjing;

    private Integer status;

    /**
     *  明细信息 用于接收前端数据
     */
    @TableField(exist = false)  // 字段数据库忽略
    private List<ProduceTechnologyBomDetail> rowList;

}
