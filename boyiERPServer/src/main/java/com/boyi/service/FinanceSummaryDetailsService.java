package com.boyi.service;

import com.boyi.entity.FinanceSummaryDetails;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.FinanceSupplierPayshoesDetails;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-06
 */
public interface FinanceSummaryDetailsService extends IService<FinanceSummaryDetails> {

    void delByDocumentIds(Long[] ids);

    List<FinanceSummaryDetails> listByForeignId(Long id);

    boolean removeByDocId(Long id);
}
