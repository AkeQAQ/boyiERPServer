package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RepositoryClose extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 创建人
     */
    @TableField("created_user")
    private String createdUser;

    /**
     * 修改人
     */
    @TableField("updated_user")
    private String updateUser;

    private LocalDate closeDate;



}
