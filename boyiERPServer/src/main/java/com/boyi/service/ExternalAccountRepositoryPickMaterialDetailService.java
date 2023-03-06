package com.boyi.service;

import com.boyi.entity.ExternalAccountRepositoryPickMaterialDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.ExternalAccountRepositoryPickMaterialDetail;

import java.util.List;

/**
 * <p>
 * 仓库模块-领料模块-详情表 服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
public interface ExternalAccountRepositoryPickMaterialDetailService extends IService<ExternalAccountRepositoryPickMaterialDetail> {
    boolean delByDocumentIds(Long[] ids);

    List<ExternalAccountRepositoryPickMaterialDetail> listByDocumentId(Long id);

    // 根据入库单ID 删除
    boolean removeByDocId(Long id);

    int countByMaterialId(String[] ids);
}
