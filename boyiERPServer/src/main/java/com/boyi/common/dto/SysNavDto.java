package com.boyi.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public
class SysNavDto implements Serializable {
    private Long id;
    private String title;
    private String icon;
    private String path;
    private String routerName;
    private String component;
    private Integer status;
    private Integer orderType;
    List<SysNavDto> children = new ArrayList<>();
}