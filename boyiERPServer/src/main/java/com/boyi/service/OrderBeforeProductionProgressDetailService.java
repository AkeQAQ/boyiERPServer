package com.boyi.service;

import com.boyi.entity.OrderBeforeProductionProgressDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-08-27
 */
public interface OrderBeforeProductionProgressDetailService extends IService<OrderBeforeProductionProgressDetail> {

    List<OrderBeforeProductionProgressDetail> listByForeignId(Long id);

    void updateOtherIsCurrent(Long foreignId, Long id, Integer progressFieldvalue1);
}
