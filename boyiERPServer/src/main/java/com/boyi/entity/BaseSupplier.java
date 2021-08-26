package com.boyi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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

    private Integer status;
    private String id;
    /**
     * 分组ID
     */
    private String groupCode;
    /**
     * 子编码
     */
    private Integer subId;


    private String name;
    private String groupName;

    /**
     * 地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String mobile;

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

}
