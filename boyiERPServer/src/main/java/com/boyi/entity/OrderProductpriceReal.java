package com.boyi.entity;

import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 新产品成本核算-实际
 * </p>
 *
 * @author sunke
 * @since 2021-09-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderProductpriceReal extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 公司货号
     */
    private String companyNum;

    /**
     * 客户公司名称
     */
    private String customer;

    private String createdUser;

    private String updateUser;

    /**
     * 文件存储路径
     */
    private String savePath;

    private Integer status;

    private Double price;

    private String uploadName;
    private Long preId;

}
