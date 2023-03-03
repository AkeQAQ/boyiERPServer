package com.boyi.entity;

import java.time.LocalDate;

import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 仓库关账模块
 * </p>
 *
 * @author sunke
 * @since 2023-03-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceClose extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private LocalDate closeDate;

    private String createdUser;

    private String updatedUser;


}
