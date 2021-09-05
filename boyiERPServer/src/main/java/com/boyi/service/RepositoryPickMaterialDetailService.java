package com.boyi.service;

import com.boyi.entity.RepositoryBuyinDocumentDetail;
import com.boyi.entity.RepositoryPickMaterialDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 仓库模块-领料模块-详情表 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
public interface RepositoryPickMaterialDetailService extends IService<RepositoryPickMaterialDetail> {
    boolean delByDocumentIds(Long[] ids);

    List<RepositoryPickMaterialDetail> listByDocumentId(Long id);

    // 根据入库单ID 删除
    boolean removeByDocId(Long id);

    int countByMaterialId(String[] ids);
}
