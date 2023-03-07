package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSupplierTaxDeduction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.FinanceSupplierTaxDeduction;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2023-02-27
 */
@Repository
public interface FinanceSupplierTaxDeductionMapper extends BaseMapper<FinanceSupplierTaxDeduction> {

    String querySql = "SELECT fsd.*,bs.name supplier_name FROM finance_supplier_tax_deduction fsd,base_supplier bs where fsd.supplier_id = bs.id";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<FinanceSupplierTaxDeduction> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<FinanceSupplierTaxDeduction> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    FinanceSupplierTaxDeduction one(@Param("ew") Wrapper queryWrapper);

    @Select("select m.supplier_id,sum(m.deduction_amount) total_amount from finance_supplier_tax_deduction m" +
            " where m.document_date >=#{startDate} and m.document_date <= #{endDate}" +
            " group by m.supplier_id")
    List<FinanceSupplierTaxDeduction> getSupplierTotalAmountBetweenDate(@Param("startDate") LocalDate startDateTime,@Param("endDate") LocalDate endDateTime);
}
