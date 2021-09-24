package com.boyi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.RepositoryCheckDetail;

import java.util.List;

public interface RepositoryCheckDetailService extends IService<RepositoryCheckDetail> {
    boolean delByDocumentIds(Long[] ids);

    List<RepositoryCheckDetail> listByDocumentId(Long id);

    // 根据入库单ID 删除
    boolean removeByDocId(Long id);

    int countByMaterialId(String[] ids);
}
