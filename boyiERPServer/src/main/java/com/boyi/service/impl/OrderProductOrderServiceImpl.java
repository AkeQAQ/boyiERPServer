package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.vo.OrderProductCalVO;
import com.boyi.entity.AnalysisProductOrderVO;
import com.boyi.entity.OrderProductOrder;
import com.boyi.entity.ProduceOrderMaterialProgress;
import com.boyi.entity.RepositoryStock;
import com.boyi.mapper.OrderProductOrderMapper;
import com.boyi.service.OrderProductOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

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

    @Autowired
    private OrderProductOrderMapper orderProductOrderMapper;

    @Override
    public Page<OrderProductOrder> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, List<Long> searchStatus2,List<Long> searchStatus3, Map<String, String> otherSearch) {
        QueryWrapper<OrderProductOrder> queryWrapper = new QueryWrapper<>();

        // 对某一搜索条件，内容进行去重存储
        Map<String, Set<String>> searchFieldAndVals = new HashMap<>();
        for (String key : otherSearch.keySet()){
            Set<String> sets = searchFieldAndVals.get(key);
            String val = otherSearch.get(key);
            if(StrUtil.isBlank(val) || val.equals("null")
                    || StrUtil.isBlank(key)){
                continue;
            }
            if(sets == null || sets.isEmpty()){
                sets = new HashSet<String>();
                searchFieldAndVals.put(key,sets);
            }
            String[] split = val.split("\\|");
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].trim();
            }
            sets.addAll(Arrays.asList(split));

        }
        if(!searchFieldAndVals.isEmpty()){
            for(Map.Entry<String,Set<String>> entry : searchFieldAndVals.entrySet()){

                Set<String> sets = entry.getValue();
                queryWrapper.and(qw -> {
                    for (String key : sets){
                        qw.or().like(entry.getKey(), key);
                    }
                });


            }

        }
        String[] splits = searchStr.split("\\|");

        if(splits.length > 0 && !splits[0].trim().equals("")){
            queryWrapper.and(qw -> {
                for (int i = 0; i < splits.length; i++) {
                    splits[i] = splits[i].trim();
                    if(StrUtil.isNotBlank(searchStr) && !searchStr.equals("null")
                            && StrUtil.isNotBlank(searchField)){
                        qw.or().like(queryField, splits[i]);
                    }
                }
            });
        }



        Page page2 = this.page(page,
                queryWrapper
                        .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDNAME, searchStatus)
                        .in(searchStatus2 != null && searchStatus2.size() > 0, DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDNAME, searchStatus2)
                        .in(searchStatus3 != null && searchStatus3.size() > 0, DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_TYPE_FIELDNAME, searchStatus3)

                        .orderByDesc(DBConstant.TABLE_ORDER_PRODUCT_ORDER.CREATED_FIELDNAME)

        );
        String targetSql = queryWrapper.getTargetSql();

        return page2;
    }

    @Override
    public void updatePrepared(Long orderId, Integer preparedFieldvalue1) {
        UpdateWrapper<OrderProductOrder> update = new UpdateWrapper<>();
        update.set(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDNAME,preparedFieldvalue1)
                .eq(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ID_FIELDNAME,orderId);
        this.update(update);
    }

    @Override
    public List<OrderProductOrder> getByNumBrand(String productNum, String productBrand) {
        QueryWrapper<OrderProductOrder> queryW = new QueryWrapper<>();
        queryW.eq(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PRODUCT_NUM_FIELDNAME,productNum)
                .eq(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PRODUCT_BRAND_FIELDNAME,productBrand);
        return this.list(queryW);
    }

    @Override
    public List<OrderProductOrder> listBatchMaterialsByOrderIds(List<Long> orderIds) {

        return orderProductOrderMapper.listBatchMaterialsByOrderIds(orderIds);
    }

    @Override
    public List<OrderProductOrder> listProductNumBrand(List<Long> orderIds) {
        return orderProductOrderMapper.listProductNumBrand(orderIds);
    }

    @Override
    public List<OrderProductOrder> listByMonthAndDay(String md) {
        return this.list(new QueryWrapper<OrderProductOrder>()
                .likeRight(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_NUM_FIELDNAME,md)
                .lt(DBConstant.TABLE_ORDER_PRODUCT_ORDER.CREATED_FIELDNAME,LocalDate.now().plusDays(-300)));
    }

    @Override
    public OrderProductOrder getByOrderNum(String orderNum) {
        return this.getOne(new QueryWrapper<OrderProductOrder>().eq(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_NUM_FIELDNAME,orderNum));
    }

    @Override
    public List<OrderProductOrder> listByOrderNums(Set<String> orderNums) {
        return this.list(new QueryWrapper<OrderProductOrder>().in(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_NUM_FIELDNAME,orderNums));
    }

    @Override
    public List<ProduceOrderMaterialProgress> listByProductNumBrandAndProgressMaterialId(String productNum, String productBrand, String materialId) {
        return orderProductOrderMapper.listByProductNumBrandAndProgressMaterialId(productNum,productBrand,materialId);
    }

    @Override
    public List<AnalysisProductOrderVO> listGroupByProductNum(String searchStartDate, String searchEndDate) {
        return this.orderProductOrderMapper.listGroupByProductNum(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_TYPE_FIELDVALUE_2,searchStartDate,searchEndDate);
    }

    @Override
    public List<AnalysisProductOrderVO> listByDate(String searchStartDate, String searchEndDate) {
        return this.orderProductOrderMapper.listByDate(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_TYPE_FIELDVALUE_2,searchStartDate,searchEndDate);
    }

    @Override
    public List<AnalysisProductOrderVO> listGroupByProductBrand(String searchStartDate, String searchEndDate) {
        return this.orderProductOrderMapper.listGroupByProductBrand(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_TYPE_FIELDVALUE_2,searchStartDate,searchEndDate);
    }

    @Override
    public List<AnalysisProductOrderVO> listGroupByMostProductNum(String searchStartDate, String searchEndDate) {
        return this.orderProductOrderMapper.listGroupByMostProductNum(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_TYPE_FIELDVALUE_2,searchStartDate,searchEndDate);
    }

    @Override
    public List<OrderProductCalVO> calNoProductOrders() {
        return this.orderProductOrderMapper.calNoProductOrders();
    }

    @Override
    public List<OrderProductOrder> listNoProduct() {
        return this.orderProductOrderMapper.listNoProduct();
    }

    @Override
    public void addOrderNumberByOrderNum(String orderNum, String needAddNum) {
        OrderProductOrder oldOPO = this.getByOrderNum(orderNum);
        Integer oldOrderNumber = oldOPO.getOrderNumber();
        BigDecimalUtil.add(oldOrderNumber+"",needAddNum);
        this.update(new UpdateWrapper<OrderProductOrder>()
                .set(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_NUMBER_FIELDNAME,BigDecimalUtil.add(oldOrderNumber+"",needAddNum).intValue())
                .eq(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_NUM_FIELDNAME,orderNum));
    }

    @Override
    public List<RepositoryStock> listNoPickMaterials() {
        return this.orderProductOrderMapper.listNoPickMaterials();
    }

}
