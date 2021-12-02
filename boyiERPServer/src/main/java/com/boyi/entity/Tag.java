package com.boyi.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2021-11-06
 */
@Data
public class Tag  {

    private static final long serialVersionUID = 1L;

    private String tagName;

    /**
     * 1:采购入库，2：采购退料，3：生产领料，4：生产退料
     */
    private Integer type;

    private String created;

    private LocalDateTime createdTime;

    private String searchField;
    private String searchStr;
    private LocalDate searchStartDate;
    private LocalDate searchEndDate;
    private String searchStatus;

    private String searchOther;


}
