package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.FinanceSummary;
import com.boyi.entity.FinanceSummary;
import com.boyi.mapper.FinanceSummaryMapper;
import com.boyi.mapper.FinanceSupplierPayshoesMapper;
import com.boyi.service.FinanceSummaryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-03-06
 */
@Service
public class FinanceSummaryServiceImpl extends ServiceImpl<FinanceSummaryMapper, FinanceSummary> implements FinanceSummaryService {

    @Autowired
    public FinanceSummaryMapper financeSummaryMapper;

    @Override
    public Page<FinanceSummary> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, List<Long> payStatusList, Map<String, String> otherSearch, String searchStartDate, String searchEndDate,
                                                       String searchStartSettleDate, String searchEndSettleDate) {
        QueryWrapper<FinanceSummary> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        if(payStatusList.size() ==1){
            Long payStatus = payStatusList.get(0);
            if(payStatus ==0){
                queryWrapper.eq(DBConstant.TABLE_FINANCE_SUMMARY.REMAINING_AMOUNT_FIELDNAME,0);
            }else{
                queryWrapper.ne(DBConstant.TABLE_FINANCE_SUMMARY.REMAINING_AMOUNT_FIELDNAME,0);
            }
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_FINANCE_SUMMARY.STATUS_FIELDNAME,searchStatus)
                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_FINANCE_SUMMARY.SUMMARY_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_FINANCE_SUMMARY.SUMMARY_DATE_FIELDNAME,searchEndDate)
                        .ge(StrUtil.isNotBlank(searchStartSettleDate)&& !searchStartSettleDate.equals("null"),DBConstant.TABLE_FINANCE_SUMMARY.SETTLE_DATE_FIELDNAME,searchStartSettleDate)
                        .le(StrUtil.isNotBlank(searchEndSettleDate)&& !searchEndSettleDate.equals("null"),DBConstant.TABLE_FINANCE_SUMMARY.SETTLE_DATE_FIELDNAME,searchEndSettleDate)

                        .orderByDesc(DBConstant.TABLE_FINANCE_SUMMARY.ID_FIELDNAME)

        );
    }

    @Override
    public Page<FinanceSummary> innerQuery(Page page, QueryWrapper<FinanceSummary> eq) {
        return financeSummaryMapper.page(page,eq);

    }

    @Override
    public void updateNullWithField(FinanceSummary ppc, String picName) {
        this.update(new UpdateWrapper<FinanceSummary>()
                .set(picName,null)
                .eq(DBConstant.TABLE_FINANCE_SUMMARY.ID_FIELDNAME,ppc.getId()));
    }

    @Override
    public List<FinanceSummary> countLTByCloseDate(LocalDate closeDate) {
        return this.list(new QueryWrapper<FinanceSummary>()
                .le(DBConstant.TABLE_FINANCE_SUMMARY.SUMMARY_DATE_FIELDNAME, closeDate)
                .ne(DBConstant.TABLE_FINANCE_SUMMARY.STATUS_FIELDNAME,
                        DBConstant.TABLE_FINANCE_SUMMARY.STATUS_FIELDVALUE_0));
    }

    @Override
    public Integer countByDate(String addDate) {
        return this.count(new QueryWrapper<FinanceSummary>()
                .eq(DBConstant.TABLE_FINANCE_SUMMARY.SUMMARY_DATE_FIELDNAME,addDate));
    }

    @Override
    public List<FinanceSummary> listByDate(String addDate) {
        return this.list(new QueryWrapper<FinanceSummary>()
                .eq(DBConstant.TABLE_FINANCE_SUMMARY.SUMMARY_DATE_FIELDNAME,addDate));
    }
}
