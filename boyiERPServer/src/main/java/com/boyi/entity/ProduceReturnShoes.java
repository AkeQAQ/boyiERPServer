package com.boyi.entity;

import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2021-11-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProduceReturnShoes extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String userName;

    private String packageNo;

    private String userArtNo;

    private String size;

    private String num;

    private String userRequest;

    private String dealSituation;

    private String backPackage;

    private LocalDate returnDate;


}
