package com.boyi.service;

import com.boyi.entity.OrderProductpricePre;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 新产品成本核算-报价 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-09-09
 */
public interface OrderProductpricePreService extends IService<OrderProductpricePre> {

    void updateFilePathByCompanyNumAndCustomer(String companyNum, String customer, String storePath);

    OrderProductpricePre getByCustomerAndCompanyNum(String customer, String companyNum);

    void updateStatusSuccess(String updateUser,Long id);

    void updateStatusReturn(String updateUser,Long id);

    OrderProductpricePre getByIdAndStatusSuccess(Long preId);

    void updateStatusFinal(String updateUser,Long id);

    void updateStatusReturnReal(String updateUser,Long id);

    OrderProductpricePre getByCustomerAndCompanyNumSimple(String productBrand, String productNum);

    List<OrderProductpricePre> listByLikeProductNum(String substring);

    List<OrderProductpricePre> listByLikeProductNumWithExcelJson(String productNum);
}
