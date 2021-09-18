package com.boyi.entity;

import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * <p>
 *  工艺单模块
 * </p>
 *
 * @author sunke
 * @since 2021-09-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceCraft extends BaseEntity {

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

    private Integer status;

    private String excelJson;
    private String realJson;

    private String lastUpdateUser;
    private LocalDateTime lastUpdateDate;

    private String devLastUpdateUser;
    private LocalDateTime devLastUpdateDate;


}
