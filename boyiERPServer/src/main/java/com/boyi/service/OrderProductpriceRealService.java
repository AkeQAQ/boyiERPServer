package com.boyi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.OrderProductpricePre;
import com.boyi.entity.OrderProductpriceReal;

/**
 * <p>
 * 新产品成本核算-实际 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-09-09
 */
public interface OrderProductpriceRealService extends IService<OrderProductpriceReal> {

    void updateFilePathByCompanyNumAndCustomer(String companyNum, String customer, String storePath);

    OrderProductpriceReal getByCustomerAndCompanyNum(String customer, String companyNum);

    void updateStatusSuccess(Long id);

    void updateStatusReturn(Long id);
}
