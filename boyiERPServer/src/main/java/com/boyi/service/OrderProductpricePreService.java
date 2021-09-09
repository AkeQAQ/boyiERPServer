package com.boyi.service;

import com.boyi.entity.OrderProductpricePre;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 新产品成本核算-报价 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-09-09
 */
public interface OrderProductpricePreService extends IService<OrderProductpricePre> {

    void updateFilePathByCompanyNumAndCustomer(Integer companyNum, String customer, String storePath);

    OrderProductpricePre getByCustomerAndCompanyNum(String customer, Integer companyNum);

    void updateStatusSuccess(Long id);

    void updateStatusReturn(Long id);

    OrderProductpricePre getByIdAndStatusSuccess(Long preId);
}
