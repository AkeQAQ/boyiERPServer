package com.boyi.service;

import com.boyi.entity.FinanceSupplierPayshoesDetails;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-02-24
 */
public interface FinanceSupplierPayshoesDetailsService extends IService<FinanceSupplierPayshoesDetails> {

    void delByDocumentIds(Long[] ids);

    List<FinanceSupplierPayshoesDetails> listByForeignId(Long id);

    boolean removeByDocId(Long id);
}
