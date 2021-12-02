package com.boyi.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.boyi.common.utils.ExcelAttribute;
import com.boyi.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunke
 * @since 2021-10-29
 */
@Data
public class AnalysisRequest  {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
     * 请求URL
     */
    private String url;

    /**
     * 来源IP
     */
    private String ip;

    /**
     * 执行的class方法
     */
    private String classMethod;

    /**
     * 毫秒单位
     */
    private Long cast;

    /**
     * 请求用户
     */
    private String userName;

    private LocalDateTime createdTime;


}
