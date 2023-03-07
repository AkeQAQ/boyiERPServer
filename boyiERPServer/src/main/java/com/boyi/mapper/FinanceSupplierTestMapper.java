package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSupplierRoundDown;
import com.boyi.entity.FinanceSupplierTest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
 * @since 2023-02-26
 */
@Repository
public interface FinanceSupplierTestMapper extends BaseMapper<FinanceSupplierTest> {

    String querySql = "SELECT fsd.*,bs.name supplier_name FROM finance_supplier_test fsd,base_supplier bs where fsd.supplier_id = bs.id";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<FinanceSupplierTest> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<FinanceSupplierTest> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    FinanceSupplierTest one(@Param("ew") Wrapper queryWrapper);

    @Select("select m.supplier_id,sum(m.test_amount) total_amount from finance_supplier_test m" +
            " where m.test_date >=#{startDate} and m.test_date <= #{endDate}" +
            " group by m.supplier_id")
    List<FinanceSupplierTest> getSupplierTotalAmountBetweenDate(@Param("startDate") LocalDate startDateTime,@Param("endDate") LocalDate endDateTime);
}
