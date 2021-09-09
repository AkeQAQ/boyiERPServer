package com.boyi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.RepositoryBuyinDocumentDetail;
import com.boyi.entity.RepositoryBuyoutDocumentDetail;

import java.util.List;

/**
 * <p>
 * 仓库模块-采购退料单-详情内容 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
public interface RepositoryBuyoutDocumentDetailService extends IService<RepositoryBuyoutDocumentDetail> {

    boolean delByDocumentIds(Long[] ids);

    List<RepositoryBuyoutDocumentDetail> listByDocumentId(Long id);

    // 根据入库单ID 删除
    boolean removeByDocId(Long id);

    int countByMaterialId(String[] ids);

}
