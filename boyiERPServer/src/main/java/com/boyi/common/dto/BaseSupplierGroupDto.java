package com.boyi.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BaseSupplierGroupDto {

    private Long parentId;

    private Long id;
    /**
     * 显示字样
     */
    private String label;

    private String code;

    private String parentCode;

    private String name;

    private Integer status;

    private List<BaseSupplierGroupDto> children = new ArrayList<BaseSupplierGroupDto>();
}
