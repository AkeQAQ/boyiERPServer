package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSummary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.FinanceSummary;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2023-03-06
 */
@Repository
public interface FinanceSummaryMapper extends BaseMapper<FinanceSummary> {

    String querySql = "" +
            "select t.*,(t.need_pay_amount - (IFNULL(t.payed_amount,0)) ) remaining_amount,bs.name supplier_name from " +
            " (" +
            "" +
            " select " +
            " fs.*," +
            " (" +
            " select sum(fsd1.pay_amount) from finance_summary_details fsd1" +
            " where fsd1.summary_id = fs.id" +
            " ) payed_amount" +
            "" +
            " from finance_summary fs " +
            " )t ," +
            " base_supplier bs " +
            " where  t.supplier_id = bs.id  ";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<FinanceSummary> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<FinanceSummary> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    FinanceSummary one(@Param("ew") Wrapper queryWrapper);
}
