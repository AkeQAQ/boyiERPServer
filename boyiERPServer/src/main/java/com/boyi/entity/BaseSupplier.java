package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.boyi.common.utils.ExcelAttribute;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2021-08-22
 */
@Data
public class BaseSupplier {

    private static final long serialVersionUID = 1L;

    private LocalDateTime created;
    private LocalDateTime updated;

    @ExcelAttribute(sort = 0)
    private String id;
    /**
     * 分组ID
     */
    private String groupCode;
    /**
     * 子编码
     */
    private Integer subId;

    @ExcelAttribute(sort = 1)
    private String name;
    @ExcelAttribute(sort = 2)
    private String groupName;

    /**
     * 地址
     */
    @ExcelAttribute(sort = 4)
    private String address;

    /**
     * 联系电话
     */
    @ExcelAttribute(sort = 3)
    private String mobile;

    /**
     * 含税
     */
    @ExcelAttribute(sort = 5)
    private String tax;

    /**
     * 创建人
     */
    @TableField("created_user")
    private String createdUser;

    /**
     * 修改人
     */
    @TableField("update_user")
    private String updateUser;

    @ExcelAttribute(sort = 7)
    @TableField("comment")
    private String comment;

    @ExcelAttribute(sort = 6)
    @TableField("zq")
    private String zq;

}
