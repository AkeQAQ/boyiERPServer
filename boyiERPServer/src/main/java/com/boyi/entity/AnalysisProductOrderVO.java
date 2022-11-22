package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class AnalysisProductOrderVO {

    private static final long serialVersionUID = 1L;
    /**
     * 生产货号
     */
    private String productNum;

    /**
     * 合计数目
     */
    private String sum;

    private LocalDateTime created;

    private String orderNum;

    private String orderNumber;

    private String productBrand;

    private Integer orderType;


}
