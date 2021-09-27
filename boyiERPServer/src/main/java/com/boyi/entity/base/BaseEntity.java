package com.boyi.entity.base;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.boyi.common.utils.ExcelAttribute;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {

    @TableId(value = "id",type = IdType.AUTO)
    @ExcelAttribute(sort = 0)
    private Long id;
    private LocalDateTime created;

    @ExcelAttribute(sort = 4)
    private LocalDateTime updated;

}
