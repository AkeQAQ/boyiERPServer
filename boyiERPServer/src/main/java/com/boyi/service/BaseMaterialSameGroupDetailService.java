package com.boyi.service;

import com.boyi.entity.BaseMaterialSameGroupDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-11-14
 */
public interface BaseMaterialSameGroupDetailService extends IService<BaseMaterialSameGroupDetail> {

    void removeByGroupId(Long id);

    List<BaseMaterialSameGroupDetail> listByGroupId(Long id);
}
