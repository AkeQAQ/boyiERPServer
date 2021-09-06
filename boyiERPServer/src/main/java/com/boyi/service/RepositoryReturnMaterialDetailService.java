package com.boyi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.RepositoryPickMaterialDetail;
import com.boyi.entity.RepositoryReturnMaterialDetail;

import java.util.List;

/**
 * <p>
 * 仓库模块-退料模块-详情表 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
public interface RepositoryReturnMaterialDetailService extends IService<RepositoryReturnMaterialDetail> {
    boolean delByDocumentIds(Long[] ids);

    List<RepositoryReturnMaterialDetail> listByDocumentId(Long id);

    // 根据入库单ID 删除
    boolean removeByDocId(Long id);

    int countByMaterialId(String[] ids);
}
