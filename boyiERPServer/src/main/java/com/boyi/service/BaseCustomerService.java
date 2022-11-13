package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseCustomer;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-11-13
 */
public interface BaseCustomerService extends IService<BaseCustomer> {

    Page<BaseCustomer> pageBySearch(Page page, String searchName);

}
