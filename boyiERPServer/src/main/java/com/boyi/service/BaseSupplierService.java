package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.BaseMaterial;
import com.boyi.entity.BaseSupplier;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-21
 */
public interface BaseSupplierService extends IService<BaseSupplier> {
    Integer countByGroupCode(String groupCode);


    Page<BaseSupplier> pageByGroupCode(Page page, String searchStr);

    Page<BaseSupplier> pageBySearch(Page page,String queryField, String searchStr);

}
