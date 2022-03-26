package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.OrderProductOrder;
import com.boyi.entity.ProduceProductConstituent;
import com.boyi.mapper.OrderProductOrderMapper;
import com.boyi.service.OrderProductOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-03-25
 */
@Service
public class OrderProductOrderServiceImpl extends ServiceImpl<OrderProductOrderMapper, OrderProductOrder> implements OrderProductOrderService {

    @Override
    public Page<OrderProductOrder> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<OrderProductOrder> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.page(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDNAME,searchStatus)
                        .orderByDesc(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_NUM_FIELDNAME)

        );
    }

}
