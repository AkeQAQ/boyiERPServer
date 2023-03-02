package com.boyi.service;

import com.boyi.entity.HisOrderProductOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.OrderProductOrder;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-04-28
 */
public interface HisOrderProductOrderService extends IService<HisOrderProductOrder> {
    List<HisOrderProductOrder> getByNumBrand(String productNum, String productBrand);

}
