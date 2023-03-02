package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.HisOrderProductOrder;
import com.boyi.entity.OrderProductOrder;
import com.boyi.mapper.HisOrderProductOrderMapper;
import com.boyi.service.HisOrderProductOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-04-28
 */
@Service
public class HisOrderProductOrderServiceImpl extends ServiceImpl<HisOrderProductOrderMapper, HisOrderProductOrder> implements HisOrderProductOrderService {

    @Override
    public List<HisOrderProductOrder> getByNumBrand(String productNum, String productBrand) {
        QueryWrapper<HisOrderProductOrder> queryW = new QueryWrapper<>();
        queryW.eq(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PRODUCT_NUM_FIELDNAME,productNum)
                .eq(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PRODUCT_BRAND_FIELDNAME,productBrand);
        return this.list(queryW);
    }
}
