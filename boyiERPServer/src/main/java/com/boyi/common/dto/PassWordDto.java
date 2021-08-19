package com.boyi.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public
class PassWordDto implements Serializable {
    @NotBlank(message = "新密码不能为空")
    private String currentPass;
    @NotBlank(message = "新密码不能为空")
    private String pass;
}