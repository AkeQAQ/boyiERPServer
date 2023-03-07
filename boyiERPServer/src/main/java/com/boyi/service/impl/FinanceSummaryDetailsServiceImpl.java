package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.FinanceSummaryDetails;
import com.boyi.entity.FinanceSummaryDetails;
import com.boyi.mapper.FinanceSummaryDetailsMapper;
import com.boyi.service.FinanceSummaryDetailsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-03-06
 */
@Service
public class FinanceSummaryDetailsServiceImpl extends ServiceImpl<FinanceSummaryDetailsMapper, FinanceSummaryDetails> implements FinanceSummaryDetailsService {

    @Override
    public void delByDocumentIds(Long[] ids) {
        this.remove(new QueryWrapper<FinanceSummaryDetails>()
                .in(DBConstant.TABLE_FINANCE_SUMMARY_DETAILS.SUMMARY_ID_FIELDNAME, ids));
    }

    @Override
    public List<FinanceSummaryDetails> listByForeignId(Long id) {
        return this.list(new QueryWrapper<FinanceSummaryDetails>()
                .eq(DBConstant.TABLE_FINANCE_SUMMARY_DETAILS.SUMMARY_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_FINANCE_SUMMARY_DETAILS.ID_FIELDNAME)
        );
    }

    @Override
    public boolean removeByDocId(Long id) {
        return this.remove(
                new QueryWrapper<FinanceSummaryDetails>()
                        .eq(DBConstant.TABLE_FINANCE_SUMMARY_DETAILS.SUMMARY_ID_FIELDNAME, id));
    }
}
