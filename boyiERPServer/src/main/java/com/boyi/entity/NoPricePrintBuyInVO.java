package com.boyi.entity;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2022-06-09
 */
@Data
public class NoPricePrintBuyInVO {

    private static final long serialVersionUID = 1L;

    private String supplierName;
    private List<RepositoryBuyinDocumentDetail> rowList;


}
