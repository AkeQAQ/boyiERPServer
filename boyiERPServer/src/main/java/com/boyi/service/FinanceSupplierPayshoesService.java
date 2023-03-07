package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSupplierPayshoes;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.ProduceProductConstituent;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-02-24
 */
public interface FinanceSupplierPayshoesService extends IService<FinanceSupplierPayshoes> {

    Page<FinanceSupplierPayshoes> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatusList, List<Long> takeStatusList, List<Long> payTypeStatusList, Map<String, String> queryMap,String searchStartDate,String searchEndDate);

    Page<FinanceSupplierPayshoes> innerQuery(Page page, QueryWrapper<FinanceSupplierPayshoes> like);

    void updateNullWithField(FinanceSupplierPayshoes ppc, String picUrlFieldname);

    List<FinanceSupplierPayshoes> countLTByCloseDate(LocalDate closeDate);

    List<FinanceSupplierPayshoes> getSupplierTotalAmountBetweenDate(LocalDate startDateTime, LocalDate endDateTime);
}
