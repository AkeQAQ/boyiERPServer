package com.boyi.service;

import com.boyi.entity.ExternalAccountRepositoryBuyinDocumentDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.ExternalAccountRepositoryBuyinDocumentDetail;

import java.util.List;

/**
 * <p>
 * 仓库模块-采购入库单-详情内容 服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
public interface ExternalAccountRepositoryBuyinDocumentDetailService extends IService<ExternalAccountRepositoryBuyinDocumentDetail> {

    boolean delByDocumentIds(Long[] ids);

    List<ExternalAccountRepositoryBuyinDocumentDetail> listByDocumentId(Long id);

    // 根据入库单ID 删除
    boolean removeByDocId(Long id);

    int countByMaterialId(String[] ids);

    List<ExternalAccountRepositoryBuyinDocumentDetail> listByOrderDetailId(Long[] orderDetailIds);

    void removeByDocIdAndInIds(Long id, List<Long> detailIds);
}
