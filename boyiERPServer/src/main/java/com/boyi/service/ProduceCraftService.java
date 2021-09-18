package com.boyi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.OrderProductpricePre;
import com.boyi.entity.ProduceCraft;

/**
 * <p>
 *  工艺单
 * </p>
 *
 * @author sunke
 * @since 2021-09-09
 */
public interface ProduceCraftService extends IService<ProduceCraft> {

    ProduceCraft getByCustomerAndCompanyNum(String customer, String companyNum);

    void updateStatusSuccess(String updateUser,Long id);

    void updateStatusReturn(String updateUser,Long id);

    ProduceCraft getByIdAndStatusSuccess(Long preId);

    void updateStatusFinal(String updateUser,Long id);

    void updateStatusReturnReal(String updateUser,Long id);
}
