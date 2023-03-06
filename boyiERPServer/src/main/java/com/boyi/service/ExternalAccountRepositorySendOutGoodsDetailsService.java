package com.boyi.service;

import com.boyi.entity.ExternalAccountRepositoryBuyinDocumentDetail;
import com.boyi.entity.ExternalAccountRepositorySendOutGoodsDetails;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.ExternalAccountRepositorySendOutGoodsDetails;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-05
 */
public interface ExternalAccountRepositorySendOutGoodsDetailsService extends IService<ExternalAccountRepositorySendOutGoodsDetails> {

    List<ExternalAccountRepositorySendOutGoodsDetails> listByDocumentId(Long id);

    List<ExternalAccountRepositorySendOutGoodsDetails> listByForeignId(Long id);

    boolean removeByDocId(Long id);

    void delByDocumentIds(Long[] ids);
}
