package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ExternalAccountBaseSupplier;
import com.boyi.entity.ExternalAccountBaseSupplier;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
public interface ExternalAccountBaseSupplierService extends IService<ExternalAccountBaseSupplier> {
    Integer countByGroupCode(String groupCode);


    Page<ExternalAccountBaseSupplier> pageByGroupCode(Page page, String searchStr);

    Page<ExternalAccountBaseSupplier> pageBySearch(Page page,String queryField, String searchStr);
}
