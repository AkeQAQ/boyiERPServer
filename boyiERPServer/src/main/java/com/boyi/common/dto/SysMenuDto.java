package com.boyi.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SysMenuDto {

    private Long id;
    @NotNull(message = "上级菜单不能为空")
    private Long parentId;
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;
    @NotBlank(message = "权限编码不能为空")
    private String authority;

    private String icon;
    private String url;
    private String component;
    private Integer orderType;

    @NotNull(message = "类型不能为空")
    private Integer type;
    @NotNull(message = "状态不能为空")
    private Integer status;

}
