package com.boyi.service;

import com.boyi.entity.FinanceSupplierChangeDetails;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-02-25
 */
public interface FinanceSupplierChangeDetailsService extends IService<FinanceSupplierChangeDetails> {

    List<FinanceSupplierChangeDetails> listByForeignId(Long id);

    boolean removeByDocId(Long id);

    void delByDocumentIds(Long[] ids);
}
