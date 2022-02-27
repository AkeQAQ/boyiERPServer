package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BuyMaterialSupplier;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-02-22
 */
@Service
public interface BuyMaterialSupplierService extends IService<BuyMaterialSupplier> {

    Page<BuyMaterialSupplier> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, Map<String, String> queryMap);

    BuyMaterialSupplier isExist(String supplierId, String supplierMaterialId);
}
