package com.boyi.service;

import com.boyi.entity.BaseMaterial;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-21
 */
public interface BaseMaterialService extends IService<BaseMaterial> {

    Integer countByGroupCode(String groupCode);
}
