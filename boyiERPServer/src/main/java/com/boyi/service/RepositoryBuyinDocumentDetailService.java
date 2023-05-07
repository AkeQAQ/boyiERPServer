package com.boyi.service;

import com.boyi.entity.RepositoryBuyinDocumentDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 仓库模块-采购入库单-详情内容 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
public interface RepositoryBuyinDocumentDetailService extends IService<RepositoryBuyinDocumentDetail> {

    boolean delByDocumentIds(Long[] ids);

    List<RepositoryBuyinDocumentDetail> listByDocumentId(Long id);

    // 根据入库单ID 删除
    boolean removeByDocId(Long id);

    int countByMaterialId(String[] ids);

    List<RepositoryBuyinDocumentDetail> listByOrderDetailId(Long[] orderDetailIds);

    void removeByDocIdAndInIds(Long id, List<Long> detailIds);

    List<RepositoryBuyinDocumentDetail> listNoPriceForeignMaterials();
}
